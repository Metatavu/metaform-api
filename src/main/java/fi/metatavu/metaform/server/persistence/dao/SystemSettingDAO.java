package fi.metatavu.metaform.server.persistence.dao;

import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;
import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import fi.metatavu.metaform.server.persistence.model.SystemSetting;
import fi.metatavu.metaform.server.persistence.model.SystemSetting_;

/**
 * DAO class for SystemSetting entity
 * 
 * @author Antti Leppä
 */
@ApplicationScoped
public class SystemSettingDAO extends AbstractDAO<SystemSetting> {

  /**
   * Creates new SystemSetting entity
   * 
   * @param id id
   * @param key key of setting. Must by unique within the system
   * @param value setting value. Not nullable.
   * 
   * @return created SystemSetting entity
   */
  public SystemSetting create(UUID id, String key, String value) {
    SystemSetting systemSetting = new SystemSetting();

    systemSetting.setId(id);
    systemSetting.setKey(key);
    systemSetting.setValue(value);

    return persist(systemSetting);
  }

  /**
   * Finds system setting by key
   * 
   * @param key setting key
   * @return found setting or null if non found
   */
  public SystemSetting findByKey(String key) {
    EntityManager entityManager = getEntityManager();

    CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
    CriteriaQuery<SystemSetting> criteria = criteriaBuilder.createQuery(SystemSetting.class);
    Root<SystemSetting> root = criteria.from(SystemSetting.class);
    criteria.select(root);
    criteria.where(criteriaBuilder.equal(root.get(SystemSetting_.key), key));

    return getSingleResult(entityManager.createQuery(criteria));
  }

  /**
   * Updates system setting value
   * 
   * @param setting system setting
   * @param value new value
   * @return updated system setting
   */
  public SystemSetting updateValue(SystemSetting setting, String value) {
    setting.setValue(value);
    return persist(setting);
  }

}
