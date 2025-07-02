package no.nav.tilbakekreving

import arrow.core.getOrElse
import io.ktor.client.engine.cio.CIO
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.application.log
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.bearer
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import no.nav.tilbakekreving.infrastructure.client.AccessTokenVerifier
import no.nav.tilbakekreving.infrastructure.client.TexasClient
import no.nav.tilbakekreving.infrastructure.client.maskinporten.TexasMaskinportenClient
import no.nav.tilbakekreving.infrastructure.client.skatteetaten.SkatteetatenInnkrevingsoppdragHttpClient
import no.nav.tilbakekreving.infrastructure.route.hentKravdetaljerRoute
import no.nav.tilbakekreving.infrastructure.route.hentKravoversikt
import no.nav.tilbakekreving.plugin.MaskinportenAuthHeaderPlugin
import no.nav.tilbakekreving.setup.configureCallLogging
import no.nav.tilbakekreving.setup.configureSerialization
import no.nav.tilbakekreving.setup.createHttpClient
import no.nav.tilbakekreving.setup.loadConfiguration
import org.slf4j.LoggerFactory

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
    val httpClient = createHttpClient(CIO.create(), appEnv)

    val maskinportenAccessTokenProvider =
        TexasMaskinportenClient(httpClient, tilbakekrevingConfig.nais.naisTokenEndpoint)
    val skatteetatenClient =
        createHttpClient(CIO.create(), appEnv) {
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
    configureSerialization()
    configureCallLogging(appEnv)
    install(Authentication) {
        val logger = LoggerFactory.getLogger("Authentication")
        bearer("entra-id") {
            authenticate { credentials ->
                accessTokenVerifier
                    .verifyToken(credentials.token)
                    .getOrElse { error ->
                        when (error) {
                            is AccessTokenVerifier.VerificationError.FailedToVerifyToken -> {
                                logger.error("Token verification failed: $error")
                                null
                            }

                            is AccessTokenVerifier.VerificationError.InvalidToken -> {
                                logger.error("Token is invalid: $error")
                                null
                            }
                        }
                    }?.groupIds
            }
        }
    }

    routing {
        route("/internal") {
            authenticate("entra-id") {
                route("/kravdetaljer") {
                    hentKravdetaljerRoute(innkrevingsoppdragHttpClient)
                }
                route("/kravoversikt") {
                    hentKravoversikt(innkrevingsoppdragHttpClient)
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
