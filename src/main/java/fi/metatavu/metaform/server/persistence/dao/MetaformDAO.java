package fi.metatavu.metaform.server.persistence.dao;

import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;

import fi.metatavu.metaform.server.persistence.model.Metaform;

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
