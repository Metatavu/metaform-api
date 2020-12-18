package fi.metatavu.metaform.server.rest.translate;

import org.slf4j.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import fi.metatavu.metaform.server.rest.model.Draft;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;

/**
 * Translator for drafts
 * 
 * @author Antti Leppä
 */
@ApplicationScoped
public class DraftTranslator {
  
  @Inject
  private Logger logger;

  /**
   * Translates JPA draft object into REST draft object
   * 
   * @param draft JPA drasft object
   * @return REST object
   */
  public Draft translateDraft(fi.metatavu.metaform.server.persistence.model.Draft draft) {
    if (draft == null) {
      return null;
    }
      
    Draft result = new Draft();
    result.setId(draft.getId());
    result.setData(deserializeData(draft.getData()));
    
    return result;
  }

  /**
   * Deserializes draft data from string
   * 
   * @param data data
   * @return draft data
   */
  private Map<String, Object> deserializeData(String data) {
    ObjectMapper objectMapper = new ObjectMapper();
    try {
      return objectMapper.readValue(data, new TypeReference<Map<String,Object>>() {});
    } catch (Exception e) {
      logger.error("Failed to read draft data", e);
    }
    
    return null;
  }
  
}
