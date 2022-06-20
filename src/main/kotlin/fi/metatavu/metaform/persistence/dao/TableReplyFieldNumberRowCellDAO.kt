package fi.metatavu.metaform.persistence.dao

import fi.metatavu.metaform.persistence.model.TableReplyFieldNumberRowCell
import fi.metatavu.metaform.persistence.model.TableReplyFieldRow
import java.util.*
import javax.enterprise.context.ApplicationScoped

/**
 * DAO class for TableReplyFieldNumberRowCell entity
 *
 * @author Antti Leppä
 */
@ApplicationScoped
class TableReplyFieldNumberRowCellDAO :
  AbstractTableReplyFieldRowCellDAO<TableReplyFieldNumberRowCell>() {
  /**
   * Creates new tableReplyFieldNumberRowCell
   *
   * @param id id
   * @param row row
   * @param value value
   * @param name name
   * @return created tableReplyFieldNumberRowCell
   */
  fun create(
    id: UUID,
    row: TableReplyFieldRow,
    name: String,
    value: Double
  ): TableReplyFieldNumberRowCell? {
    val tableReplyFieldNumberRowCell = TableReplyFieldNumberRowCell()
    tableReplyFieldNumberRowCell.id = id
    tableReplyFieldNumberRowCell.row = row
    tableReplyFieldNumberRowCell.name = name
    tableReplyFieldNumberRowCell.value = value
    return persist(tableReplyFieldNumberRowCell)
  }

  /**
   * Updates value
   *
   * @param value value
   * @return updated tableReplyFieldNumberRowCell
   */
  fun updateValue(
    tableReplyFieldNumberRowCell: TableReplyFieldNumberRowCell,
    value: Double
  ): TableReplyFieldNumberRowCell? {
    tableReplyFieldNumberRowCell.value = value
    return persist(tableReplyFieldNumberRowCell)
  }
}