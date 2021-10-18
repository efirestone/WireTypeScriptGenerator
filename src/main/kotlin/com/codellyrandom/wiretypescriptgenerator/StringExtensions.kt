package com.codellyrandom.wiretypescriptgenerator

fun String.toDocumentation(indent: Int): String {
    if (this.isBlank()) { return "" }

    return this.split("\n")
        .map { "${" ".repeat(indent) }// $it"}
        .joinToString("\n")
}

fun String.toKebabCase(): String {
    return this.mapIndexed { index, c ->
        if (index == 0) {
            c.lowercase()
        } else if (c.isUpperCase()) {
            "-${c.lowercase()}"
        } else {
            c
        }
    }.joinToString("")
}

fun String.trimEmptyLines(): String {
    val lines = this.split("\n").toMutableList()
    while (lines.firstOrNull()?.isBlank() == true) {
        lines.removeFirst()
    }
    while (lines.lastOrNull()?.isBlank() == true) {
        lines.removeLast()
    }
    return lines.joinToString("\n")
}
