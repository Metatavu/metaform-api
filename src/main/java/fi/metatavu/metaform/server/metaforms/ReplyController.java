package fi.metatavu.metaform.server.metaforms;

import java.util.List;
import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;

import fi.metatavu.metaform.server.persistence.dao.AnyReplyFieldDAO;
import fi.metatavu.metaform.server.persistence.dao.BooleanReplyFieldDAO;
import fi.metatavu.metaform.server.persistence.dao.NumberReplyFieldDAO;
import fi.metatavu.metaform.server.persistence.dao.ReplyDAO;
import fi.metatavu.metaform.server.persistence.dao.StringReplyFieldDAO;
import fi.metatavu.metaform.server.persistence.model.BooleanReplyField;
import fi.metatavu.metaform.server.persistence.model.Metaform;
import fi.metatavu.metaform.server.persistence.model.NumberReplyField;
import fi.metatavu.metaform.server.persistence.model.Reply;
import fi.metatavu.metaform.server.persistence.model.ReplyField;
import fi.metatavu.metaform.server.persistence.model.StringReplyField;

/**
 * Reply controller
 * 
 * @author Antti Leppä
 */
@ApplicationScoped
public class ReplyController {

  @Inject
  private Logger logger;
 
  @Inject
  private ReplyDAO replyDAO;

  @Inject
  private AnyReplyFieldDAO anyReplyFieldDAO;

  @Inject
  private StringReplyFieldDAO stringReplyFieldDAO;

  @Inject
  private BooleanReplyFieldDAO booleanReplyFieldDAO;

  @Inject
  private NumberReplyFieldDAO numberReplyFieldDAO;
  
  /**
   * Creates new reply
   * 
   * @param userId user id 
   * @param metaform Metaform
   * @return Created reply
   */
  public Reply createReply(UUID userId, Metaform metaform) {
    UUID id = UUID.randomUUID();
    return replyDAO.create(id, userId, metaform);
  }

  /**
   * Lists field names used in reply
   * 
   * @param reply reply
   * @return field names used in reply
   */
  public List<String> listFieldNames(Reply reply) {
    return anyReplyFieldDAO.listNamesByReply(reply);
  }

  /**
   * Finds reply by id
   * 
   * @param replyId replyId
   * @return reply
   */
  public Reply findReplyById(UUID replyId) {
    return replyDAO.findById(replyId);
  }

  /**
   * Sets reply field value
   * 
   * @param reply reply
   * @param name name 
   * @param value value
   * @return updated field
   */
  public ReplyField setReplyField(Reply reply, String name, Object value) {
    if (value instanceof Boolean) {
      return setReplyField(reply, name, (Boolean) value);
    } else if (value instanceof Number) {
      return setReplyField(reply, name, (Number) value);
    } else if (value instanceof String) {
      return setReplyField(reply, name, (String) value);
    } else {
      logger.error(String.format("Unsupported to type (%s) for field %s in reply %s", value.getClass().getName(), name, reply.getId().toString()));
    }
    
    return null;
  }

  /**
   * Sets reply field value
   * 
   * @param reply reply
   * @param name name 
   * @param value value
   * @return updated field
   */
  public ReplyField setReplyField(Reply reply, String name, String value) {
    ReplyField replyField = anyReplyFieldDAO.findByReplyAndName(reply, name);
    if (replyField != null && !(replyField instanceof StringReplyField)) {
      anyReplyFieldDAO.delete(replyField);
      replyField = null;
    }
    
    if (replyField == null) {
      return stringReplyFieldDAO.create(UUID.randomUUID(), reply, name, value);
    } else {
      return stringReplyFieldDAO.updateValue((StringReplyField) replyField, value);
    }
  }

  /**
   * Sets reply field value
   * 
   * @param reply reply
   * @param name name 
   * @param value value
   * @return updated field
   */
  public ReplyField setReplyField(Reply reply, String name, Boolean value) {
    ReplyField replyField = anyReplyFieldDAO.findByReplyAndName(reply, name);
    if (replyField != null && !(replyField instanceof BooleanReplyField)) {
      anyReplyFieldDAO.delete(replyField);
      replyField = null;
    }
    
    if (replyField == null) {
      return booleanReplyFieldDAO.create(UUID.randomUUID(), reply, name, value);
    } else {
      return booleanReplyFieldDAO.updateValue((BooleanReplyField) replyField, value);
    }
  }

  /**
   * Sets reply field value
   * 
   * @param reply reply
   * @param name name 
   * @param value value
   * @return updated field
   */
  public ReplyField setReplyField(Reply reply, String name, Number value) {
    ReplyField replyField = anyReplyFieldDAO.findByReplyAndName(reply, name);
    if (replyField != null && !(replyField instanceof NumberReplyField)) {
      anyReplyFieldDAO.delete(replyField);
      replyField = null;
    }
    
    if (replyField == null) {
      return numberReplyFieldDAO.create(UUID.randomUUID(), reply, name, value.doubleValue());
    } else {
      return numberReplyFieldDAO.updateValue((NumberReplyField) replyField, value.doubleValue());
    }
  }

  /**
   * Deletes specified fields from the reply
   * 
   * @param reply reply
   * @param fieldNames field names to be deleted
   */
  public void deleteReplyFields(Reply reply, List<String> fieldNames) {
    for (String fieldName : fieldNames) {
      ReplyField replyField = anyReplyFieldDAO.findByReplyAndName(reply, fieldName);
      if (replyField != null) {
        anyReplyFieldDAO.delete(replyField);
      }
    }
  }

  /**
   * Lists replies by Metaform
   * 
   * @param metaform Metaform
   * @return replies
   */
  public List<Reply> listReplies(Metaform metaform) {
    return replyDAO.listByMetaform(metaform);
  }

  /**
   * Lists reply fields by reply
   * 
   * @param reply reply
   * @return reply fields
   */
  public List<ReplyField> listReplyFields(Reply reply) {
    return anyReplyFieldDAO.listByReply(reply);
  }
  
}
