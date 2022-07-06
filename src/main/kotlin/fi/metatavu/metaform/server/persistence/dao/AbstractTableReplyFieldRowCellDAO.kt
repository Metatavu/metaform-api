package fi.metatavu.metaform.server.persistence.dao

import fi.metatavu.metaform.server.persistence.model.TableReplyFieldRow
import fi.metatavu.metaform.server.persistence.model.TableReplyFieldRowCell
import fi.metatavu.metaform.server.persistence.model.TableReplyFieldRowCell_

/**
 * DAO class for TableReplyFieldRow entity
 *
 * @author Antti Leppä
 */
abstract class AbstractTableReplyFieldRowCellDAO<T : TableReplyFieldRowCell?> : AbstractDAO<T>() {
  /**
   * Lists reply field rows by field
   *
   * @param row list reply field
   * @return list of rows
   */
  fun listByRow(row: TableReplyFieldRow?): List<TableReplyFieldRowCell> {
    val criteriaBuilder = entityManager.criteriaBuilder
    val criteria = criteriaBuilder.createQuery(
      TableReplyFieldRowCell::class.java
    )
    val root = criteria.from(
      TableReplyFieldRowCell::class.java
    )
    criteria.select(root)
    criteria.where(criteriaBuilder.equal(root.get(TableReplyFieldRowCell_.row), row))
    return entityManager.createQuery(criteria).resultList
  }
}