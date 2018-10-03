package fi.metatavu.metaform.server.persistence.dao;

import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;

import fi.metatavu.metaform.server.persistence.model.TableReplyField;
import fi.metatavu.metaform.server.persistence.model.Reply;

/**
 * DAO class for TableReplyField entity
 * 
 * @author Antti Leppä
 */
@ApplicationScoped
public class TableReplyFieldDAO extends ReplyFieldDAO<TableReplyField> {
  
  /**
   * Creates new TableReplyField 
   * 
   * @param id id
   * @param reply reply
   * @param name field name
   * @param value field value
   * @return created field
   */
  public TableReplyField create(UUID id, Reply reply, String name) {
    TableReplyField replyField = new TableReplyField(); 
    replyField.setId(id);
    replyField.setName(name);
    replyField.setReply(reply);
    return persist(replyField);
  }
  
}
