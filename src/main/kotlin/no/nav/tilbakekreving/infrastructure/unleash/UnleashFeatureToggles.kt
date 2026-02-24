package no.nav.tilbakekreving.infrastructure.unleash

import io.getunleash.DefaultUnleash
import no.nav.tilbakekreving.app.FeatureToggles
import no.nav.tilbakekreving.app.Toggle
import no.nav.tilbakekreving.config.UnleashConfig
import io.getunleash.util.UnleashConfig as SdkUnleashConfig

class UnleashFeatureToggles(
    config: UnleashConfig,
) : FeatureToggles {
    private val unleash =
        DefaultUnleash(
            SdkUnleashConfig
                .builder()
                .appName(config.appName)
                .instanceId(config.appName)
                .unleashAPI("${config.serverApiUrl}/api")
                .apiKey(config.serverApiToken.value)
                .build(),
        )

    override fun isEnabled(toggle: Toggle): Boolean = unleash.isEnabled(toggle.toggleName)
}
