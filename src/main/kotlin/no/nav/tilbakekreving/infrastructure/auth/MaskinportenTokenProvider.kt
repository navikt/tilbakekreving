package no.nav.tilbakekreving.infrastructure.auth

import arrow.core.Either
import no.nav.tilbakekreving.infrastructure.auth.model.MaskinportenToken
import no.nav.tilbakekreving.infrastructure.auth.model.Scope
import no.nav.tilbakekreving.infrastructure.client.texas.TexasClient

class MaskinportenTokenProvider(
    private val texasClient: TexasClient,
) : AccessTokenProvider<MaskinportenToken> {
    override suspend fun getAccessToken(scopes: Set<Scope>): Either<AccessTokenProvider.GetAccessTokenError, MaskinportenToken> =
        texasClient
            .getToken(
                identityProvider = "maskinporten",
                target = scopes.joinToString(" ") { it.scope },
            ).map { MaskinportenToken(it.accessToken) }
            .mapLeft { AccessTokenProvider.GetAccessTokenError.FailedToGetAccessToken }
}
