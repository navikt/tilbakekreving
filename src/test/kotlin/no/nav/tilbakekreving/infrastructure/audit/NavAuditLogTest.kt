package no.nav.tilbakekreving.infrastructure.audit

import io.kotest.core.spec.style.WordSpec
import io.kotest.matchers.equals.shouldBeEqual
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
                        applicationName = "Tilbakekreving",
                        loggerName = "Auditlog",
                        loggType = "Sporingslogg",
                    )
                val navAuditLog = NavAuditLog(auditLogger, config)

                val cefMessageSlot = slot<CefMessage>()
                every { auditLogger.log(capture(cefMessageSlot)) } answers { callOriginal() }

                val timestamp = System.currentTimeMillis()
                val message =
                    AuditLog.Message(
                        sourceUserId = "Z123456",
                        destinationUserId = "12345678901",
                        event = AuditLog.EventType.ACCESS,
                        message = "Hentet kravdetaljer for innkrevingskrav",
                        timestamp = timestamp,
                        firstAttribute =
                            AuditLog.AttributeLabel("Nav-kravidentifikator") to
                                AuditLog.AttributeValue("K123456"),
                    )

                navAuditLog.info(message)

                verify(exactly = 1) { auditLogger.log(any<CefMessage>()) }

                cefMessageSlot.captured.toString() shouldBeEqual
                    "CEF:0|Tilbakekreving|Auditlog|1.0|audit:access|Sporingslogg|INFO|msg=Hentet kravdetaljer for innkrevingskrav flexString1=K123456 duid=12345678901 end=$timestamp flexString1Label=Nav-kravidentifikator suid=Z123456"
            }
        }
    })
