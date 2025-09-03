package no.nav.tilbakekreving.setup

import arrow.core.getOrElse
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.bearer
import no.nav.tilbakekreving.infrastructure.auth.UserGroupIdsPrincipal
import no.nav.tilbakekreving.infrastructure.client.AccessTokenVerifier
import org.slf4j.LoggerFactory

@JvmInline
value class AuthenticationConfigName(
    val name: String,
)

fun Application.configureAuthentication(
    authenticationConfigName: AuthenticationConfigName,
    accessTokenVerifier: AccessTokenVerifier,
) {
    install(Authentication) {
        val logger = LoggerFactory.getLogger("Authentication")
        bearer(authenticationConfigName.name) {
            authenticate { credentials ->
                accessTokenVerifier
                    .verifyToken(credentials.token)
                    .getOrElse { error ->
                        when (error) {
                            is AccessTokenVerifier.VerificationError.FailedToVerifyToken -> {
                                logger.error("Token verification failed: $error")
                                null
                            }

                            is AccessTokenVerifier.VerificationError.InvalidToken -> {
                                logger.error("Token is invalid: $error")
                                null
                            }
                        }
                    }?.let { validated ->
                        UserGroupIdsPrincipal(validated.groupIds)
                    }
            }
        }
    }
}
