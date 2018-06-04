package fi.metatavu.metaform.server.metaforms;

/**
 * Field filter
 * 
 * @author Antti Leppä
 */
public class FieldFilter {

  private String field;
  private Object value;
  private StoreDataType dataType;
  private FieldFilterOperator operator;

  /**
   * Constructor
   * 
   * @param dataType store data type
   * @param field field name
   * @param value field value
   * @param operator operator
   */
  public FieldFilter(StoreDataType dataType, String field, Object value, FieldFilterOperator operator) {
    super();
    this.field = field;
    this.value = value;
    this.operator = operator;
    this.dataType = dataType;
  }
  
  public String getField() {
    return field;
  }
  
  public Object getValue() {
    return value;
  }
  
  public StoreDataType getDataType() {
    return dataType;
  }
  
  public FieldFilterOperator getOperator() {
    return operator;
  }

}
