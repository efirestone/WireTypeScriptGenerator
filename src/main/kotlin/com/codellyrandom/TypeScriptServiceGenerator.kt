package com.codellyrandom

import com.squareup.wire.schema.MessageType
import com.squareup.wire.schema.Service

class TypeScriptServiceGenerator(
    private val service: Service,
    private val typeResolver: TypeResolver = TypeResolver()
) {
    fun generate(): String {
        return """
            |${documentation}export default class ${service.name} {
            |  client: ServiceNetworkClient
            |  
            |  constructor(client: ServiceNetworkClient) {
            |    this.client = client
            |  }
            |
            |$rpcs
            |}
            |""".trimMargin().trimEnd()
    }

    private val documentation: String = service.documentation.toDocumentation(0)

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
                        |  async $functionName(): Promise<$responseTypeName> {
                        |    const response = await this.client.post("$path", null)
                        |    return plainToClass($responseTypeName, response.data as Map<string, any>)
                        |  }
                        |""".trimMargin().trimEnd() + "\n"
                } else {
                    val requestTypeName = typeResolver.nameFor(requestType.type)
                    acc + """
                        |  async $functionName(request: $requestTypeName): Promise<$responseTypeName> {
                        |    const response = await this.client.post("$path", serialize(request))
                        |    return plainToClass($responseTypeName, response.data as Map<string, any>)
                        |  }
                        |""".trimMargin().trimEnd() + "\n"
                }
            }.trimEmptyLines()
        }

}
