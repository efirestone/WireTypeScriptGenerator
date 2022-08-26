package com.codellyrandom.wiretypescriptgenerator

import com.squareup.wire.WireLogger
import com.squareup.wire.schema.ProtoType
import okio.Path

internal class BlackHoleWireLogger : WireLogger {
    override fun artifactHandled(outputPath: Path, qualifiedName: String, targetName: String) {}

    override fun artifactSkipped(type: ProtoType, targetName: String) {}

    override fun unusedExcludesInTarget(unusedExcludes: Set<String>) {}

    override fun unusedIncludesInTarget(unusedIncludes: Set<String>) {}

    override fun unusedPrunes(unusedPrunes: Set<String>) {}

    override fun unusedRoots(unusedRoots: Set<String>) {}
}
