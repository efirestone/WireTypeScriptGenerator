package com.codellyrandom

import com.squareup.javapoet.JavaFile
import com.squareup.kotlinpoet.FileSpec
import com.squareup.wire.WireLogger
import com.squareup.wire.schema.ProtoType
import java.nio.file.Path
import io.outfoxx.swiftpoet.FileSpec as SwiftFileSpec

internal class BlackHoleWireLogger : WireLogger {
    override fun setQuiet(quiet: Boolean) {}

    override fun artifact(outputPath: Path, filePath: String) {}

    override fun artifactSkipped(type: ProtoType) {}

    override fun info(message: String) {}

    override fun artifact(outputPath: Path, javaFile: JavaFile) {}

    override fun artifact(outputPath: Path, kotlinFile: FileSpec) {}

    override fun artifact(outputPath: Path, type: ProtoType, swiftFile: SwiftFileSpec) {}
}
