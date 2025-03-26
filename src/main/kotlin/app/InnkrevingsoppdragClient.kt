package no.nav.app

import arrow.core.Either
import no.nav.domain.Kravdetaljer

interface InnkrevingsoppdragClient {
    suspend fun hentKravdetaljer(
        kravidentifikator: String,
        kravidentifikatortype: String,
    ): Either<HentKravdetaljerFeil, Kravdetaljer>
}

sealed class HentKravdetaljerFeil {
    data object FeilVedHentingAvKravdetaljer : HentKravdetaljerFeil()
}
