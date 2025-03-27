package no.nav

import com.sksamuel.hoplite.ConfigLoaderBuilder
import com.sksamuel.hoplite.ExperimentalHoplite

/**
 * Laster inn konfigurasjon fra filer under main/resources. Se for eksempel `src/main/resources/application.conf`.
 * Miljøvariabler som er referert i filene og lastes også inn her.
 */
@OptIn(ExperimentalHoplite::class)
fun loadConfiguration(): TilbakekrevingConfig {
    val resourceFiles =
        listOfNotNull(
            "/application.conf",
        )
    return ConfigLoaderBuilder
        .default()
        .withExplicitSealedTypes()
        .build()
        .loadConfigOrThrow<TilbakekrevingConfig>(resourceFiles)
}

data class TilbakekrevingConfig(
    val nais: NaisConfig,
)

data class NaisConfig(
    val naisTokenEndpoint: String,
)
