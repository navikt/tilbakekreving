package no.nav.tilbakekreving.domain

data class Kravoversikt(
    val oppdragsgiver: Oppdragsgiver,
    val krav: List<Krav>,
    val gjenståendeBeløpForSkyldner: Double,
    val skyldner: KravoversiktSkyldner,
)

data class Krav(
    val kravidentifikator: Kravidentifikator,
    val kravtype: Kravtype,
    val kravbeskrivelse: MultiSpråkTekst,
    val kravgrunnlag: KravoversiktKravgrunnlag,
    val gjenståendeBeløp: Double,
)

data class Oppdragsgiver(
    val organisasjonsnummer: String,
    val organisasjonsnavn: String,
)

data class KravoversiktSkyldner(
    val identifikator: String,
    val skyldnersNavn: String?,
)

data class MultiSpråkTekst(
    val språkTekst: List<SpråkTekst>,
)

data class SpråkTekst(
    val tekst: String,
    val språk: String,
)

data class KravoversiktKravgrunnlag(
    val oppdragsgiversKravidentifikator: String,
    val oppdragsgiversReferanse: String?,
)

@JvmInline
value class Kravtype(
    val value: String,
)
