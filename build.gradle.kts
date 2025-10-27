import org.gradle.api.tasks.testing.logging.TestExceptionFormat

val kotlinVersion = "2.2.20"

plugins {
    kotlin("jvm") version "2.2.21"
    id("io.ktor.plugin") version "3.3.1"
    id("org.jetbrains.kotlin.plugin.serialization") version "2.2.20"
    id("com.github.ben-manes.versions") version "0.53.0"
}

kotlin {
    compilerOptions {
        freeCompilerArgs.add("-Xcontext-parameters")
    }
}

group = "no.nav"
version = "0.0.1"

application {
    mainClass = "no.nav.tilbakekreving.ApplicationKt"

    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

repositories {
    mavenCentral()
}

dependencies {
    val ktorVersion = "3.3.1"
    implementation("io.ktor:ktor-server-core:$ktorVersion")
    implementation("io.ktor:ktor-server-netty:$ktorVersion")
    implementation("io.ktor:ktor-server-config-yaml:$ktorVersion")
    implementation("io.ktor:ktor-server-content-negotiation:$ktorVersion")
    implementation("io.ktor:ktor-server-auth:$ktorVersion")
    implementation("io.ktor:ktor-server-call-logging:$ktorVersion")

    implementation("io.ktor:ktor-client-core:$ktorVersion")
    implementation("io.ktor:ktor-client-cio:$ktorVersion")
    implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")
    implementation("io.ktor:ktor-client-auth:$ktorVersion")
    implementation("io.ktor:ktor-client-logging:$ktorVersion")

    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")

    implementation("ch.qos.logback:logback-classic:1.5.20")

    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.7.1-0.6.x-compat")

    val arrowVersion = "2.1.2"
    implementation("io.arrow-kt:arrow-core:$arrowVersion")
    implementation("io.arrow-kt:arrow-fx-coroutines:$arrowVersion")

    val hopliteVersion = "2.9.0"
    implementation("com.sksamuel.hoplite:hoplite-core:$hopliteVersion")
    implementation("com.sksamuel.hoplite:hoplite-hocon:$hopliteVersion")

    testImplementation("io.ktor:ktor-server-test-host:$ktorVersion")
    testImplementation("io.ktor:ktor-client-mock:$ktorVersion")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:$kotlinVersion")

    val kotestVersion = "6.0.4"
    testImplementation("io.kotest:kotest-runner-junit5:$kotestVersion")
    testImplementation("io.kotest:kotest-assertions-core:$kotestVersion")
    testImplementation("io.kotest:kotest-assertions-json:$kotestVersion")
    testImplementation("io.kotest:kotest-assertions-ktor:$kotestVersion")
    testImplementation("io.kotest:kotest-assertions-arrow:$kotestVersion")

    val mockkVersion = "1.14.6"
    testImplementation("io.mockk:mockk:$mockkVersion")
}

tasks {
    test {
        useJUnitPlatform()
        testLogging {
            events("skipped", "failed")
            exceptionFormat = TestExceptionFormat.FULL
        }
        // Required for testing environment variables
        jvmArgs("--add-opens=java.base/java.util=ALL-UNNAMED")
    }

    dependencyUpdates.configure {
        rejectVersionIf {
            isNonStable(candidate.version) && !isNonStable(currentVersion)
        }
    }
}

fun isNonStable(version: String): Boolean {
    val stableKeyword = listOf("RELEASE", "FINAL", "GA").any { version.uppercase().contains(it) }
    val regex = "^[0-9,.v-]+(-r)?$".toRegex()
    val isStable = stableKeyword || regex.matches(version)
    return isStable.not()
}
