package com.codellyrandom

import com.squareup.wire.schema.MessageType
import com.squareup.wire.schema.Service

class TypeScriptServiceGenerator(
    private val service: Service,
    private val typeResolver: TypeResolver
) {
    fun generate(): String {
        return """
            |${service.documentation.toDocumentation(0)}
            |export default class ${service.name} {
            |  client: ServiceNetworkClient
            |
            |  constructor(client: ServiceNetworkClient) {
            |    this.client = client
            |  }
            |
            |$rpcs
            |}
            |""".trimMargin()
    }

    private val rpcs: String
        get() {
            return service.rpcs.fold("") { acc, rpc ->
                val functionName = rpc.name.mapIndexed { index, c ->
                    if (index == 0) { c.lowercase() } else { c }
                }.joinToString("")

                val path = service.pathFor(rpc)
                val requestType = rpc.requestType?.let { typeResolver.typeFor(it) }
                val responseTypeName = typeResolver.nameFor(rpc.responseType!!)
                if (requestType == null || (requestType is MessageType && requestType.fieldsAndOneOfFields.isEmpty())) {
                    acc + """
                        |${rpc.documentation.toDocumentation(2)}
                        |  async $functionName(): Promise<$responseTypeName> {
                        |    const response = await this.client.post("$path", null)
                        |    return plainToClass($responseTypeName, response.data as JSON)
                        |  }
                        |""".trimMargin().trimEmptyLines() + "\n"
                } else {
                    val requestTypeName = typeResolver.nameFor(requestType.type)
                    acc + """
                        |${rpc.documentation.toDocumentation(2)}
                        |  async $functionName(request: $requestTypeName): Promise<$responseTypeName> {
                        |    const response = await this.client.post("$path", serialize(request))
                        |    return plainToClass($responseTypeName, response.data as JSON)
                        |  }
                        |""".trimMargin().trimEmptyLines() + "\n"
                }
            }.trimEmptyLines()
        }

}
