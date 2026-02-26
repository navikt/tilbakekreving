package no.nav.tilbakekreving.infrastructure.route.util

import io.ktor.server.auth.principal
import io.ktor.server.routing.Route
import io.ktor.server.routing.RoutingContext
import io.ktor.server.routing.post
import no.nav.tilbakekreving.infrastructure.auth.NavUserPrincipal

fun Route.authenticatedPost(body: suspend RoutingContext.(NavUserPrincipal) -> Unit) {
    post {
        val principal =
            requireNotNull(call.principal<NavUserPrincipal>()) {
                "NavUserPrincipal must be present inside an authenticate block"
            }
        body(principal)
    }
}
