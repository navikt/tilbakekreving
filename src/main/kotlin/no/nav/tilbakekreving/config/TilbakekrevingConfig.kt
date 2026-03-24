package no.nav.tilbakekreving.config

import no.nav.tilbakekreving.domain.Kravtype
import no.nav.tilbakekreving.infrastructure.audit.AuditLog
import no.nav.tilbakekreving.infrastructure.auth.model.Enhetsnummer
import no.nav.tilbakekreving.infrastructure.auth.model.GroupId

data class TilbakekrevingConfig(
    val nais: NaisConfig,
    val skatteetaten: SkatteetatenConfig,
    val unleash: UnleashConfig,
    val kravtypeAcl: Map<Enhetsnummer, Set<Kravtype>>,
    val kravTilgangsgruppe: GroupId,
    val auditlog: AuditLog.Config,
    val entraProxy: EntraProxyConfig,
)
