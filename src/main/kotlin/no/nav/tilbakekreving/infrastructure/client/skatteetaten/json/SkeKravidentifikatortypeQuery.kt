package no.nav.tilbakekreving.infrastructure.client.skatteetaten.json

import no.nav.tilbakekreving.domain.Kravidentifikator

enum class SkeKravidentifikatortypeQuery {
    OPPDRAGSGIVERS_KRAVIDENTIFIKATOR,
    SKATTEETATENS_KRAVIDENTIFIKATOR,
    ;

    companion object {
        fun from(kravidentifikator: Kravidentifikator) =
            when (kravidentifikator) {
                is Kravidentifikator.Nav -> OPPDRAGSGIVERS_KRAVIDENTIFIKATOR
                is Kravidentifikator.Skatteetaten -> SKATTEETATENS_KRAVIDENTIFIKATOR
            }
    }
}
