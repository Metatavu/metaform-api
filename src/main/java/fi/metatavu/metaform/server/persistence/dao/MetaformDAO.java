package fi.metatavu.metaform.server.persistence.dao;

import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;
import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import fi.metatavu.metaform.server.persistence.model.ExportTheme;
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
   * @param slug form slug
   * @param exportTheme export theme
   * @param allowAnonymous whether to allow anonymous repliers
   * @param data form JSON
   * @return created Metaform
   */
  public Metaform create(UUID id, String slug, ExportTheme exportTheme, Boolean allowAnonymous, String data) {
    Metaform metaform = new Metaform(); 
    
    metaform.setId(id);
    metaform.setExportTheme(exportTheme);
    metaform.setData(data);
    metaform.setSlug(slug);
    metaform.setAllowAnonymous(allowAnonymous);
    
    return persist(metaform);
  }
  
  /**
   * Finds Metaform by slug
   * 
   * @param slug slug
   * @return found Metaform or null if non found
   */
  public Metaform findBySlug(String slug) {
    EntityManager entityManager = getEntityManager();

    CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
    CriteriaQuery<Metaform> criteria = criteriaBuilder.createQuery(Metaform.class);
    Root<Metaform> root = criteria.from(Metaform.class);
    criteria.select(root);
    criteria.where(criteriaBuilder.equal(root.get(Metaform_.slug), slug));

    return getSingleResult(entityManager.createQuery(criteria));
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

  /**
   * Updates allowAnonymous value
   * 
   * @param metaform Metaform
   * @param allowAnonymous allow anonymous 
   * @return Updated Metaform
   */
  public Metaform updateAllowAnonymous(Metaform metaform, Boolean allowAnonymous) {
    metaform.setAllowAnonymous(allowAnonymous);
    return persist(metaform);
  }

  /**
   * Updates exportTheme value
   * 
   * @param metaform Metaform
   * @param exportTheme export theme
   * @return Updated Metaform
   */
  public Metaform updateExportTheme(Metaform metaform, ExportTheme exportTheme) {
    metaform.setExportTheme(exportTheme);
    return persist(metaform);
  }


}
