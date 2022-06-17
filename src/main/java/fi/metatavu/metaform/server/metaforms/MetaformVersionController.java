package fi.metatavu.metaform.server.metaforms;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import fi.metatavu.metaform.api.spec.model.MetaformVersionType;
import fi.metatavu.metaform.server.persistence.dao.MetaformVersionDAO;
import fi.metatavu.metaform.server.persistence.model.Metaform;
import fi.metatavu.metaform.server.persistence.model.MetaformVersion;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.*;

/**
 * Metaform version controller
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
          Object data,
          UUID userId
  ) {
    ObjectMapper objectMapper = new ObjectMapper();

    try {
      String formDataString = objectMapper.writeValueAsString(data);

      return metaformVersionDAO.create(
              UUID.randomUUID(),
              metaform,
              type,
              formDataString,
              userId,
              userId
      );
    } catch (JsonProcessingException e) {
      return null;
    }
  }

  /**
   * Finds metaform version by Id
   *
   * @param metaformVersionId Metaform version id
   * @return item if found
   */
  public MetaformVersion findMetaformVersionById(UUID metaformVersionId) {
    return metaformVersionDAO.findById(metaformVersionId);
  }

  /**
   * Lists versions by Metaforms
   *
   * @param metaform Metaform
   * @return item if found
   */
  public List<MetaformVersion> listMetaformVersionsByMetaform(Metaform metaform) {
    return metaformVersionDAO.listByMetaform(metaform);
  }
  
  /**
   * Deletes Metaform version
   *
   * @param metaformVersion Metaform version
   */
  public void deleteMetaformVersion(MetaformVersion metaformVersion) {
    metaformVersionDAO.delete(metaformVersion);
  }
}
