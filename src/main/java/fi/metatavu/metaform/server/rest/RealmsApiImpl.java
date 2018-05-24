package fi.metatavu.metaform.server.rest;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.ejb.Stateful;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.core.Response;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import fi.metatavu.metaform.server.metaforms.MetaformController;
import fi.metatavu.metaform.server.metaforms.ReplyController;
import fi.metatavu.metaform.server.rest.model.Metaform;
import fi.metatavu.metaform.server.rest.model.Reply;
import fi.metatavu.metaform.server.rest.model.ReplyData;
import fi.metatavu.metaform.server.rest.translate.MetaformTranslator;
import fi.metatavu.metaform.server.rest.translate.ReplyTranslator;

/**
 * Realms REST Service implementation
 * 
 * @author Antti Leppä
 */
@RequestScoped
@Stateful
public class RealmsApiImpl extends AbstractApi implements RealmsApi {
  
  private static final String ANONYMOUS_USERS_LIST_METAFORMS_MESSAGE = "Anonymous users are not allowed to list Metaforms";
  private static final String ANONYMOUS_USERS_FIND_METAFORM_MESSAGE = "Anonymous users are not allowed to find Metaforms";
  private static final String NOT_ALLOWED_TO_VIEW_THESE_REPLIES = "You are not allowed to view these replies";
  private static final String NOT_ALLOWED_TO_VIEW_REPLY_MESSAGE = "You are not allowed to view this reply";
  private static final String ANONYMOUS_USERS_MESSAGE = "Anonymous users are not allowed on this Metaform";
  private static final String NOT_FOUND_MESSAGE = "Not found";
  private static final String UNAUTHORIZED = "Unauthorized";

  @Inject
  private Logger logger;
  
  @Inject
  private MetaformController metaformController;

  @Inject
  private ReplyController replyController;

  @Inject
  private MetaformTranslator metaformTranslator;

  @Inject
  private ReplyTranslator replyTranslator;
  
  @Override
  public Response createReply(String realmId, UUID metaformId, Reply payload, Boolean updateExisting) throws Exception {
    UUID loggedUserId = getLoggerUserId();
    if (loggedUserId == null) {
      return createForbidden(UNAUTHORIZED);
    }
    
    fi.metatavu.metaform.server.persistence.model.Metaform metaform = metaformController.findMetaformById(metaformId);
    if (metaform == null) {
      return createNotFound(NOT_FOUND_MESSAGE);
    }
    
    boolean anonymous = !isRealmUser();
    if (!metaform.getAllowAnonymous() && anonymous) {
      return createForbidden(ANONYMOUS_USERS_MESSAGE);
    }
    
    UUID userId = payload.getUserId();
    if (!isRealmMetaformAdmin() || userId == null) {
      userId = loggedUserId;
    }
    
    // TODO: Permission check
    // TODO: Support multiple
    
    fi.metatavu.metaform.server.persistence.model.Reply reply = null;
    if (anonymous) {
      reply = replyController.createReply(userId, metaform);
    } else {
      reply = replyController.findActiveReplyByMetaformAndUserId(metaform, userId);
      if (reply == null) {
        reply = replyController.createReply(userId, metaform);
      } else {
        if (!updateExisting) {
          // If there is already an existing reply but we are not updating it
          // We need to change the existing reply into a revision and create new reply
          replyController.convertToRevision(reply);
          reply = replyController.createReply(userId, metaform);
        }
      }
    }
    
    ReplyData data = payload.getData();
    if (data == null) {
      logger.warn("Received a reply with null data");
    } else {
      for (Entry<String, Object> entry : data.entrySet()) {
        String fieldName = entry.getKey();
        Object fieldValue = entry.getValue();
        
        if (fieldValue != null) {
          replyController.setReplyField(reply, fieldName, fieldValue);
        }
      }
    }
    
    return createOk(replyTranslator.translateReply(reply));
  }

