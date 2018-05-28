package fi.metatavu.metaform.server.persistence.dao;

import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;

import fi.metatavu.metaform.server.persistence.model.ListReplyField;
import fi.metatavu.metaform.server.persistence.model.Reply;

/**
 * DAO class for ListReplyField entity
 * 
 * @author Antti Leppä
 */
@ApplicationScoped
public class ListReplyFieldDAO extends ReplyFieldDAO<ListReplyField> {
  
  /**
   * Creates new ListReplyField 
   * 
   * @param id id
   * @param reply reply
   * @param name field name
   * @param value field value
   * @return created field
   */
  public ListReplyField create(UUID id, Reply reply, String name) {
    ListReplyField replyField = new ListReplyField(); 
    replyField.setId(id);
    replyField.setName(name);
    replyField.setReply(reply);
    return persist(replyField);
  }
  
}
