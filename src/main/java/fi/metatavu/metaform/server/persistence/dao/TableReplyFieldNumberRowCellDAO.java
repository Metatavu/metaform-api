package fi.metatavu.metaform.server.persistence.dao;

import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;

import fi.metatavu.metaform.server.persistence.model.TableReplyFieldNumberRowCell;
import fi.metatavu.metaform.server.persistence.model.TableReplyFieldRow;

/**
 * DAO class for TableReplyFieldNumberRowCell entity
 * 
 * @author Antti Leppä
 */
@ApplicationScoped
public class TableReplyFieldNumberRowCellDAO extends AbstractTableReplyFieldRowCellDAO<TableReplyFieldNumberRowCell> {

  /**
   * Creates new tableReplyFieldNumberRowCell
   *
   * @param id id
   * @param row row
   * @param value value
   * @param name name
   * @return created tableReplyFieldNumberRowCell
   */
  public TableReplyFieldNumberRowCell create(UUID id, TableReplyFieldRow row, String name, Double value) {
    TableReplyFieldNumberRowCell tableReplyFieldNumberRowCell = new TableReplyFieldNumberRowCell();
    tableReplyFieldNumberRowCell.setId(id);
    tableReplyFieldNumberRowCell.setRow(row);
    tableReplyFieldNumberRowCell.setName(name);
    tableReplyFieldNumberRowCell.setValue(value);
    return persist(tableReplyFieldNumberRowCell);
  }

  /**
  * Updates value
  *
  * @param value value
  * @return updated tableReplyFieldNumberRowCell
  */
  public TableReplyFieldNumberRowCell updateValue(TableReplyFieldNumberRowCell tableReplyFieldNumberRowCell, Double value) {
    tableReplyFieldNumberRowCell.setValue(value);
    return persist(tableReplyFieldNumberRowCell);
  }

}
