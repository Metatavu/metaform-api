package fi.metatavu.metaform.server.metaforms;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.StringTokenizer;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import fi.metatavu.metaform.api.spec.model.Metaform;
import fi.metatavu.metaform.api.spec.model.MetaformField;
import fi.metatavu.metaform.api.spec.model.MetaformFieldType;
import fi.metatavu.metaform.api.spec.model.MetaformSection;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;

import fi.metatavu.metaform.server.persistence.dao.AnyReplyFieldDAO;
import fi.metatavu.metaform.server.persistence.dao.AnyTableReplyFieldRowCellDAO;
import fi.metatavu.metaform.server.persistence.dao.AttachmentReplyFieldItemDAO;
import fi.metatavu.metaform.server.persistence.dao.ListReplyFieldItemDAO;
import fi.metatavu.metaform.server.persistence.dao.TableReplyFieldRowDAO;
import fi.metatavu.metaform.server.persistence.model.AttachmentReplyField;
import fi.metatavu.metaform.server.persistence.model.BooleanReplyField;
import fi.metatavu.metaform.server.persistence.model.ListReplyField;
import fi.metatavu.metaform.server.persistence.model.NumberReplyField;
import fi.metatavu.metaform.server.persistence.model.Reply;
import fi.metatavu.metaform.server.persistence.model.ReplyField;
import fi.metatavu.metaform.server.persistence.model.StringReplyField;
import fi.metatavu.metaform.server.persistence.model.TableReplyField;
import fi.metatavu.metaform.server.persistence.model.TableReplyFieldNumberRowCell;
import fi.metatavu.metaform.server.persistence.model.TableReplyFieldRow;
import fi.metatavu.metaform.server.persistence.model.TableReplyFieldRowCell;
import fi.metatavu.metaform.server.persistence.model.TableReplyFieldStringRowCell;
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

  @Inject
  private AttachmentReplyFieldItemDAO attachmentReplyFieldItemDAO;
  
  @Inject
  private TableReplyFieldRowDAO tableReplyFieldRowDAO;
  
  @Inject
  private AnyTableReplyFieldRowCellDAO anyTableReplyFieldRowCellDAO;
  
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
  public MetaformFieldType getFieldType(Metaform metaformEntity, String name) {
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
   * @param replyFieldMap map containing reply fields
   * @return field value or null if not found
   */
  public Object getFieldValue(Metaform metaformEntity, Reply reply, String fieldName, Map<String, ReplyField> replyFieldMap) {
    if (isMetafield(metaformEntity, fieldName)) {
      return resolveMetaField(fieldName, reply);
    }
    
    ReplyField field = replyFieldMap.get(fieldName);
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
      return listReplyFieldItemDAO.listItemValuesByField((ListReplyField) field);
    } else if (field instanceof AttachmentReplyField) {
      return attachmentReplyFieldItemDAO.listAttachmentIdsByField((AttachmentReplyField) field);
    } else if (field instanceof TableReplyField) {
      return tableReplyFieldRowDAO.listByField((TableReplyField) field).stream()
        .map(this::getTableRowValue).collect(Collectors.toList());
    } else {
      logger.error("Could not resolve {}", fieldName); 
    }
    
    return null;
  }

  /**
   * Returns table field row as map
   * 
   * @param row row
   * @return table field row as map
   */
  private Map<String, Object> getTableRowValue(TableReplyFieldRow row) {
    return anyTableReplyFieldRowCellDAO.listByRow(row).stream()
      .collect(Collectors.toMap(TableReplyFieldRowCell::getName, cell -> {
        if (cell instanceof TableReplyFieldNumberRowCell) {
          return ((TableReplyFieldNumberRowCell) cell).getValue();
        } else if (cell instanceof TableReplyFieldStringRowCell) {
          return ((TableReplyFieldStringRowCell) cell).getValue();
        }
        
        return null;
      }));
  }
  
  /**
   * Returns whether form field is a meta field
   * 
   * @param metaformEntity form
   * @param name name
   * @return whether form field is a meta field
   */
  private boolean isMetafield(Metaform metaformEntity, String name) {
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
  public MetaformField getField(Metaform metaformEntity, String name) {
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
   * Returns field name <> type map from Metaform
   * 
   * @param metaformEntity Metaform REST entity
   * @return field name <> type map
   */
  public Map<String, MetaformField> getFieldMap(Metaform metaformEntity) {
    return metaformEntity.getSections().stream()
      .map(MetaformSection::getFields)
      .flatMap(List::stream)
      .collect(Collectors.toMap(MetaformField::getName, field -> field));
  }

  /**
   * Lists field name by type
   * 
   * @param metaformEntity Metaform
   * @param type type
   * @return field names by type
   */
  public List<String> getFieldNamesByType(Metaform metaformEntity, MetaformFieldType type) {
    return metaformEntity.getSections().stream()
      .map(MetaformSection::getFields)
      .flatMap(List::stream)
      .filter(field -> type.equals(field.getType()))
      .map(MetaformField::getName)
      .collect(Collectors.toList());
  }

  /**
   * Returns map of reply fields where reply field name is a key and the field value
   * 
   * @param reply reply
   * @return map of reply fields where reply field name is a key and the field value
   */
  public Map<String, ReplyField> getReplyFieldMap(Reply reply) {
    return anyReplyfieldDAO.listByReply(reply).stream().collect(Collectors.toMap(ReplyField::getName, replyField -> replyField));
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
