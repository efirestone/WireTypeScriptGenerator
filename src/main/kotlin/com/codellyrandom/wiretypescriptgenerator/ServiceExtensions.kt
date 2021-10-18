package com.codellyrandom.wiretypescriptgenerator

import com.squareup.wire.schema.ProtoType
import com.squareup.wire.schema.Rpc
import com.squareup.wire.schema.Service

val Service.requestAndResponseProtoTypes: List<ProtoType>
    get() {
        return this.rpcs.fold(mutableListOf<ProtoType>()) { acc, rpc ->
            val requestType = rpc.requestType
            if (requestType != null) {
                acc.add(requestType)
            }
            val responseType = rpc.responseType
            if (responseType != null) {
                acc.add(responseType)
            }
            acc
        }.toList()
    }

fun Service.pathFor(rpc: Rpc): String {
    val prefix = this.name.removeSuffix("Service").toKebabCase()
    val suffix = rpc.name.toKebabCase()

    return "$prefix/$suffix"
}
