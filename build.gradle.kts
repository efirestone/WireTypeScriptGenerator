val artifactGroup = "com.codellyrandom.wiretypescriptgenerator"
val artifactVersion = "0.3.0"

group = artifactGroup
version = artifactVersion

plugins {
    kotlin("jvm") version "1.7.10"
    id("java")
    id("maven-publish")
    id("signing")
}

dependencies {
    implementation(kotlin("stdlib:1.7.10"))
    implementation("com.squareup.wire:wire-compiler:4.4.1")
    testImplementation(kotlin("test-junit:1.7.10"))
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
                        url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
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