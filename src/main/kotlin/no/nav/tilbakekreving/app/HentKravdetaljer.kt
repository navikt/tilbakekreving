package no.nav.tilbakekreving.app

import arrow.core.Either
import no.nav.tilbakekreving.domain.Kravdetaljer
import no.nav.tilbakekreving.domain.Kravidentifikator

interface HentKravdetaljer {
    suspend fun hentKravdetaljer(kravidentifikator: Kravidentifikator): Either<HentKravdetaljerFeil, Kravdetaljer>

    sealed class HentKravdetaljerFeil {
        data object FantIkkeKravdetaljer : HentKravdetaljerFeil()

        data object FeilVedHentingAvKravdetaljer : HentKravdetaljerFeil()
    }
}
