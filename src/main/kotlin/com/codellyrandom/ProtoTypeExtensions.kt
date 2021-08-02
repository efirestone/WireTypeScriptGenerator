package com.codellyrandom

import com.squareup.wire.schema.ProtoType

val ProtoType.isRootType: Boolean
    get() {
        return this.toString()
            .split(".")
            .filter { it[0].isUpperCase() }
            .size == 1
    }

val ProtoType.packageComponents: List<String>
    get() {
        return this.toString()
            .split(".")
            .filter { it[0].isLowerCase() }
    }
