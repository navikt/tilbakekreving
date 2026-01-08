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
data class HentKravoversiktResponseJson(
    val oppdragsgiver: OppdragsgiverJson,
    val krav: List<KravJson>? = null,
    @SerialName("gjenstaaendeBeloepForSkyldner") val gjenståendeBeløpForSkyldner: Double,
    val skyldner: SkyldnerJson,
) {
    fun toDomain(): Kravoversikt =
        Kravoversikt(
            oppdragsgiver = oppdragsgiver.toDomain(),
            krav = krav?.map(KravJson::toDomain) ?: emptyList(),
            gjenståendeBeløpForSkyldner = gjenståendeBeløpForSkyldner,
            skyldner = skyldner.toDomain(),
        )
}

@Serializable
data class KravJson(
    val skatteetatensKravidentifikator: String?,
    val kravtype: String,
    val kravbeskrivelse: MultiSpråkTekstJson,
    val kravgrunnlag: KravgrunnlagJson,
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
data class KravgrunnlagJson(
    val oppdragsgiversKravidentifikator: String,
    val oppdragsgiversReferanse: String? = null,
)

@Serializable
data class OppdragsgiverJson(
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
data class SkyldnerJson(
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
data class MultiSpråkTekstJson(
    @SerialName("spraakTekst") val språkTekst: List<SpråkTekstJson>,
) {
    fun toDomain(): List<Kravbeskrivelse> = språkTekst.map(SpråkTekstJson::toDomain)
}

@Serializable
data class SpråkTekstJson(
    val tekst: String,
    @SerialName("spraak") val språk: String,
) {
    fun toDomain(): Kravbeskrivelse = Kravbeskrivelse(Locale.forLanguageTag(språk), tekst)
}
