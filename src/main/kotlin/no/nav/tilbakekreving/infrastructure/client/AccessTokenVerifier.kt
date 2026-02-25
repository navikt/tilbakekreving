package no.nav.tilbakekreving.infrastructure.client

import arrow.core.Either

interface AccessTokenVerifier<VerifiedToken> {
    suspend fun verifyToken(token: String): Either<VerificationError, VerifiedToken>

    sealed class VerificationError {
        data object FailedToVerifyToken : VerificationError()

        data object InvalidToken : VerificationError()
    }
}
