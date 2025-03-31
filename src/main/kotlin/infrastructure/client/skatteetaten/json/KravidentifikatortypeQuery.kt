package no.nav.infrastructure.client.skatteetaten.json

import no.nav.domain.Kravidentifikator

enum class KravidentifikatortypeQuery {
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
