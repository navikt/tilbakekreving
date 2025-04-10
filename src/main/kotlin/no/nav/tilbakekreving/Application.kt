package no.nav.tilbakekreving

import io.ktor.client.engine.cio.CIO
import io.ktor.server.application.Application
import io.ktor.server.application.log
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.routing.route
import no.nav.tilbakekreving.infrastructure.client.maskinporten.TexasMaskinportenClient
import no.nav.tilbakekreving.infrastructure.client.skatteetaten.SkatteetatenInnkrevingsoppdragHttpClient
import no.nav.tilbakekreving.infrastructure.route.configureRouting
import no.nav.tilbakekreving.infrastructure.route.hentKravdetaljerRoute
import no.nav.tilbakekreving.infrastructure.route.hentKravoversikt
import no.nav.tilbakekreving.plugin.MaskinportenAuthHeaderPlugin
import no.nav.tilbakekreving.setup.configureSerialization
import no.nav.tilbakekreving.setup.createHttpClient
import no.nav.tilbakekreving.setup.loadConfiguration

fun main() {
    embeddedServer(
        factory = Netty,
        port = 8080,
        host = "0.0.0.0",
        module = Application::module,
    ).start(wait = true)
}

fun Application.module() {
    val appEnv = AppEnv.getFromEnvVariable("NAIS_CLUSTER_NAME", log)
    log.info("Starting application in $appEnv")

    val tilbakekrevingConfig = loadConfiguration(appEnv)
    val maskinportenClient = createHttpClient(CIO.create())
    val texasClient = TexasMaskinportenClient(maskinportenClient, tilbakekrevingConfig.nais.naisTokenEndpoint)
    val skatteetatenClient =
        createHttpClient(CIO.create()) {
            install(MaskinportenAuthHeaderPlugin) {
                accessTokenProvider = texasClient
                scopes = tilbakekrevingConfig.skatteetaten.scopes
            }
        }
    val innkrevingsoppdragHttpClient =
        SkatteetatenInnkrevingsoppdragHttpClient(tilbakekrevingConfig.skatteetaten.baseUrl, skatteetatenClient)

    configureSerialization()
    configureRouting {
        route("/internal") {
            route("/kravdetaljer") {
                hentKravdetaljerRoute(innkrevingsoppdragHttpClient)
            }
            route("/kravoversikt") {
                hentKravoversikt(innkrevingsoppdragHttpClient)
            }
        }
    }
}
