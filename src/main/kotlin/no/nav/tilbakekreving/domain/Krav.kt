package no.nav.tilbakekreving.domain

import java.util.Locale

data class Kravoversikt(
    val oppdragsgiver: Oppdragsgiver,
    val krav: List<Krav>,
    val gjenståendeBeløpForSkyldner: Double,
    val skyldner: KravoversiktSkyldner,
)

data class Krav(
    val skeKravidentifikator: Kravidentifikator.Skatteetaten,
    val navKravidentifikator: Kravidentifikator.Nav,
    val navReferanse: String?,
    val kravtype: Kravtype,
    val kravbeskrivelse: List<Kravbeskrivelse>,
    val gjenståendeBeløp: Double,
)

data class Oppdragsgiver(
    val organisasjonsnummer: String,
    val organisasjonsnavn: String?,
)

data class KravoversiktSkyldner(
    val identifikator: String,
    val skyldnersNavn: String?,
)

data class Kravbeskrivelse(
    val locale: Locale,
    val beskrivelse: String,
)

@JvmInline
value class Kravtype(
    val value: String,
)
