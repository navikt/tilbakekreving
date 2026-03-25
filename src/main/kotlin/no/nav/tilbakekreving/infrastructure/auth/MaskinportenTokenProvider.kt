package no.nav.tilbakekreving.infrastructure.auth

import arrow.core.Either
import no.nav.tilbakekreving.infrastructure.auth.model.MaskinportenToken
import no.nav.tilbakekreving.infrastructure.auth.model.Scope
import no.nav.tilbakekreving.infrastructure.client.texas.TexasClient
import no.nav.tilbakekreving.infrastructure.client.texas.json.IdentityProviderJson

class MaskinportenTokenProvider(
    private val texasClient: TexasClient,
) : AccessTokenProvider<MaskinportenToken> {
    override suspend fun getAccessToken(scopes: Set<Scope>): Either<AccessTokenProvider.GetAccessTokenError, MaskinportenToken> =
        texasClient
            .getToken(
                identityProvider = IdentityProviderJson.MASKINPORTEN,
                target = scopes.joinToString(" ") { it.scope },
            ).map { MaskinportenToken(it.accessToken) }
            .mapLeft { AccessTokenProvider.GetAccessTokenError.FailedToGetAccessToken }
}
