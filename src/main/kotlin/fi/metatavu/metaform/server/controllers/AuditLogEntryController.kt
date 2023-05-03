package fi.metatavu.metaform.server.controllers

import fi.metatavu.metaform.api.spec.model.AuditLogEntryType
import fi.metatavu.metaform.server.persistence.dao.AuditLogEntryDAO
import fi.metatavu.metaform.server.persistence.model.AuditLogEntry
import fi.metatavu.metaform.server.persistence.model.Metaform
import java.time.OffsetDateTime
import java.util.*
import javax.enterprise.context.ApplicationScoped
import javax.inject.Inject

/**
 * Controller for Audit logs
 */
@ApplicationScoped
class AuditLogEntryController {

    @Inject
    lateinit var auditLogEntryDAO: AuditLogEntryDAO

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
    fun createAuditLogEntry(
            metaform: Metaform,
            userId: UUID,
            type: AuditLogEntryType,
            replyId: UUID?,
            attachmentId: UUID?,
            message: String?
    ): AuditLogEntry {
        return auditLogEntryDAO.create(
            id = UUID.randomUUID(),
            metaform = metaform,
            userId = userId,
            auditLogEntryType = type,
            replyId = replyId,
            attachmentId = attachmentId,
            message = message
        )
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
    fun listAuditLogEntries(
        metaform: Metaform,
        replyId: UUID?,
        userId: UUID?,
        createdBefore: OffsetDateTime?,
        createdAfter: OffsetDateTime?
    ): List<AuditLogEntry> {
        return auditLogEntryDAO.list(metaform, replyId, userId, createdBefore, createdAfter)
    }

    /**
     * Deletes audit log entry
     *
     * @param auditLogEntry auditLogEntry
     */
    fun deleteAuditLogEntry(auditLogEntry: AuditLogEntry) {
        auditLogEntryDAO.delete(auditLogEntry)
    }

    /**
     * Finds audit log entry by id
     *
     * @param auditLogEntryId auditLogEntryId
     * @return audit log entry
     */
    fun findAuditLogEntryById(auditLogEntryId: UUID): AuditLogEntry? {
        return auditLogEntryDAO.findById(auditLogEntryId)
    }

    /**
     * Creates and saves audit log based on the parameters
     *
     * @param metaform metaform
     * @param userId userId
     * @param replyId replyId
     * @param attachmentId attachmentId
     * @param type logEntryType
     * @param loggedUserId logger user id
     */
    fun generateAuditLog(
            metaform: Metaform,
            userId: UUID,
            replyId: UUID,
            attachmentId: UUID?,
            action: String?,
            type: AuditLogEntryType
    ): AuditLogEntry {
        val defaction = when (type) {
            AuditLogEntryType.DELETE_REPLY -> "deleted reply"
            AuditLogEntryType.CREATE_REPLY -> "created reply"
            AuditLogEntryType.MODIFY_REPLY -> "modified reply"
            AuditLogEntryType.LIST_REPLY -> "listed reply"
            AuditLogEntryType.VIEW_REPLY -> "viewed reply"
            AuditLogEntryType.VIEW_REPLY_ATTACHMENT -> "viewed attachment of reply "
            AuditLogEntryType.DOWNLOAD_REPLY_ATTACHMENT -> "downloaded attachment of reply "
            AuditLogEntryType.EXPORT_REPLY_PDF -> "exported to pdf "
            AuditLogEntryType.EXPORT_REPLY_XLSX -> "exported to xlsx"
        }

        return createAuditLogEntry(
            metaform = metaform,
            userId = userId,
            type = type,
            replyId = replyId,
            attachmentId = attachmentId,
            message = action ?: String.format("user %1\$s %2\$s %3\$s", userId.toString(), defaction, replyId.toString())
        )
    }

}