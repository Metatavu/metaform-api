package fi.metatavu.metaform.server.rest;

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

  public Response createReply(String realmId, UUID metaformId, Reply payload) throws Exception {
    UUID loggedUserId = getLoggerUserId();
    fi.metatavu.metaform.server.persistence.model.Metaform metaform = metaformController.findMetaformById(metaformId);
    if (metaform == null) {
      return createNotFound("Not found");
    }
    
    // TODO: Permission check
    
    fi.metatavu.metaform.server.persistence.model.Reply reply = replyController.createReply(loggedUserId, metaform);
    
    for (Entry<String, Object> entry : payload.getData().entrySet()) {
      String fieldName = entry.getKey();
      Object fieldValue = entry.getValue();
      
      if (fieldValue != null) {
        replyController.setReplyField(reply, fieldName, fieldValue);
      }
    }
    
    return createOk(replyTranslator.translateReply(reply));
  }

  public Response findReply(String realmId, UUID metaformId, UUID replyId) throws Exception {
    // TODO: Permission check
    
    fi.metatavu.metaform.server.persistence.model.Metaform metaform = metaformController.findMetaformById(metaformId);
    if (metaform == null) {
      return createNotFound("Not found");
    }
    
    fi.metatavu.metaform.server.persistence.model.Reply reply = replyController.findReplyById(replyId);
    return createOk(replyTranslator.translateReply(reply));
  }

  public Response listReplies(String realmId, UUID metaformId, UUID userId) throws Exception {
    // TODO: Permission check
    
    fi.metatavu.metaform.server.persistence.model.Metaform metaform = metaformController.findMetaformById(metaformId);
    if (metaform == null) {
      return createNotFound("Not found");
    }

    List<fi.metatavu.metaform.server.persistence.model.Reply> replies = replyController.listReplies(metaform);
    
    List<Reply> result = replies.stream().map((entity) -> {
     return replyTranslator.translateReply(entity);
    }).collect(Collectors.toList());
    
    
    return createOk(result);
  }

  public Response updateReply(String realmId, UUID metaformId, UUID replyId, Reply payload) throws Exception {
    UUID loggedUserId = getLoggerUserId();
    fi.metatavu.metaform.server.persistence.model.Metaform metaform = metaformController.findMetaformById(metaformId);
    if (metaform == null) {
      return createNotFound("Not found");
    }
    
    fi.metatavu.metaform.server.persistence.model.Reply reply = replyController.findReplyById(replyId);
    if (reply == null) {
      return createNotFound("Not found");
    }
    
    if (!reply.getUserId().equals(loggedUserId)) {
      return createNotFound("Not found");
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
  public Response createMetaform(String realmId, Metaform payload) throws Exception {
    String data = serializeMetaform(payload);
    if (data == null) {
      return createBadRequest("Invalid Metaform JSON");  
    }

    // TODO: Permission check
    
    return createOk(metaformTranslator.translateMetaform(metaformController.createMetaform(realmId, data)));
  }

  public Response listMetaforms(String realmId) throws Exception {
   // TODO: Permission check
    
   return createOk(metaformController.listMetaforms(realmId).stream().map((entity) -> {
     return metaformTranslator.translateMetaform(entity);
   }).collect(Collectors.toList()));
  }

  public Response findMetaform(String realmId, UUID metaformId) throws Exception {
    // TODO: Permission check

    fi.metatavu.metaform.server.persistence.model.Metaform metaform = metaformController.findMetaformById(metaformId);
    if (metaform == null) {
      return createNotFound("Not found");
    }
    
    if (!StringUtils.equals(metaform.getRealmId(), realmId)) {
      return createNotFound("Not found");
    }
    
    return createOk(metaformTranslator.translateMetaform(metaform));
  }

  @Override
  public Response updateMetaform(String realmId, UUID metaformId, Metaform payload) throws Exception {
    String data = serializeMetaform(payload);
    if (data == null) {
      return createBadRequest("Invalid Metaform JSON");  
    }
    
    fi.metatavu.metaform.server.persistence.model.Metaform metaform = metaformController.findMetaformById(metaformId);
    if (metaform == null) {
      return createNotFound("Not found");
    }
    
    // TODO: Permission check
    
    return createOk(metaformTranslator.translateMetaform(metaformController.updateMetaform(metaform, data)));
  }

  public Response findReplyMeta(String realmId, UUID metaformId, UUID replyId) throws Exception {
    fi.metatavu.metaform.server.persistence.model.Metaform metaform = metaformController.findMetaformById(metaformId);
    if (metaform == null) {
      return createNotFound("Not found");
    }
    
    fi.metatavu.metaform.server.persistence.model.Reply reply = replyController.findReplyById(replyId);
    if (reply == null) {
      return createNotFound("Not found");
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
