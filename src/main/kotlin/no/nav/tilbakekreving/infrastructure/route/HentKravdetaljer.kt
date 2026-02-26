package no.nav.tilbakekreving.infrastructure.route

import arrow.core.getOrElse
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import no.nav.tilbakekreving.app.HentKravdetaljer
import no.nav.tilbakekreving.infrastructure.audit.AuditLog
import no.nav.tilbakekreving.infrastructure.route.json.HentKravdetaljerJsonRequest
import no.nav.tilbakekreving.infrastructure.route.json.HentKravdetaljerJsonResponse
import no.nav.tilbakekreving.infrastructure.route.util.authenticatedPost

context(auditLog: AuditLog)
fun Route.hentKravdetaljerRoute(hentKravdetaljer: HentKravdetaljer) {
    authenticatedPost { principal ->
        val kravidentifikator = call.receive<HentKravdetaljerJsonRequest>().toDomain()

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
                return@authenticatedPost
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