  public Response findReply(String realmId, UUID metaformId, UUID replyId) throws Exception {
    if (!isRealmUser()) {
      return createForbidden(ANONYMOUS_USERS_MESSAGE);
    }
    
    // TODO: Permission check
    
    fi.metatavu.metaform.server.persistence.model.Metaform metaform = metaformController.findMetaformById(metaformId);
    if (metaform == null) {
      return createNotFound(NOT_FOUND_MESSAGE);
    }
    
    fi.metatavu.metaform.server.persistence.model.Reply reply = replyController.findReplyById(replyId);
    if (reply == null) {
      return createNotFound(NOT_FOUND_MESSAGE);
    }

    if (!isRealmMetaformAdmin() && !getLoggerUserId().equals(reply.getUserId())) {
      return createForbidden(NOT_ALLOWED_TO_VIEW_REPLY_MESSAGE);
    }
    
    if (!reply.getMetaform().getId().equals(metaform.getId())) {
      return createNotFound(NOT_FOUND_MESSAGE);
    }
    
    return createOk(replyTranslator.translateReply(reply));
  }
  
  @Override
  public Response listReplies(String realmId, UUID metaformId, UUID userId, String createdBeforeParam, String createdAfterParam,
      String modifiedBeforeParam, String modifiedAfterParam, Boolean includeRevisions) throws Exception {
    // TODO: Permission check
    
    OffsetDateTime createdBefore = parseTime(createdBeforeParam);
    OffsetDateTime createdAfter = parseTime(createdAfterParam);
    OffsetDateTime modifiedBefore = parseTime(modifiedBeforeParam);
    OffsetDateTime modifiedAfter = parseTime(modifiedAfterParam);

    if (!isRealmUser()) {
      return createForbidden(ANONYMOUS_USERS_MESSAGE);
    }
    
    if ((userId == null || !userId.equals(getLoggerUserId())) && (!hasRealmRole(ADMIN_ROLE, VIEW_ALL_REPLIES_ROLE))) {
      return createForbidden(NOT_ALLOWED_TO_VIEW_THESE_REPLIES);
    }
    
    fi.metatavu.metaform.server.persistence.model.Metaform metaform = metaformController.findMetaformById(metaformId);
    if (metaform == null) {
      return createNotFound(NOT_FOUND_MESSAGE);
    }

    List<fi.metatavu.metaform.server.persistence.model.Reply> replies = replyController.listReplies(metaform, 
        userId, 
        createdBefore, 
        createdAfter, 
        modifiedBefore, 
        modifiedAfter,
        includeRevisions == null ? false : includeRevisions);

    List<Reply> result = replies.stream().map((entity) -> {
     return replyTranslator.translateReply(entity);
    }).collect(Collectors.toList());
    
    
    return createOk(result);
  }

  public Response updateReply(String realmId, UUID metaformId, UUID replyId, Reply payload) throws Exception {
    if (!isRealmUser()) {
      return createForbidden(ANONYMOUS_USERS_MESSAGE);
    }
    
    UUID loggedUserId = getLoggerUserId();
    fi.metatavu.metaform.server.persistence.model.Metaform metaform = metaformController.findMetaformById(metaformId);
    if (metaform == null) {
      return createNotFound(NOT_FOUND_MESSAGE);
    }
    
    fi.metatavu.metaform.server.persistence.model.Reply reply = replyController.findReplyById(replyId);
    if (reply == null) {
      return createNotFound(NOT_FOUND_MESSAGE);
    }
    
    if (!reply.getUserId().equals(loggedUserId)) {
      return createNotFound(NOT_FOUND_MESSAGE);
    }

    List<String> fieldNames = new ArrayList<>(replyController.listFieldNames(reply));
    
    for (Entry<String, Object> entry : payload.getData().entrySet()) {
      String fieldName = entry.getKey();
      replyController.setReplyField(reply, fieldName, entry.getValue());
      fieldNames.remove(fieldName);
    }
    
    replyController.deleteReplyFields(reply, fieldNames);
    
    return createNoContent();
  }

  @Override
  public Response deleteReply(String realmId, UUID metaformId, UUID replyId) throws Exception {
    if (!isRealmMetaformAdmin()) {
      return createForbidden("You are not allowed to delete replies");
    }

    fi.metatavu.metaform.server.persistence.model.Metaform metaform = metaformController.findMetaformById(metaformId);
    if (metaform == null) {
      return createNotFound(NOT_FOUND_MESSAGE);
    }

    fi.metatavu.metaform.server.persistence.model.Reply reply = replyController.findReplyById(replyId);
    if (reply == null) {
      return createNotFound(NOT_FOUND_MESSAGE);
    }
    
    if (!reply.getMetaform().getId().equals(metaform.getId())) {
      return createNotFound(NOT_FOUND_MESSAGE);
    }
    
    replyController.deleteReply(reply);
    
    return null;
  }

