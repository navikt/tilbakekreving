package no.nav.tilbakekreving.config

import java.net.URL

data class EntraProxyConfig(
    val baseUrl: URL,
    val apiTarget: String,
)
