package fi.metatavu.metaform.server.persistence.dao;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;

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
