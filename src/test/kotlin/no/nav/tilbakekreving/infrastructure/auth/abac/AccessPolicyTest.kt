package no.nav.tilbakekreving.infrastructure.auth.abac

import io.kotest.core.spec.style.WordSpec
import io.kotest.matchers.shouldBe

class AccessPolicyTest :
    WordSpec({
        data class User(
            val role: String,
            val department: String,
        )

        data class Document(
            val ownerId: String,
            val confidential: Boolean,
        )

        "AccessPolicy" should {
            "allow access when no rules are defined" {
                val policy = accessPolicy<User, Document> {}
                policy.isAllowed(User("user", "HR"), Document("someone", false)) shouldBe true
            }

            "allow access when all require rules pass" {
                val policy =
                    accessPolicy<User, Document> {
                        require { subject.role == "admin" }
                        require { subject.department == "IT" }
                    }
                policy.isAllowed(User("admin", "IT"), Document("x", false)) shouldBe true
            }

            "deny access when any require rule fails" {
                val policy =
                    accessPolicy<User, Document> {
                        require { subject.role == "admin" }
                        require { subject.department == "IT" }
                    }
                policy.isAllowed(User("admin", "HR"), Document("x", false)) shouldBe false
            }

            "deny access when a deny rule matches" {
                val policy =
                    accessPolicy<User, Document> {
                        deny { resource.confidential }
                    }
                policy.isAllowed(User("admin", "IT"), Document("x", true)) shouldBe false
            }

            "deny rules take precedence over require rules" {
                val policy =
                    accessPolicy<User, Document> {
                        require { subject.role == "admin" }
                        deny { resource.confidential }
                    }
                policy.isAllowed(User("admin", "IT"), Document("x", true)) shouldBe false
            }
        }
    })
