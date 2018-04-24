package fi.metatavu.metaform.server.persistence.dao;

import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;

import fi.metatavu.metaform.server.persistence.model.NumberReplyField;
import fi.metatavu.metaform.server.persistence.model.Reply;

/**
 * DAO class for NumberReplyField entity
 * 
 * @author Antti Leppä
 */
@ApplicationScoped
public class NumberReplyFieldDAO extends ReplyFieldDAO<NumberReplyField> {

  /**
   * Creates new NumberReplyField 
   * 
   * @param id id
   * @param reply reply
   * @param name field name
   * @param value field value
   * @return created field
   */
  public NumberReplyField create(UUID id, Reply reply, String name, Double value) {
    NumberReplyField replyField = new NumberReplyField(); 
    replyField.setId(id);
    replyField.setName(name);
    replyField.setReply(reply);
    replyField.setValue(value);
    return persist(replyField);
  }

  /**
   * Updates reply field 
   * 
   * @param replyField reply field
   * @param value value
   * @return updated field
   */
  public NumberReplyField updateValue(NumberReplyField replyField, Double value) {
    replyField.setValue(value);
    return persist(replyField);
  }
  
}
