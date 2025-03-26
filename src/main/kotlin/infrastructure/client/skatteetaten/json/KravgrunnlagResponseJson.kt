package no.nav.infrastructure.client.skatteetaten.json

import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable
import no.nav.domain.Kravgrunnlag

@Serializable
data class KravgrunnlagResponseJson(
    val datoNaarKravVarBesluttetHosOppdragsgiver: String,
) {
    fun toDomain(): Kravgrunnlag =
        Kravgrunnlag(
            datoNaarKravVarBesluttetHosOppdragsgiver = LocalDate.parse(datoNaarKravVarBesluttetHosOppdragsgiver),
        )
}