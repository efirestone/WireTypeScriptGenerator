package com.codellyrandom.wiretypescriptgenerator

import com.squareup.wire.schema.SchemaHandler

class TypeScriptGeneratorFactory: SchemaHandler.Factory {
    override fun create(
        includes: List<String>,
        excludes: List<String>,
        exclusive: Boolean,
        outDirectory: String,
        options: Map<String, String>
    ): SchemaHandler {
        return TypeScriptGenerator()
    }
}