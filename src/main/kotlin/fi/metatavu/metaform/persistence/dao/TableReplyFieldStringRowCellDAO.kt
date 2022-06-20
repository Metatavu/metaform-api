package fi.metatavu.metaform.persistence.dao

import fi.metatavu.metaform.persistence.model.TableReplyFieldRow
import fi.metatavu.metaform.persistence.model.TableReplyFieldStringRowCell
import java.util.*
import javax.enterprise.context.ApplicationScoped

/**
 * DAO class for TableReplyFieldStringRowCell entity
 *
 * @author Antti Leppä
 */
@ApplicationScoped
class TableReplyFieldStringRowCellDAO :
  AbstractTableReplyFieldRowCellDAO<TableReplyFieldStringRowCell>() {
  /**
   * Creates new tableReplyFieldStringRowCell
   *
   * @param id id
   * @param row row
   * @param value value
   * @param name name
   * @return created tableReplyFieldStringRowCell
   */
  fun create(
    id: UUID,
    row: TableReplyFieldRow,
    name: String,
    value: String
  ): TableReplyFieldStringRowCell {
    val tableReplyFieldStringRowCell = TableReplyFieldStringRowCell()
    tableReplyFieldStringRowCell.id = id
    tableReplyFieldStringRowCell.row = row
    tableReplyFieldStringRowCell.name = name
    tableReplyFieldStringRowCell.value = value
    return persist(tableReplyFieldStringRowCell)
  }

  /**
   * Updates value
   *
   * @param value value
   * @return updated tableReplyFieldStringRowCell
   */
  fun updateValue(
    tableReplyFieldStringRowCell: TableReplyFieldStringRowCell,
    value: String
  ): TableReplyFieldStringRowCell {
    tableReplyFieldStringRowCell.value = value
    return persist(tableReplyFieldStringRowCell)
  }
}