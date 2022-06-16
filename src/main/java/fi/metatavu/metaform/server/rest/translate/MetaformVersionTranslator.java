package fi.metatavu.metaform.server.rest.translate;


import fi.metatavu.metaform.server.persistence.model.MetaformVersion;

import javax.enterprise.context.ApplicationScoped;


/**
 * Translatro for Metaform versions
 * 
 * @author Tianxing Wu
 */
@ApplicationScoped
public class MetaformVersionTranslator {
  
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
    result.setData(entity.getData());
    result.setType(entity.getType());
    return result;
  }
  
}
