package com.codellyrandom

import com.squareup.wire.schema.ProtoType
import java.lang.IllegalStateException

// Converts protobuf types to TypeScript types
class TypeResolver() {
    fun nameFor(type: ProtoType): String {
        if (type.isScalar) {
            return when (type.toString()) {
                "double", "float",
                "int32", "int64",
                "uint32", "uint64",
                "sint32", "sint64",
                "fixed32", "fixed64",
                "sfixed32", "sfixed64" -> "number"
                "bool" -> "boolean"
                "string" -> "string"
                "bytes" -> "Uint8Array"
                else -> throw IllegalStateException("Unknown scalar type $type")
            }
        }
        return type.toString()
            .split(".")
            .filter { it[0].isUpperCase() }
            .joinToString("_")
    }
}
