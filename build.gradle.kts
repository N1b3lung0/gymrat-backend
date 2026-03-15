// Root build file — shared conventions only.
// No source code lives here. Each submodule declares its own plugins and dependencies.
group = "com.n1b3lung0"
version = "0.0.1-SNAPSHOT"
description = "gymrat-backend — API to manage gym workouts, exercises and series"

// ---------------------------------------------------------------------------
// Shared test JVM args — required by Mockito byte-buddy on Java 21+
// ---------------------------------------------------------------------------
subprojects {
    tasks.withType<Test>().configureEach {
        jvmArgs(
            "--add-opens", "java.base/java.lang=ALL-UNNAMED",
            "--add-opens", "java.base/java.util=ALL-UNNAMED",
            "--add-opens", "java.base/java.lang.reflect=ALL-UNNAMED"
        )
    }
}

