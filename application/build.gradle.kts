// Application module — use cases, commands, queries, ports.
// Only allowed production dependency: :domain. Test dependencies (e.g. JUnit, Mockito) are allowed. Must never import Spring, JPA, or infrastructure in main code.
plugins {
    java
}

group = "com.n1b3lung0"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(libs.versions.java.get().toInt())
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":domain"))

    testImplementation(libs.junit.jupiter)
    testImplementation(libs.mockito.junit.jupiter)
    testRuntimeOnly(libs.junit.platform.launcher)
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}

