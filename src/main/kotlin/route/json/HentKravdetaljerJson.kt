package no.nav.route.json

import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable
import no.nav.domain.Kravdetaljer
import no.nav.domain.Kravgrunnlag
import no.nav.domain.Kravidentifikator
import no.nav.domain.Kravlinje

@Serializable
data class HentKravdetaljerJsonResponse(
    val kravgrunnlag: KravgrunnlagResponseJson,
    val kravlinjer: List<KravlinjeResponseJson>,
) {
    companion object {
        fun fromDomain(kravdetaljer: Kravdetaljer): HentKravdetaljerJsonResponse =
            HentKravdetaljerJsonResponse(
                kravgrunnlag = KravgrunnlagResponseJson.fromDomain(kravdetaljer.kravgrunnlag),
                kravlinjer = kravdetaljer.kravlinjer.map(KravlinjeResponseJson::fromDomain),
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

enum class KravidentifikatorType {
    NAV,
    SKATTEETATEN,
}
