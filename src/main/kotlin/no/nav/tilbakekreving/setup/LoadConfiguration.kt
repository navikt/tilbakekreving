package no.nav.tilbakekreving.setup

import com.sksamuel.hoplite.ConfigLoaderBuilder
import com.sksamuel.hoplite.ExperimentalHoplite
import no.nav.tilbakekreving.AppEnv
import no.nav.tilbakekreving.config.TilbakekrevingConfig

/**
 * Laster inn konfigurasjon fra filer under main/resources. Se for eksempel `src/main/resources/application.conf`.
 * Miljøvariabler som er referert i filene og lastes også inn her.
 */
@OptIn(ExperimentalHoplite::class)
context(appEnv: AppEnv)
fun loadConfiguration(): TilbakekrevingConfig {
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
