package no.nav.tilbakekreving.infrastructure.unleash

import no.nav.tilbakekreving.app.FeatureToggles
import no.nav.tilbakekreving.app.Toggle

class StubFeatureToggles(
    private val default: Boolean = false,
) : FeatureToggles {
    override fun isEnabled(toggle: Toggle): Boolean = default
}
