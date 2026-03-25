package no.nav.tilbakekreving.infrastructure.auth

import arrow.core.Either
import no.nav.tilbakekreving.infrastructure.auth.model.OboToken
import no.nav.tilbakekreving.infrastructure.client.texas.TexasClient
import no.nav.tilbakekreving.infrastructure.client.texas.json.IdentityProviderJson

class EntraOboTokenExchanger(
    private val texasClient: TexasClient,
) {
    suspend fun exchange(
        userToken: String,
        target: String,
    ): Either<OboTokenError, OboToken> =
        texasClient
            .exchangeToken(
                identityProvider = IdentityProviderJson.ENTRA_ID,
                target = target,
                userToken = userToken,
            ).map { OboToken(it.accessToken) }
            .mapLeft { OboTokenError.FailedToExchange }
}

sealed class OboTokenError {
    data object FailedToExchange : OboTokenError()
}
