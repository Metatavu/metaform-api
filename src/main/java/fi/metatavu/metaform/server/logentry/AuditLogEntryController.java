package fi.metatavu.metaform.server.logentry;

import fi.metatavu.metaform.client.model.AuditLogEntryType;
import fi.metatavu.metaform.server.persistence.dao.AuditLogEntryDAO;
import fi.metatavu.metaform.server.persistence.model.AuditLogEntry;
import fi.metatavu.metaform.server.persistence.model.Metaform;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.time.OffsetDateTime;
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
	 *Create AuditLogEntry and fill the missing fields (generate uuid, fill current time)
	 *
	 * @param metaform metaform
	 * @param userId userId
	 * @param replyId replyId
	 * @param attachmentId attachmentId
	 * @param message message
	 * @return created entry
	 */
	public AuditLogEntry createAuditLogEntry(Metaform metaform, UUID userId, AuditLogEntryType type, UUID replyId,
																					 UUID attachmentId, String message) {
		return auditLogEntryDAO.create(UUID.randomUUID(), metaform, userId, OffsetDateTime.now(), type, replyId, attachmentId, message);
	}

	/**
	 * List audit log entries by replies, userId, createdBefore and createdAfter
	 *
	 * @param metaform filter results by metaform
   * @param replyId (optional) filter results by list of corresponding replies
   * @param userId (optional) filter results by userId
   * @param createdBefore (optional) filter results before createdBefore
   * @param createdAfter (optional) filter results after createdAfter
   * @return list of AuditLogEntries
   */
	public List<AuditLogEntry> listAuditLogEntries(Metaform metaform, UUID replyId, UUID userId, OffsetDateTime createdBefore, OffsetDateTime createdAfter) {
		return auditLogEntryDAO.listAuditLogEntries(metaform, replyId, userId, createdBefore, createdAfter);
	}

	/**
	 * deletes audit log entry
	 *
	 * @param auditLogEntry auditLogEntry
	 */
	public void deleteAuditLogEntry(AuditLogEntry auditLogEntry){
		auditLogEntryDAO.delete(auditLogEntry);
	}

	/**
	 * finds audit log entry by id
	 *
 	 * @param auditLogEntryId	auditLogEntryId
	 * @return audit log entry
	 */
	public AuditLogEntry findAuditLogEntryById(UUID auditLogEntryId) {
		return auditLogEntryDAO.findById(auditLogEntryId);
	}
}
