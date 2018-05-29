package fi.metatavu.metaform.server.metaforms;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;

import fi.metatavu.metaform.server.rest.model.MetaformFieldType;

/**
 * Mapper for mapping field types into store data types
 * 
 * @author anttileppa
 *
 */
@ApplicationScoped
public class FieldTypeMapper {
  
  @Inject
  private Logger logger;
  
  /**
   * Maps field type into store data types
   * 
   * @param fieldType field type
   * @return store type
   */
  @SuppressWarnings ("squid:S3457")
  public StoreDataType getStoreDataType(MetaformFieldType fieldType) {
    if (fieldType == null) {
      logger.error("Failed to resolve field type from null");
      return null;
    }
    
    switch (fieldType) {
      case AUTOCOMPLETE:
        return StoreDataType.STRING;
      case AUTOCOMPLETE_MULTIPLE:
        return StoreDataType.STRING;
      case BOOLEAN:
        return StoreDataType.BOOLEAN;
      case CHECKLIST:
        return StoreDataType.LIST;
      case DATE:
        return StoreDataType.STRING;
      case DATE_TIME:
        return StoreDataType.STRING;
      case EMAIL:
        return StoreDataType.STRING;
      case FILES:
        return StoreDataType.STRING;
      case HIDDEN:
        return StoreDataType.STRING;
      case HTML:
        return StoreDataType.NONE;
      case LOGO:
        return StoreDataType.NONE;
      case MEMO:
        return StoreDataType.STRING;
      case NUMBER:
        return StoreDataType.NUMBER;
      case RADIO:
        return StoreDataType.STRING;
      case SELECT:
        return StoreDataType.STRING;
      case SMALL_TEXT:
        return StoreDataType.NONE;
      case SUBMIT:
        return StoreDataType.NONE;
      case TABLE:
        return StoreDataType.STRING;
      case TEXT:
        return StoreDataType.STRING;
      case TIME:
        return StoreDataType.STRING;
      default:
        logger.error("Failed to resolve field type {}", fieldType);
        return StoreDataType.NONE;
    }    
  }
  
  
  
}
