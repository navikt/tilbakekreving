package no.nav.tilbakekreving.setup

import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.calllogging.CallLogging
import no.nav.tilbakekreving.AppEnv
import org.slf4j.event.Level

context(appEnv: AppEnv)
fun Application.configureCallLogging() {
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
