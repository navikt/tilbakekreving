package no.nav.tilbakekreving.infrastructure.route

import io.kotest.core.spec.style.WordSpec
import io.kotest.matchers.shouldBe
import no.nav.tilbakekreving.domain.Krav
import no.nav.tilbakekreving.domain.Kravbeskrivelse
import no.nav.tilbakekreving.domain.Kravidentifikator
import no.nav.tilbakekreving.domain.Kravtype
import no.nav.tilbakekreving.infrastructure.auth.GroupId
import no.nav.tilbakekreving.infrastructure.auth.abac.policy.KravAccessSubject
import no.nav.tilbakekreving.infrastructure.auth.abac.policy.lesKravAccessPolicy
import no.nav.tilbakekreving.infrastructure.unleash.StubFeatureToggles
import java.util.Locale

class KravAccessPolicyTest :
    WordSpec({
        val kravAccessGroup = GroupId("tilgang_til_krav")
        val enhetAccess =
            mapOf(
                Kravtype("TYPE_A") to setOf(GroupId("enhet_a")),
                Kravtype("TYPE_B") to setOf(GroupId("enhet_b")),
            )

        fun krav(kravtype: String) =
            Krav(
                skeKravidentifikator = Kravidentifikator.Skatteetaten("skatte-123"),
                navKravidentifikator = Kravidentifikator.Nav("123"),
                navReferanse = null,
                kravtype = Kravtype(kravtype),
                kravbeskrivelse = listOf(Kravbeskrivelse(Locale.forLanguageTag("nb"), "Test")),
                gjenståendeBeløp = 1000.0,
            )

        "kravAccessPolicy with toggle disabled" should {
            "allow access when user has the krav access group" {
                val policy = context(StubFeatureToggles(default = false)) { lesKravAccessPolicy(kravAccessGroup) }
                val subject = KravAccessSubject(setOf(kravAccessGroup))
                policy.isAllowed(subject, krav("TYPE_A")) shouldBe true
            }

            "deny access when user does not have the krav access group" {
                val policy = context(StubFeatureToggles(default = false)) { lesKravAccessPolicy(kravAccessGroup) }
                val subject = KravAccessSubject(setOf(GroupId("other_group")))
                policy.isAllowed(subject, krav("TYPE_A")) shouldBe false
            }

            "filter krav list based on group membership" {
                val policy = context(StubFeatureToggles(default = false)) { lesKravAccessPolicy(kravAccessGroup) }
                val kravList = listOf(krav("TYPE_A"), krav("TYPE_B"))

                val allowedSubject = KravAccessSubject(setOf(kravAccessGroup))
                policy.filter(allowedSubject, kravList) shouldBe kravList

                val deniedSubject = KravAccessSubject(setOf(GroupId("no_access")))
                policy.filter(deniedSubject, kravList) shouldBe emptyList()
            }

            "not filter by enhet when toggle is disabled" {
                val policy =
                    context(StubFeatureToggles(default = false)) {
                        lesKravAccessPolicy(
                            kravAccessGroup,
                            enhetAccess,
                        )
                    }
                val subject = KravAccessSubject(setOf(kravAccessGroup))
                val kravList = listOf(krav("TYPE_A"), krav("TYPE_B"))

                policy.filter(subject, kravList) shouldBe kravList
            }
        }

        "kravAccessPolicy with toggle enabled" should {
            "filter krav by kravtype when enhet rule is enabled via toggle" {
                val policy =
                    context(StubFeatureToggles(default = true)) {
                        lesKravAccessPolicy(
                            kravAccessGroup,
                            enhetAccess,
                        )
                    }

                val subjectWithEnhetA = KravAccessSubject(setOf(kravAccessGroup, GroupId("enhet_a")))
                val kravList = listOf(krav("TYPE_A"), krav("TYPE_B"))

                policy.filter(subjectWithEnhetA, kravList) shouldBe listOf(krav("TYPE_A"))
            }

            "deny all krav when user has base group but no enhet groups" {
                val policy =
                    context(StubFeatureToggles(default = true)) {
                        lesKravAccessPolicy(
                            kravAccessGroup,
                            enhetAccess,
                        )
                    }

                val subject = KravAccessSubject(setOf(kravAccessGroup))
                policy.filter(subject, listOf(krav("TYPE_A"))) shouldBe emptyList()
            }

            "allow krav when user has both base group and matching enhet group" {
                val policy =
                    context(StubFeatureToggles(default = true)) {
                        lesKravAccessPolicy(
                            kravAccessGroup,
                            enhetAccess,
                        )
                    }

                val subject = KravAccessSubject(setOf(kravAccessGroup, GroupId("enhet_a"), GroupId("enhet_b")))
                val kravList = listOf(krav("TYPE_A"), krav("TYPE_B"))

                policy.filter(subject, kravList) shouldBe kravList
            }
        }
    })
