package fi.metatavu.metaform.server.rest

import fi.metatavu.metaform.api.spec.model.AuditLogEntryType
import fi.metatavu.metaform.server.controllers.AttachmentController
import fi.metatavu.metaform.server.persistence.model.Attachment
import fi.metatavu.metaform.server.rest.translate.AttachmentTranslator
import java.util.*
import javax.enterprise.context.RequestScoped
import javax.inject.Inject
import javax.transaction.Transactional
import javax.ws.rs.core.Response

@RequestScoped
@Transactional
class AttachmentsApi: fi.metatavu.metaform.api.spec.AttachmentsApi, AbstractApi() {

  @Inject
  lateinit var attachmentController: AttachmentController

  @Inject
  lateinit var attachmentTranslator: AttachmentTranslator

  /**
   * Find attachment by id
   * @param attachmentId attachment id
   * @param ownerKey owner key
   * 
   * @return attachment
   */
  override suspend fun findAttachment(attachmentId: UUID, ownerKey: String?): Response {
    val userId = loggedUserId ?: return createForbidden(UNAUTHORIZED)

    val attachment: Attachment = attachmentController.findAttachmentById(attachmentId)
            ?: return createNotFound(createNotFoundMessage(ATTACHMENT, attachmentId))

    if (!isPermittedAttachment(attachment, ownerKey)) {
      return createForbidden(createAnonNotAllowedMessage(FIND, ATTACHMENT))
    }

    attachmentController.logAttachmentAccess(attachment, null, AuditLogEntryType.DOWNLOAD_REPLY_ATTACHMENT, userId)

    return createOk(attachmentTranslator.translate(attachment))
  }

  /**
   * Returns whether given attachment is permitted
   *
   * @param attachment attachment
   * @param ownerKey reply owner key
   * @return whether given attachment is permitted
   */
  private fun isPermittedAttachment(attachment: Attachment, ownerKey: String?): Boolean {
    if (isRealmMetaformAdmin || isRealmMetaformSuper || isRealmUser) {
      return true
    }
    val reply = attachmentController.findReplyByAttachment(attachment)
    return if (reply?.resourceId == null) {
      false
    } else replyController.isValidOwnerKey(reply, ownerKey)
  }

  /**
   * Find attachment data by id
   * @param attachmentId attachment id
   * @param ownerKey owner key
   * 
   * @return attachment data
   */
  override suspend fun findAttachmentData(attachmentId: UUID, ownerKey: String?): Response {
    val userId = loggedUserId ?: return createForbidden(UNAUTHORIZED)
    val attachment: Attachment = attachmentController.findAttachmentById(attachmentId)
            ?: return createNotFound(createNotFoundMessage(ATTACHMENT, attachmentId))

    if (!isPermittedAttachment(attachment, ownerKey)) {
      return createForbidden(createAnonNotAllowedMessage(FIND, ATTACHMENT))
    }

    attachmentController.logAttachmentAccess(attachment, null, AuditLogEntryType.VIEW_REPLY_ATTACHMENT, userId)

    return streamResponse(attachment.content, attachment.contentType)

  }
}