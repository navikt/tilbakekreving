package no.nav.tilbakekreving

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.HttpTimeout
import io.ktor.server.application.Application
import io.ktor.server.application.log
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.di.dependencies
import no.nav.tilbakekreving.app.FeatureToggle
import no.nav.tilbakekreving.config.EntraProxyConfig
import no.nav.tilbakekreving.config.SkatteetatenConfig
import no.nav.tilbakekreving.config.TilbakekrevingConfig
import no.nav.tilbakekreving.infrastructure.audit.AuditLog
import no.nav.tilbakekreving.infrastructure.audit.NavAuditLog
import no.nav.tilbakekreving.infrastructure.auth.AccessTokenProvider
import no.nav.tilbakekreving.infrastructure.auth.AccessTokenValidator
import no.nav.tilbakekreving.infrastructure.auth.EntraOboTokenExchanger
import no.nav.tilbakekreving.infrastructure.auth.EntraTokenValidator
import no.nav.tilbakekreving.infrastructure.auth.MaskinportenTokenProvider
import no.nav.tilbakekreving.infrastructure.auth.abac.policy.LesKravAccessPolicy
import no.nav.tilbakekreving.infrastructure.auth.abac.policy.lesKravAccessPolicy
import no.nav.tilbakekreving.infrastructure.auth.model.MaskinportenToken
import no.nav.tilbakekreving.infrastructure.auth.model.ValidatedEntraToken
import no.nav.tilbakekreving.infrastructure.client.entra.proxy.EntraProxyClient
import no.nav.tilbakekreving.infrastructure.client.skatteetaten.SkatteetatenInnkrevingsoppdragHttpClient
import no.nav.tilbakekreving.infrastructure.client.texas.TexasClient
import no.nav.tilbakekreving.infrastructure.unleash.StubFeatureToggle
import no.nav.tilbakekreving.infrastructure.unleash.UnleashFeatureToggle
import no.nav.tilbakekreving.plugin.MaskinportenAuthHeaderPlugin
import no.nav.tilbakekreving.setup.configureCallLogging
import no.nav.tilbakekreving.setup.configureEntraAuthentication
import no.nav.tilbakekreving.setup.configureRouting
import no.nav.tilbakekreving.setup.configureSerialization
import no.nav.tilbakekreving.setup.createHttpClient
import no.nav.tilbakekreving.setup.loadConfiguration
import org.slf4j.LoggerFactory
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
            provide { resolve<TilbakekrevingConfig>().entraProxy }

            provide<HttpClient> { createHttpClient(CIO.create()) }

            provide<FeatureToggle> {
                when (appEnv) {
                    AppEnv.LOCAL -> StubFeatureToggle()
                    AppEnv.DEV, AppEnv.PROD -> UnleashFeatureToggle(config = resolve())
                }
            }

            provide<AuditLog>(NavAuditLog::class)

            provide<TexasClient> {
                TexasClient(resolve(), resolve())
            }

            provide<AccessTokenValidator<ValidatedEntraToken>> {
                EntraTokenValidator(resolve())
            }

            provide<EntraOboTokenExchanger> {
                EntraOboTokenExchanger(resolve())
            }

            provide<AccessTokenProvider<MaskinportenToken>> {
                MaskinportenTokenProvider(resolve())
            }

            provide<SkatteetatenInnkrevingsoppdragHttpClient> {
                val skatteetatenConfig = resolve<SkatteetatenConfig>()
                val maskinportenTokenProvider = resolve<AccessTokenProvider<MaskinportenToken>>()
                val skatteetatenClient =
                    resolve<HttpClient>().config {
                        install(MaskinportenAuthHeaderPlugin) {
                            accessTokenProvider = maskinportenTokenProvider
                            scopes = skatteetatenConfig.scopes
                        }
                        install(HttpTimeout) {
                            requestTimeoutMillis = 30.seconds.inWholeMilliseconds
                        }
                    }
                SkatteetatenInnkrevingsoppdragHttpClient(skatteetatenConfig.baseUrl, skatteetatenClient)
            }

            provide<EntraProxyClient> {
                EntraProxyClient(resolve(), resolve<EntraProxyConfig>().baseUrl)
            }

            provide<LesKravAccessPolicy> {
                context(resolve<FeatureToggle>(), LoggerFactory.getLogger(LesKravAccessPolicy::class.java)) {
                    val config = resolve<TilbakekrevingConfig>()
                    lesKravAccessPolicy(config.kravTilgangsgruppe, config.kravtypeAcl)
                }
            }
        }

        configureSerialization()
        configureCallLogging()
        configureEntraAuthentication()
        configureRouting()
    }
}
