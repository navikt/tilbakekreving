package no.nav.tilbakekreving.infrastructure.audit

import io.kotest.core.spec.style.WordSpec
import io.kotest.matchers.string.shouldContain
import io.mockk.every
import io.mockk.slot
import io.mockk.spyk
import io.mockk.verify
import no.nav.common.audit_log.cef.CefMessage
import no.nav.common.audit_log.log.AuditLoggerImpl

class NavAuditLogTest :
    WordSpec({
        "NavAuditLog" should {
            "log audit message with correct CEF format" {
                val auditLogger = spyk(AuditLoggerImpl())
                val config =
                    AuditLog.Config(
                        applicationName = "test-app",
                        loggerName = "test-logger",
                        loggType = "audit-test",
                    )
                val navAuditLog = NavAuditLog(auditLogger, config)

                val cefMessageSlot = slot<CefMessage>()
                every { auditLogger.log(capture(cefMessageSlot)) } answers { callOriginal() }

                val message =
                    AuditLog.Message(
                        sourceUserId = "Z123456",
                        destinationUserId = "12345678901",
                        event = AuditLog.EventType.ACCESS,
                        message = "Accessed user data",
                    )

                navAuditLog.info(message)

                verify(exactly = 1) { auditLogger.log(any<CefMessage>()) }

                val capturedMessage = cefMessageSlot.captured
                val formattedMessage = capturedMessage.toString()

                formattedMessage shouldContain "CEF:0"
                formattedMessage shouldContain "test-app"
                formattedMessage shouldContain "test-logger"
                formattedMessage shouldContain "audit-test"
                formattedMessage shouldContain "suid=Z123456"
                formattedMessage shouldContain "duid=12345678901"
                formattedMessage shouldContain "msg=Accessed user data"
                formattedMessage shouldContain "end="
            }
        }
    })
