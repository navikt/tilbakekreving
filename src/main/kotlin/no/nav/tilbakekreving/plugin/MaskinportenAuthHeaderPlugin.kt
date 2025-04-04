package no.nav.tilbakekreving.plugin

import arrow.core.getOrElse
import io.ktor.client.plugins.api.createClientPlugin
import io.ktor.client.request.bearerAuth
import no.nav.tilbakekreving.infrastructure.client.AccessTokenProvider

data class MaskinportenAuthHeaderPluginConfig(
    var accessTokenProvider: AccessTokenProvider? = null,
)

val MaskinportenAuthHeaderPlugin =
    createClientPlugin("MaskinportenAuthHeaderPlugin", ::MaskinportenAuthHeaderPluginConfig) {
        val accessTokenProvider =
            pluginConfig.accessTokenProvider ?: throw IllegalStateException("Access token provider is not set")

        onRequest { request, _ ->
            val accessToken =
                accessTokenProvider.getAccessToken().getOrElse {
                    throw IllegalStateException("Failed to get access token: $it")
                }

            request.bearerAuth(accessToken)
        }
    }
