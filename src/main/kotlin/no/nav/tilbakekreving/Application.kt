package no.nav.tilbakekreving

import io.ktor.client.engine.cio.CIO
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.log
import io.ktor.server.auth.authenticate
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import no.nav.tilbakekreving.infrastructure.client.TexasClient
import no.nav.tilbakekreving.infrastructure.client.maskinporten.TexasMaskinportenClient
import no.nav.tilbakekreving.infrastructure.client.skatteetaten.SkatteetatenInnkrevingsoppdragHttpClient
import no.nav.tilbakekreving.infrastructure.route.KravAccessControl
import no.nav.tilbakekreving.infrastructure.route.hentKravdetaljerRoute
import no.nav.tilbakekreving.infrastructure.route.hentKravoversikt
import no.nav.tilbakekreving.plugin.MaskinportenAuthHeaderPlugin
import no.nav.tilbakekreving.setup.AuthenticationConfigName
import no.nav.tilbakekreving.setup.configureAuthentication
import no.nav.tilbakekreving.setup.configureCallLogging
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
    val appEnv = context(log) { AppEnv.getFromEnvVariable("NAIS_CLUSTER_NAME") }
    log.info("Starting application in $appEnv")

    context(appEnv) {
        val tilbakekrevingConfig = loadConfiguration()
        val httpClient = createHttpClient(CIO.create())

        val maskinportenAccessTokenProvider =
            TexasMaskinportenClient(httpClient, tilbakekrevingConfig.nais.naisTokenEndpoint)
        val skatteetatenClient =
            createHttpClient(CIO.create()) {
                install(MaskinportenAuthHeaderPlugin) {
                    accessTokenProvider = maskinportenAccessTokenProvider
                    scopes = tilbakekrevingConfig.skatteetaten.scopes
                }
            }
        val innkrevingsoppdragHttpClient =
            SkatteetatenInnkrevingsoppdragHttpClient(
                tilbakekrevingConfig.skatteetaten.baseUrl,
                skatteetatenClient,
            )

        val accessTokenVerifier = TexasClient(httpClient, tilbakekrevingConfig.nais.naisTokenIntrospectionEndpoint)
        val kravAccessControl = KravAccessControl(tilbakekrevingConfig.kravAcl)
        configureSerialization()
        configureCallLogging()

        val authenticationConfigName = AuthenticationConfigName("entra-id")
        configureAuthentication(authenticationConfigName, accessTokenVerifier)

        routing {
            route("/internal") {
                authenticate(authenticationConfigName.name) {
                    context(kravAccessControl) {
                        route("/kravdetaljer") {
                            hentKravdetaljerRoute(innkrevingsoppdragHttpClient)
                        }
                        route("/kravoversikt") {
                            hentKravoversikt(innkrevingsoppdragHttpClient)
                        }
                    }
                }
                get("/isAlive") {
                    call.respond<HttpStatusCode>(HttpStatusCode.OK)
                }
                get("/isReady") {
                    call.respond<HttpStatusCode>(HttpStatusCode.OK)
                }
            }
        }
    }
}
