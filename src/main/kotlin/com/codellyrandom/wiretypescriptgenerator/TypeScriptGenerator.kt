package com.codellyrandom.wiretypescriptgenerator

import com.squareup.wire.schema.*
import okio.FileSystem
import okio.Path
import okio.Path.Companion.toPath
import java.lang.IllegalStateException

/**
 * This is a sample handler that writes text files that describe types.
 */
@Suppress("unused")
class TypeScriptGenerator : SchemaHandler() {
    private val typeResolver = TypeResolver()
    private val unresolvedTypeManager = UnresolvedTypeManager(typeResolver)

    override fun handle(schema: Schema, context: Context) {
        schema.protoFiles.forEach { protoFile ->
            protoFile.services.forEach { typeResolver.add(it) }
            val typesInFile = protoFile.types.fold(setOf<Type>()) { acc, type ->
                acc.plus(elements = type.typesAndNestedTypes())
            }
            typesInFile.forEach { typeResolver.add(it) }
            unresolvedTypeManager.resolve(typesInFile)
        }
        super.handle(schema, context)
    }
    override fun handle(extend: Extend, field: Field, context: Context): Path? {
        return null
    }

    override fun handle(service: Service, context: Context): List<Path> {
        writeClientInterface(service, context.outDirectory, context.fileSystem)

        val staticImports = listOf(
            "import { plainToClass, serialize } from \"class-transformer\"",
            "import ServiceNetworkClient from \"./ServiceNetworkClient\"",
        )

        val types = service.requestAndResponseProtoTypes.map {
            // We rely on the fact that any referenced request/response types have already been parsed.
            // This will be true for any types declared in the same file, which is good enough for now.
            typeResolver.typeFor(it)!!
        }
        val imports = types.fold(staticImports) { acc, type ->
            acc.plus(toTypeScriptImports(type))
        }.distinct().sorted()

        val typeContent = LinkedHashMap<Type, String>()
        types.forEach { toTypeScriptTypes(it, false, typeResolver, typeContent) }

        val serviceContent = TypeScriptServiceGenerator(service, typeResolver).generate()

        var content = ""
        imports.forEach { content += it + "\n"}
        content += "\n"
        typeContent.forEach { content += it.value + "\n\n" }
        content += serviceContent

        val path = writeFile(service.type, content, context.outDirectory, context.fileSystem)
        types.forEach { unresolvedTypeManager.setPathForProtoType(path, it.type) }

        return listOf(path)
    }

    override fun handle(type: Type, context: Context): Path? {
        // If this type is a request or response in a service then it will
        // be included in the service's file.
        if (typeResolver.rpcForRequestOrResponse(type.type) != null) {
            return null
        }

        val generated = toTypeScriptTypes(type, true, typeResolver)

        val imports = toTypeScriptImports(type)

        var content = ""
        imports.forEach { content += it + "\n"}
        content += "\n"
        generated.forEach { content += it.value + "\n\n" }
        return writeFile(type.type, content.trim() + "\n", context.outDirectory, context.fileSystem)
    }

    private fun writeFile(protoType: ProtoType, content: String, outDirectory: Path, fileSystem: FileSystem): Path {
        val path = outDirectory / toPath(protoType).joinToString("/")
        unresolvedTypeManager.setPathForProtoType(path, protoType)
        fileSystem.createDirectories(path.parent!!)
        fileSystem.write(path) {
            writeUtf8(content)
        }
        return path
    }

    private fun writeClientInterface(service: Service, outDirectory: Path, fileSystem: FileSystem) {
        val parts = toPath(service.type).toMutableList()
        parts[parts.size - 1] = "ServiceNetworkClient.ts"

        val content = """
                    // A network response.
                    // AxiosResponse fulfills the requirements of this interface.  
                    export interface ServiceNetworkResponse<T = any> {
                      data: T;
                    }
                    
                    // A network client which can send requests.
                    // AxiosInstance fulfills the requirements of this interface and can be passed in via
                    //   (axios as ServiceNetwork)
                    export default interface ServiceNetworkClient {
                      // Send a POST network request to a given path. 
                      // The path will not include the domain and will be something like "/users/add"
                      // The data will be the a JSON string to send as the request payload.
                      post<T = any, R = ServiceNetworkResponse<T>>(path: string, data?: any): Promise<R>;
                    }
                """.trimIndent()

        val path = outDirectory / parts.joinToString("/")
        fileSystem.createDirectories(path.parent!!)
        fileSystem.write(path) {
            writeUtf8(content)
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

    private fun toTypeScript(type: Type, exportAsDefault: Boolean, typeResolver: TypeResolver): String {
        return when (type) {
            is EnumType -> TypeScriptEnumGenerator(type, typeResolver).generate()
            is MessageType -> TypeScriptClassGenerator(
                type,
                exportAsDefault,
                typeResolver,
                unresolvedTypeManager
            ).generate()
            else -> throw IllegalStateException("Unknown proto type $type")
        }
    }

    // Return the list of imports required for a given type.
    private fun toTypeScriptImports(type: Type): List<String> {
        val importCalculator = ImportPathCalculator(type.type, typeResolver)
        val referencedTypes = type.referencedTypesAndNestedReferencedTypes
        val typesInFile = type.typesAndNestedTypes().map { it.type }

        // We only need to import the types not in this file.
        // Also filter out the built-in types as we'll use native TypeScript types for those.
        val typesOutsideFile = referencedTypes
            .subtract(typesInFile.toSet())
            .filter { !it.toString().startsWith("google.protobuf") }

        val transformerImport = if (referencedTypes.isEmpty()) {
            listOf()
        } else {
            listOf("import { Type } from \"class-transformer\"\n")
        }
        val typeImports = typesOutsideFile.map {
            importCalculator.importStatementFor(it)
        }

        return transformerImport.plus(typeImports)
    }

    // Generate the TypeScript code for this type and nested types.
    // Returns a LinkedHashMap so that it's ordered.
    private fun toTypeScriptTypes(
        type: Type,
        exportAsDefault: Boolean,
        typeResolver: TypeResolver,
        generated: LinkedHashMap<Type, String> = LinkedHashMap()
    ): LinkedHashMap<Type, String> {
        generated[type] = toTypeScript(type, exportAsDefault, typeResolver)
        type.nestedTypes.forEach {
            toTypeScriptTypes(it, exportAsDefault, typeResolver, generated)
        }
        return generated
    }

}
