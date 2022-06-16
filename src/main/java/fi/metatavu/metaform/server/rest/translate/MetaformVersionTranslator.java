package fi.metatavu.metaform.server.rest.translate;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import fi.metatavu.metaform.server.persistence.model.MetaformVersion;
import org.slf4j.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.Map;


/**
 * Translatro for Metaform versions
 * 
 * @author Tianxing Wu
 */
@ApplicationScoped
public class MetaformVersionTranslator {

  @Inject
  private Logger logger;

  /**
   * Translates JPA Metaform version into REST Metaform version
   * 
   * @param entity JPA Metaform version
   * @return REST Metaform version
   */
  public fi.metatavu.metaform.api.spec.model.MetaformVersion translateMetaformVersion(MetaformVersion entity) {
    if (entity == null) {
      return null;
    }

//     TODO check data translation
    fi.metatavu.metaform.api.spec.model.MetaformVersion result = new fi.metatavu.metaform.api.spec.model.MetaformVersion();
    result.setCreatedAt(entity.getCreatedAt());
    result.setCreatorId(entity.creatorId);
    result.setModifiedAt(entity.getModifiedAt());
    result.setLastModifierId(entity.lastModifierId);
    result.setData(deserializeData(entity.getData()));
    result.setType(entity.getType());
    result.setId(entity.id);
    return result;
  }

  /**
   * Deserializes version data from string
   *
   * @param data data
   * @return version data
   */
  private Map<String, Object> deserializeData(String data) {
    ObjectMapper objectMapper = new ObjectMapper();
    try {
      return objectMapper.readValue(data, new TypeReference<Map<String,Object>>() {});
    } catch (Exception e) {
      logger.error("Failed to read version data", e);
    }

    return null;
  }
}
