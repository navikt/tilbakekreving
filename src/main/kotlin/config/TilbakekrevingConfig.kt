package no.nav.config

import no.nav.config.SkatteetatenConfig

data class TilbakekrevingConfig(
    val nais: NaisConfig,
    val skatteetaten: SkatteetatenConfig,
)