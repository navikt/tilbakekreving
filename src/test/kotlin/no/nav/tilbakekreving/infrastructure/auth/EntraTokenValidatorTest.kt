package no.nav.tilbakekreving.infrastructure.auth

import arrow.core.left
import arrow.core.right
import io.kotest.assertions.arrow.core.shouldBeLeft
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.core.spec.style.WordSpec
import io.mockk.coEvery
import io.mockk.mockk
import no.nav.tilbakekreving.infrastructure.auth.model.GroupId
import no.nav.tilbakekreving.infrastructure.auth.model.ValidatedEntraToken
import no.nav.tilbakekreving.infrastructure.client.texas.TexasClient
import no.nav.tilbakekreving.infrastructure.client.texas.TexasError
import no.nav.tilbakekreving.infrastructure.client.texas.json.ValidateTokenResponse

class EntraTokenValidatorTest :
    WordSpec({
        val texasClient = mockk<TexasClient>()
        val validator = EntraTokenValidator(texasClient)

        "validateToken" should {
            "return ValidatedEntraToken when introspection returns active token" {
                coEvery {
                    texasClient.introspectToken("azuread", "valid-token")
                } returns
                    ValidateTokenResponse
                        .ValidTokenResponse(
                            active = true,
                            NAVident = "Z123456",
                            exp = 1609459200,
                            iat = 1609455600,
                            groups = listOf("group1", "group2", "group3"),
                        ).right()

                val result = validator.validateToken("valid-token")

                result.shouldBeRight(
                    ValidatedEntraToken(
                        "Z123456",
                        listOf("group1", "group2", "group3").map(::GroupId).toSet(),
                    ),
                )
            }

            "return InvalidToken when introspection returns inactive token" {
                coEvery {
                    texasClient.introspectToken("azuread", "invalid-token")
                } returns
                    ValidateTokenResponse
                        .InvalidTokenResponse(
                            active = false,
                            error = "token is expired",
                        ).right()

                val result = validator.validateToken("invalid-token")

                result.shouldBeLeft(AccessTokenValidator.ValidationError.InvalidToken)
            }

            "return FailedToValidateToken when Texas request fails" {
                coEvery {
                    texasClient.introspectToken("azuread", "failing-token")
                } returns TexasError.RequestFailed.left()

                val result = validator.validateToken("failing-token")

                result.shouldBeLeft(AccessTokenValidator.ValidationError.FailedToValidateToken)
            }
        }
    })
