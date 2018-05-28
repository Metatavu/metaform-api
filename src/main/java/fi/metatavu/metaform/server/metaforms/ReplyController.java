package fi.metatavu.metaform.server.metaforms;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;

import fi.metatavu.metaform.server.persistence.dao.AnyReplyFieldDAO;
import fi.metatavu.metaform.server.persistence.dao.BooleanReplyFieldDAO;
import fi.metatavu.metaform.server.persistence.dao.ListReplyFieldDAO;
import fi.metatavu.metaform.server.persistence.dao.ListReplyFieldItemDAO;
import fi.metatavu.metaform.server.persistence.dao.NumberReplyFieldDAO;
import fi.metatavu.metaform.server.persistence.dao.ReplyDAO;
import fi.metatavu.metaform.server.persistence.dao.StringReplyFieldDAO;
import fi.metatavu.metaform.server.persistence.model.BooleanReplyField;
import fi.metatavu.metaform.server.persistence.model.ListReplyField;
import fi.metatavu.metaform.server.persistence.model.ListReplyFieldItem;
import fi.metatavu.metaform.server.persistence.model.Metaform;
import fi.metatavu.metaform.server.persistence.model.NumberReplyField;
import fi.metatavu.metaform.server.persistence.model.Reply;
import fi.metatavu.metaform.server.persistence.model.ReplyField;
import fi.metatavu.metaform.server.persistence.model.StringReplyField;
import fi.metatavu.metaform.server.rest.model.MetaformField;
import fi.metatavu.metaform.server.rest.model.MetaformSection;

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

  @Inject
  private ListReplyFieldDAO listReplyFieldDAO;

  @Inject
  private ListReplyFieldItemDAO listReplyFieldItemDAO;
  
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
   * Fields active (non revisioned) reply by metaform and user id
   * 
   * @param metaform metaform
   * @param userId user id
   * @return found reply
   */
  public Reply findActiveReplyByMetaformAndUserId(Metaform metaform, UUID userId) {
    return replyDAO.findByMetaformAndUserIdAndRevisionNull(metaform, userId);
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
    } else if (value instanceof List) {
      return setReplyField(reply, name, (List<?>) value);
    } else {
      if (logger.isErrorEnabled()) {
        logger.error(String.format("Unsupported to type (%s) for field %s in reply %s", value.getClass().getName(), name, reply.getId().toString()));
      }
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
      deleteField(replyField);
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
      deleteField(replyField);
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
      deleteField(replyField);
      replyField = null;
    }
    
    if (replyField == null) {
      return numberReplyFieldDAO.create(UUID.randomUUID(), reply, name, value.doubleValue());
    } else {
      return numberReplyFieldDAO.updateValue((NumberReplyField) replyField, value.doubleValue());
    }
  }

  /**
   * Sets reply field value
   * 
   * @param reply reply
   * @param name name 
   * @param values values
   * @return updated field
   */
  public ReplyField setReplyField(Reply reply, String name, List<?> values) {
    ReplyField replyField = anyReplyFieldDAO.findByReplyAndName(reply, name);
    if (replyField != null && !(replyField instanceof ListReplyField)) {
      deleteField(replyField);
      replyField = null;
    }
    
    ListReplyField listReplyField = (ListReplyField) replyField;
    
    if (listReplyField == null) {
      listReplyField = listReplyFieldDAO.create(UUID.randomUUID(), reply, name);
    } else {
      deleteListReplyFieldItems(listReplyField); 
    }

    for (Object value : values) {
      if (value != null) {
        listReplyFieldItemDAO.create(UUID.randomUUID(), listReplyField, value.toString());
      }
    }
    
    return listReplyField;
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
        deleteField(replyField);
      }
    }
  }

  /**
   * Deletes a reply
   * 
   * @param reply reply
   */
  public void deleteReply(Reply reply) {
    anyReplyFieldDAO.listByReply(reply).stream()
      .forEach(this::deleteField);
    
    replyDAO.delete(reply);
  }

  /**
   * Lists replies
   * 
   * @param metaform Metaform
   * @param userId userId
   * @param createdBefore filter results by created before specified time.
   * @param createdAfter filter results by created after specified time.
   * @param modifiedBefore filter results by modified before specified time.
   * @param modifiedAfter filter results by modified after specified time.
   * @param includeRevisions 
   * @return replies list of replies
   * @return replies
   */
  public List<Reply> listReplies(Metaform metaform, UUID userId, OffsetDateTime createdBefore, OffsetDateTime createdAfter, OffsetDateTime modifiedBefore, OffsetDateTime modifiedAfter, boolean includeRevisions) {
    return replyDAO.list(metaform, userId, includeRevisions, createdBefore, createdAfter, modifiedBefore, modifiedAfter);
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

  /**
   * Converts reply into a revision by updating the modifiedAt field into the revision field.
   * 
   * @param reply reply to be converted into a revision
   */
  public void convertToRevision(Reply reply) {
    replyDAO.updateRevision(reply, reply.getModifiedAt());
  }

  /**
   * Returns value for a reply field
   * 
   * @param metaformEntity metaform
   * @param reply reply
   * @param field field
   * @return value
   */
  public Object getFieldValue(fi.metatavu.metaform.server.rest.model.Metaform metaformEntity, Reply reply, ReplyField field) {
    String fieldName = field.getName();
    
    if (isMetafield(metaformEntity, fieldName)) {
      return resolveMetaField(fieldName, reply);
    } else {
      if (field instanceof NumberReplyField) {
        return ((NumberReplyField) field).getValue();
      } else if (field instanceof BooleanReplyField) {
        return ((BooleanReplyField) field).getValue();
      } else if (field instanceof StringReplyField) {
        return ((StringReplyField) field).getValue();
      } else if (field instanceof ListReplyField) {
        return listReplyFieldItemDAO.listByField((ListReplyField) field).stream()
          .map(ListReplyFieldItem::getValue)
          .collect(Collectors.toList());
      } else {
        logger.error("Could not resolve {}", fieldName); 
      }
    }
    
    return null;
  }
  
  /**
   * Deletes a reply field
   * 
   * @param replyField reply field
   */
  private void deleteField(ReplyField replyField) {
    if (replyField instanceof ListReplyField) {
      ListReplyField listReplyField = (ListReplyField) replyField;
      deleteListReplyFieldItems(listReplyField);
    }
    
    anyReplyFieldDAO.delete(replyField);
  }

  /**
   * Removes all items from list reply field
   * 
   * @param listReplyField field
   */
  private void deleteListReplyFieldItems(ListReplyField listReplyField) {
    listReplyFieldItemDAO.listByField(listReplyField).stream().forEach(listReplyFieldItemDAO::delete);
  }
    
  /**
   * Returns whether form field is a meta field
   * 
   * @param metaformEntity form
   * @param name name
   * @return whether form field is a meta field
   */
  private boolean isMetafield(fi.metatavu.metaform.server.rest.model.Metaform metaformEntity, String name) {
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
  private MetaformField getField(fi.metatavu.metaform.server.rest.model.Metaform metaformEntity, String name) {
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
   * @param fieldName field name
   * @param entity reply
   * @return meta field value
   */
  private Object resolveMetaField(String fieldName, fi.metatavu.metaform.server.persistence.model.Reply entity) {
    switch (fieldName) {
      case "lastEditor":
        return entity.getUserId();
      case "created":
        return formatDateTime(entity.getCreatedAt());
      case "modified":
        return formatDateTime(entity.getModifiedAt());
      default:
        logger.warn("Metafield {} not recognized", fieldName);
      break;
    }
    
    return null;
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
