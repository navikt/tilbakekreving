package no.nav.setup

import com.sksamuel.hoplite.ConfigLoaderBuilder
import com.sksamuel.hoplite.ExperimentalHoplite
import no.nav.AppEnv
import no.nav.config.TilbakekrevingConfig

/**
 * Laster inn konfigurasjon fra filer under main/resources. Se for eksempel `src/main/resources/application.conf`.
 * Miljøvariabler som er referert i filene og lastes også inn her.
 */
@OptIn(ExperimentalHoplite::class)
fun loadConfiguration(appEnv: AppEnv): TilbakekrevingConfig {
    val resourceFiles =
        listOfNotNull(
            when (appEnv) {
                AppEnv.DEV -> "/application-dev.conf"
                AppEnv.LOCAL -> "/application-local.conf"
                AppEnv.PROD -> null
            },
            "/application.conf",
        )
    return ConfigLoaderBuilder
        .default()
        .withExplicitSealedTypes()
        .build()
        .loadConfigOrThrow<TilbakekrevingConfig>(resourceFiles)
}
