package no.nav.tilbakekreving.infrastructure.client

import arrow.core.Either

interface AccessTokenVerifier {
    suspend fun verifyToken(token: String): Either<VerificationError.FailedToVerifyToken, UserGroups>

    data class UserGroups(
        val groupIds: List<String>,
    )

    sealed class VerificationError {
        data object FailedToVerifyToken : VerificationError()
    }
}
