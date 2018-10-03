package fi.metatavu.metaform.server.persistence.dao;

import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;

import fi.metatavu.metaform.server.persistence.model.TableReplyFieldRow;
import fi.metatavu.metaform.server.persistence.model.TableReplyFieldStringRowCell;

/**
 * DAO class for TableReplyFieldStringRowCell entity
 * 
 * @author Antti Leppä
 */
@ApplicationScoped
public class TableReplyFieldStringRowCellDAO extends AbstractTableReplyFieldRowCellDAO<TableReplyFieldStringRowCell> {

  /**
   * Creates new tableReplyFieldStringRowCell
   *
   * @param id id
   * @param row row
   * @param value value
   * @param name name
   * @return created tableReplyFieldStringRowCell
  */
  public TableReplyFieldStringRowCell create(UUID id, TableReplyFieldRow row, String name, String value) {
    TableReplyFieldStringRowCell tableReplyFieldStringRowCell = new TableReplyFieldStringRowCell();
    tableReplyFieldStringRowCell.setId(id);
    tableReplyFieldStringRowCell.setRow(row);
    tableReplyFieldStringRowCell.setName(name);
    tableReplyFieldStringRowCell.setValue(value);    
    return persist(tableReplyFieldStringRowCell);
  }

  /**
  * Updates value
  *
  * @param value value
  * @return updated tableReplyFieldStringRowCell
  */
  public TableReplyFieldStringRowCell updateValue(TableReplyFieldStringRowCell tableReplyFieldStringRowCell, String value) {
    tableReplyFieldStringRowCell.setValue(value);
    return persist(tableReplyFieldStringRowCell);
  }

}
