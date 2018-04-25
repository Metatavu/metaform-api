package fi.metatavu.metaform.server.metaforms;

import java.util.List;
import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import fi.metatavu.metaform.server.persistence.dao.MetaformDAO;
import fi.metatavu.metaform.server.persistence.model.Metaform;

/**
 * Metaform controller
 * 
 * @author Antti Leppä
 */
@ApplicationScoped
public class MetaformController {
 
  @Inject
  private MetaformDAO metaformDAO;
  
  /**
   * Creates new Metaform
   * 
   * @param realmId realm id
   * @param data form JSON
   * @return Metaform
   */
  public Metaform createMetaform(String realmId, String data) {
    UUID id = UUID.randomUUID();
    return metaformDAO.create(id, realmId, data);    
  }

  /**
   * Finds Metaform by id
   * 
   * @param id Metaform id
   * @return Metaform
   */
  public Metaform findMetaformById(UUID id) {
    return metaformDAO.findById(id);
  }
  
  /**
   * Lists Metaform from realm
   * 
   * @param realmId realm
   * @return list of Metaforms
   */
  public List<Metaform> listMetaforms(String realmId) {
     return metaformDAO.listByRealmId(realmId);
  }
  
  /**
   * Updates Metaform
   * 
   * @param metaform Metaform
   * @param data form JSON
   * @return Updated Metaform
   */
  public Metaform updateMetaform(Metaform metaform, String data) {
    return metaformDAO.updateData(metaform, data);
  }
  
}
