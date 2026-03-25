package no.nav.tilbakekreving.infrastructure.auth

import arrow.core.left
import arrow.core.right
import io.kotest.assertions.arrow.core.shouldBeLeft
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.core.spec.style.WordSpec
import io.mockk.coEvery
import io.mockk.mockk
import no.nav.tilbakekreving.infrastructure.auth.model.MaskinportenToken
import no.nav.tilbakekreving.infrastructure.auth.model.Scope
import no.nav.tilbakekreving.infrastructure.client.texas.TexasClient
import no.nav.tilbakekreving.infrastructure.client.texas.TexasError
import no.nav.tilbakekreving.infrastructure.client.texas.json.IdentityProviderJson
import no.nav.tilbakekreving.infrastructure.client.texas.json.TexasTokenResponse

class MaskinportenTokenProviderTest :
    WordSpec({
        val texasClient = mockk<TexasClient>()
        val provider = MaskinportenTokenProvider(texasClient)

        "getAccessToken" should {
            "return access token when Texas returns successfully" {
                coEvery {
                    texasClient.getToken(IdentityProviderJson.MASKINPORTEN, "scope1 scope2")
                } returns
                    TexasTokenResponse(
                        accessToken = "token",
                        expiresIn = 3600,
                        tokenType = "Bearer",
                    ).right()

                val result = provider.getAccessToken(setOf(Scope("scope1"), Scope("scope2")))

                result.shouldBeRight(MaskinportenToken("token"))
            }

            "return FailedToGetAccessToken when Texas request fails" {
                coEvery {
                    texasClient.getToken(IdentityProviderJson.MASKINPORTEN, "scope1")
                } returns TexasError.RequestFailed.left()

                val result = provider.getAccessToken(setOf(Scope("scope1")))

                result.shouldBeLeft(AccessTokenProvider.GetAccessTokenError.FailedToGetAccessToken)
            }
        }
    })
