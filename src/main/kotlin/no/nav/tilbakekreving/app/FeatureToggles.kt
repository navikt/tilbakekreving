package no.nav.tilbakekreving.app

enum class Toggle(
    val toggleName: String,
) {
    KRAVTYPE_ENHET_TILGANGSKONTROLL("tilbakekreving.kravtype-enhet-tilgangskontroll"),
}

interface FeatureToggles {
    fun isEnabled(toggle: Toggle): Boolean
}
