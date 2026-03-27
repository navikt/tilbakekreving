package no.nav.tilbakekreving.infrastructure.route

import io.kotest.core.spec.style.WordSpec
import io.kotest.matchers.shouldBe
import no.nav.tilbakekreving.domain.Krav
import no.nav.tilbakekreving.domain.Kravbeskrivelse
import no.nav.tilbakekreving.domain.Kravidentifikator
import no.nav.tilbakekreving.domain.Kravtype
import no.nav.tilbakekreving.infrastructure.auth.abac.policy.NavSaksbehandler
import no.nav.tilbakekreving.infrastructure.auth.abac.policy.lesKravAccessPolicy
import no.nav.tilbakekreving.infrastructure.auth.model.Enhetsnummer
import no.nav.tilbakekreving.infrastructure.auth.model.GroupId
import no.nav.tilbakekreving.infrastructure.unleash.StubFeatureToggle
import org.slf4j.LoggerFactory
import java.util.Locale

class KravAccessPolicyTest :
    WordSpec({
        val kravAccessGroup = GroupId("tilgang_til_krav")
        val enhetA = Enhetsnummer("1111")
        val enhetB = Enhetsnummer("2222")

        fun krav(kravtype: Kravtype) =
            Krav(
                skeKravidentifikator = Kravidentifikator.Skatteetaten("skatte-123"),
                navKravidentifikator = Kravidentifikator.Nav("123"),
                navReferanse = null,
                kravtype = kravtype,
                kravbeskrivelse = listOf(Kravbeskrivelse(Locale.forLanguageTag("nb"), "Test")),
                gjenståendeBeløp = 1000.0,
            )

        val kravtypeA = Kravtype.TILBAKEKREVING_BARNETRYGD
        val kravtypeB = Kravtype.TILBAKEKREVING_DAGPENGER
        val kravA = krav(kravtypeA)
        val kravB = krav(kravtypeB)
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
                policy.isAllowed(subject, kravA) shouldBe true
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
                policy.isAllowed(subject, kravA) shouldBe false
            }

            "not filter krav by enhetAccess" {
                val policy =
                    context(disabledFeatureToggle, logger) {
                        lesKravAccessPolicy(
                            kravAccessGroup,
                            enhetAccess,
                        )
                    }
                val subject = NavSaksbehandler(setOf(kravAccessGroup), setOf(enhetA))
                val kravList = listOf(kravA, kravB)

                policy.filter(subject, kravList) shouldBe kravList
            }
        }

        "les krav access policy with feature toggle enabled" should {
            val enabledFeatureToggle = StubFeatureToggle(default = true)
            "filter krav based on access policy" {
                val policy =
                    context(enabledFeatureToggle, logger) {
                        lesKravAccessPolicy(
                            kravAccessGroup,
                            enhetAccess,
                        )
                    }

                val subjectWithEnhetA = NavSaksbehandler(setOf(kravAccessGroup), setOf(enhetA))
                val kravList = listOf(kravA, kravB)

                policy.filter(subjectWithEnhetA, kravList) shouldBe listOf(kravA)
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
                policy.filter(subject, listOf(kravA)) shouldBe emptyList()
            }

            "allow krav when user has both base group and all enhetsnummer" {
                val policy =
                    context(enabledFeatureToggle, logger) {
                        lesKravAccessPolicy(
                            kravAccessGroup,
                            enhetAccess,
                        )
                    }

                val subject = NavSaksbehandler(setOf(kravAccessGroup), setOf(enhetA, enhetB))
                val kravList = listOf(kravA, kravB)

                policy.filter(subject, kravList) shouldBe kravList
            }
        }
    })
