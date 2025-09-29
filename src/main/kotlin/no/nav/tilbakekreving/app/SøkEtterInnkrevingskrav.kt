package no.nav.tilbakekreving.app

import arrow.core.Either
import no.nav.tilbakekreving.domain.Krav
import no.nav.tilbakekreving.domain.Skyldnersøk

interface SøkEtterInnkrevingskrav {
    suspend fun søk(skyldnersøk: Skyldnersøk): Either<Feil, List<Krav>>

    sealed class Feil {
        data object SøkEtterInnkrevingskravFeil : Feil()
    }
}

