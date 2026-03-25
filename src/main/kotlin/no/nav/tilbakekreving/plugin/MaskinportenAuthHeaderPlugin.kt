package no.nav.tilbakekreving.plugin

import arrow.core.getOrElse
import io.ktor.client.plugins.api.createClientPlugin
import io.ktor.client.request.bearerAuth
import no.nav.tilbakekreving.infrastructure.auth.AccessTokenProvider
import no.nav.tilbakekreving.infrastructure.auth.model.MaskinportenToken
import no.nav.tilbakekreving.infrastructure.auth.model.Scope

data class MaskinportenAuthHeaderPluginConfig(
    var accessTokenProvider: AccessTokenProvider<MaskinportenToken>? = null,
    var scopes: List<String> = emptyList(),
)

val MaskinportenAuthHeaderPlugin =
    createClientPlugin("MaskinportenAuthHeaderPlugin", ::MaskinportenAuthHeaderPluginConfig) {
        val accessTokenProvider =
            pluginConfig.accessTokenProvider ?: throw IllegalStateException("Access token provider is not set")
        val scopes = pluginConfig.scopes

        onRequest { request, _ ->
            val maskinportenToken =
                accessTokenProvider.getAccessToken(scopes.map { Scope(it) }.toSet()).getOrElse {
                    throw IllegalStateException("Failed to get access token: $it")
                }

            request.bearerAuth(maskinportenToken.token)
        }
    }
