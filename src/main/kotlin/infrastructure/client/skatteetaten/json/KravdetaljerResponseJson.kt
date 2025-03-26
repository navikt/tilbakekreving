package no.nav.infrastructure.client.skatteetaten.json

import kotlinx.serialization.Serializable
import no.nav.domain.Kravdetaljer

@Serializable
data class KravdetaljerResponseJson(
    val kravgrunnlag: KravgrunnlagResponseJson,
    val kravlinjer: List<KravlinjeResponseJson>,
) {
    fun toDomain(): Kravdetaljer =
        Kravdetaljer(
            kravgrunnlag = kravgrunnlag.toDomain(),
            kravlinjer = kravlinjer.map(KravlinjeResponseJson::toDomain),
        )
}