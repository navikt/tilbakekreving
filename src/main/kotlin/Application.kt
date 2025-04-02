package no.nav

import io.ktor.client.engine.cio.CIO
import io.ktor.server.application.Application
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.routing.route
import no.nav.infrastructure.client.maskinporten.TexasMaskinportenClient
import no.nav.infrastructure.client.skatteetaten.SkatteetatenInnkrevingsoppdragHttpClient
import no.nav.infrastructure.route.configureRouting
import no.nav.infrastructure.route.hentKravdetaljer
import no.nav.plugin.MaskinportenAuthHeaderPlugin
import no.nav.setup.configureSerialization
import no.nav.setup.createHttpClient
import no.nav.setup.loadConfiguration

fun main() {
    embeddedServer(
        factory = Netty,
        port = 8080,
        host = "0.0.0.0",
        module = Application::module,
    ).start(wait = true)
}

fun Application.module() {
    val appEnv = AppEnv.getFromEnvVariable("NAIS_CLUSTER_NAME")
    val tilbakekrevingConfig = loadConfiguration(appEnv)
    val maskinportenClient = createHttpClient(CIO.create())
    val texasClient = TexasMaskinportenClient(maskinportenClient, tilbakekrevingConfig.nais.naisTokenEndpoint)
    val skatteetatenClient =
        createHttpClient(CIO.create()) {
            install(MaskinportenAuthHeaderPlugin) {
                accessTokenProvider = texasClient
            }
        }
    val skatteetatenInnkrevingsoppdragHttpClient =
        SkatteetatenInnkrevingsoppdragHttpClient(tilbakekrevingConfig.skatteetaten.baseUrl, skatteetatenClient)

    configureSerialization()
    configureRouting {
        route("/internal") {
            route("/kravdetaljer") {
                hentKravdetaljer(skatteetatenInnkrevingsoppdragHttpClient)
            }
        }
    }
}
