package com.codellyrandom

import com.squareup.wire.WireLogger
import com.squareup.wire.schema.*
import com.squareup.wire.schema.Target
import okio.buffer
import okio.sink
import java.lang.IllegalStateException
import java.nio.file.FileSystem
import java.nio.file.Files
import java.nio.file.Path

/**
 * This is a sample handler that writes text files that describe types.
 */
@Suppress("unused")
class TypeScriptGenerator : CustomHandlerBeta {
    val typeResolver = TypeResolver()
    val unresolvedTypeManager = UnresolvedTypeManager(typeResolver)

    override fun newHandler(
        schema: Schema,
        fs: FileSystem,
        outDirectory: String,
        logger: WireLogger,
        profileLoader: ProfileLoader
    ): Target.SchemaHandler {
        return object : Target.SchemaHandler {
            override fun handle(type: Type): Path? {
                val generated = LinkedHashMap<Type, String>()
                toTypeScriptTypes(type, typeResolver, generated)

                val imports = toTypeScriptImports(type)

                var content = ""
                imports.forEach { content += it + "\n"}
                content += "\n"
                generated.forEach { content += it.value + "\n\n" }
                return writeFile(type.type, content.trim() + "\n")
            }

            override fun handle(service: Service): List<Path> {
                // We don't support generating services for now.
                return emptyList()
            }

            override fun handle(extend: Extend, field: Field): Path? {
                return null
            }

            override fun handle(
                protoFile: ProtoFile,
                emittingRules: EmittingRules,
                claimedDefinitions: ClaimedDefinitions,
                claimedPaths: MutableMap<Path, String>,
                isExclusive: Boolean
            ) {
                val typesInFile = protoFile.types.fold(setOf<Type>()) { acc, type ->
                    acc.plus(elements = type.typesAndNestedTypes())
                }
                typesInFile.forEach { typeResolver.add(it) }
                unresolvedTypeManager.resolve(typesInFile)

                super.handle(protoFile, emittingRules, claimedDefinitions, claimedPaths, isExclusive)
            }

            private fun writeFile(protoType: ProtoType, content: String): Path {
                val path = fs.getPath(outDirectory, *toPath(protoType).toTypedArray())
                unresolvedTypeManager.setPathForProtoType(path, protoType)
                Files.createDirectories(path.parent)
                path.sink().buffer().use { sink ->
                    sink.writeUtf8(content)
                }
                return path
            }
        }
    }

    /** Returns a path like `squareup/colors/Blue.ts`. */
    private fun toPath(protoType: ProtoType): List<String> {
        val result = mutableListOf<String>()
        for (part in protoType.toString().split(".")) {
            result += part
        }
        result[result.size - 1] = (result[result.size - 1]) + ".ts"
        return result
    }

    private fun toTypeScript(type: Type, typeResolver: TypeResolver): String {
        return when (type) {
            is EnumType -> TypeScriptEnumGenerator(type, typeResolver).generate()
            is MessageType -> TypeScriptClassGenerator(type, typeResolver, unresolvedTypeManager).generate()
            else -> throw IllegalStateException("Unknown proto type $type")
        }
    }

    private fun toTypeScriptImports(type: Type): List<String> {
        val packageComponents = type.type.packageComponents
        val referencedTypes = type.referencedTypesAndNestedReferencedTypes
        val typesInFile = type.typesAndNestedTypes().map { it.type }

        // We only need to import the types not in this file.
        // Also filter out the built-in types as we'll use native TypeScript types for those.
        val typesOutsideFile = referencedTypes
            .subtract(typesInFile)
            .filter { !it.toString().startsWith("google.protobuf") }

        val transformerImport = if (referencedTypes.isEmpty()) {
            listOf()
        } else {
            listOf("import { Type } from \"class-transformer\"\n")
        }
        val typeImports = typesOutsideFile.map {
            // Convert package/directory structure to be relative to
            // the current package/directory.
            val relativePackage = it.packageComponents.foldIndexed(mutableListOf<String>()) { index, acc, component ->
                if (acc.isNotEmpty() || component != packageComponents[index]) {
                    acc.add(component)
                }
                acc
            }

            // Turn the components into a relative path compared this file.
            val commonComponentCount = it.packageComponents.size - relativePackage.size
            var backingOutPath = (1 .. (packageComponents.size - commonComponentCount)).map { ".." }
            if (backingOutPath.isEmpty()) {
                backingOutPath = listOf(".")
            }
            val importPath = backingOutPath.plus(relativePackage).joinToString("/")
            // For now this only supports importing the default export.
            "import ${it.simpleName} from \"$importPath/${it.simpleName}\""
        }

        return transformerImport.plus(typeImports)
    }

    // Generate the TypeScript code for this type and nested types.
    // Returns a LinkedHashMap so that it's ordered.
    private fun toTypeScriptTypes(type: Type, typeResolver: TypeResolver, generated: LinkedHashMap<Type, String>) {
        generated[type] = toTypeScript(type, typeResolver)
        type.nestedTypes.forEach {
            toTypeScriptTypes(it, typeResolver, generated)
        }
    }
}
