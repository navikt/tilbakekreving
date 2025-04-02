package no.nav.util

import io.kotest.core.spec.DslDrivenSpec
import io.ktor.server.testing.TestApplication
import io.ktor.server.testing.TestApplicationBuilder

fun DslDrivenSpec.specWideTestApplication(testApplicationBuilder: TestApplicationBuilder.() -> Unit): TestApplication =
    TestApplication(testApplicationBuilder).also { testApplication ->
        afterSpec { testApplication.stop() }
    }
