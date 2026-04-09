package no.nav.tilbakekreving.infrastructure.auth.abac

/**
 * An attribute-based access policy that evaluates a set of rules against a subject and resource.
 *
 * Rules are evaluated in order:
 * - `deny` rules are checked first — if any matches, access is denied.
 * - `require` rules are checked next — all must pass, or access is denied.
 * - If no rules deny access, access is allowed.
 */
class AccessPolicy<Subject, Resource>(
    private val denyRules: List<AccessRuleContext<Subject, Resource>.() -> Boolean>,
    private val requireRules: List<AccessRuleContext<Subject, Resource>.() -> Boolean>,
) {
    fun isAllowed(
        subject: Subject,
        resource: Resource,
    ): Boolean {
        val context = AccessRuleContext(subject, resource)
        if (denyRules.any { context.it() }) return false
        return requireRules.all { context.it() }
    }
}
