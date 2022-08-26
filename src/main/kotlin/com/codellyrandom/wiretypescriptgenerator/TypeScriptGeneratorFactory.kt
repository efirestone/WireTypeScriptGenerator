package com.codellyrandom.wiretypescriptgenerator

import com.squareup.wire.schema.SchemaHandler

class TypeScriptGeneratorFactory: SchemaHandler.Factory {
    override fun create() = TypeScriptGenerator()
}