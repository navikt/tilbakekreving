package no.nav.infrastructure.route

import arrow.core.getOrElse
import io.ktor.http.HttpStatusCode
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import no.nav.app.HentKravdetaljer
import no.nav.infrastructure.route.json.HentKravdetaljerJsonRequest
import no.nav.infrastructure.route.json.HentKravdetaljerJsonResponse

fun Route.hentKravdetaljer(hentKravdetaljer: HentKravdetaljer) {
    post<HentKravdetaljerJsonRequest> { jsonRequest ->
        val kravdetaljer =
            hentKravdetaljer.hentKravdetaljer(jsonRequest.toDomain()).getOrElse {
                call.respond(HttpStatusCode.InternalServerError, "Feil ved henting av kravdetaljer")
                return@post
            }

        call.respond(HttpStatusCode.OK, HentKravdetaljerJsonResponse.fromDomain(kravdetaljer))
    }
}
