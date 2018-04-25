package fi.metatavu.metaform.server.persistence.dao;

import java.util.List;
import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;
import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import fi.metatavu.metaform.server.persistence.model.Metaform;
import fi.metatavu.metaform.server.persistence.model.Metaform_;

/**
 * DAO class for Metaform entity
 * 
 * @author Antti Leppä
 */
@ApplicationScoped
public class MetaformDAO extends AbstractDAO<Metaform> {
  
  /**
   * Creates new Metaform
   * 
   * @param id id
   * @param realmId realm
   * @param data form JSON
   * @return created Metaform
   */
  public Metaform create(UUID id, String realmId, String data) {
    Metaform metaform = new Metaform(); 
    metaform.setId(id);
    metaform.setData(data);
    metaform.setRealmId(realmId);
    return persist(metaform);
  }

  /**
   * List Metaforms by realm
   * 
   * @param realmId realm id
   * @return list of Metaforms
   */
  public List<Metaform> listByRealmId(String realmId) {
    EntityManager entityManager = getEntityManager();

    CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
    CriteriaQuery<Metaform> criteria = criteriaBuilder.createQuery(Metaform.class);
    Root<Metaform> root = criteria.from(Metaform.class);
    criteria.select(root);

    criteria.where(
      criteriaBuilder.equal(root.get(Metaform_.realmId), realmId)
    );
    
    return entityManager.createQuery(criteria).getResultList();
  }

  /**
   * Updates JSON data
   * 
   * @param metaform Metaform
   * @param data form JSON
   * @return Updated Metaform
   */
  public Metaform updateData(Metaform metaform, String data) {
    metaform.setData(data);
    return persist(metaform);
  }

}
