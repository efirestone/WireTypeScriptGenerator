package com.codellyrandom

import com.squareup.wire.schema.*
import java.lang.IllegalStateException
import java.nio.file.Path

class UnresolvedTypeManager(
    private val typeResolver: TypeResolver,
    private val filesByProtoType: MutableMap<String, Path> = mutableMapOf(),
    private val unresolvedFieldProtoTypesByParentProtoType: MutableMap<String, Set<ProtoType>> = mutableMapOf()
) {
    fun addUnresolvedFieldProtoType(fieldProtoType: ProtoType, inProtoType: ProtoType) {
        unresolvedFieldProtoTypesByParentProtoType[inProtoType.toString()] =
            (unresolvedFieldProtoTypesByParentProtoType[inProtoType.toString()] ?: setOf()).plus(fieldProtoType)
    }

    // When we see a type we'll register the path where we saw it.
    // We need this because when we parse the type later we don't have the path.
    fun setPathForProtoType(path: Path, protoType: ProtoType) {
        filesByProtoType[protoType.toString()] = path
    }

    // When we encounter new types they should be passed here
    // so that they can be resolved in other files that may have referenced them.
    fun resolve(types: Set<Type>) {
        unresolvedFieldProtoTypesByParentProtoType.forEach { entry ->
            val typesToResolve = types.filter { entry.value.contains(it.type) }
            if (typesToResolve.isEmpty()) {
                // Don't need to resolve any of these types in this file.
                return@forEach
            }

            val encoding = Charsets.UTF_8

            // The file path is registered for the root type.
            val components = entry.key.split(".").toMutableList()
            while (components[components.size - 2][0].isUpperCase()) {
                components.removeLast()
            }
            val rootType = components.joinToString(".")

            val file = filesByProtoType[rootType]!!.toFile()
            var contents = file.inputStream().readBytes().toString(encoding)
            typesToResolve.forEach { type ->
                val token = type.type.fieldAssociationToken
                contents = when (type) {
                    is MessageType -> contents.replace(token, type.type.fieldAssociation(typeResolver))
                    is EnumType -> contents.replace(token, "")
                    else -> throw IllegalStateException("Unknown proto type $type")
                }
            }
            file.outputStream().write(contents.toByteArray(encoding))
        }
    }
}
