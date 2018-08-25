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
import fi.metatavu.metaform.server.persistence.model.ExportThemeFile;
import fi.metatavu.metaform.server.persistence.model.ExportThemeFile_;

/**
 * DAO class for ExportThemeFile entity
 * 
 * @author Antti Leppä
 */
@ApplicationScoped
public class ExportThemeFileDAO extends AbstractDAO<ExportThemeFile> {
  
  /**
  * Creates new exportThemeFile
  *
  * @param id id
  * @param path path
  * @param content content
  * @param theme theme
  * @param lastModifier modifier
  * @return created exportThemeFile
  */
  public ExportThemeFile create(UUID id, ExportTheme theme, String path, String content, UUID creator, UUID lastModifier) {
    ExportThemeFile exportThemeFile = new ExportThemeFile();
    exportThemeFile.setId(id);
    exportThemeFile.setPath(path);
    exportThemeFile.setContent(content);
    exportThemeFile.setTheme(theme);
    exportThemeFile.setLastModifier(lastModifier);
    exportThemeFile.setCreator(creator);
    return persist(exportThemeFile);
  }

  /**
   * Finds a theme file by theme and path
   * 
   * @param theme theme
   * @param path path
   * @return Found theme file or null if not found
   */
  public ExportThemeFile findByThemeAndPath(ExportTheme theme, String path) {
    EntityManager entityManager = getEntityManager();
    
    CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
    CriteriaQuery<ExportThemeFile> criteria = criteriaBuilder.createQuery(ExportThemeFile.class);
    Root<ExportThemeFile> root = criteria.from(ExportThemeFile.class);
    criteria.select(root);
    criteria.where(
      criteriaBuilder.and(
        criteriaBuilder.equal(root.get(ExportThemeFile_.theme), theme),
        criteriaBuilder.equal(root.get(ExportThemeFile_.path), path)
      )
    );
    
    TypedQuery<ExportThemeFile> query = entityManager.createQuery(criteria);
    
    return getSingleResult(query);
  }

  /**
   * Lists theme files by theme
   * 
   * @param theme theme
   * @return List of theme files
   */
  public List<ExportThemeFile> listByTheme(ExportTheme theme) {
    EntityManager entityManager = getEntityManager();
    
    CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
    CriteriaQuery<ExportThemeFile> criteria = criteriaBuilder.createQuery(ExportThemeFile.class);
    Root<ExportThemeFile> root = criteria.from(ExportThemeFile.class);
    criteria.select(root);
    criteria.where(criteriaBuilder.equal(root.get(ExportThemeFile_.theme), theme));
    
    TypedQuery<ExportThemeFile> query = entityManager.createQuery(criteria);
    
    return query.getResultList();
  }
  
  /**
  * Updates path
  *
  * @param path path
  * @param lastModifier modifier
  * @return updated exportThemeFile
  */
  public ExportThemeFile updatePath(ExportThemeFile exportThemeFile, String path, UUID lastModifier) {
    exportThemeFile.setLastModifier(lastModifier);
    exportThemeFile.setPath(path);
    return persist(exportThemeFile);
  }
  
  /**
  * Updates content
  *
  * @param content content
  * @param lastModifier modifier
  * @return updated exportThemeFile
  */
  public ExportThemeFile updateContent(ExportThemeFile exportThemeFile, String content, UUID lastModifier) {
    exportThemeFile.setLastModifier(lastModifier);
    exportThemeFile.setContent(content);
    return persist(exportThemeFile);
  }

  /**
  * Updates theme
  *
  * @param theme theme
  * @param lastModifier modifier
  * @return updated exportThemeFile
  */
  public ExportThemeFile updateTheme(ExportThemeFile exportThemeFile, ExportTheme theme, UUID lastModifier) {
    exportThemeFile.setLastModifier(lastModifier);
    exportThemeFile.setTheme(theme);
    return persist(exportThemeFile);
  }

}
