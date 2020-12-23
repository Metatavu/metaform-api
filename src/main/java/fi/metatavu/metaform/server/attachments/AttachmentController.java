package fi.metatavu.metaform.server.attachments;

import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import fi.metatavu.metaform.server.persistence.dao.AttachmentDAO;
import fi.metatavu.metaform.server.persistence.dao.AttachmentReplyFieldItemDAO;
import fi.metatavu.metaform.server.persistence.model.Attachment;
import fi.metatavu.metaform.server.persistence.model.AttachmentReplyFieldItem;
import fi.metatavu.metaform.server.persistence.model.Reply;

/**
 * Controller for attachment related operations
 * 
 * @author Antti Leppä
 */
@ApplicationScoped
public class AttachmentController {

  @Inject
  private AttachmentDAO attachmentDAO;

  @Inject
  private AttachmentReplyFieldItemDAO attachmentReplyFieldItemDAO;

  /**
   * Creates new attachment
   *
   * @param name name
   * @param content content
   * @param contentType contentType
   * @param userId userId
   * @return created attachment
   */
  public Attachment create(UUID id, String name, byte[] content, String contentType, UUID userId) {
    return attachmentDAO.create(id, name, content, contentType, userId);
  }

  /**
    * Finds attachment by id
    * 
    * @param attachmentId attachment id
    * @return
    */
  public Attachment findAttachmentById(UUID attachmentId) {
    return attachmentDAO.findById(attachmentId);
  }

  /**
   * Finds reply by attachment
   * 
   * @param attachment attachment to find reply for
   * @return reply
   */
  public Reply findReplyByAttachment(Attachment attachment) {
    AttachmentReplyFieldItem item = attachmentReplyFieldItemDAO.findByAttachment(attachment);
    if (item == null) {
      return null;
    }
    return item.getField().getReply();
  }

  /**
   * Update attachment
   *
   * @param name name
   * @param content content
   * @param contentType contentType
   * @return updated attachment
   */
  public Attachment updateAttachment(Attachment attachment, String name, byte[] content, String contentType) {
    attachmentDAO.updateName(attachment, name);
    attachmentDAO.updateContent(attachment, content);
    attachmentDAO.updateContentType(attachment, contentType);
    return attachment;
  }
  
  /**
   * Deletes an attachment
   * 
   * @param attachment attachment
   */
  public void deleteAttachment(Attachment attachment) {
    attachmentDAO.delete(attachment);
  }

}
