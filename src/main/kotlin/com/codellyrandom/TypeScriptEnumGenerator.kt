package com.codellyrandom

import com.squareup.wire.schema.*
import java.lang.IllegalStateException

class TypeScriptEnumGenerator(
    private val type: Type,
    private val typeResolver: TypeResolver = TypeResolver()
) {
    fun generate(): String {
        return """
            |${documentation}${inlineExport}enum ${typeResolver.nameFor(type.type)} {
            |$constants
            |}
            |
            |$exportStatement
            |""".trimMargin().trimEnd()
    }

    private val documentation: String = type.documentation.toDocumentation(0)

    private val inlineExport: String
        get() = if (type.type.isRootType) "" else "export "

    private val exportStatement: String
        get() = if (type.type.isRootType) "export default ${typeResolver.nameFor(type.type)};" else ""

    private val constants: String
        get() {
            if (type is EnumType) {
                return type.constants.fold("") { acc, value ->
                    acc + toTypeScript(value) + "\n"
                }.trimEnd()
            }
            throw IllegalStateException("Protobuf type $type cannot be converted to a TypeScript enum")
        }

    private fun toTypeScript(
        constant: EnumConstant,
        includeDocumentation: Boolean = true
    ): String {
        val stringBuilder = StringBuilder()
        if (includeDocumentation) {
            stringBuilder.append(constant.documentation.toDocumentation(2))
        }
        stringBuilder.append("  ${constant.name} = \"${constant.name}\",")

        return stringBuilder.toString()
    }
}
