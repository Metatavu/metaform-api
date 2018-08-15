package fi.metatavu.metaform.server.metaforms;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.StringTokenizer;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;

import fi.metatavu.metaform.server.persistence.dao.AnyReplyFieldDAO;
import fi.metatavu.metaform.server.persistence.dao.ListReplyFieldItemDAO;
import fi.metatavu.metaform.server.persistence.model.BooleanReplyField;
import fi.metatavu.metaform.server.persistence.model.ListReplyField;
import fi.metatavu.metaform.server.persistence.model.ListReplyFieldItem;
import fi.metatavu.metaform.server.persistence.model.NumberReplyField;
import fi.metatavu.metaform.server.persistence.model.Reply;
import fi.metatavu.metaform.server.persistence.model.ReplyField;
import fi.metatavu.metaform.server.persistence.model.StringReplyField;
import fi.metatavu.metaform.server.rest.model.Metaform;
import fi.metatavu.metaform.server.rest.model.MetaformField;
import fi.metatavu.metaform.server.rest.model.MetaformFieldType;
import fi.metatavu.metaform.server.rest.model.MetaformSection;

/**
 * Field controller
 * 
 * @author Antti Leppä
 */
@ApplicationScoped
public class FieldController {
  
  @Inject
  private Logger logger;

  @Inject
  private FieldTypeMapper fieldTypeMapper;

  @Inject
  private AnyReplyFieldDAO anyReplyfieldDAO;

  @Inject
  private ListReplyFieldItemDAO listReplyFieldItemDAO;
  
  /**
   * Parses field filters
   * 
   * @param metaform metaform
   * @param filterList list of filters
   * @return parsed filters
   */
  public FieldFilters parseFilters(Metaform metaform, List<String> filterList) {
    if (filterList == null) {
      return null;
    }
    
    List<String> filterListCombined = new ArrayList<>(filterList.size());
    
    filterList.stream()
      .filter(StringUtils::isNoneEmpty)
      .forEach(filter -> filterListCombined.addAll(Arrays.asList(StringUtils.split(filter, ','))));
    
    List<FieldFilter> filters = filterListCombined.stream()
      .map(filter -> parseFilter(metaform, filter))
      .filter(Objects::nonNull)
      .collect(Collectors.toList());
    
    if (filters == null || filters.isEmpty()) {
      return null;
    }
    
    return new FieldFilters(filters);
  }

  /**
   * Resolves field type for a field name
   * 
   * @param metaformEntity metaform
   * @param name field name
   * @return field type for a field name
   */
  public MetaformFieldType getFieldType(fi.metatavu.metaform.server.rest.model.Metaform metaformEntity, String name) {
    MetaformField field = getField(metaformEntity, name);
    if (field == null) {
      return null;
    }
    
    return field.getType();
  }

  /**
   * Returns field value for a reply
   * 
   * @param metaformEntity metaform model
   * @param reply reply
   * @param fieldName field name
   * @return field value or null if not found
   */
  public Object getFieldValue(Metaform metaformEntity, Reply reply, String fieldName) {
    if (isMetafield(metaformEntity, fieldName)) {
      return resolveMetaField(fieldName, reply);
    }
    
    ReplyField field = getReplyField(reply, fieldName);
    if (field == null) {
      return null;
    }
    
    if (field instanceof NumberReplyField) {
      return ((NumberReplyField) field).getValue();
    } else if (field instanceof BooleanReplyField) {
      return ((BooleanReplyField) field).getValue();
    } else if (field instanceof StringReplyField) {
      return ((StringReplyField) field).getValue();
    } else if (field instanceof ListReplyField) {
      return listReplyFieldItemDAO.listByField((ListReplyField) field).stream()
        .map(ListReplyFieldItem::getValue)
        .collect(Collectors.toList());
    } else {
      logger.error("Could not resolve {}", fieldName); 
    }
    
    return null;
  }
  
  private ReplyField getReplyField(Reply reply, String fieldName) {
    return anyReplyfieldDAO.findByReplyAndName(reply, fieldName);
  }

