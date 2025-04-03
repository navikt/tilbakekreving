package no.nav.app

import arrow.core.Either
import no.nav.domain.Kravdetaljer
import no.nav.domain.Kravidentifikator

interface HentKravdetaljer {
    suspend fun hentKravdetaljer(kravidentifikator: Kravidentifikator): Either<HentKravdetaljerFeil, Kravdetaljer>

    sealed class HentKravdetaljerFeil {
        data object FantIkkeKravdetaljer : HentKravdetaljerFeil()

        data object FeilVedHentingAvKravdetaljer : HentKravdetaljerFeil()
    }
}
