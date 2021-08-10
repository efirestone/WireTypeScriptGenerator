package com.codellyrandom

import com.squareup.wire.schema.ProtoType

val ProtoType.isRootType: Boolean
    get() {
        return this.toString()
            .split(".")
            .filter { it[0].isUpperCase() }
            .size == 1
    }

// The package, like ["com", "codellyrandom"] in "com.codellyrandom.Foo.Bar"
val ProtoType.packageComponents: List<String>
    get() {
        return this.toString()
            .split(".")
            .filter { it[0].isLowerCase() }
    }

// The nested type names, like ["Foo", "Bar"] in "com.codellyrandom.Foo.Bar"
val ProtoType.nameComponents: List<String>
    get() {
        return this.toString()
            .split(".")
            .filter { it[0].isUpperCase() }
    }

// A placeholder token for the TypeScript association for this field.
// This token will be used for types that we haven't seen yet, and will
// be replaced when we see that type.
val ProtoType.fieldDecoratorToken: String
    get() = "{{fieldDecorator:${toString()}}}"

fun ProtoType.fieldDecorator(typeResolver: TypeResolver): String =
    "@Type(() => ${typeResolver.nameFor(this)})\n"