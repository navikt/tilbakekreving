package no.nav.tilbakekreving.domain

data class Kravlinje(
    val kravlinjetype: String,
    val opprinneligBeløp: Double,
    val gjenståendeBeløp: Double,
    val kravlinjeBeskrivelse: MultiSpråkTekst? = null,
)
