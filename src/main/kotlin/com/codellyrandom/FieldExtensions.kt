package com.codellyrandom

import com.squareup.wire.schema.EnumType
import com.squareup.wire.schema.Field
import com.squareup.wire.schema.MessageType
import java.lang.IllegalStateException

fun List<Field>.toConstructorArguments(
    typeResolver: TypeResolver,
    additionalArguments: List<String> = listOf()
): String {
    if (this.isEmpty()) { return "" }

    val arguments = this.map { it.toArgument(typeResolver) }.plus(additionalArguments)
    val singleLine = arguments.joinToString(", ")

    return if (singleLine.length < 98) {
        singleLine
    } else {
        "\n    ${arguments.joinToString(",\n    " )}\n  "
    }
}

fun Field.toArgument(typeResolver: TypeResolver): String {
    val stringBuilder = StringBuilder()
    val fieldType = this.type!!

    stringBuilder.append(this.jsonName ?: this.name)

    stringBuilder.append(": ")
    stringBuilder.append(typeResolver.nameFor(fieldType))
    if (this.isRepeated) {
        stringBuilder.append("[]")
    }
    return stringBuilder.toString()
}

fun Field.toClassMember(typeResolver: TypeResolver): String {
    val indent = 2
    val stringBuilder = StringBuilder()
    val fieldType = this.type!!
    if (this.documentation.isNotBlank()) {
        stringBuilder.append(this.documentation.toDocumentation(2) + "\n")
    }

    if (!fieldType.isScalar) {
        val fullType = typeResolver.typeFor(fieldType)
        stringBuilder.append(
            when (fullType) {
                is EnumType -> ""
                is MessageType -> " ".repeat(indent) + fieldType.fieldDecorator(typeResolver)
                null -> fieldType.fieldDecoratorToken
                else -> throw IllegalStateException("Unknown field type.")
            }
        )
    }
    stringBuilder.append(" ".repeat(indent))
    stringBuilder.append(this.jsonName ?: this.name)

    val isOptional = this.label == Field.Label.OPTIONAL || this.isOneOf
    if (isOptional) {
        stringBuilder.append("?")
    }
    stringBuilder.append(": ")
    stringBuilder.append(typeResolver.nameFor(fieldType))
    if (this.isRepeated) {
        stringBuilder.append("[]")
    }

    if (this.default != null) {
        stringBuilder.append(" = ${this.default}")
    } else if (isOptional) {
        stringBuilder.append(" = undefined")
    } else if (this.isRepeated) {
        stringBuilder.append(" = []")
    }

    return stringBuilder.toString()
}