package no.nav.tilbakekreving.infrastructure.auth

import arrow.core.Either
import arrow.core.raise.either
import no.nav.tilbakekreving.infrastructure.auth.model.GroupId
import no.nav.tilbakekreving.infrastructure.auth.model.ValidatedEntraToken
import no.nav.tilbakekreving.infrastructure.client.texas.TexasClient
import no.nav.tilbakekreving.infrastructure.client.texas.json.ValidateTokenResponse
import org.slf4j.LoggerFactory

class EntraTokenValidator(
    private val texasClient: TexasClient,
) : AccessTokenValidator<ValidatedEntraToken> {
    private val logger = LoggerFactory.getLogger(this::class.java)

    override suspend fun validateToken(token: String): Either<AccessTokenValidator.ValidationError, ValidatedEntraToken> =
        either {
            val response =
                texasClient
                    .introspectToken(identityProvider = "azuread", token = token)
                    .mapLeft { AccessTokenValidator.ValidationError.FailedToValidateToken }
                    .bind()

            when (response) {
                is ValidateTokenResponse.ValidTokenResponse -> {
                    ValidatedEntraToken(
                        navIdent = response.NAVident,
                        groupIds = response.groups.map(::GroupId).toSet(),
                    )
                }

                is ValidateTokenResponse.InvalidTokenResponse -> {
                    logger.info("Token is invalid: ${response.error}")
                    raise(AccessTokenValidator.ValidationError.InvalidToken)
                }
            }
        }
}
