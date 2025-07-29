package no.nav.tilbakekreving.infrastructure.route.json

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import no.nav.tilbakekreving.domain.Krav
import no.nav.tilbakekreving.domain.Kravidentifikator

@Serializable
data class HentKravoversiktJsonResponse(
    val krav: List<KravResponseJson>,
) {
    companion object {
        fun fromDomain(kravoversikt: List<Krav>): HentKravoversiktJsonResponse =
            HentKravoversiktJsonResponse(
                krav = kravoversikt.map(KravResponseJson::from),
            )
    }
}

@Serializable
data class KravResponseJson(
    val kravidentifikator: KravidentifikatorJsonResponse,
    val kravtype: String,
) {
    companion object {
        fun from(krav: Krav): KravResponseJson =
            KravResponseJson(
                kravidentifikator = KravidentifikatorJsonResponse.from(krav.kravidentifikator),
                kravtype = krav.kravtype.value,
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