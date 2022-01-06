package com.codellyrandom.wiretypescriptgenerator

import com.squareup.wire.schema.CustomTargetBeta
import com.squareup.wire.schema.Location
import com.squareup.wire.schema.WireRun
import okio.FileSystem
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Path
import kotlin.streams.toList
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class TypeScriptGeneratorTests {
    private val fs = FileSystems.getDefault()
    private val logger = BlackHoleWireLogger()

    @Test
    fun generateTypeScript() {
        val testName = "generateTypeScript"
        val outDir = "build/generated/$testName"
        val wireRun = WireRun(
            sourcePath = listOf(Location.get("src/test/proto/com/codellyrandom")),
            protoPath = listOf(Location.get("src/test/proto/com/codellyrandom")),
            targets = listOf(CustomTargetBeta(
                outDirectory = outDir,
                customHandler = TypeScriptGenerator()
            ))
        )
        wireRun.execute(FileSystem.SYSTEM, logger)

        assertNoDiff(outDir, "generateTypeScript")
    }

    private fun assertNoDiff(outDir: String, expectedDirName: String) {
        val expectedResultDir = this.javaClass.classLoader.getResource("expected/$expectedDirName")!!
        val outPath = fs.getPath(outDir)

        val expectedRelativePaths = Files.walk(outPath)
            .filter(Files::isRegularFile)
            .map { outPath.toUri().relativize(it.toUri()).path }
            .toList()
            .sorted()
        val actualRelativePaths = Files.walk(Path.of(expectedResultDir.toURI()))
            .filter(Files::isRegularFile)
            .map { expectedResultDir.toURI().relativize(it.toUri()).path }
            .toList()
            .sorted()

        assertEquals(
            actualRelativePaths,
            expectedRelativePaths,
            "Actual generated file list doesn't match the expected list"
        )

        if (expectedRelativePaths != actualRelativePaths) {
            return
        }

        Files.walk(outPath)
            .filter(Files::isRegularFile)
            .forEach {
                val relativePath = outPath.toUri().relativize(it.toUri())
                val expectedFile = fs.getPath(expectedResultDir.file, relativePath.toString())
                val expectedBytes = expectedFile.toFile().inputStream().readBytes()
                val actualBytes = it.toFile().inputStream().readBytes()

                if (!expectedBytes.contentEquals(actualBytes)) {
                    println("File ${it.toFile().name} does not match the expected value.")
                    println("Run the following to see the difference:")
                    println("   diff -C10 \"${it.toAbsolutePath()}\" \"${expectedFile.toAbsolutePath()}\"")
                    fail("File ${it.toFile().name} does not match the expected value.")
                }
            }
    }
}