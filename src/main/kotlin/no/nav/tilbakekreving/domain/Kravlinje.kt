package no.nav.tilbakekreving.domain

import java.util.Locale

data class Kravlinje(
    val kravlinjetype: String,
    val opprinneligBeløp: Double,
    val gjenståendeBeløp: Double,
    val kravlinjeBeskrivelse: Map<Locale, Kravlinjebeskrivelse>,
)

@JvmInline
value class Kravlinjebeskrivelse(
    val value: String,
)
