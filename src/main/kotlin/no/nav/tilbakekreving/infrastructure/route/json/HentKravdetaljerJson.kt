package no.nav.tilbakekreving.infrastructure.route.json

import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable
import no.nav.tilbakekreving.domain.Kravdetaljer
import no.nav.tilbakekreving.domain.Kravgrunnlag
import no.nav.tilbakekreving.domain.Kravidentifikator
import no.nav.tilbakekreving.domain.Kravlinje

@Serializable
data class HentKravdetaljerJsonResponse(
    val kravgrunnlag: KravgrunnlagResponseJson,
    val kravlinjer: List<KravlinjeResponseJson>,
    val tilleggsfrist: String? = null,
) {
    companion object {
        fun fromDomain(kravdetaljer: Kravdetaljer): HentKravdetaljerJsonResponse =
            HentKravdetaljerJsonResponse(
                kravgrunnlag = KravgrunnlagResponseJson.fromDomain(kravdetaljer.kravgrunnlag),
                kravlinjer = kravdetaljer.kravlinjer.map(KravlinjeResponseJson.Companion::fromDomain),
                tilleggsfrist = kravdetaljer.tilleggsfrist?.toString(),
            )
    }
}

@Serializable
data class KravgrunnlagResponseJson(
    val datoNårKravVarBesluttetHosOppdragsgiver: LocalDate,
) {
    companion object {
        fun fromDomain(kravgrunnlag: Kravgrunnlag): KravgrunnlagResponseJson =
            KravgrunnlagResponseJson(
                datoNårKravVarBesluttetHosOppdragsgiver = kravgrunnlag.datoNaarKravVarBesluttetHosOppdragsgiver,
            )
    }
}

@Serializable
data class KravlinjeResponseJson(
    val kravlinjetype: String,
    val opprinneligBeløp: Double,
    val gjenståendeBeløp: Double?,
) {
    companion object {
        fun fromDomain(kravlinje: Kravlinje): KravlinjeResponseJson =
            KravlinjeResponseJson(
                kravlinjetype = kravlinje.kravlinjetype,
                opprinneligBeløp = kravlinje.opprinneligBeloep,
                gjenståendeBeløp = kravlinje.gjenstaaendeBeloep,
            )
    }
}

@Serializable
data class HentKravdetaljerJsonRequest(
    val id: String,
    val type: KravidentifikatorType,
) {
    fun toDomain(): Kravidentifikator =
        when (type) {
            KravidentifikatorType.NAV -> Kravidentifikator.Nav(id)
            KravidentifikatorType.SKATTEETATEN -> Kravidentifikator.Skatteetaten(id)
        }
}

@Serializable
enum class KravidentifikatorType {
    NAV,
    SKATTEETATEN,
}
