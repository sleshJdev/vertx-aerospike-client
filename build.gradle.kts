plugins {
    `java-library`
    `maven-publish`
    signing
}

group = "dev.slesh"
version = "1.0.0"
extra["isReleaseVersion"] = !version.toString().endsWith("-SNAPSHOT")

repositories {
    mavenCentral()
}

dependencies {
    compileOnly("io.vertx:vertx-core:4.3.8")
    compileOnly("com.aerospike:aerospike-client:6.0.0")
    testImplementation("io.vertx:vertx-junit5:4.3.8")
    testImplementation("org.testcontainers:testcontainers:1.16.3")
    testImplementation("org.testcontainers:junit-jupiter:1.16.3")
    testImplementation("com.playtika.testcontainers:embedded-aerospike:2.1.4") {
        exclude(group = "com.google.code.gson", module = "gson")
    }
    testImplementation("com.aerospike:aerospike-client:6.0.0")
    testImplementation("org.junit.jupiter:junit-jupiter:5.8.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.1")
}

publishing {
    publications {
        create<MavenPublication>("nexus") {
            from(components["java"])
            versionMapping {
                usage("java-api") {
                    fromResolutionOf("runtimeClasspath")
                }
                usage("java-runtime") {
                    fromResolutionResult()
                }
            }
            pom {
                name.set("Vert.x Aerospike Client")
                description.set("""
                    Provides future based implementation of async aerospike client.
                    Extends the aerospike's event loops to be aware about vert.x context event loop.
                """.trimIndent())
                url.set("https://github.com/sleshJdev/vertx-aerospike-client")
                licenses {
                    license {
                        name.set("MIT license")
                        url.set("https://www.mit.edu/~amini/LICENSE.md")
                    }
                }
                developers {
                    developer {
                        id.set("slesh")
                        name.set("Yauheni Freeman")
                        email.set("slesh.eugene93@gmail.com")
                    }
                }
                scm {
                    connection.set("scm:git:git@github.com:sleshJdev/vertx-aerospike-client.git")
                    developerConnection.set("scm:git:git@github.com:sleshJdev/vertx-aerospike-client.git")
                    url.set("https://github.com/sleshJdev/vertx-aerospike-client/tree/main")
                    tag.set("HEAD")
                }
            }
        }
    }

    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/sleshJdev/vertx-aerospike-client")
            credentials {
                val githubActor: String by project
                val githubToken: String by project
                username = githubActor
                password = githubToken
            }
        }

        maven {
            name = "OSSRH"
            url = if (project.extra["isReleaseVersion"] as Boolean) {
                uri("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
            } else {
                uri("https://s01.oss.sonatype.org/content/repositories/snapshots/")
            }
            credentials {
                val ossrhUsername: String by project
                val ossrhPassword: String by project
                username = ossrhUsername
                password = ossrhPassword
            }
        }
    }
}

java {
    withJavadocJar()
    withSourcesJar()
}

signing {
    val signingKey: String? by project
    val signingPassword: String? by project
    useInMemoryPgpKeys(signingKey, signingPassword)
    sign(publishing.publications["nexus"])
}

tasks {
    getByName<Test>("test") {
        useJUnitPlatform()
    }

    javadoc {
        if (JavaVersion.current().isJava9Compatible) {
            (options as StandardJavadocDocletOptions).addBooleanOption("html5", true)
        }
    }
}
