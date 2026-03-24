package no.nav.tilbakekreving.infrastructure.auth

import arrow.core.Either

interface AccessTokenValidator<ValidatedToken> {
    suspend fun validateToken(token: String): Either<ValidationError, ValidatedToken>

    sealed class ValidationError {
        data object FailedToValidateToken : ValidationError()

        data object InvalidToken : ValidationError()
    }
}
