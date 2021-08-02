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
                toTypeScriptTypes(type, generated)

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

            private fun writeFile(protoType: ProtoType, content: String): Path {
                val path = fs.getPath(outDirectory, *toPath(protoType).toTypedArray())
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

    private fun toTypeScript(type: Type): String {
        return when (type) {
            is EnumType -> TypeScriptEnumGenerator(type).generate()
            is MessageType -> TypeScriptClassGenerator(type).generate()
            else -> throw IllegalStateException("Unknown proto type $type")
        }
    }

    private fun toTypeScriptImports(type: Type): List<String> {
        val packageComponents = type.type.packageComponents
        val referencedTypes = type.referencedTypesAndNestedReferencedTypes

        // TypeScript automatically imports anything in the same directory,
        // which given our generated directory structure, means anything in
        // the same package.
        val typesOutsideFile = referencedTypes
            .filter { it.packageComponents != packageComponents }

        return typesOutsideFile.map {
            // Convert package/directory structure to be relative to
            // the current package/directory.
            val relativePackage = it.packageComponents.foldIndexed(mutableListOf<String>()) { index, acc, component ->
                if (acc.isNotEmpty() || component != packageComponents[index]) {
                    acc.add(component)
                }
                acc
            }
            //
            val commonComponentCount = it.packageComponents.size - relativePackage.size
            val backingOutPath = (1 .. (packageComponents.size - commonComponentCount)).map { ".." }
            val importPath = backingOutPath.plus(relativePackage).joinToString("/")
            // For now this only supports importing the default export.
            "import ${it.simpleName} from \"$importPath/${it.simpleName}\""
        }
    }

    // Generate the TypeScript code for this type and nested types.
    // Returns a LinkedHashMap so that it's ordered.
    private fun toTypeScriptTypes(type: Type, generated: LinkedHashMap<Type, String>) {
        generated[type] = toTypeScript(type)
        type.nestedTypes.forEach {
            toTypeScriptTypes(it, generated)
        }
    }
}
