package no.nav.tilbakekreving.setup

import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.calllogging.CallLogging
import no.nav.tilbakekreving.AppEnv
import org.slf4j.event.Level

fun Application.configureCallLogging(appEnv: AppEnv) {
    install(CallLogging) {
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
