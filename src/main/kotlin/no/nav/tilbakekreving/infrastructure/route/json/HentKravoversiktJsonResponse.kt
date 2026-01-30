package no.nav.tilbakekreving.infrastructure.route.json

import kotlinx.serialization.Serializable
import no.nav.tilbakekreving.domain.Krav
import no.nav.tilbakekreving.domain.Kravbeskrivelse
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
data class KravbeskrivelseJson(
    val språk: String,
    val tekst: String,
) {
    companion object {
        fun fromDomain(kravbeskrivelse: Kravbeskrivelse): KravbeskrivelseJson =
            KravbeskrivelseJson(
                språk = kravbeskrivelse.locale.toLanguageTag(),
                tekst = kravbeskrivelse.beskrivelse,
            )
    }
}

@Serializable
data class KravResponseJson(
    val skeKravidentifikator: String?,
    val navKravidentifikator: String,
    val navReferanse: String?,
    val kravtype: String,
    val kravbeskrivelse: Array<KravbeskrivelseJson>,
    val `gjenståendeBeløp`: Double,
) {
    companion object {
        fun from(krav: Krav): KravResponseJson =
            KravResponseJson(
                skeKravidentifikator = krav.skeKravidentifikator?.id,
                navKravidentifikator = krav.navKravidentifikator.id,
                navReferanse = krav.navReferanse,
                kravtype = krav.kravtype.value,
                kravbeskrivelse = krav.kravbeskrivelse.map(KravbeskrivelseJson::fromDomain).toTypedArray(),
                `gjenståendeBeløp` = krav.gjenståendeBeløp,
            )
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as KravResponseJson

        if (`gjenståendeBeløp` != other.`gjenståendeBeløp`) return false
        if (skeKravidentifikator != other.skeKravidentifikator) return false
        if (navKravidentifikator != other.navKravidentifikator) return false
        if (navReferanse != other.navReferanse) return false
        if (kravtype != other.kravtype) return false
        if (!kravbeskrivelse.contentEquals(other.kravbeskrivelse)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = `gjenståendeBeløp`.hashCode()
        result = 31 * result + (skeKravidentifikator?.hashCode() ?: 0)
        result = 31 * result + navKravidentifikator.hashCode()
        result = 31 * result + (navReferanse?.hashCode() ?: 0)
        result = 31 * result + kravtype.hashCode()
        result = 31 * result + kravbeskrivelse.contentHashCode()
        return result
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
