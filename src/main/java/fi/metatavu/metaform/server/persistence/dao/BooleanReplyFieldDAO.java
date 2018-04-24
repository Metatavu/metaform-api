package fi.metatavu.metaform.server.persistence.dao;

import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;

import fi.metatavu.metaform.server.persistence.model.BooleanReplyField;
import fi.metatavu.metaform.server.persistence.model.Reply;

/**
 * DAO class for BooleanReplyField entity
 * 
 * @author Antti Leppä
 */
@ApplicationScoped
public class BooleanReplyFieldDAO extends ReplyFieldDAO<BooleanReplyField> {

  /**
   * Creates new BooleanReplyField 
   * 
   * @param id id
   * @param reply reply
   * @param name field name
   * @param value field value
   * @return created field
   */
  public BooleanReplyField create(UUID id, Reply reply, String name, Boolean value) {
    BooleanReplyField replyField = new BooleanReplyField(); 
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
  public BooleanReplyField updateValue(BooleanReplyField replyField, Boolean value) {
    replyField.setValue(value);
    return persist(replyField);
  }
  
}
