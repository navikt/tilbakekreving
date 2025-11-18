package no.nav.tilbakekreving.infrastructure.route.json

import kotlinx.serialization.Serializable
import no.nav.tilbakekreving.domain.Krav
import no.nav.tilbakekreving.domain.Kravoversikt
import no.nav.tilbakekreving.domain.KravoversiktSkyldner
import no.nav.tilbakekreving.domain.Oppdragsgiver

@Serializable
data class HentKravoversiktJsonResponse(
    val oppdragsgiver: OppdragsgiverJsonResponse,
    val krav: List<KravResponseJson>,
    val gjenståendeBeløpForSkyldner: Double,
    val skyldner: SkyldnerJsonResponse,
) {
    companion object {
        fun fromDomain(kravoversikt: Kravoversikt): HentKravoversiktJsonResponse =
            HentKravoversiktJsonResponse(
                oppdragsgiver = OppdragsgiverJsonResponse.from(kravoversikt.oppdragsgiver),
                krav = kravoversikt.krav.map(KravResponseJson::from),
                gjenståendeBeløpForSkyldner = kravoversikt.gjenståendeBeløpForSkyldner,
                skyldner = SkyldnerJsonResponse.from(kravoversikt.skyldner),
            )
    }
}

@Serializable
data class KravResponseJson(
    val skeKravidentifikator: String,
    val navKravidentifikator: String,
    val navReferanse: String?,
    val kravtype: String,
    val kravbeskrivelse: Map<String, String>,
    val gjenståendeBeløp: Double,
) {
    companion object {
        fun from(krav: Krav): KravResponseJson =
            KravResponseJson(
                skeKravidentifikator = krav.skeKravidentifikator.id,
                navKravidentifikator = krav.navKravidentifikator.id,
                navReferanse = krav.navReferanse,
                kravtype = krav.kravtype.value,
                kravbeskrivelse =
                    krav.kravbeskrivelse
                        .map { (locale, beskrivelse) -> locale.toLanguageTag() to beskrivelse.value }
                        .toMap(),
                gjenståendeBeløp = krav.gjenståendeBeløp,
            )
    }
}

@Serializable
data class OppdragsgiverJsonResponse(
    val organisasjonsnummer: String,
    val organisasjonsnavn: String?,
) {
    companion object {
        fun from(oppdragsgiver: Oppdragsgiver): OppdragsgiverJsonResponse =
            OppdragsgiverJsonResponse(
                organisasjonsnummer = oppdragsgiver.organisasjonsnummer,
                organisasjonsnavn = oppdragsgiver.organisasjonsnavn,
            )
    }
}

@Serializable
data class SkyldnerJsonResponse(
    val identifikator: String,
    val skyldnersNavn: String?,
) {
    companion object {
        fun from(skyldner: KravoversiktSkyldner): SkyldnerJsonResponse =
            SkyldnerJsonResponse(
                identifikator = skyldner.identifikator,
                skyldnersNavn = skyldner.skyldnersNavn,
            )
    }
}
