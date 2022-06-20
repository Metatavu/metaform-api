package fi.metatavu.metaform.server.persistence.dao;

import java.util.UUID;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import org.slf4j.Logger;
import fi.metatavu.metaform.server.persistence.model.Draft;
import fi.metatavu.metaform.server.persistence.model.Draft_;
import fi.metatavu.metaform.server.persistence.model.Metaform;

/**
 * DAO class for draft entity
 * 
 * @author Antti Leppä
 */
@ApplicationScoped
public class DraftDAO extends AbstractDAO<Draft> {
  
  @Inject
  private Logger logger;
  
  /**
   * Creates new draft
   * 
   * @param id id
   * @param userId user id
   * @param metaform Metaform
   * @param data data
   * @return created Metaform
   */
  public Draft create(UUID id, UUID userId, Metaform metaform, String data) {
    Draft draft = new Draft(); 
    draft.setId(id);
    draft.setMetaform(metaform);
    draft.setUserId(userId);
    draft.setData(data);
    return persist(draft);
  }
  
  /**
   * Updates data
   * 
   * @param draft draft
   * @param data data
   * @return updated draft
   */
  public Draft updateData(Draft draft, String data) {
    draft.setData(data);
    return persist(draft);
  }

}
