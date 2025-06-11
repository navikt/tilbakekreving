package no.nav.tilbakekreving.infrastructure.route

import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.principal
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import no.nav.tilbakekreving.domain.TilleggsfristStore
import no.nav.tilbakekreving.infrastructure.auth.UserGroupIdsPrincipal
import no.nav.tilbakekreving.infrastructure.route.json.OppdaterTilleggsfristJsonRequest
import org.slf4j.LoggerFactory

/**
 * Route handler for updating the tilleggsfrist for a specific kravdetaljer.
 */
fun Route.oppdaterTilleggsfristRoute(tilleggsfristStore: TilleggsfristStore) {
    val logger = LoggerFactory.getLogger("OppdaterTilleggsfristRoute")

    post<OppdaterTilleggsfristJsonRequest> { oppdaterTilleggsfristJson ->
        val groupIds = call.principal<UserGroupIdsPrincipal>()
        logger.info("Oppdaterer tilleggsfrist for kravdetaljer med userGroups=${groupIds?.groupIds}")

        try {
            val (kravidentifikator, tilleggsfrist) = oppdaterTilleggsfristJson.toDomain()
            tilleggsfristStore.setTilleggsfrist(kravidentifikator, tilleggsfrist)

            call.respond(HttpStatusCode.OK)
        } catch (e: Exception) {
            logger.error("Feil ved oppdatering av tilleggsfrist", e)
            call.respond(HttpStatusCode.BadRequest)
        }
    }
}
