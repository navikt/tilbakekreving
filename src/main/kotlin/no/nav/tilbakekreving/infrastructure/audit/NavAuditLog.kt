package no.nav.tilbakekreving.infrastructure.audit

import no.nav.common.audit_log.cef.CefMessage
import no.nav.common.audit_log.cef.CefMessageEvent
import no.nav.common.audit_log.cef.CefMessageSeverity
import no.nav.common.audit_log.log.AuditLogger
import no.nav.common.audit_log.log.AuditLoggerImpl

class NavAuditLog(
    private val auditLogger: AuditLogger = AuditLoggerImpl(),
    private val config: AuditLog.Config,
) : AuditLog {
    override fun info(message: AuditLog.Message) {
        val cefMessage =
            CefMessage
                .builder()
                .applicationName(config.applicationName) // Device Vendor
                .loggerName(config.loggerName) // Device Product
                .name(config.loggType) // Name
                .event(message.event.toCefMessageEvent()) // Create, Access, Update, Delete
                .sourceUserId(message.sourceUserId) // suid
                .destinationUserId(message.destinationUserId) // duid
                .timeEnded(System.currentTimeMillis()) // end
                .severity(CefMessageSeverity.INFO)
                .extension("msg", message.message) // extension, msg i dette tilfellet
                .build()

        auditLogger.log(cefMessage)
    }

    private fun AuditLog.EventType.toCefMessageEvent(): CefMessageEvent =
        when (this) {
            AuditLog.EventType.CREATE -> CefMessageEvent.CREATE
            AuditLog.EventType.ACCESS -> CefMessageEvent.ACCESS
            AuditLog.EventType.UPDATE -> CefMessageEvent.UPDATE
            AuditLog.EventType.DELETE -> CefMessageEvent.DELETE
        }
}
