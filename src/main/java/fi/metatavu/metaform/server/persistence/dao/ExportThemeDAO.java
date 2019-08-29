package fi.metatavu.metaform.server.persistence.dao;

import java.util.List;
import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import fi.metatavu.metaform.server.persistence.model.ExportTheme;
import fi.metatavu.metaform.server.persistence.model.ExportTheme_;

/**
 * DAO class for ExportThemeFile entity
 * 
 * @author Antti Leppä
 */
@ApplicationScoped
public class ExportThemeDAO extends AbstractDAO<ExportTheme> {

  /**
  * Creates new exportTheme
  *
  * @param id id
  * @param locales locales
  * @param parent parent
  * @param name name
  * @param lastModifier modifier
  * @return created exportTheme
  */
  public ExportTheme create(UUID id, String locales, ExportTheme parent, String name, UUID creator, UUID lastModifier) {
    ExportTheme exportTheme = new ExportTheme();
    exportTheme.setId(id);
    exportTheme.setLocales(locales);
    exportTheme.setParent(parent);
    exportTheme.setName(name);
    exportTheme.setLastModifier(lastModifier);
    exportTheme.setCreator(creator);
    return persist(exportTheme);
  }

  /**
   * Finds a theme by name
   * 
   * @param name name
   * @return Found theme or null if not found
   */
  public ExportTheme findByName(String name) {
    EntityManager entityManager = getEntityManager();
    
    CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
    CriteriaQuery<ExportTheme> criteria = criteriaBuilder.createQuery(ExportTheme.class);
    Root<ExportTheme> root = criteria.from(ExportTheme.class);
    criteria.select(root);
    criteria.where(criteriaBuilder.equal(root.get(ExportTheme_.name), name));
    
    TypedQuery<ExportTheme> query = entityManager.createQuery(criteria);
    
    return getSingleResult(query);
  }

  /**
   * Lists themes
   * 
   * @return List of themes
   */
  public List<ExportTheme> list() {
    EntityManager entityManager = getEntityManager();
    
    CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
    CriteriaQuery<ExportTheme> criteria = criteriaBuilder.createQuery(ExportTheme.class);
    Root<ExportTheme> root = criteria.from(ExportTheme.class);
    criteria.select(root);
    criteria.orderBy(criteriaBuilder.asc(root.get(ExportTheme_.createdAt)));
    
    return entityManager.createQuery(criteria).getResultList();
  }
  
  /**
  * Updates locales
  *
  * @param locales locales
  * @param lastModifier modifier
  * @return updated exportTheme
  */
  public ExportTheme updateLocales(ExportTheme exportTheme, String locales, UUID lastModifier) {
    exportTheme.setLastModifier(lastModifier);
    exportTheme.setLocales(locales);
    return persist(exportTheme);
  }

  /**
  * Updates parent
  *
  * @param parent parent
  * @param lastModifier modifier
  * @return updated exportTheme
  */
  public ExportTheme updateParent(ExportTheme exportTheme, ExportTheme parent, UUID lastModifier) {
    exportTheme.setLastModifier(lastModifier);
    exportTheme.setParent(parent);
    return persist(exportTheme);
  }

  /**
  * Updates name
  *
  * @param name name
  * @param lastModifier modifier
  * @return updated exportTheme
  */
  public ExportTheme updateName(ExportTheme exportTheme, String name, UUID lastModifier) {
    exportTheme.setLastModifier(lastModifier);
    exportTheme.setName(name);
    return persist(exportTheme);
  }

}
