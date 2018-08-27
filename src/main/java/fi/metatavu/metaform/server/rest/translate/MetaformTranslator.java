package fi.metatavu.metaform.server.rest.translate;

import java.io.IOException;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;

import fi.metatavu.metaform.server.rest.model.Metaform;

/**
 * Translatro for Metaforms
 * 
 * @author Antti Leppä
 */
@ApplicationScoped
public class MetaformTranslator {
  
  @Inject
  private Logger logger;

  /**
   * Translates JPA Metaform into REST Metaform
   * 
   * @param entity JPA Metaform
   * @return REST Metaform
   */
  public Metaform translateMetaform(fi.metatavu.metaform.server.persistence.model.Metaform entity) {
    if (entity == null) {
      return null;
    }
    
    Metaform result = null;
    
    ObjectMapper objectMapper = new ObjectMapper();
    
    try {
      result = objectMapper.readValue(entity.getData(), Metaform.class);
    } catch (IOException e) {
      logger.error(String.format("Failed to translate metaform %s", entity.getId().toString()), e);
    }
    
    if (result != null) {
      result.setId(entity.getId());
    }
    
    if (entity.getExportTheme() != null) {
      result.setExportThemeId(entity.getExportTheme().getId());
    }
    
    return result;
  }
  
}
