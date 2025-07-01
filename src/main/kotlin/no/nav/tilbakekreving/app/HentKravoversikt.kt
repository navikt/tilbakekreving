package no.nav.tilbakekreving.app

import arrow.core.Either
import no.nav.tilbakekreving.domain.Krav
import no.nav.tilbakekreving.domain.Kravfilter
import no.nav.tilbakekreving.domain.Skyldner

interface HentKravoversikt {
    suspend fun hentKravoversikt(
        skyldner: Skyldner,
        kravfilter: Kravfilter,
    ): Either<HentKravoversiktFeil, List<Krav>>

    sealed class HentKravoversiktFeil {
        data object FeilVedHentingAvKrav : HentKravoversiktFeil()
    }
}
