package fi.metatavu.metaform.server.metaforms;

import fi.metatavu.metaform.api.spec.model.MetaformVersionType;
import fi.metatavu.metaform.server.persistence.dao.MetaformVersionDAO;
import fi.metatavu.metaform.server.persistence.model.Metaform;
import fi.metatavu.metaform.server.persistence.model.MetaformVersion;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.*;

/**
 * Metaform controller
 * 
 * @author Tianxing Wu
 */
@ApplicationScoped
public class MetaformVersionController {
 
  @Inject
  private MetaformVersionDAO metaformVersionDAO;

  /**
   * Creates new Metaform version
   *
   * @param metaform Metaform
   * @param type Metaform version type
   * @param data Metaform form JSON
   * @param userId user id
   */
  public MetaformVersion create(
          Metaform metaform,
          MetaformVersionType type,
          String data,
          UUID userId
  ) {
    return metaformVersionDAO.create(
        UUID.randomUUID(),
        metaform,
        type,
        data,
        userId,
        userId
    );
  }

  /**
   * Finds metaform version by Id
   *
   * @param metaformVersionId Metaform version id
   * @return item if found
   */
  public MetaformVersion find(UUID metaformVersionId) {
    return metaformVersionDAO.findById(metaformVersionId);
  }

  /**
   * Lists versions by Metaforms
   *
   * @param metaform Metaform
   * @return item if found
   */
  public List<MetaformVersion> listByMetaform(Metaform metaform) {
    return metaformVersionDAO.listByMetaform(metaform);
  }
  
  /**
   * Lists Metaforms
   * 
   * @return list of Metaforms
   */
  public List<MetaformVersion> list() {
    return metaformVersionDAO.listAll();
  }


  /**
   * Lists Metaforms
   *
   * @return list of Metaforms
   */
  public void delete(MetaformVersion metaformVersion) {
    metaformVersionDAO.delete(metaformVersion);
  }
}
