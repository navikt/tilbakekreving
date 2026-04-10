package no.nav.tilbakekreving.infrastructure.route

import arrow.core.left
import arrow.core.right
import io.kotest.core.spec.style.WordSpec
import io.kotest.matchers.shouldBe
import no.nav.tilbakekreving.domain.Kravtype
import no.nav.tilbakekreving.domain.UkjentKravtype
import no.nav.tilbakekreving.infrastructure.auth.abac.policy.NavSaksbehandler
import no.nav.tilbakekreving.infrastructure.auth.abac.policy.lesKravAccessPolicy
import no.nav.tilbakekreving.infrastructure.auth.model.Enhetsnummer
import no.nav.tilbakekreving.infrastructure.auth.model.GroupId
import no.nav.tilbakekreving.infrastructure.unleash.StubFeatureToggle
import org.slf4j.LoggerFactory

class KravAccessPolicyTest :
    WordSpec({
        val kravAccessGroup = GroupId("tilgang_til_krav")
        val enhetA = Enhetsnummer("1111")
        val enhetB = Enhetsnummer("2222")

        val kravtypeA = Kravtype.TILBAKEKREVING_BARNETRYGD
        val kravtypeB = Kravtype.TILBAKEKREVING_DAGPENGER
        val enhetAccess =
            mapOf(
                enhetA to setOf(kravtypeA),
                enhetB to setOf(kravtypeB),
            )

        val logger = LoggerFactory.getLogger(this::class.java)

        "les krav access policy with feature toggle disabled" should {
            val disabledFeatureToggle = StubFeatureToggle(default = false)
            "allow access when user has the krav access group" {
                val policy =
                    context(disabledFeatureToggle, logger) {
                        lesKravAccessPolicy(
                            kravAccessGroup,
                            emptyMap(),
                        )
                    }
                val subject = NavSaksbehandler(setOf(kravAccessGroup), emptySet())
                policy.isAllowed(subject, kravtypeA.right()) shouldBe true
            }

            "deny access when user does not have the krav access group" {
                val policy =
                    context(disabledFeatureToggle, logger) {
                        lesKravAccessPolicy(
                            kravAccessGroup,
                            emptyMap(),
                        )
                    }
                val subject = NavSaksbehandler(setOf(GroupId("other_group")), emptySet())
                policy.isAllowed(subject, kravtypeA.right()) shouldBe false
            }

            "allow access to any kravtype when enhetAccess is set but toggle is disabled" {
                val policy =
                    context(disabledFeatureToggle, logger) {
                        lesKravAccessPolicy(
                            kravAccessGroup,
                            enhetAccess,
                        )
                    }
                val subject = NavSaksbehandler(setOf(kravAccessGroup), setOf(enhetA))

                policy.isAllowed(subject, kravtypeA.right()) shouldBe true
                policy.isAllowed(subject, kravtypeB.right()) shouldBe true
            }
        }

        "les krav access policy with feature toggle enabled" should {
            val enabledFeatureToggle = StubFeatureToggle(default = true)
            "allow access to kravtype matching enhet mapping" {
                val policy =
                    context(enabledFeatureToggle, logger) {
                        lesKravAccessPolicy(
                            kravAccessGroup,
                            enhetAccess,
                        )
                    }

                val subjectWithEnhetA = NavSaksbehandler(setOf(kravAccessGroup), setOf(enhetA))

                subjectWithEnhetA.let {
                    policy.isAllowed(it, kravtypeA.right()) shouldBe true
                    policy.isAllowed(it, kravtypeB.right()) shouldBe false
                }
            }

            "deny all krav when user has base group but no enhet groups" {
                val policy =
                    context(enabledFeatureToggle, logger) {
                        lesKravAccessPolicy(
                            kravAccessGroup,
                            enhetAccess,
                        )
                    }

                val subject = NavSaksbehandler(setOf(kravAccessGroup), emptySet())
                policy.isAllowed(subject, kravtypeA.right()) shouldBe false
            }

            "allow all kravtyper when user has all enhetsnummer" {
                val policy =
                    context(enabledFeatureToggle, logger) {
                        lesKravAccessPolicy(
                            kravAccessGroup,
                            enhetAccess,
                        )
                    }

                val subject = NavSaksbehandler(setOf(kravAccessGroup), setOf(enhetA, enhetB))

                policy.isAllowed(subject, kravtypeA.right()) shouldBe true
                policy.isAllowed(subject, kravtypeB.right()) shouldBe true
            }

            "allow access to any kravtype when enhet is mapped to Kravtype.ALLE" {
                val enhetAccessWithAlle = mapOf(enhetA to setOf(Kravtype.ALLE))
                val policy =
                    context(enabledFeatureToggle, logger) {
                        lesKravAccessPolicy(
                            kravAccessGroup,
                            enhetAccessWithAlle,
                        )
                    }

                val subject = NavSaksbehandler(setOf(kravAccessGroup), setOf(enhetA))
                policy.isAllowed(subject, kravtypeA.right()) shouldBe true
                policy.isAllowed(subject, kravtypeB.right()) shouldBe true
            }

            "allow all krav when one enhet has Kravtype.ALLE even if another has specific kravtype" {
                val mixedEnhetAccess =
                    mapOf(
                        enhetA to setOf(Kravtype.ALLE),
                        enhetB to setOf(Kravtype.TILBAKEKREVING_DAGPENGER),
                    )
                val policy =
                    context(enabledFeatureToggle, logger) {
                        lesKravAccessPolicy(
                            kravAccessGroup,
                            mixedEnhetAccess,
                        )
                    }

                val subject = NavSaksbehandler(setOf(kravAccessGroup), setOf(enhetA, enhetB))
                policy.isAllowed(subject, kravtypeA.right()) shouldBe true
                policy.isAllowed(subject, kravtypeB.right()) shouldBe true
            }

            "deny unknown kravtype when enhet does not have ALLE" {
                val policy =
                    context(enabledFeatureToggle, logger) {
                        lesKravAccessPolicy(
                            kravAccessGroup,
                            enhetAccess,
                        )
                    }

                val subject = NavSaksbehandler(setOf(kravAccessGroup), setOf(enhetA))
                policy.isAllowed(subject, UkjentKravtype("NY_KRAVTYPE").left()) shouldBe false
            }

            "allow unknown kravtype when enhet has ALLE" {
                val enhetAccessWithAlle = mapOf(enhetA to setOf(Kravtype.ALLE))
                val policy =
                    context(enabledFeatureToggle, logger) {
                        lesKravAccessPolicy(
                            kravAccessGroup,
                            enhetAccessWithAlle,
                        )
                    }

                val subject = NavSaksbehandler(setOf(kravAccessGroup), setOf(enhetA))
                policy.isAllowed(subject, UkjentKravtype("NY_KRAVTYPE").left()) shouldBe true
            }
        }
    })
