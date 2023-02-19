plugins {
    id("java")
}

repositories {
    mavenCentral()
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}

dependencies {
    compileOnly("io.vertx:vertx-core:4.3.8")
    compileOnly("com.aerospike:aerospike-client:6.0.0")
    testImplementation("io.vertx:vertx-junit5:4.3.8")
    testImplementation("org.testcontainers:testcontainers:1.16.3")
    testImplementation("org.testcontainers:junit-jupiter:1.16.3")
    testImplementation("com.playtika.testcontainers:embedded-aerospike:2.1.4") {
        exclude(group="com.google.code.gson", module = "gson")
    }
    testImplementation("com.aerospike:aerospike-client:6.0.0")
    testImplementation("org.junit.jupiter:junit-jupiter:5.8.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.1")
}