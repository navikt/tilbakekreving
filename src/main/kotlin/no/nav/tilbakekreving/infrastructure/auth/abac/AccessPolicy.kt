package no.nav.tilbakekreving.infrastructure.auth.abac

/**
 * An attribute-based access policy that evaluates a set of rules against a subject and resource.
 *
 * Rules are evaluated in order:
 * - `deny` rules are checked first — if any matches, access is denied.
 * - `require` rules are checked next — all must pass, or access is denied.
 * - If no rules deny access, access is allowed.
 */
class AccessPolicy<S, R>(
    private val denyRules: List<AccessRuleContext<S, R>.() -> Boolean>,
    private val requireRules: List<AccessRuleContext<S, R>.() -> Boolean>,
) {
    fun isAllowed(
        subject: S,
        resource: R,
    ): Boolean {
        val context = AccessRuleContext(subject, resource)
        if (denyRules.any { context.it() }) return false
        return requireRules.all { context.it() }
    }

    fun filter(
        subject: S,
        resources: List<R>,
    ): List<R> = resources.filter { isAllowed(subject, it) }
}

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
