package fi.metatavu.metaform.server.rest;

import java.util.UUID;

import javax.ejb.Stateful;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.core.Response;

import fi.metatavu.metaform.server.attachments.AttachmentController;
import fi.metatavu.metaform.server.persistence.model.Attachment;
import fi.metatavu.metaform.server.rest.translate.AttachmentTranslator;

/**
 * Attachments REST Service implementation
 * 
 * @author Antti Leppä
 */
@RequestScoped
@Stateful
public class AttachmentsApiImpl extends AbstractApi implements AttachmentsApi {
  
  @Inject
  private AttachmentController attachmentController;

  @Inject
  private AttachmentTranslator attachmentTranslator;

  @Override
  public Response findAttachment(UUID attachmentId) throws Exception {
    Attachment attachment = attachmentController.findAttachmentById(attachmentId);
    if (attachment == null) {
      return createNotFound(NOT_FOUND_MESSAGE);
    }

    return createOk(attachmentTranslator.translateAttachment(attachment));
  }

  @Override
  public Response findAttachmentData(UUID attachmentId) throws Exception {
    Attachment attachment = attachmentController.findAttachmentById(attachmentId);
    if (attachment == null) {
      return createNotFound(NOT_FOUND_MESSAGE);
    }

    return streamResponse(attachment.getContent(), attachment.getContentType());
  }

}
