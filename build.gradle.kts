// Single-module build — all layers (domain, application, infrastructure) in one compilation unit.
// Architecture boundaries are enforced by ArchUnit, not by Gradle module separation.
plugins {
    java
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.spring.dependency.mgmt)
    alias(libs.plugins.spotless)
}

group = "com.n1b3lung0"
version = "0.0.1-SNAPSHOT"
description = "gymrat-backend — API to manage gym workouts, exercises and series"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(libs.versions.java.get().toInt())
    }
}

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

repositories {
    mavenCentral()
}

dependencies {
    // Spring Boot starters
    implementation(libs.bundles.spring.web)
    implementation(libs.spring.boot.jpa)
    implementation(libs.springdoc.openapi)
    implementation(libs.bundles.flyway)

    // Observability
    implementation(libs.bundles.observability)

    // Code generation
    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok)

    // Development
    developmentOnly(libs.spring.boot.devtools)
    developmentOnly(libs.spring.boot.docker.compose)

    // Runtime
    runtimeOnly(libs.postgresql)

    // Testing
    testImplementation(libs.spring.boot.test)
    testImplementation(libs.spring.boot.testcontainers)
    testImplementation(libs.bundles.testcontainers)
    testImplementation(libs.archunit)
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.mockito.junit.jupiter)
    testRuntimeOnly(libs.junit.platform.launcher)
}

tasks.named<Test>("test") {
    useJUnitPlatform()
    jvmArgs(
        "--add-opens", "java.base/java.lang=ALL-UNNAMED",
        "--add-opens", "java.base/java.util=ALL-UNNAMED",
        "--add-opens", "java.base/java.lang.reflect=ALL-UNNAMED"
    )
}

tasks.named<org.springframework.boot.gradle.tasks.run.BootRun>("bootRun") {
    workingDir = rootProject.projectDir
}

spotless {
    java {
        googleJavaFormat("1.22.0")
        removeUnusedImports()
        trimTrailingWhitespace()
        endWithNewline()
    }
    kotlinGradle {
        ktlint()
    }
}
