package fi.metatavu.metaform.server.persistence.dao;

import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;

import fi.metatavu.metaform.server.persistence.model.AttachmentReplyField;
import fi.metatavu.metaform.server.persistence.model.Reply;

/**
 * DAO class for AttachmentReplyField entity
 * 
 * @author Antti Leppä
 */
@ApplicationScoped
public class AttachmentReplyFieldDAO extends ReplyFieldDAO<AttachmentReplyField> {
  
  /**
   * Creates new AttachmentReplyField 
   * 
   * @param id id
   * @param reply reply
   * @param name field name
   * @param value field value
   * @return created field
   */
  public AttachmentReplyField create(UUID id, Reply reply, String name) {
    AttachmentReplyField replyField = new AttachmentReplyField(); 
    replyField.setId(id);
    replyField.setName(name);
    replyField.setReply(reply);
    return persist(replyField);
  }
  
}
