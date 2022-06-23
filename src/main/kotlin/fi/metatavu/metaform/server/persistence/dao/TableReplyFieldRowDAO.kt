package fi.metatavu.metaform.server.persistence.dao

import fi.metatavu.metaform.server.persistence.model.TableReplyField
import fi.metatavu.metaform.server.persistence.model.TableReplyFieldRow
import fi.metatavu.metaform.server.persistence.model.TableReplyFieldRow_
import java.util.*
import javax.enterprise.context.ApplicationScoped

/**
 * DAO class for TableReplyFieldRowCell entity
 *
 * @author Antti Leppä
 */
@ApplicationScoped
class TableReplyFieldRowDAO : AbstractDAO<TableReplyFieldRow>() {
  /**
   * Creates new tableReplyFieldRow
   *
   * @param id id
   * @param field field
   * @return created tableReplyFieldRow
   */
  fun create(id: UUID, field: TableReplyField): TableReplyFieldRow {
    val tableReplyFieldRow = TableReplyFieldRow()
    tableReplyFieldRow.id = id
    tableReplyFieldRow.field = field
    return persist(tableReplyFieldRow)
  }

  /**
   * Lists reply field rows by field
   *
   * @param field list reply field
   * @return list of rows
   */
  fun listByField(field: TableReplyField): List<TableReplyFieldRow> {
    val entityManager = getEntityManager()
    val criteriaBuilder = entityManager.criteriaBuilder
    val criteria = criteriaBuilder.createQuery(
      TableReplyFieldRow::class.java
    )
    val root = criteria.from(
      TableReplyFieldRow::class.java
    )
    criteria.select(root)
    criteria.where(criteriaBuilder.equal(root.get(TableReplyFieldRow_.field), field))
    return entityManager.createQuery(criteria).resultList
  }
}