package com.codellyrandom

fun String.toDocumentation(indent: Int): String {
    if (this.isBlank()) { return "" }

    return this.split("\n")
        .map { "${" ".repeat(indent) }// $it"}
        .joinToString("\n") + "\n"
}
