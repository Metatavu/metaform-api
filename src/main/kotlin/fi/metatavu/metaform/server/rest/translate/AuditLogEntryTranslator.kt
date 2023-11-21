package fi.metatavu.metaform.server.rest.translate

import fi.metatavu.metaform.server.persistence.model.AuditLogEntry
import jakarta.enterprise.context.ApplicationScoped

/**
 * Translator for audit log entries
 *
 * @author Katja Danilova
 */
@ApplicationScoped
class AuditLogEntryTranslator {
  /**
   * Translates into REST AuditLogEntry
   *
   * @param auditLogEntry JPA auditLogEntry
   * @return REST AuditLogEntry
   */
  fun translate(auditLogEntry: AuditLogEntry): fi.metatavu.metaform.api.spec.model.AuditLogEntry {
    return fi.metatavu.metaform.api.spec.model.AuditLogEntry(
      id = auditLogEntry.id,
      message = auditLogEntry.message,
      userId = auditLogEntry.userId,
      replyId = auditLogEntry.replyId,
      logEntryType = auditLogEntry.logEntryType,
      createdAt = auditLogEntry.createdAt
    )
  }
}