package no.nav.tilbakekreving.infrastructure.auth.abac

/**
 * Context available inside rule definitions, providing access to the subject and resource being evaluated.
 */
@AccessPolicyDsl
class AccessRuleContext<S, R>(
    val subject: S,
    val resource: R,
)
