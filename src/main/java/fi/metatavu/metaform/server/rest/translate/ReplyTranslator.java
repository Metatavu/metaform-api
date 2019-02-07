package fi.metatavu.metaform.server.rest.translate;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import fi.metatavu.metaform.server.metaforms.FieldController;
import fi.metatavu.metaform.server.persistence.model.ReplyField;
import fi.metatavu.metaform.server.rest.model.Metaform;
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
  private FieldController fieldController;

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
    
    Map<String, ReplyField> replyFieldMap = fieldController.getReplyFieldMap(reply);
    
    ReplyData replyData = new ReplyData();
    
    metaformEntity.getSections().stream()
      .map(MetaformSection::getFields)
      .flatMap(List::stream)
      .filter(field -> Objects.nonNull(field.getName()))
      .forEach(field -> {
        String fieldName = field.getName();
        Object value = fieldController.getFieldValue(metaformEntity, reply, fieldName, replyFieldMap);
        if (value != null) {
          replyData.put(fieldName, value);
        }
      });
      
    Reply result = new Reply();
    result.setId(reply.getId());
    result.setData(replyData);
    result.setUserId(reply.getUserId());
    result.setRevision(reply.getRevision());
    
    return result;
  }
  
}
