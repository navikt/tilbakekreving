package no.nav.tilbakekreving.config

import no.nav.tilbakekreving.domain.Kravtype
import no.nav.tilbakekreving.infrastructure.auth.GroupId

data class TilbakekrevingConfig(
    val nais: NaisConfig,
    val skatteetaten: SkatteetatenConfig,
    val kravAcl: Map<Kravtype, Set<GroupId>>,
)
