// Infrastructure module — Spring Boot wiring, JPA adapters, REST controllers, config.
// Depends on :application and :domain. All Spring/JPA annotations live here.
plugins {
    java
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.spring.dependency.mgmt)
}

group = "com.n1b3lung0"
version = "0.0.1-SNAPSHOT"

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
    implementation(project(":application"))
    implementation(project(":domain"))

    implementation(libs.bundles.spring.web)
    implementation(libs.spring.boot.jpa)
    implementation(libs.springdoc.openapi)
    implementation(libs.bundles.flyway)

    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok)

    developmentOnly(libs.spring.boot.devtools)
    developmentOnly(libs.spring.boot.docker.compose)

    runtimeOnly(libs.postgresql)

    testImplementation(libs.spring.boot.test)
    testImplementation(libs.spring.boot.testcontainers)
    testImplementation(libs.bundles.testcontainers)
    testImplementation(libs.archunit)
    testRuntimeOnly(libs.junit.platform.launcher)
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}

tasks.named<org.springframework.boot.gradle.tasks.run.BootRun>("bootRun") {
    // Run from the project root so Spring Boot Docker Compose finds compose.yaml.
    // IntelliJ also uses the project root as working dir by default.
    workingDir = rootProject.projectDir
}

