package fi.metatavu.metaform.server.rest.translate;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import fi.metatavu.metaform.server.metaforms.ReplyController;
import fi.metatavu.metaform.server.rest.model.Metaform;
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
  private ReplyController replyController;

  /**
   * Translates JPA reply object into REST reply object
   * 
   * @param metaformEntity Metaform entity
   * @param reply JPA reply object
   * @return REST reply
   */
  public Reply translateReply(Metaform metaformEntity, fi.metatavu.metaform.server.persistence.model.Reply reply) {
    if (reply == null) {
      return null;
    }
    
    ReplyData replyData = new ReplyData();
    replyController.listReplyFields(reply).forEach(field -> {
      Object value = replyController.getFieldValue(metaformEntity, reply, field);
      String fieldName = field.getName();
      replyData.put(fieldName, value);
    });
    
    Reply result = new Reply();
    result.setId(reply.getId());
    result.setData(replyData);
    result.setUserId(reply.getUserId());
    result.setRevision(reply.getRevision());
    
    return result;
  }
  
}
