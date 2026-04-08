package no.nav.tilbakekreving.setup

import arrow.core.getOrElse
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.bearer
import io.ktor.server.plugins.di.dependencies
import no.nav.tilbakekreving.AppEnv
import no.nav.tilbakekreving.config.AuthenticationConfigName
import no.nav.tilbakekreving.config.EntraProxyConfig
import no.nav.tilbakekreving.infrastructure.auth.AccessTokenValidator
import no.nav.tilbakekreving.infrastructure.auth.EntraOboTokenExchanger
import no.nav.tilbakekreving.infrastructure.auth.model.NavUserPrincipal
import no.nav.tilbakekreving.infrastructure.auth.model.ValidatedEntraToken
import no.nav.tilbakekreving.infrastructure.client.entra.proxy.EntraProxyClient
import org.slf4j.LoggerFactory

/**
 * Validerer Entra-token for saksbehandlere i NAV. Henter også inn enhetene saksbehandleren tilhører
 * og tilgjengeligjør [NavUserPrincipal] via [io.ktor.server.auth.principal].
 */
fun Application.configureEntraAuthentication() {
    val authenticationConfigName = AuthenticationConfigName.ENTRA_ID
    val accessTokenValidator: AccessTokenValidator<ValidatedEntraToken> by dependencies
    val entraOboTokenExchanger: EntraOboTokenExchanger by dependencies
    val entraProxyClient: EntraProxyClient by dependencies
    val entraProxyConfig: EntraProxyConfig by dependencies
    val appEnv: AppEnv by dependencies

    install(Authentication) {
        val logger = LoggerFactory.getLogger("ConfigureEntraAuthentication")
        bearer(authenticationConfigName.configName) {
            authenticate { credentials ->
                val validatedToken =
                    accessTokenValidator.validateToken(credentials.token).getOrElse { error ->
                        when (error) {
                            is AccessTokenValidator.ValidationError.FailedToValidateToken -> {
                                logger.warn("Token verification failed: $error")
                                return@authenticate null
                            }

                            is AccessTokenValidator.ValidationError.InvalidToken -> {
                                logger.warn("Token is invalid: $error")
                                return@authenticate null
                            }
                        }
                    }

                val oboToken =
                    entraOboTokenExchanger.exchange(credentials.token, entraProxyConfig.apiTarget).getOrElse { error ->
                        logger.warn("Failed to exchange user token for OBO token: $error")
                        return@authenticate null
                    }

                val enheter =
                    entraProxyClient.hentEnheter(oboToken).getOrElse { error ->
                        logger.warn("Failed to hent enheter for user: $error")
                        return@authenticate null
                    }

                NavUserPrincipal(
                    navIdent = validatedToken.navIdent,
                    groupIds = validatedToken.groupIds,
                    enheter = enheter,
                ).also {
                    when (appEnv) {
                        AppEnv.LOCAL,
                        AppEnv.DEV,
                        -> {
                            logger.info("Nav user principal: {}", it)
                        }

                        AppEnv.PROD -> {}
                    }
                }
            }
        }
    }
}
