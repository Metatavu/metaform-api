package fi.metatavu.metaform.server.rest.translate;

import fi.metatavu.metaform.server.persistence.model.AuditLogEntry;
import fi.metatavu.metaform.server.rest.model.AuditLogEntryType;

import javax.enterprise.context.ApplicationScoped;

/**
 * Translator for replies
 *
 * @author Antti Leppä
 */
@ApplicationScoped
public class AuditLogEntryTranslator {

    public fi.metatavu.metaform.server.rest.model.AuditLogEntry translateAuditLogEntry(AuditLogEntry auditLogEntry) {
        if (auditLogEntry == null) {
            return null;
        }

        fi.metatavu.metaform.server.rest.model.AuditLogEntry result = new fi.metatavu.metaform.server.rest.model.AuditLogEntry();
        result.setTime(auditLogEntry.getTime());
        result.setId(auditLogEntry.getId());
        result.setMessage(auditLogEntry.getMessage());
        result.setUserId(auditLogEntry.getUserId());
        result.setReplyId(auditLogEntry.getReplyId());
        result.setLogEntryType(AuditLogEntryType.fromValue(auditLogEntry.getLogEntryType().getValue()));
        return result;
    }
}
