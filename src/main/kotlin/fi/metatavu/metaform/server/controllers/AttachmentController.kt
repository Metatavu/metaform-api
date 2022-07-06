package fi.metatavu.metaform.server.controllers

import fi.metatavu.metaform.api.spec.model.AuditLogEntryType
import fi.metatavu.metaform.server.persistence.dao.AttachmentDAO
import fi.metatavu.metaform.server.persistence.dao.AttachmentReplyFieldItemDAO
import fi.metatavu.metaform.server.persistence.model.Attachment
import fi.metatavu.metaform.server.persistence.model.Reply
import java.util.*
import javax.enterprise.context.ApplicationScoped
import javax.inject.Inject

/**
 * Controller for Attachments
 */
@ApplicationScoped
class AttachmentController {

    @Inject
    lateinit var attachmentDAO: AttachmentDAO

    @Inject
    lateinit var attachmentReplyFieldItemDAO: AttachmentReplyFieldItemDAO

    @Inject
    lateinit var auditLogEntryController: AuditLogEntryController

    /**
     * Creates new attachment
     *
     * @param name name
     * @param content content
     * @param contentType contentType
     * @param userId userId
     * @return created attachment
     */
    fun create(
        id: UUID,
        name: String,
        content: ByteArray,
        contentType: String,
        userId: UUID
    ): Attachment {
        return attachmentDAO.create(
            id,
            name,
            content,
            contentType,
            userId
        )
    }

    /**
     * Finds attachment by id
     *
     * @param attachmentId attachment id
     * @return
     */
    fun findAttachmentById(attachmentId: UUID): Attachment? {
        return attachmentDAO.findById(attachmentId)
    }

    /**
     * Finds reply by attachment
     *
     * @param attachment attachment to find reply for
     * @return reply
     */
    fun findReplyByAttachment(attachment: Attachment): Reply? {
        val item = attachmentReplyFieldItemDAO.findByAttachment(attachment)
        return item?.field?.reply
    }

    /**
     * Update attachment
     *
     * @param attachment attachment
     * @param name name
     * @param content content
     * @param contentType contentType
     * @return updated attachment
     */
    fun updateAttachment(
        attachment: Attachment,
        name: String,
        content: ByteArray,
        contentType: String
    ): Attachment {
        attachmentDAO.updateName(attachment, name)
        attachmentDAO.updateContent(attachment, content)
        attachmentDAO.updateContentType(attachment, contentType)
        return attachment
    }

    /**
     * Deletes an attachment
     *
     * @param attachment attachment
     */
    fun deleteAttachment(attachment: Attachment) {
        attachmentDAO.delete(attachment)
    }

    /**
     * Creates audit log entry for attachment and saves it
     *
     * @param attachment attachment
     * @param action action
     * @param auditLogEntryType auditLogEntryType
     * @param userId logger user id
     */
    fun logAttachmentAccess(
        attachment: Attachment,
        action: String?,
        auditLogEntryType: AuditLogEntryType,
        userId: UUID
    ) {
        val replyByAttachment = findReplyByAttachment(attachment)
        if (replyByAttachment?.metaform == null) return
        auditLogEntryController.generateAuditLog(
            replyByAttachment.metaform,
            userId,
            replyByAttachment.id!!,
            attachment.id,
            action,
            auditLogEntryType
        )
    }
}