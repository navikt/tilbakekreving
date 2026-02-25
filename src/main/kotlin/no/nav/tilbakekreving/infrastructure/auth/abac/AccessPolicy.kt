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
