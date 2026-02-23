package no.nav.tilbakekreving.infrastructure.unleash

import io.getunleash.DefaultUnleash
import io.getunleash.util.UnleashConfig
import no.nav.tilbakekreving.app.FeatureToggles
import no.nav.tilbakekreving.app.Toggle

class UnleashFeatureToggles(
    unleashServerApiUrl: String,
    unleashServerApiToken: String,
    appName: String = "tilbakekreving",
) : FeatureToggles {
    private val unleash =
        DefaultUnleash(
            UnleashConfig
                .builder()
                .appName(appName)
                .instanceId(appName)
                .unleashAPI("$unleashServerApiUrl/api")
                .apiKey(unleashServerApiToken)
                .build(),
        )

    override fun isEnabled(toggle: Toggle): Boolean = unleash.isEnabled(toggle.toggleName)
}
