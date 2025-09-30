package no.nav.tilbakekreving.infrastructure.route.json

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import no.nav.tilbakekreving.domain.Krav
import no.nav.tilbakekreving.domain.Kravidentifikator
import no.nav.tilbakekreving.domain.Kravoversikt
import no.nav.tilbakekreving.domain.KravoversiktKravgrunnlag
import no.nav.tilbakekreving.domain.KravoversiktSkyldner
import no.nav.tilbakekreving.domain.MultiSpråkTekst
import no.nav.tilbakekreving.domain.Oppdragsgiver
import no.nav.tilbakekreving.domain.SpråkTekst

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
    val kravidentifikator: KravidentifikatorJsonResponse,
    val kravtype: String,
    val kravbeskrivelse: MultiSpråkTekstJsonResponse,
    val kravgrunnlag: KravgrunnlagJsonResponse,
    val gjenståendeBeløp: Double,
) {
    companion object {
        fun from(krav: Krav): KravResponseJson =
            KravResponseJson(
                kravidentifikator = KravidentifikatorJsonResponse.from(krav.kravidentifikator),
                kravtype = krav.kravtype.value,
                kravbeskrivelse = MultiSpråkTekstJsonResponse.from(krav.kravbeskrivelse),
                kravgrunnlag = KravgrunnlagJsonResponse.from(krav.kravgrunnlag),
                gjenståendeBeløp = krav.gjenståendeBeløp,
            )
    }
}

@Serializable
sealed class KravidentifikatorJsonResponse {
    abstract val id: String

    companion object {
        fun from(kravidentifikator: Kravidentifikator): KravidentifikatorJsonResponse =
            when (kravidentifikator) {
                is Kravidentifikator.Nav -> Nav(kravidentifikator.id)

                is Kravidentifikator.Skatteetaten -> Skatteetaten(kravidentifikator.id)
            }
    }

    @Serializable
    @SerialName("nav")
    data class Nav(
        override val id: String,
    ) : KravidentifikatorJsonResponse()

    @Serializable
    @SerialName("skatteetaten")
    data class Skatteetaten(
        override val id: String,
    ) : KravidentifikatorJsonResponse()
}

@Serializable
data class OppdragsgiverJsonResponse(
    val organisasjonsnummer: String,
    val organisasjonsnavn: String,
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

@Serializable
data class MultiSpråkTekstJsonResponse(
    val språkTekst: List<SpråkTekstJsonResponse>,
) {
    companion object {
        fun from(multiSpråkTekst: MultiSpråkTekst): MultiSpråkTekstJsonResponse =
            MultiSpråkTekstJsonResponse(
                språkTekst = multiSpråkTekst.språkTekst.map(SpråkTekstJsonResponse::from),
            )
    }
}

@Serializable
data class SpråkTekstJsonResponse(
    val tekst: String,
    val språk: String,
) {
    companion object {
        fun from(språkTekst: SpråkTekst): SpråkTekstJsonResponse =
            SpråkTekstJsonResponse(
                tekst = språkTekst.tekst,
                språk = språkTekst.språk,
            )
    }
}

@Serializable
data class KravgrunnlagJsonResponse(
    val oppdragsgiversKravidentifikator: String,
    val oppdragsgiversReferanse: String?,
) {
    companion object {
        fun from(kravgrunnlag: KravoversiktKravgrunnlag): KravgrunnlagJsonResponse =
            KravgrunnlagJsonResponse(
                oppdragsgiversKravidentifikator = kravgrunnlag.oppdragsgiversKravidentifikator,
                oppdragsgiversReferanse = kravgrunnlag.oppdragsgiversReferanse,
            )
    }
}
