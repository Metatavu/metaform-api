package fi.metatavu.metaform.server.logentry;

import fi.metatavu.metaform.client.model.AuditLogEntryType;
import fi.metatavu.metaform.server.persistence.dao.AuditLogEntryDAO;
import fi.metatavu.metaform.server.persistence.model.AuditLogEntry;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Controller for audit logs
 *
 * @author Katja Danilova
 */
@ApplicationScoped
public class AuditLogEntryController {
    @Inject
    private AuditLogEntryDAO auditLogEntryDAO;

    /**
     * Create AuditLogEntry and fill the missing fields (generate uuid, fill current time)
     * @param userId userId
     * @param replyId replyId
     * @param attachmentId attachmentId
     * @param message message
     * @return AuditLogEntry created entry
     */
    public AuditLogEntry createAuditLogEntry(UUID userId, AuditLogEntryType type, UUID replyId,
                                             UUID attachmentId, String message){
        return auditLogEntryDAO.create(UUID.randomUUID(), userId, OffsetDateTime.now(), type, replyId, attachmentId, message);
    }

    /**
     * List audit log entries by replies, userId, createdBefore and createdAfter
     * @param replyIds filter results by list of corresponding replies
     * @param userId filter results by userId
     * @param createdBefore filter results before createdBefore
     * @param createdAfter filter results after createdAfter
     * @return list of AuditLogEntries
     */
    public List<AuditLogEntry> listAuditLogEntries(List<UUID> replyIds, UUID userId, OffsetDateTime createdBefore, OffsetDateTime createdAfter) {
    	if (replyIds.isEmpty())
				return Collections.emptyList();
			return auditLogEntryDAO.listAuditLogEntries(replyIds, userId, createdBefore, createdAfter);
    }

	/**
	 * deletes audit log entry
	 * @param auditLogEntry audit log entry
	 */
	public void deleteAuditLogEntry(AuditLogEntry auditLogEntry){
    	auditLogEntryDAO.delete(auditLogEntry);
		}

	/**
	 * finds audit log entry by id
 	 * @param auditLogEntryId	audit log id
	 * @return audit log entry
	 */
	public AuditLogEntry findAuditLogEntryById(UUID auditLogEntryId) {
		return auditLogEntryDAO.findById(auditLogEntryId);
	}
}
