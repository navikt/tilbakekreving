package no.nav.tilbakekreving.infrastructure.route

import arrow.core.getOrElse
import io.ktor.http.HttpStatusCode
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import no.nav.tilbakekreving.app.HentKravdetaljer
import no.nav.tilbakekreving.infrastructure.audit.AuditLog
import no.nav.tilbakekreving.infrastructure.route.json.HentKravdetaljerJsonRequest
import no.nav.tilbakekreving.infrastructure.route.json.HentKravdetaljerJsonResponse
import no.nav.tilbakekreving.infrastructure.route.util.navUserPrincipal
import org.slf4j.LoggerFactory

context(auditLog: AuditLog)
fun Route.hentKravdetaljerRoute(hentKravdetaljer: HentKravdetaljer) {
    val logger = LoggerFactory.getLogger("HentKravdetaljerRoute")

    post<HentKravdetaljerJsonRequest> { hentKravdetaljerJson ->
        val principal =
            navUserPrincipal() ?: run {
                logger.warn("Fant ikke navUserPrincipal ved henting av kravdetaljer")
                call.respond(HttpStatusCode.Unauthorized, "Ugyldig bruker")
                return@post
            }

        val kravidentifikator = hentKravdetaljerJson.toDomain()

        val kravdetaljer =
            hentKravdetaljer.hentKravdetaljer(kravidentifikator).getOrElse {
                when (it) {
                    HentKravdetaljer.HentKravdetaljerFeil.FantIkkeKravdetaljer -> {
                        call.respond(
                            HttpStatusCode.NoContent,
                        )
                    }

                    HentKravdetaljer.HentKravdetaljerFeil.FeilVedHentingAvKravdetaljer -> {
                        call.respond(
                            HttpStatusCode.InternalServerError,
                            "Feil ved henting av kravdetaljer",
                        )
                    }
                }
                return@post
            }

        auditLog.info(
            AuditLog.Message(
                sourceUserId = principal.navIdent,
                destinationUserId = kravdetaljer.skyldner.identifikator,
                event = AuditLog.EventType.ACCESS,
                message = "Hentet kravdetaljer for innkrevingskrav",
                firstAttribute =
                    Pair(
                        AuditLog.AttributeLabel("Nav-kravidentifikator"),
                        AuditLog.AttributeValue(kravdetaljer.krav.kravgrunnlag.oppdragsgiversKravidentifikator),
                    ),
            ),
        )

        call.respond(HttpStatusCode.OK, HentKravdetaljerJsonResponse.fromDomain(kravdetaljer))
    }
}
