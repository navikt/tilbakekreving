package no.nav.tilbakekreving.config

import com.sksamuel.hoplite.Masked

data class UnleashConfig(
    val serverApiUrl: String,
    val serverApiToken: Masked,
)
