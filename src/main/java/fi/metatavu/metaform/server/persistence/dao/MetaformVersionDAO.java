package fi.metatavu.metaform.server.persistence.dao;

import fi.metatavu.metaform.api.spec.model.MetaformVersionType;
import fi.metatavu.metaform.server.persistence.model.*;
import fi.metatavu.metaform.server.persistence.model.MetaformVersion;
import java.util.*;
import javax.enterprise.context.ApplicationScoped;
import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import fi.metatavu.metaform.server.persistence.model.MetaformVersion_;


/**
 * DAO class for Metaform version entities
 * 
 * @author Tianxing Wu
 */
@ApplicationScoped
public class MetaformVersionDAO extends AbstractDAO<MetaformVersion> {
  
  /**
   * Creates new Metaform version
   * 
   * @param id id
   * @param metaform Metaform
   * @param type Metaform version type
   * @param data Metaform form JSON
   * @param creatorId creator id
   * @param lastModifierId last modifier id
   *
   * @return created Metaform
   */
  public MetaformVersion create(
    UUID id,
    Metaform metaform,
    MetaformVersionType type,
    String data,
    UUID creatorId,
    UUID lastModifierId
  ) {
  MetaformVersion metaformVersion = new MetaformVersion();
    metaformVersion.setId(id);
    metaformVersion.setMetaform(metaform);
    metaformVersion.setType(type);
    metaformVersion.setData(data);
    metaformVersion.setCreatorId(creatorId);
    metaformVersion.setLastModifierId(lastModifierId);

    return persist(metaformVersion);
  }

  /**
   * Lists Metaform version by Metaform
   *
   * @param metaform Metaform
   *
   * @return Metaform versions
   */
  public List<MetaformVersion> listByMetaform(
      Metaform metaform
  ) {

    EntityManager entityManager = getEntityManager();
    CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
    CriteriaQuery<MetaformVersion> criteria = criteriaBuilder.createQuery(MetaformVersion.class);
    Root<MetaformVersion> root = criteria.from(MetaformVersion.class);
    criteria.select(root);
    criteria.where(
            criteriaBuilder.equal(root.get(MetaformVersion_.metaform), metaform)
    );

    return entityManager.createQuery(criteria).getResultList();

  }
}