  @Override
  public Response createMetaform(String realmId, Metaform payload) throws Exception {
    if (!isRealmMetaformAdmin()) {
      return createForbidden("You are not allowed to create Metaforms");
    }

    String data = serializeMetaform(payload);
    if (data == null) {
      return createBadRequest("Invalid Metaform JSON");  
    }
    
    Boolean allowAnonymous = payload.isAllowAnonymous();
    if (allowAnonymous == null) {
      allowAnonymous  = false;
    }
    
    // TODO: Permission check
    
    return createOk(metaformTranslator.translateMetaform(metaformController.createMetaform(realmId, allowAnonymous, data)));
  }

  public Response listMetaforms(String realmId) throws Exception {
    // TODO: Permission check
    if (!isRealmUser()) {
      return createForbidden(ANONYMOUS_USERS_LIST_METAFORMS_MESSAGE);
    }

    return createOk(metaformController.listMetaforms(realmId).stream().map((entity) -> {
      return metaformTranslator.translateMetaform(entity);
    }).collect(Collectors.toList()));
  }

  public Response findMetaform(String realmId, UUID metaformId) throws Exception {
    // TODO: Permission check
    if (!isRealmUser()) {
      return createForbidden(ANONYMOUS_USERS_FIND_METAFORM_MESSAGE);
    }

    fi.metatavu.metaform.server.persistence.model.Metaform metaform = metaformController.findMetaformById(metaformId);
    if (metaform == null) {
      return createNotFound(NOT_FOUND_MESSAGE);
    }
    
    if (!StringUtils.equals(metaform.getRealmId(), realmId)) {
      return createNotFound(NOT_FOUND_MESSAGE);
    }
    
    return createOk(metaformTranslator.translateMetaform(metaform));
  }

  @Override
  public Response updateMetaform(String realmId, UUID metaformId, Metaform payload) throws Exception {
    if (!isRealmMetaformAdmin()) {
      return createForbidden("You are not allowed to update Metaforms");
    }

    String data = serializeMetaform(payload);
    if (data == null) {
      return createBadRequest("Invalid Metaform JSON");  
    }
    
    fi.metatavu.metaform.server.persistence.model.Metaform metaform = metaformController.findMetaformById(metaformId);
    if (metaform == null) {
      return createNotFound(NOT_FOUND_MESSAGE);
    }
    
    // TODO: Permission check
    
    return createOk(metaformTranslator.translateMetaform(metaformController.updateMetaform(metaform, data)));
  }
  
  @Override
  public Response deleteMetaform(String realmId, UUID metaformId) throws Exception {
    if (!isRealmMetaformAdmin()) {
      return createForbidden("You are not allowed to delete Metaforms");
    }

    fi.metatavu.metaform.server.persistence.model.Metaform metaform = metaformController.findMetaformById(metaformId);
    if (metaform == null) {
      return createNotFound(NOT_FOUND_MESSAGE);
    }

    // TODO: Permission check
    
    metaformController.deleteMetaform(metaform);
    
    return createNoContent();
  }
  
  public Response findReplyMeta(String realmId, UUID metaformId, UUID replyId) throws Exception {
    fi.metatavu.metaform.server.persistence.model.Metaform metaform = metaformController.findMetaformById(metaformId);
    if (metaform == null) {
      return createNotFound(NOT_FOUND_MESSAGE);
    }
    
    fi.metatavu.metaform.server.persistence.model.Reply reply = replyController.findReplyById(replyId);
    if (reply == null) {
      return createNotFound(NOT_FOUND_MESSAGE);
    }

    if (!isRealmMetaformAdmin() && !getLoggerUserId().equals(reply.getUserId())) {
      return createForbidden(NOT_ALLOWED_TO_VIEW_REPLY_MESSAGE);
    }
    
    // TODO: Permission check
    
    return createOk(replyTranslator.translateReplyMeta(reply));
  }

  public Response export(String realmId, UUID metaformId, String format) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  private String serializeMetaform(Metaform metaform) {
    ObjectMapper objectMapper = new ObjectMapper();
    
    try {
      return objectMapper.writeValueAsString(metaform);
    } catch (JsonProcessingException e) {
      logger.error("Failed to serialze metaform", e);
    }
    
    return null;
  }

}
