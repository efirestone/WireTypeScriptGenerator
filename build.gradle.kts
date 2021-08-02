group = "com.codellyrandom"
version = "0.1.0"

plugins {
    kotlin("jvm") version "1.5.21"
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib")
    implementation("com.squareup.wire:wire-compiler:3.7.0")
    testImplementation(kotlin("test-junit"))
    testImplementation("com.squareup:javapoet:1.13.0")
    testImplementation("io.outfoxx:swiftpoet:1.0.0")
}

repositories {
    mavenCentral()
}
