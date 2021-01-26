package fi.metatavu.metaform.server.rest;

import java.util.UUID;

import javax.ejb.Stateful;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.core.Response;

import _fi.metatavu.metaform.server.rest.api.AttachmentsApi;
import fi.metatavu.metaform.server.attachments.AttachmentController;
import fi.metatavu.metaform.server.logentry.AuditLogEntryController;
import fi.metatavu.metaform.server.metaforms.ReplyController;
import fi.metatavu.metaform.server.persistence.model.Attachment;
import fi.metatavu.metaform.server.persistence.model.Reply;
import fi.metatavu.metaform.server.rest.translate.AttachmentTranslator;

/**
 * Attachments REST Service implementation
 * 
 * @author Antti Leppä
 */
@RequestScoped
@Stateful
public class AttachmentsApiImpl extends AbstractApi implements AttachmentsApi {
  
  private static final String ANONYMOUS_USERS_MESSAGE = "Anonymous users are not allowed on this Metaform";

  @Inject
  private AttachmentController attachmentController;

  @Inject
  private AttachmentTranslator attachmentTranslator;

  @Inject
  private ReplyController replyController;
  
  @Override
  public Response findAttachment(UUID attachmentId, String ownerKey) {
    Attachment attachment = attachmentController.findAttachmentById(attachmentId);
    if (attachment == null) {
      return createNotFound(NOT_FOUND_MESSAGE);
    }
    
    if (!isPermittedAttachment(attachment, ownerKey)) {
      return createForbidden(ANONYMOUS_USERS_MESSAGE);
    }

    return createOk(attachmentTranslator.translateAttachment(attachment));
  }

  @Override
  public Response findAttachmentData(UUID attachmentId, String ownerKey) {
    Attachment attachment = attachmentController.findAttachmentById(attachmentId);
    if (attachment == null) {
      return createNotFound(NOT_FOUND_MESSAGE);
    }

    if (!isPermittedAttachment(attachment, ownerKey)) {
      return createForbidden(ANONYMOUS_USERS_MESSAGE);
    }

    return streamResponse(attachment.getContent(), attachment.getContentType());
  }

  /**
   * Returns whether given attachment is permitted
   * 
   * @param attachment attachment
   * @param ownerKey reply owner key
   * @return whether given attachment is permitted
   */
  private boolean isPermittedAttachment(Attachment attachment, String ownerKey) {
    if (isRealmMetaformAdmin() || isRealmMetaformSuper() || isRealmUser()) {
      return true;
    }
    
    Reply reply = attachmentController.findReplyByAttachment(attachment);
    if (reply == null || reply.getResourceId() == null) {
      return false;
    }

    return replyController.isValidOwnerKey(reply, ownerKey);
  }
}
