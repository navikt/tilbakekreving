package no.nav.tilbakekreving.infrastructure.unleash

import no.nav.tilbakekreving.app.FeatureToggle
import no.nav.tilbakekreving.app.Toggle

class StubFeatureToggle(
    private val default: Boolean = false,
) : FeatureToggle {
    override fun isEnabled(toggle: Toggle): Boolean = default
}
