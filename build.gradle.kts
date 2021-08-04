val artifactGroup = "com.codellyrandom"
val artifactVersion = "0.1.0"

group = artifactGroup
version = artifactVersion

plugins {
    kotlin("jvm") version "1.5.21"
    id("maven-publish")
    id("java")
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib")
    implementation("com.squareup.wire:wire-compiler:3.7.0")
    testImplementation(kotlin("test-junit"))
    testImplementation("com.squareup:javapoet:1.13.0")
    testImplementation("io.outfoxx:swiftpoet:1.0.0")
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = artifactGroup
            artifactId = "wire-typescript-generator"
            version = artifactVersion

            from(components["java"])
        }
    }
}

repositories {
    mavenCentral()
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}