  /**
   * Returns whether form field is a meta field
   * 
   * @param metaformEntity form
   * @param name name
   * @return whether form field is a meta field
   */
  private boolean isMetafield(fi.metatavu.metaform.server.rest.model.Metaform metaformEntity, String name) {
    MetaformField field = getField(metaformEntity, name);
    return field != null && field.getContexts() != null && field.getContexts().contains("META");
  }
  
  /**
   * Returns field by name
   * 
   * @param metaformEntity form
   * @param name name
   * @return field
   */
  public MetaformField getField(fi.metatavu.metaform.server.rest.model.Metaform metaformEntity, String name) {
    List<MetaformSection> sections = metaformEntity.getSections();
    
    for (MetaformSection section : sections) {
      for (MetaformField field : section.getFields()) {
        if (name.equals(field.getName())) {
          return field;
        }
      }
    }
    
    return null;
  }
  
  /**
   * Resolves meta field
   * 
   * @param fieldName field name
   * @param entity reply
   * @return meta field value
   */
  public Object resolveMetaField(String fieldName, fi.metatavu.metaform.server.persistence.model.Reply entity) {
    switch (fieldName) {
      case "lastEditor":
        return entity.getUserId();
      case "created":
        return formatDateTime(entity.getCreatedAt());
      case "createdAt":
          return formatDateTime(entity.getCreatedAt());
      case "modified":
        return formatDateTime(entity.getModifiedAt());
      default:
        logger.warn("Metafield {} not recognized", fieldName);
      break;
    }
    
    return null;
  }
  
  /**
   * Parses a field filter
   * 
   * @param metaformEntity
   * @param filter
   * @return
   */
  private FieldFilter parseFilter(Metaform metaformEntity, String filter) {
    List<String> tokenizedFilter = tokenizeFilter(filter);
    if (tokenizedFilter.size() == 3) {
      String fieldName = tokenizedFilter.get(0);
      String operatorString = tokenizedFilter.get(1);
      String valueString = tokenizedFilter.get(2);
      MetaformFieldType fieldType = getFieldType(metaformEntity, fieldName);
      StoreDataType storeDataType = fieldTypeMapper.getStoreDataType(fieldType);
      FieldFilterOperator operator = null;
      
      if (":".equals(operatorString)) {
        operator = FieldFilterOperator.EQUALS;
      } else  if ("^".equals(operatorString)) {
        operator = FieldFilterOperator.NOT_EQUALS;
      } else { 
        logger.error("Could not parse operator string {}", operatorString);
        return null;
      }
      
      if (storeDataType == null || storeDataType == StoreDataType.NONE) {
        return null;
      }
      
      return new FieldFilter(storeDataType, fieldName, getFieldFilterValue(storeDataType, valueString), operator);
    }
    
    return null;
  }
  
  /**
   * Tokenizes a field filter
   * 
   * @param filter filter string
   * @return tokenized filter
   */
  private List<String> tokenizeFilter(String filter) {
    StringTokenizer stringTokenizer = new StringTokenizer(filter, ":^", true);
    List<String> result = new ArrayList<>(3);
    
    while (stringTokenizer.hasMoreTokens()) {
      result.add(stringTokenizer.nextToken());
    }
    
    return result;
  }
  
  /**
   * Resolves field filter value
   * 
   * @param storeDataType store data type
   * @param valueString value as string
   * @return field filter value
   */
  private Object getFieldFilterValue(StoreDataType storeDataType, String valueString) {
    if (valueString == null) {
      return null;
    }
    
    switch (storeDataType) {
      case BOOLEAN:
        return BooleanUtils.toBooleanObject(valueString);
      case LIST:
      case STRING:
        return valueString;
      case NUMBER:
        try {
          return NumberUtils.createDouble(valueString);
        } catch (NumberFormatException e) {
          return null;
        }
      default:
        logger.error("Failed to parse valueString {}", valueString);
    }
    
    return null;
  }
  
  /**
   * Formats date time in ISO date-time format
   * 
   * @param dateTime date time
   * @return date time in ISO date-time format
   */
  private String formatDateTime(OffsetDateTime dateTime) {
    if (dateTime == null) {
      return null;
    }
    
    return dateTime.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
  }
  
}
