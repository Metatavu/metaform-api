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
	 * Creates AuditLogEntry and fill the missing fields (generate uuid, fill current time)
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
	 * Lists audit log entries by replies, userId, createdBefore and createdAfter
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
	 * Deletes audit log entry
	 *
	 * @param auditLogEntry auditLogEntry
	 */
	public void deleteAuditLogEntry(AuditLogEntry auditLogEntry){
		auditLogEntryDAO.delete(auditLogEntry);
	}

	/**
	 * Finds audit log entry by id
	 *
 	 * @param auditLogEntryId	auditLogEntryId
	 * @return audit log entry
	 */
	public AuditLogEntry findAuditLogEntryById(UUID auditLogEntryId) {
		return auditLogEntryDAO.findById(auditLogEntryId);
	}

	/**
	 * Creates and saves audit log based on the parameters
	 *
	 * @param metaform metaform
	 * @param userId userId
	 * @param replyId replyId
	 * @param attachmentId attachmentId
	 * @param type  logEntryType
	 */
	public AuditLogEntry generateAuditLog(fi.metatavu.metaform.server.persistence.model.Metaform metaform, UUID userId, UUID replyId, UUID attachmentId, String action, AuditLogEntryType type){
		String defaction = "";
		switch (type) {
			case DELETE_REPLY:
				defaction = "deleted reply";
				break;
			case CREATE_REPLY:
				defaction = "created reply";
				break;
			case MODIFY_REPLY:
				defaction = "modified reply";
				break;
			case LIST_REPLY:
				defaction = "listed reply";
				break;
			case VIEW_REPLY:
				defaction = "viewed reply";
				break;
			case VIEW_REPLY_ATTACHMENT:
				defaction = "viewed attachment of reply ";
				break;
			case DOWNLOAD_REPLY_ATTACHMENT:
				defaction = "downloaded attachment of reply ";
				break;
			case EXPORT_REPLY_PDF:
				defaction = "exported to pdf ";
				break;
			case EXPORT_REPLY_XLSX:
				defaction = "exported to xlsx";
				break;
		}

		return createAuditLogEntry(metaform, userId, type, replyId, attachmentId, action != null ? action : String.format("user %1$s %2$s %3$s", userId.toString(), defaction, replyId.toString()));
	}
}
