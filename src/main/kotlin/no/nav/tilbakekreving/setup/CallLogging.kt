package no.nav.tilbakekreving.setup

import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.calllogging.CallLogging
import io.ktor.server.request.path
import no.nav.tilbakekreving.AppEnv
import org.slf4j.event.Level

context(appEnv: AppEnv)
fun Application.configureCallLogging() {
    install(CallLogging) {
        filter { call ->
            call.request.path() !in setOf("/internal/isAlive", "/internal/isReady")
        }
        when (appEnv) {
            AppEnv.LOCAL,
            AppEnv.DEV,
            -> {
                level = Level.INFO
            }

            AppEnv.PROD -> {}
        }
    }
}
