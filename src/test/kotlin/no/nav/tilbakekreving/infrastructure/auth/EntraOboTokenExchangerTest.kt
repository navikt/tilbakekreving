package no.nav.tilbakekreving.infrastructure.auth

import arrow.core.left
import arrow.core.right
import io.kotest.assertions.arrow.core.shouldBeLeft
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.core.spec.style.WordSpec
import io.mockk.coEvery
import io.mockk.mockk
import no.nav.tilbakekreving.infrastructure.auth.model.OboToken
import no.nav.tilbakekreving.infrastructure.client.texas.TexasClient
import no.nav.tilbakekreving.infrastructure.client.texas.TexasError
import no.nav.tilbakekreving.infrastructure.client.texas.json.IdentityProviderJson
import no.nav.tilbakekreving.infrastructure.client.texas.json.TexasTokenResponse

class EntraOboTokenExchangerTest :
    WordSpec({
        val texasClient = mockk<TexasClient>()
        val exchanger = EntraOboTokenExchanger(texasClient)

        "exchange" should {
            "return OboToken when exchange is successful" {
                coEvery {
                    texasClient.exchangeToken(
                        identityProvider = IdentityProviderJson.ENTRA_ID,
                        target = "api://target-app/.default",
                        userToken = "user-token-123",
                    )
                } returns
                    TexasTokenResponse(
                        accessToken = "exchanged-obo-token",
                        expiresIn = 3600,
                        tokenType = "Bearer",
                    ).right()

                val result = exchanger.exchange("user-token-123", "api://target-app/.default")

                result.shouldBeRight(OboToken("exchanged-obo-token"))
            }

            "return FailedToExchange when Texas request fails" {
                coEvery {
                    texasClient.exchangeToken(
                        identityProvider = IdentityProviderJson.ENTRA_ID,
                        target = "api://target",
                        userToken = "user-token",
                    )
                } returns TexasError.RequestFailed.left()

                val result = exchanger.exchange("user-token", "api://target")

                result.shouldBeLeft(OboTokenError.FailedToExchange)
            }
        }
    })
