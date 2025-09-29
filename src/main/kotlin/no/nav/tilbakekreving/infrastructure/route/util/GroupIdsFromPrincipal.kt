package no.nav.tilbakekreving.infrastructure.route.util

import io.ktor.server.auth.principal
import io.ktor.server.routing.RoutingContext
import no.nav.tilbakekreving.infrastructure.auth.UserGroupIdsPrincipal

fun RoutingContext.groupIdsFromPrincipal() = call.principal<UserGroupIdsPrincipal>()?.groupIds?.toSet() ?: emptySet()
