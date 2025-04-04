package no.nav.tilbakekreving.infrastructure.client

import arrow.core.Either

interface AccessTokenProvider {
    suspend fun getAccessToken(vararg scopes: String): Either<GetAccessTokenError, String>

    sealed class GetAccessTokenError {
        data object FailedToGetAccessToken : GetAccessTokenError()
    }
}