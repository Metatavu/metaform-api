package fi.metatavu.metaform.server.persistence.dao;

import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;

import fi.metatavu.metaform.server.persistence.model.StringReplyField;
import fi.metatavu.metaform.server.persistence.model.Reply;

/**
 * DAO class for StringReplyField entity
 * 
 * @author Antti Leppä
 */
@ApplicationScoped
public class StringReplyFieldDAO extends ReplyFieldDAO<StringReplyField> {
  
  /**
   * Creates new StringReplyField 
   * 
   * @param id id
   * @param reply reply
   * @param name field name
   * @param value field value
   * @return created field
   */
  public StringReplyField create(UUID id, Reply reply, String name, String value) {
    StringReplyField replyField = new StringReplyField(); 
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
  public StringReplyField updateValue(StringReplyField replyField, String value) {
    replyField.setValue(value);
    return persist(replyField);
  }
  
}
