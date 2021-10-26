package fi.metatavu.metaform.server.drafts;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import fi.metatavu.metaform.server.persistence.dao.DraftDAO;
import fi.metatavu.metaform.server.persistence.model.Draft;
import fi.metatavu.metaform.server.persistence.model.Metaform;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.slf4j.Logger;

/**
 * Controller for draft related operations
 * 
 * @author Antti Leppä
 */
@ApplicationScoped
public class DraftController {
  
  @Inject
  private Logger logger;

  @Inject
  private DraftDAO draftDAO;
  
  /**
   * Creates new draft
   * 
   * @param metaform metaform
   * @param userId user id
   * @param data draft data
   * @return created draft
   */
  public Draft createDraft(Metaform metaform, UUID userId, Map<String, Object> data) {
    return draftDAO.create(UUID.randomUUID(), userId, metaform, serializeData(data));
  }

  /**
   * Finds a draft by id
   * 
   * @param id id
   * @return found draft or null if not found
   */
  public Draft findDraftById(UUID id) {
    return draftDAO.findById(id);
  }

  /**
   * Updates a draft
   * 
   * @param draft draftt
   * @param data data
   * @return updated draft
   */
  public Draft updateDraft(Draft draft, Map<String, Object> data) {
    return draftDAO.updateData(draft, serializeData(data));
  }

  /**
   * Deletes an draft
   * 
   * @param draft draft to be deleted
   */
  public void deleteDraft(Draft draft) {
    draftDAO.delete(draft);
  }

  /**
   * Serializes data as string
   * 
   * @param data data
   * @return data as string
   */
  private String serializeData(Map<String, Object> data) {
    try {
      ObjectMapper objectMapper = new ObjectMapper();
      return objectMapper.writeValueAsString(data);
    } catch (Exception e) {
      logger.error("Failed to serialize draft data", e);
    }
    
    return null;
  }
  
}
