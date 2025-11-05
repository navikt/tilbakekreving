package no.nav.tilbakekreving.infrastructure.route.util

import io.ktor.server.auth.principal
import io.ktor.server.routing.RoutingContext
import no.nav.tilbakekreving.infrastructure.auth.NavUserPrincipal

/**
 * Henter NavUserPrincipal fra RoutingContext som blir tilgjengeligjort av [no.nav.tilbakekreving.setup.configureAuthentication].
 */
fun RoutingContext.navUserPrincipal() = call.principal<NavUserPrincipal>()
