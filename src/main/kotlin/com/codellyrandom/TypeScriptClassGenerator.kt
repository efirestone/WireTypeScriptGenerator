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
        var body = """
            |$fields
            |
            |$constructor
            |""".trimMargin()
        body = if (body.isBlank()) { "" } else { "\n  ${body.trim()}\n" }

        return """
            |$documentation$export class ${typeResolver.nameFor(type.type)} {$body}
            |""".trimMargin().trimEnd()
    }

    // Provide a convenient constructor that works for all types.
    // Any arguments without defaults are added as required parameters.
    // Usage:
    // const foo = Foo(f => {
    //   f.bar = "baz"
    // })
    private val constructor: String
        get() {
            if (type !is MessageType) {
                throw IllegalStateException("Protobuf type $type cannot be converted to a TypeScript class")
            }
            val requiredFields = (type.declaredFields + type.extensionFields)
                .filter { it.label == Field.Label.REQUIRED || it.label == null }
            if (type.fieldsAndOneOfFields.isEmpty()) {
                // No fields, so no need for a constructor
                return ""
            } else if (requiredFields.isEmpty()) {
                // Return a nicely formatted convenience constructor
                return """
                    |  constructor(configure: (o: ${typeResolver.nameFor(type.type)}) => void = (_ => {})) {
                    |    configure(this)
                    |  }
                    |""".trimMargin().trimEnd()
            }

            val arguments = requiredFields.fold("") { acc, field ->
                acc + toTypeScript(
                    field = field,
                    indent = 4,
                    includeDocumentation = false,
                    includeTypeAssociation = false,
                    includeDefault = true,
                    useShortcutOptional = false
                ) + ",\n"
            }.trimEnd()
            val assignments = requiredFields.fold("") { acc, field ->
                val name = field.jsonName ?: field.name
                "$acc    this.$name = $name\n"
            }.trimEnd()

            val includeConfigureBlock = (requiredFields.size < type.fieldsAndOneOfFields.size)
            if (!includeConfigureBlock) {
                // If we can configure all the fields as arguments, then there's no need
                // for the configure block.
                return """
                    |  constructor(
                    |${arguments.trimEnd(',')}
                    |  ) {
                    |$assignments
                    |  }
                    |""".trimMargin().trimEnd()
            } else {
                return """
                    |  constructor(
                    |$arguments
                    |    configure: (o: ${typeResolver.nameFor(type.type)}) => void = (_ => {})
                    |  ) {
                    |$assignments
                    |    configure(this)
                    |  }
                    |""".trimMargin().trimEnd()
            }
        }

    private val documentation: String = type.documentation.toDocumentation(0)

    private val export: String
        get() = if (type.type.isRootType) "export default" else "export"

    private val fields: String
        get() {
            if (type is MessageType) {
                var content = (type.declaredFields + type.extensionFields).fold("") { acc, field ->
                    acc + toTypeScript(
                        field = field,
                        indent = 2,
                        includeDocumentation = true,
                        includeTypeAssociation = true,
                        includeDefault = true,
                        useShortcutOptional = true
                    ) +"\n"
                }.trimEnd()

                type.oneOfs.forEach { oneOf ->
                    content += "\n\n  // ${oneOf.name}: At most one of these fields will be non-null\n"
                    content += oneOf.documentation.toDocumentation(2)
                    content = oneOf.fields.fold(content) { acc, field ->
                        acc + toTypeScript(
                            field = field,
                            indent = 2,
                            includeDocumentation = true,
                            includeTypeAssociation = true,
                            includeDefault = true,
                            useShortcutOptional = true
                        ) +"\n"
                    }.trimEnd()
                }

                return content
            }
            throw IllegalStateException("Protobuf type $type cannot be converted to a TypeScript class")
        }

    private fun toTypeScript(
        field: Field,
        indent: Int,
        includeDocumentation: Boolean,
        includeTypeAssociation: Boolean,
        includeDefault: Boolean,
        useShortcutOptional: Boolean
    ): String {
        val stringBuilder = StringBuilder()
        val fieldType = field.type!!
        if (includeDocumentation) {
            stringBuilder.append(field.documentation.toDocumentation(2))
        }
        if (includeTypeAssociation && !fieldType.isScalar) {
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
        stringBuilder.append(" ".repeat(indent))
        stringBuilder.append(field.jsonName ?: field.name)

        val isOptional = field.label == Field.Label.OPTIONAL || field.isOneOf
        if (useShortcutOptional && isOptional) {
            stringBuilder.append("?")
        }
        stringBuilder.append(": ")
        stringBuilder.append(typeResolver.nameFor(fieldType))
        if (field.isRepeated) {
            stringBuilder.append("[]")
        }
        if (!useShortcutOptional && isOptional) {
            stringBuilder.append(" | undefined")
        }
        if (includeDefault) {
            if (field.default != null) {
                stringBuilder.append(" = ${field.default}")
            } else if (isOptional) {
                stringBuilder.append(" = undefined")
            } else if (field.isRepeated) {
                stringBuilder.append(" = []")
            }
        }
        return stringBuilder.toString()
    }
}
