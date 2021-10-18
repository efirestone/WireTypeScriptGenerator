package com.codellyrandom.wiretypescriptgenerator

import com.squareup.wire.schema.ProtoType

val ProtoType.isRootType: Boolean
    get() {
        return this.toString()
            .split(".")
            .filter { it[0].isUpperCase() }
            .size == 1
    }

// A placeholder token for the TypeScript association for this field.
// This token will be used for types that we haven't seen yet, and will
// be replaced when we see that type.
val ProtoType.fieldDecoratorToken: String
    get() = "{{fieldDecorator:${toString()}}}"

fun ProtoType.fieldDecorator(typeResolver: TypeResolver): String =
    "@Type(() => ${typeResolver.nameFor(this)})\n"