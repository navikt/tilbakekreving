package no.nav.tilbakekreving.config

import java.net.URL

data class NaisConfig(
    val naisTokenEndpoint: URL,
    val naisTokenIntrospectionEndpoint: URL,
    val naisTokenExchangeEndpoint: URL,
)
