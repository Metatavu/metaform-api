package fi.metatavu.metaform.server.metaforms;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Field filters
 * 
 * @author Antti Leppä
 */
public class FieldFilters {

  private List<FieldFilter> filters;

  /**
   * Constructor
   * 
   * @param filters filters
   */
  public FieldFilters(List<FieldFilter> filters) {
    super();
    this.filters = filters;
  }

  /**
   * Returns all field filters
   * 
   * @return all field filters
   */
  public List<FieldFilter> getFilters() {
    return filters;
  }
  
  /**
   * Returns field filters for single data type
   * 
   * @param storeDataType data type
   * @return field filters for single data type
   */
  public List<FieldFilter> getFilters(StoreDataType storeDataType) {
    return getFilters().stream().filter(fieldFilter -> fieldFilter.getDataType() == storeDataType).collect(Collectors.toList());
  }

}
