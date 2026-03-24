package no.nav.tilbakekreving.infrastructure.auth

import arrow.core.Either
import no.nav.tilbakekreving.infrastructure.auth.model.Scope

interface AccessTokenProvider {
    suspend fun getAccessToken(scopes: Set<Scope>): Either<GetAccessTokenError, String>

    sealed class GetAccessTokenError {
        data object FailedToGetAccessToken : GetAccessTokenError()
    }
}
