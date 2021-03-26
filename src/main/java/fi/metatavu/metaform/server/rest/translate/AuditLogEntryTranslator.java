package fi.metatavu.metaform.server.rest.translate;


import fi.metatavu.metaform.api.spec.model.AuditLogEntryType;
import fi.metatavu.metaform.server.persistence.model.AuditLogEntry;

import javax.enterprise.context.ApplicationScoped;

/**
 * Translator for audit log entries
 *
 * @author Katja Danilova
 */
@ApplicationScoped
public class AuditLogEntryTranslator {

  /**
   * Translates into REST AuditLogEntry
   *
   * @param auditLogEntry JPA auditLogEntry
   * @return REST AuditLogEntry
   */
  public fi.metatavu.metaform.api.spec.model.AuditLogEntry translateAuditLogEntry(AuditLogEntry auditLogEntry) {
    if (auditLogEntry == null) {
      return null;
    }

    fi.metatavu.metaform.api.spec.model.AuditLogEntry result = new fi.metatavu.metaform.api.spec.model.AuditLogEntry();
    result.setCreatedAt(auditLogEntry.getCreatedAt());
    result.setId(auditLogEntry.getId());
    result.setMessage(auditLogEntry.getMessage());
    result.setUserId(auditLogEntry.getUserId());
    result.setReplyId(auditLogEntry.getReplyId());
    AuditLogEntryType originalLogEntryType = auditLogEntry.getLogEntryType();
    result.setLogEntryType(AuditLogEntryType.fromValue(originalLogEntryType.toString()));
    return result;
  }
}
