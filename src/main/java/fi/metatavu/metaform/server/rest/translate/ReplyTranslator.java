package fi.metatavu.metaform.server.rest.translate;


import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;

import fi.metatavu.metaform.server.metaforms.ReplyController;
import fi.metatavu.metaform.server.persistence.model.BooleanReplyField;
import fi.metatavu.metaform.server.persistence.model.NumberReplyField;
import fi.metatavu.metaform.server.persistence.model.StringReplyField;
import fi.metatavu.metaform.server.rest.model.Metafield;
import fi.metatavu.metaform.server.rest.model.Metaform;
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
   * @param metaformEntity Metaform entity
   * @param reply JPA reply object
   * @return REST reply
   */
  public Reply translateReply(Metaform metaformEntity, fi.metatavu.metaform.server.persistence.model.Reply entity) {
    if (entity == null) {
      return null;
    }
    
    ReplyData replyData = new ReplyData();
    replyController.listReplyFields(entity).forEach(field -> {
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
    
    if (metaformEntity.getMetafields() != null) {
      metaformEntity.getMetafields().forEach((metafield) -> {
        resolveMetafield(replyData, metafield, entity);
      });
    }
    
    Reply reply = new Reply();
    reply.setId(entity.getId());
    reply.setData(replyData);
    reply.setUserId(entity.getUserId());
    reply.setRevision(entity.getRevision());
    
    return reply;
  }
  
  private void resolveMetafield(ReplyData replyData, Metafield metafield, fi.metatavu.metaform.server.persistence.model.Reply entity) {
    switch (metafield.getName()) {
      case "lastEditor":
        replyData.put(metafield.getName(), entity.getUserId());
      break;
      case "created":
        replyData.put(metafield.getName(), formatDateTime(entity.getCreatedAt()));
      break;
      case "modified":
        replyData.put(metafield.getName(), formatDateTime(entity.getModifiedAt()));
      break;
      default:
        logger.warn("Metafield {} not recognized", metafield.getName());
      break;
    }
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
  
  /**
   * Formats date time in ISO date-time format
   * 
   * @param dateTime date time
   * @return date time in ISO date-time format
   */
  private String formatDateTime(OffsetDateTime dateTime) {
    if (dateTime == null) {
      return null;
    }
    
    return dateTime.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
  }
  
}
