package fi.metatavu.metaform.server.logentry;

import fi.metatavu.metaform.client.model.AuditLogEntryType;
import fi.metatavu.metaform.server.persistence.dao.AuditLogEntryDAO;
import fi.metatavu.metaform.server.persistence.model.AuditLogEntry;
import fi.metatavu.metaform.server.persistence.model.Reply;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class AuditLogEntryController {

    @Inject
    private AuditLogEntryDAO auditLogEntryDAO;


    /**
     * Create AuditLogEntry and fille the missing fields (generate uuid, fill current time)
     * @param userId
     * @param replyId
     * @param attachmentId
     * @param message
     * @return
     */
    public AuditLogEntry createAuditLogEntry(UUID userId, AuditLogEntryType type, UUID replyId,
                                             UUID attachmentId, String message){
        AuditLogEntry auditLogEntry = auditLogEntryDAO.create(UUID.randomUUID(), userId, OffsetDateTime.now(), type, replyId, attachmentId, message);
        return auditLogEntry;
    }

    /**
     * List all audit log entries
     * @return
     */
    public List<AuditLogEntry> listAuditLogEntries(List<Reply> replies){
        return auditLogEntryDAO.listAuditLogEntriesByReplyId(replies);
    }

}
