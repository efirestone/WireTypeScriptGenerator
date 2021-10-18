val artifactGroup = "com.codellyrandom.wiretypescriptgenerator"
val artifactVersion = "0.1.8"

group = artifactGroup
version = artifactVersion

plugins {
    kotlin("jvm") version "1.5.21"
    id("maven-publish")
    id("java")
    id("signing")
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

java {
    withJavadocJar()
    withSourcesJar()
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

publishing {
    publications {
         create<MavenPublication>("mavenJava") {
            groupId = artifactGroup
            artifactId = "wire-typescript-generator"
            version = artifactVersion

            from(components["java"])
            pom {
                name.set("Wire TypeScript Generator")
                description.set("Generates TypeScript type definitions from protobuf files, using Wire.")
                url.set("https://github.com/efirestone/WireTypeScriptGenerator")
                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }
                developers {
                    developer {
                        id.set("efirestone")
                        name.set("Eric Firestone")
                    }
                }
                scm {
                    url.set("https://github.com/efirestone/WireTypeScriptGenerator")
                    connection.set("scm:git:https://github.com/efirestone/WireTypeScriptGenerator.git")
                    developerConnection.set("scm:git:ssh://git@github.com:efirestone/WireTypeScriptGenerator.git")
                }
            }
        }
    }
    repositories {
        maven {
            // Once published, visit https://s01.oss.sonatype.org/#stagingRepositories to release the artifact.
            val releasesRepoUrl = uri("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2")
            val snapshotsRepoUrl = uri("https://s01.oss.sonatype.org/content/repositories/snapshots")
            url = if (version.toString().endsWith("SNAPSHOT")) snapshotsRepoUrl else releasesRepoUrl
            credentials {
                this.username = properties["nexusUsername"] as String
                this.password = properties["nexusPassword"] as String
            }
        }
    }
}

signing {
    sign(publishing.publications["mavenJava"])
}