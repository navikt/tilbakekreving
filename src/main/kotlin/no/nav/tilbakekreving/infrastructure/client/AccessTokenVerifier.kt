package no.nav.tilbakekreving.infrastructure.client

import arrow.core.Either
import no.nav.tilbakekreving.infrastructure.auth.GroupId

interface AccessTokenVerifier {
    suspend fun verifyToken(token: String): Either<VerificationError, ValidatedToken>

    data class ValidatedToken(
        val groupIds: List<GroupId>,
    )

    sealed class VerificationError {
        data object FailedToVerifyToken : VerificationError()

        data object InvalidToken : VerificationError()
    }
}
