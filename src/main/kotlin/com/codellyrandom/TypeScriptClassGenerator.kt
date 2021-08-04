package com.codellyrandom

import com.squareup.wire.schema.EnumType
import com.squareup.wire.schema.Field
import com.squareup.wire.schema.MessageType
import com.squareup.wire.schema.Type
import java.lang.IllegalStateException

class TypeScriptClassGenerator(
    private val type: Type,
    private val typeResolver: TypeResolver,
    private val unresolvedTypeManager: UnresolvedTypeManager
) {
    fun generate(): String {
        return """
            |$documentation$export class ${typeResolver.nameFor(type.type)} {
            |$fields
            |}
            |""".trimMargin().trimEnd()
    }

    private val documentation: String = type.documentation.toDocumentation(0)

    private val export: String
        get() = if (type.type.isRootType) "export default" else "export"

    private val fields: String
        get() {
            if (type is MessageType) {
                return type.declaredFields.fold("") { acc, field ->
                    acc + toTypeScript(
                        field = field,
                        includeDocumentation = true,
                        includeDefault = true,
                        useShortcutOptional = true
                    ) +"\n"
                }.trimEnd()
            }
            throw IllegalStateException("Protobuf type $type cannot be converted to a TypeScript class")
        }

    private fun toTypeScript(
        field: Field,
        includeDocumentation: Boolean,
        includeDefault: Boolean,
        useShortcutOptional: Boolean
    ): String {
        val stringBuilder = StringBuilder()
        val fieldType = field.type!!
        if (includeDocumentation) {
            stringBuilder.append(field.documentation.toDocumentation(2))
        }
        if (!fieldType.isScalar) {
            val fullType = typeResolver.typeFor(fieldType)
            stringBuilder.append(
                when (fullType) {
                    is EnumType -> ""
                    is MessageType -> fieldType.fieldAssociation(typeResolver)
                    null -> fieldType.fieldAssociationToken
                    else -> throw IllegalStateException("Unknown field type.")
                }
            )
            if (fullType == null) {
                unresolvedTypeManager.addUnresolvedFieldProtoType(fieldType, type.type)
            }
        }
        stringBuilder.append("  ")
        stringBuilder.append(field.name)
        if (useShortcutOptional && !field.isRequired) {
            stringBuilder.append("?")
        }
        stringBuilder.append(": ")
        stringBuilder.append(typeResolver.nameFor(fieldType))
        if (field.isRepeated) {
            stringBuilder.append("[]")
        }
        if (!useShortcutOptional && !field.isRequired) {
            stringBuilder.append(" | undefined")
        }
        if (includeDefault) {
            if (field.default != null) {
                stringBuilder.append(" = ${field.default}")
            } else if (!field.isRequired) {
                stringBuilder.append(" = undefined")
            }
        }
        return stringBuilder.toString()
    }
}
