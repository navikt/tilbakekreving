package no.nav.tilbakekreving.infrastructure.client.skatteetaten.json

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import no.nav.tilbakekreving.domain.Krav
import no.nav.tilbakekreving.domain.Kravbeskrivelse
import no.nav.tilbakekreving.domain.Kravidentifikator
import no.nav.tilbakekreving.domain.Kravoversikt
import no.nav.tilbakekreving.domain.KravoversiktSkyldner
import no.nav.tilbakekreving.domain.Kravtype
import no.nav.tilbakekreving.domain.Oppdragsgiver
import java.util.Locale

@Serializable
data class SkeHentKravoversiktResponseJson(
    val oppdragsgiver: SkeOppdragsgiverJson,
    val krav: List<SkeKravJson>? = null,
    @SerialName("gjenstaaendeBeloepForSkyldner") val gjenståendeBeløpForSkyldner: Double,
    val skyldner: SkeSkyldnerJson,
) {
    fun toDomain(): Kravoversikt =
        Kravoversikt(
            oppdragsgiver = oppdragsgiver.toDomain(),
            krav = krav?.map(SkeKravJson::toDomain) ?: emptyList(),
            gjenståendeBeløpForSkyldner = gjenståendeBeløpForSkyldner,
            skyldner = skyldner.toDomain(),
        )
}

@Serializable
data class SkeKravJson(
    val skatteetatensKravidentifikator: String?,
    val kravtype: String,
    val kravbeskrivelse: SkeMultiSpråkTekstJson,
    val kravgrunnlag: SkeKravgrunnlagJson,
    @SerialName("gjenstaaendeBeloep") val gjenståendeBeløp: Double,
) {
    fun toDomain(): Krav =
        Krav(
            skeKravidentifikator = skatteetatensKravidentifikator?.let { Kravidentifikator.Skatteetaten(it) },
            navKravidentifikator = Kravidentifikator.Nav(kravgrunnlag.oppdragsgiversKravidentifikator),
            navReferanse = kravgrunnlag.oppdragsgiversReferanse,
            kravtype = Kravtype(kravtype),
            kravbeskrivelse = kravbeskrivelse.toDomain(),
            gjenståendeBeløp = gjenståendeBeløp,
        )
}

@Serializable
data class SkeKravgrunnlagJson(
    val oppdragsgiversKravidentifikator: String,
    val oppdragsgiversReferanse: String? = null,
)

@Serializable
data class SkeOppdragsgiverJson(
    val organisasjonsnummer: String,
    val organisasjonsnavn: String?,
) {
    fun toDomain(): Oppdragsgiver =
        Oppdragsgiver(
            organisasjonsnummer = organisasjonsnummer,
            organisasjonsnavn = organisasjonsnavn,
        )
}

@Serializable
data class SkeSkyldnerJson(
    val identifikator: String,
    val skyldnersNavn: String? = null,
) {
    fun toDomain(): KravoversiktSkyldner =
        KravoversiktSkyldner(
            identifikator = identifikator,
            skyldnersNavn = skyldnersNavn,
        )
}

@Serializable
data class SkeMultiSpråkTekstJson(
    @SerialName("spraakTekst") val språkTekst: List<SkeSpråkTekstJson>,
) {
    fun toDomain(): List<Kravbeskrivelse> = språkTekst.map(SkeSpråkTekstJson::toDomain)
}

@Serializable
data class SkeSpråkTekstJson(
    val tekst: String,
    @SerialName("spraak") val språk: String,
) {
    fun toDomain(): Kravbeskrivelse = Kravbeskrivelse(Locale.forLanguageTag(språk), tekst)
}
