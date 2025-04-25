package no.nav.tilbakekreving.infrastructure.route

import arrow.core.getOrElse
import io.ktor.http.HttpStatusCode
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import no.nav.tilbakekreving.app.HentKravdetaljer
import no.nav.tilbakekreving.infrastructure.route.json.HentKravdetaljerJsonRequest
import no.nav.tilbakekreving.infrastructure.route.json.HentKravdetaljerJsonResponse

fun Route.hentKravdetaljerRoute(hentKravdetaljer: HentKravdetaljer) {
    post<HentKravdetaljerJsonRequest> { hentKravdetaljerJson ->
        val kravidentifikator = hentKravdetaljerJson.toDomain()
        val kravdetaljer =
            hentKravdetaljer.hentKravdetaljer(kravidentifikator).getOrElse {
                when (it) {
                    HentKravdetaljer.HentKravdetaljerFeil.FantIkkeKravdetaljer ->
                        call.respond(
                            HttpStatusCode.NoContent,
                        )

                    HentKravdetaljer.HentKravdetaljerFeil.FeilVedHentingAvKravdetaljer ->
                        call.respond(
                            HttpStatusCode.InternalServerError,
                            "Feil ved henting av kravdetaljer",
                        )
                }
                return@post
            }

        call.respond(HttpStatusCode.OK, HentKravdetaljerJsonResponse.fromDomain(kravdetaljer))
    }
}
