package com.codellyrandom

import com.squareup.wire.schema.MessageType
import com.squareup.wire.schema.ProtoType
import com.squareup.wire.schema.Type

val Type.referencedTypesAndNestedReferencedTypes: List<ProtoType>
    get() {
        val types = mutableListOf<ProtoType>()
        if (this is MessageType) {
            types.addAll(
                fieldsAndOneOfFields
                .mapNotNull { it.type }
                .filter { !it.isScalar }
            )
        }
        return nestedTypes.fold(types) { acc, type ->
            acc.addAll(type.referencedTypesAndNestedReferencedTypes)
            return@fold acc
        }.toList()
    }
