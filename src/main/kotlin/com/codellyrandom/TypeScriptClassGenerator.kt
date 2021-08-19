package com.codellyrandom

import com.squareup.wire.schema.Field
import com.squareup.wire.schema.MessageType
import com.squareup.wire.schema.Type
import java.lang.IllegalStateException

class TypeScriptClassGenerator(
    private val type: Type,
    private val exportAsDefault: Boolean,
    private val typeResolver: TypeResolver,
    private val unresolvedTypeManager: UnresolvedTypeManager
) {
    fun generate(): String {
        var body = """
            |$fields
            |
            |$constructor
            |""".trimMargin()
        body = if (body.isBlank()) { "" } else { "\n${body.trimEmptyLines()}\n" }

        return """
            |${type.documentation.toDocumentation(0)}
            |export class ${typeResolver.nameFor(type.type)} {$body}
            |
            |$defaultExport
            |""".trimMargin().trimEmptyLines()
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
            // Only required fields go in the constructor so that we force them to be set.
            // All other fields can optionally be set with the `configure` block.
            val requiredFields = (type.declaredFields + type.extensionFields)
                .filter { it.label == Field.Label.REQUIRED || it.label == null }
            if (type.fieldsAndOneOfFields.isEmpty()) {
                // No fields, so no need for a constructor
                return ""
            } else if (requiredFields.isEmpty()) {
                // Return a nicely formatted convenience constructor
                return """
                    |  constructor(configure: ((o: ${typeResolver.nameFor(type.type)}) => void) | undefined = undefined) {
                    |    configure?.call(this, this)
                    |  }
                    |""".trimMargin().trimEnd()
            }

            val assignments = requiredFields.fold("") { acc, messageField ->
                val name = messageField.jsonName ?: messageField.name
                "$acc    this.$name = $name\n"
            }.trimEnd()

            val includeConfigureBlock = (requiredFields.size < type.fieldsAndOneOfFields.size)
            if (!includeConfigureBlock) {
                // If we can configure all the fields as arguments, then there's no need
                // for the configure block.
                return """
                    |  constructor(${requiredFields.toConstructorArguments(typeResolver)}) {
                    |$assignments
                    |  }
                    |""".trimMargin().trimEnd()
            } else {
                val arguments = requiredFields.toConstructorArguments(
                    typeResolver,
                    listOf("configure: ((o: ${typeResolver.nameFor(type.type)}) => void) | undefined = undefined")
                )
                return """
                    |  constructor($arguments) {
                    |$assignments
                    |    configure?.call(this, this)
                    |  }
                    |""".trimMargin().trimEnd()
            }
        }

    private val defaultExport: String
        get() = if (exportAsDefault && type.type.isRootType) "export default ${typeResolver.nameFor(type.type)}" else ""

    private val fields: String
        get() {
            if (type is MessageType) {
                var content = (type.declaredFields + type.extensionFields).fold("") { acc, messageField ->
                    acc + messageField.toClassMember(typeResolver) + "\n"
                }.trimEnd()

                type.oneOfs.forEach { oneOf ->
                    content += "\n\n  // ${oneOf.name}: At most one of these fields will be non-null\n"
                    content += (oneOf.documentation.toDocumentation(2) + "\n").trimEmptyLines()
                    content = oneOf.fields.fold(content) { acc, field ->
                        acc + field.toClassMember(typeResolver) + "\n"
                    }.trimEnd()
                }

                // Record any unresolved types so that when we see them in the future we can go back
                // and resolve them.
                type.fieldsAndOneOfFields.forEach {
                    val fieldProtoType = it.type
                    if (fieldProtoType != null && typeResolver.typeFor(fieldProtoType) == null) {
                        unresolvedTypeManager.addUnresolvedFieldProtoType(fieldProtoType, type.type)
                    }
                }

                return content
            }
            throw IllegalStateException("Protobuf type $type cannot be converted to a TypeScript class")
        }
}
