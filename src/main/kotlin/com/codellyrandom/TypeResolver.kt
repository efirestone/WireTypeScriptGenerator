package com.codellyrandom

import com.squareup.wire.Syntax
import com.squareup.wire.schema.*
import java.lang.IllegalStateException

// Converts protobuf types to TypeScript types
class TypeResolver(
    private val rpcByRequestOrResponseProtoType: MutableMap<ProtoType, Rpc> = mutableMapOf(),
    private val typesByProtoType: MutableMap<ProtoType, Type> = mutableMapOf()
) {

    init {
        add(Type.createDummyMessageType(ProtoType.TIMESTAMP))
    }

    fun add(service: Service) {
        service.rpcs.forEach {
            val requestType = it.requestType
            if (requestType != null) {
                rpcByRequestOrResponseProtoType[requestType] = it
            }
            val responseType = it.responseType
            if (responseType != null) {
                rpcByRequestOrResponseProtoType[responseType] = it
            }
        }
    }

    fun add(type: Type) {
        typesByProtoType[type.type] = type
    }

    // If this returns null the prototype may describe
    // a valid type, but we haven't registered it yet.
    fun rpcForRequestOrResponse(protoType: ProtoType): Rpc? = rpcByRequestOrResponseProtoType[protoType]

    // If this returns null the prototype may describe
    // a valid type, but we haven't registered it yet.
    fun typeFor(protoType: ProtoType): Type? = typesByProtoType[protoType]

    fun nameFor(type: ProtoType): String {
        // Scalars
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

        // Built-in Types
        when (type.toString()) {
            "google.protobuf.Timestamp" -> return "Date"
        }

        // Everything else
        return type.toString()
            .split(".")
            .filter { it[0].isUpperCase() }
            .joinToString("_")
    }
}

private fun Type.Companion.createDummyMessageType(protoType: ProtoType): Type {
    return MessageType(
        type = protoType,
        location = Location.get("fake/path"),
        documentation = "",
        name = "",
        declaredFields = listOf(),
        extensionFields = mutableListOf(),
        oneOfs = listOf(),
        nestedTypes = listOf(),
        extensionsList = listOf(),
        reserveds = listOf(),
        options = Options(Options.MESSAGE_OPTIONS, listOf()),
        syntax = Syntax.PROTO_3
    )
}