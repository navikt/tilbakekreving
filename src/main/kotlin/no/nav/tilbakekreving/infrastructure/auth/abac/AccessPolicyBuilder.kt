package no.nav.tilbakekreving.infrastructure.auth.abac

/**
 * DSL builder for constructing an [AccessPolicy].
 */
@AccessPolicyDsl
class AccessPolicyBuilder<S, R> {
    private val denyRules = mutableListOf<AccessRuleContext<S, R>.() -> Boolean>()
    private val requireRules = mutableListOf<AccessRuleContext<S, R>.() -> Boolean>()

    /** A rule that must be true for access to be granted. All require rules must pass. */
    fun require(rule: AccessRuleContext<S, R>.() -> Boolean) {
        requireRules += rule
    }

    /** A rule that, if true, denies access regardless of other rules. */
    fun deny(rule: AccessRuleContext<S, R>.() -> Boolean) {
        denyRules += rule
    }

    fun build(): AccessPolicy<S, R> = AccessPolicy(denyRules, requireRules)
}

/** Entry point for defining an access policy using the DSL. */
fun <S, R> accessPolicy(block: AccessPolicyBuilder<S, R>.() -> Unit): AccessPolicy<S, R> = AccessPolicyBuilder<S, R>().apply(block).build()
