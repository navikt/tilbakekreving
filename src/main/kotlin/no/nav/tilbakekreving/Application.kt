package no.nav.tilbakekreving

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.HttpTimeout
import io.ktor.server.application.Application
import io.ktor.server.application.log
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.di.dependencies
import no.nav.tilbakekreving.app.FeatureToggles
import no.nav.tilbakekreving.config.AuthenticationConfigName
import no.nav.tilbakekreving.config.NaisConfig
import no.nav.tilbakekreving.config.SkatteetatenConfig
import no.nav.tilbakekreving.config.TilbakekrevingConfig
import no.nav.tilbakekreving.infrastructure.audit.AuditLog
import no.nav.tilbakekreving.infrastructure.audit.NavAuditLog
import no.nav.tilbakekreving.infrastructure.auth.NavUserPrincipal
import no.nav.tilbakekreving.infrastructure.auth.abac.policy.LesKravAccessPolicy
import no.nav.tilbakekreving.infrastructure.auth.abac.policy.lesKravAccessPolicy
import no.nav.tilbakekreving.infrastructure.client.AccessTokenVerifier
import no.nav.tilbakekreving.infrastructure.client.TexasClient
import no.nav.tilbakekreving.infrastructure.client.maskinporten.TexasMaskinportenClient
import no.nav.tilbakekreving.infrastructure.client.skatteetaten.SkatteetatenInnkrevingsoppdragHttpClient
import no.nav.tilbakekreving.infrastructure.unleash.StubFeatureToggles
import no.nav.tilbakekreving.infrastructure.unleash.UnleashFeatureToggles
import no.nav.tilbakekreving.plugin.MaskinportenAuthHeaderPlugin
import no.nav.tilbakekreving.setup.configureAuthentication
import no.nav.tilbakekreving.setup.configureCallLogging
import no.nav.tilbakekreving.setup.configureRouting
import no.nav.tilbakekreving.setup.configureSerialization
import no.nav.tilbakekreving.setup.createHttpClient
import no.nav.tilbakekreving.setup.loadConfiguration
import kotlin.time.Duration.Companion.seconds

fun main() {
    embeddedServer(
        factory = Netty,
        port = 8080,
        host = "0.0.0.0",
        module = Application::module,
    ).start(wait = true)
}

suspend fun Application.module() {
    val appEnv = context(log) { AppEnv.getFromEnvVariable("NAIS_CLUSTER_NAME") }
    log.info("Starting application in $appEnv")

    context(appEnv) {
        dependencies {
            provide { loadConfiguration() }
            provide { resolve<TilbakekrevingConfig>().nais }
            provide { resolve<TilbakekrevingConfig>().skatteetaten }
            provide { resolve<TilbakekrevingConfig>().unleash }
            provide { resolve<TilbakekrevingConfig>().auditlog }

            provide<HttpClient> { createHttpClient(CIO.create()) }

            provide<FeatureToggles> {
                when (appEnv) {
                    AppEnv.LOCAL -> StubFeatureToggles()
                    AppEnv.DEV, AppEnv.PROD -> UnleashFeatureToggles(config = resolve())
                }
            }

            provide<AuditLog>(NavAuditLog::class)

            provide<AccessTokenVerifier<NavUserPrincipal>> {
                TexasClient(resolve(), resolve<NaisConfig>().naisTokenIntrospectionEndpoint)
            }

            provide<SkatteetatenInnkrevingsoppdragHttpClient> {
                val skatteetatenConfig = resolve<SkatteetatenConfig>()
                val maskinportenClient =
                    TexasMaskinportenClient(
                        resolve(),
                        resolve<NaisConfig>().naisTokenEndpoint,
                    )
                val skatteetatenClient =
                    resolve<HttpClient>().config {
                        install(MaskinportenAuthHeaderPlugin) {
                            accessTokenProvider = maskinportenClient
                            scopes = skatteetatenConfig.scopes
                        }
                        install(HttpTimeout) {
                            requestTimeoutMillis = 30.seconds.inWholeMilliseconds
                        }
                    }
                SkatteetatenInnkrevingsoppdragHttpClient(skatteetatenConfig.baseUrl, skatteetatenClient)
            }

            provide<LesKravAccessPolicy> {
                context(resolve<FeatureToggles>()) {
                    val config = resolve<TilbakekrevingConfig>()
                    lesKravAccessPolicy(config.kravTilgangsgruppe, config.kravAcl)
                }
            }
        }

        configureSerialization()
        configureCallLogging()
        configureAuthentication(
            AuthenticationConfigName.ENTRA_ID,
            dependencies.resolve<AccessTokenVerifier<NavUserPrincipal>>(),
        )
        configureRouting()
    }
}
