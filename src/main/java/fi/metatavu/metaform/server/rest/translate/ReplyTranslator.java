package fi.metatavu.metaform.server.rest.translate;


import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;

import fi.metatavu.metaform.server.metaforms.ReplyController;
import fi.metatavu.metaform.server.persistence.model.BooleanReplyField;
import fi.metatavu.metaform.server.persistence.model.NumberReplyField;
import fi.metatavu.metaform.server.persistence.model.StringReplyField;
import fi.metatavu.metaform.server.rest.model.Metaform;
import fi.metatavu.metaform.server.rest.model.MetaformField;
import fi.metatavu.metaform.server.rest.model.MetaformSection;
import fi.metatavu.metaform.server.rest.model.Reply;
import fi.metatavu.metaform.server.rest.model.ReplyData;

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
      
      if (isMetafield(metaformEntity, fieldName)) {
        resolveMetaField(replyData, fieldName, entity);;
      } else {
        if (field instanceof NumberReplyField) {
          replyData.put(fieldName, ((NumberReplyField) field).getValue());
        } else if (field instanceof BooleanReplyField) {
          replyData.put(fieldName, ((BooleanReplyField) field).getValue());
        } else if (field instanceof StringReplyField) {
          replyData.put(fieldName, ((StringReplyField) field).getValue());
        } else {
          logger.error(String.format("Could not resolve %s", fieldName)); 
        }
      }
    });
    
    Reply reply = new Reply();
    reply.setId(entity.getId());
    reply.setData(replyData);
    reply.setUserId(entity.getUserId());
    reply.setRevision(entity.getRevision());
    
    return reply;
  }
  
  /**
   * Returns whether form field is a meta field
   * 
   * @param metaformEntity form
   * @param name name
   * @return whether form field is a meta field
   */
  private boolean isMetafield(Metaform metaformEntity, String name) {
    MetaformField field = getField(metaformEntity, name);
    return field != null && field.getContexts() != null && field.getContexts().contains("META");
  }
  
  /**
   * Returns field by name
   * 
   * @param metaformEntity form
   * @param name name
   * @return field
   */
  private MetaformField getField(Metaform metaformEntity, String name) {
    List<MetaformSection> sections = metaformEntity.getSections();
    
    for (MetaformSection section : sections) {
      for (MetaformField field : section.getFields()) {
        if (name.equals(field.getName())) {
          return field;
        }
      }
    }
    
    return null;
  }
  
  /**
   * Resolves meta field
   * 
   * @param replyData reply data
   * @param fieldName field name
   * @param entity reply
   */
  private void resolveMetaField(ReplyData replyData, String fieldName, fi.metatavu.metaform.server.persistence.model.Reply entity) {
    switch (fieldName) {
      case "lastEditor":
        replyData.put(fieldName, entity.getUserId());
      break;
      case "created":
        replyData.put(fieldName, formatDateTime(entity.getCreatedAt()));
      break;
      case "modified":
        replyData.put(fieldName, formatDateTime(entity.getModifiedAt()));
      break;
      default:
        logger.warn("Metafield {} not recognized", fieldName);
      break;
    }
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
