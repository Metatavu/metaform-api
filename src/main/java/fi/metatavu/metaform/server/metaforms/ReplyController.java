package fi.metatavu.metaform.server.metaforms;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;

import fi.metatavu.metaform.server.attachments.AttachmentController;
import fi.metatavu.metaform.server.files.File;
import fi.metatavu.metaform.server.files.FileController;
import fi.metatavu.metaform.server.persistence.dao.AnyReplyFieldDAO;
import fi.metatavu.metaform.server.persistence.dao.AttachmentReplyFieldDAO;
import fi.metatavu.metaform.server.persistence.dao.AttachmentReplyFieldItemDAO;
import fi.metatavu.metaform.server.persistence.dao.BooleanReplyFieldDAO;
import fi.metatavu.metaform.server.persistence.dao.ListReplyFieldDAO;
import fi.metatavu.metaform.server.persistence.dao.ListReplyFieldItemDAO;
import fi.metatavu.metaform.server.persistence.dao.NumberReplyFieldDAO;
import fi.metatavu.metaform.server.persistence.dao.ReplyDAO;
import fi.metatavu.metaform.server.persistence.dao.StringReplyFieldDAO;
import fi.metatavu.metaform.server.persistence.model.Attachment;
import fi.metatavu.metaform.server.persistence.model.AttachmentReplyField;
import fi.metatavu.metaform.server.persistence.model.AttachmentReplyFieldItem;
import fi.metatavu.metaform.server.persistence.model.BooleanReplyField;
import fi.metatavu.metaform.server.persistence.model.ListReplyField;
import fi.metatavu.metaform.server.persistence.model.Metaform;
import fi.metatavu.metaform.server.persistence.model.NumberReplyField;
import fi.metatavu.metaform.server.persistence.model.Reply;
import fi.metatavu.metaform.server.persistence.model.ReplyField;
import fi.metatavu.metaform.server.persistence.model.StringReplyField;
import fi.metatavu.metaform.server.rest.model.MetaformFieldType;

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

  @Inject
  private AttachmentReplyFieldDAO attachmentReplyFieldDAO;

  @Inject
  private AttachmentReplyFieldItemDAO attachmentReplyFieldItemDAO;
  
  @Inject
  private AttachmentController attachmentController;
  
  @Inject
  private FileController fileController;
  
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
  public ReplyField setReplyField(MetaformFieldType fieldType, Reply reply, String name, Object value) {
    if (fieldType == MetaformFieldType.FILES) {
      return setFileReplyField(reply, name, value);
    }

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
  @SuppressWarnings("unchecked")
  public ReplyField setFileReplyField(Reply reply, String name, Object value) {
    List<String> fileRefs = null;
    
    if (value instanceof List<?>) {
      fileRefs = (List<String>) value;
    } else {
      fileRefs = Arrays.asList(String.valueOf(value));
    }
    
    ReplyField replyField = anyReplyFieldDAO.findByReplyAndName(reply, name);
    if (replyField != null && !(replyField instanceof AttachmentReplyField)) {
      deleteField(replyField);
      replyField = null;
    }
    
    AttachmentReplyField attachmentReplyField = (AttachmentReplyField) replyField;
    List<AttachmentReplyFieldItem> items = null;
    if (attachmentReplyField == null) {
      items = Collections.emptyList();
      attachmentReplyField = attachmentReplyFieldDAO.create(UUID.randomUUID(), reply, name);
    } else {
      items = attachmentReplyFieldItemDAO.listByField(attachmentReplyField);
      removeUnusedAttachmentReplyItems(items, fileRefs);
    }
    
    attachUsedReplyItems(reply.getUserId(), attachmentReplyField, items, fileRefs);

    return attachmentReplyField;
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
  @SuppressWarnings ("squid:S00107")
  public List<Reply> listReplies(Metaform metaform, UUID userId, OffsetDateTime createdBefore, OffsetDateTime createdAfter, OffsetDateTime modifiedBefore, OffsetDateTime modifiedAfter, boolean includeRevisions, FieldFilters fieldFilters) {
    return replyDAO.list(metaform, userId, includeRevisions, createdBefore, createdAfter, modifiedBefore, modifiedAfter, fieldFilters);
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
   * Sets reply field value
   * 
   * @param reply reply
   * @param name name 
   * @param values values
   * @return updated field
   */
  private ReplyField setReplyField(Reply reply, String name, List<?> values) {
    ReplyField replyField = anyReplyFieldDAO.findByReplyAndName(reply, name);
    if (replyField != null && !(replyField instanceof ListReplyField)) {
      deleteField(replyField);
      replyField = null;
    }
    
    ListReplyField listReplyField = (ListReplyField) replyField;
    if (values == null || values.isEmpty()) {
      if (listReplyField != null) {
        deleteListReplyFieldItems(listReplyField); 
        listReplyFieldDAO.delete(listReplyField);
      }
      
      return null;
    }
    
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
  
  private void removeUnusedAttachmentReplyItems(List<AttachmentReplyFieldItem> items, List<String> savedFileRefs) {
    items.stream()
      .filter((attachmentReplyFieldItem) -> {
        String fileRef = attachmentReplyFieldItem.getAttachment().getId().toString();
        return !savedFileRefs.contains(fileRef);
      })
      .forEach(this::deleteAttachmentReplyFieldItem);
  }

  private void deleteAttachmentReplyFieldItem(AttachmentReplyFieldItem item) {
    Attachment attachment = item.getAttachment();
    attachmentReplyFieldItemDAO.delete(item);
    attachmentController.deleteAttachment(attachment);
  }
  
  private void attachUsedReplyItems(UUID userId, AttachmentReplyField field, List<AttachmentReplyFieldItem> items, List<String> savedFileRefs) {
    List<String> usedFileRefs = items.stream()
      .map(AttachmentReplyFieldItem::getAttachment)
      .map(Attachment::getId)
      .map(UUID::toString)
      .collect(Collectors.toList());
    
    savedFileRefs.stream()
      .filter((savedFileRef) -> !usedFileRefs.contains(savedFileRef))
      .forEach((id) -> {
        Attachment attachment = retrieveOrPersistAttachment(UUID.fromString(id), userId);
        if (attachment != null) {
          attachmentReplyFieldItemDAO.create(UUID.randomUUID(), field, attachment);
        } else {
          logger.error(String.format("Could not find attachment with id", id));
        }        
      });
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
   * Deletes a reply field
   * 
   * @param replyField reply field
   */
  private void deleteField(ReplyField replyField) {
    if (replyField instanceof ListReplyField) {
      ListReplyField listReplyField = (ListReplyField) replyField;
      deleteListReplyFieldItems(listReplyField);
    }
    
    if (replyField instanceof AttachmentReplyField) {
      AttachmentReplyField attachmentReplyField = (AttachmentReplyField) replyField;
      deleteAttachmentReplyFieldItems(attachmentReplyField);
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
   * Removes all items from attachment reply field
   * 
   * @param attachmentReplyField field
   */
  private void deleteAttachmentReplyFieldItems(AttachmentReplyField attachmentReplyField) {
    attachmentReplyFieldItemDAO.listByField(attachmentReplyField).stream().forEach(this::deleteAttachmentReplyFieldItem);
  }

  /**
   * Retrieves existing attachment or persists one from previously uploaded one.
   * 
   * @param id attachment id
   * @param userId user id
   * @return attachment
   */
  private Attachment retrieveOrPersistAttachment(UUID id, UUID userId) {
    Attachment attachment = attachmentController.findAttachmentById(id);
    if (attachment == null) {
      return persistAttachment(id, userId);
    }
    
    return attachment;
  }

  /**
   * Persists previously uploaded file as attachment
   * 
   * @param id attachment id / fileRef
   * @param userId user id
   * @return 
   */
  private Attachment persistAttachment(UUID id, UUID userId) {
    File file = fileController.popFileData(id.toString());
    String name = file.getMeta().getFileName();
    byte[] content = file.getData();
    String contentType = file.getMeta().getContentType();
    return attachmentController.create(id, name, content, contentType, userId);
  }

}
