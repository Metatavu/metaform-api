package fi.metatavu.metaform.server.rest.translate;


import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;

import fi.metatavu.metaform.server.metaforms.ReplyController;
import fi.metatavu.metaform.server.persistence.model.BooleanReplyField;
import fi.metatavu.metaform.server.persistence.model.NumberReplyField;
import fi.metatavu.metaform.server.persistence.model.StringReplyField;
import fi.metatavu.metaform.server.rest.model.Reply;
import fi.metatavu.metaform.server.rest.model.ReplyData;
import fi.metatavu.metaform.server.rest.model.ReplyMeta;

/**
 * Translator for replies
 * 
 * @author Antti Leppä
 */
@ApplicationScoped
public class ReplyTranslator {
  
  @Inject
  private Logger logger;

  @Inject
  private ReplyController replyController;

  /**
   * Translates JPA reply object into REST reply object
   * 
   * @param reply JPA reply object
   * @return REST reply
   */
  public Reply translateReply(fi.metatavu.metaform.server.persistence.model.Reply entity) {
    if (entity == null) {
      return null;
    }
    
    ReplyData replyData = new ReplyData();
    replyController.listReplyFields(entity).forEach((field) -> {
      String fieldName = field.getName();
      
      if (field instanceof NumberReplyField) {
        replyData.put(fieldName, ((NumberReplyField) field).getValue());
      } else if (field instanceof BooleanReplyField) {
        replyData.put(fieldName, ((BooleanReplyField) field).getValue());
      } else if (field instanceof StringReplyField) {
        replyData.put(fieldName, ((StringReplyField) field).getValue());
      } else {
        logger.error(String.format("Could not resolve %s", fieldName)); 
      }
    });
    
    Reply reply = new Reply();
    reply.setId(entity.getId());
    reply.setData(replyData);
    reply.setUserId(entity.getUserId());
    
    return reply;
  }
  
  /**
   * Translates JPA reply object into REST reply meta
   * 
   * @param reply JPA reply object
   * @return REST reply meta
   */
  public ReplyMeta translateReplyMeta(fi.metatavu.metaform.server.persistence.model.Reply reply) {
    if (reply == null) {
      return null;
    }
    
    ReplyMeta replyMeta = new ReplyMeta();
    replyMeta.setCreatedAt(reply.getCreatedAt());
    replyMeta.setModifiedAt(reply.getModifiedAt());
    
    return replyMeta;
  }
  
}
