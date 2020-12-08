package fi.metatavu.metaform.server.xlsx;

import fi.metatavu.metaform.server.rest.model.MetaformField;

/**
 * Class for containing source details for a XLSX cell
 */
public class CellSource {
  
  private MetaformField field;
  private CellSourceType type;

  /**
   * Constructor
   * 
   * @param field source field
   * @param type cell type
   */
  public CellSource(MetaformField field, CellSourceType type) {
    this.field = field;
    this.type = type;
  }

  /**
   * Returns source field
   * 
   * @return source field
   */
  public MetaformField getField() {
    return field;
  }

  /**
   * Returns cell type
   * 
   * @return cell type
   */
  public CellSourceType getType() {
    return type;
  }

}
