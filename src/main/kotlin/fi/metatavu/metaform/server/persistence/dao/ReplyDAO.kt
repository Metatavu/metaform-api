package fi.metatavu.metaform.server.persistence.dao

import fi.metatavu.metaform.server.metaform.FieldFilter
import fi.metatavu.metaform.server.metaform.FieldFilterOperator
import fi.metatavu.metaform.server.metaform.FieldFilters
import fi.metatavu.metaform.server.metaform.StoreDataType
import fi.metatavu.metaform.server.persistence.model.*
import org.slf4j.Logger
import java.time.OffsetDateTime
import java.util.*
import java.util.function.Consumer
import java.util.function.Function
import javax.enterprise.context.ApplicationScoped
import javax.inject.Inject
import javax.persistence.EntityManager
import javax.persistence.TypedQuery
import javax.persistence.criteria.*

/**
 * DAO class for Reply entity
 *
 * @author Antti Leppä
 */
@ApplicationScoped
class ReplyDAO : AbstractDAO<Reply>() {

  /**
   * Creates new reply
   *
   * @param id id
   * @param userId user id
   * @param metaform Metaform
   * @param resourceId authorization resource id
   * @param privateKey private key
   * @param revision revision
   * @return created Metaform
   */
  fun create(
    id: UUID,
    userId: UUID,
    metaform: Metaform,
    resourceId: UUID?,
    privateKey: ByteArray?,
    revision: OffsetDateTime?
  ): Reply {
    val reply = Reply()
    reply.id = id
    reply.metaform = metaform
    reply.userId = userId
    reply.resourceId = resourceId
    reply.privateKey = privateKey
    reply.revision = revision 
    return persist(reply)
  }

  /**
   * Finds reply by Metaform, user id and null revision.
   *
   * @param metaform Metaform
   * @param userId userId
   * @return reply
   */
  fun findByMetaformAndUserIdAndRevisionNull(metaform: Metaform, userId: UUID): Reply? {
    val criteriaBuilder: CriteriaBuilder = entityManager.criteriaBuilder
    val criteria: CriteriaQuery<Reply> = criteriaBuilder.createQuery<Reply>(
      Reply::class.java
    )
    val root = criteria.from(
      Reply::class.java
    )
    criteria.select(root)
    criteria.where(
      criteriaBuilder.and(
        criteriaBuilder.equal(root.get(Reply_.metaform), metaform),
        criteriaBuilder.equal(root.get(Reply_.userId), userId),
        criteriaBuilder.isNull(root.get(Reply_.revision))
      )
    )
    return getSingleResult<Reply>(entityManager.createQuery(criteria))
  }

  /**
   * Lists replies by Metaform
   *
   * @param metaform Metaform
   * @return list of replies
   */
  fun listByMetaform(metaform: Metaform): List<Reply> {
    val criteriaBuilder: CriteriaBuilder = entityManager.criteriaBuilder
    val criteria: CriteriaQuery<Reply> = criteriaBuilder.createQuery(
      Reply::class.java
    )
    val root = criteria.from(
      Reply::class.java
    )
    criteria.select(root)
    criteria.where(criteriaBuilder.equal(root.get(Reply_.metaform), metaform))
    val query: TypedQuery<Reply> = entityManager.createQuery(criteria)
    return query.resultList
  }

  /**
   * List replies by multiple filters.
   *
   * All parameters can be nulled. Nulled parameters will be ignored.
   *
   * @param metaform Metaform
   * @param userId userId
   * @param revisionNull true to include only null replies with null revision, false to only non null revisions.
   * @param createdBefore filter results by created before specified time.
   * @param createdAfter filter results by created after specified time.
   * @param modifiedBefore filter results by modified before specified time.
   * @param modifiedAfter filter results by modified after specified time.
   * @param fieldFilters field filters
   * @return replies list of replies
   */
  fun list(
    metaform: Metaform?,
    userId: UUID?,
    includeRevisions: Boolean,
    createdBefore: OffsetDateTime?,
    createdAfter: OffsetDateTime?,
    modifiedBefore: OffsetDateTime?,
    modifiedAfter: OffsetDateTime?,
    fieldFilters: FieldFilters?
  ): List<Reply> {
    val criteriaBuilder: CriteriaBuilder = entityManager.criteriaBuilder
    val criteria: CriteriaQuery<Reply> = criteriaBuilder.createQuery(
      Reply::class.java
    )
    val root = criteria.from(
      Reply::class.java
    )
    val restrictions: MutableList<Predicate> = ArrayList()
    if (metaform != null) {
      restrictions.add(criteriaBuilder.equal(root.get(Reply_.metaform), metaform))
    }
    if (userId != null) {
      restrictions.add(criteriaBuilder.equal(root.get(Reply_.userId), userId))
    }
    if (!includeRevisions) {
      restrictions.add(criteriaBuilder.isNull(root.get(Reply_.revision)))
    }
    if (createdBefore != null) {
      restrictions.add(criteriaBuilder.lessThanOrEqualTo(root.get(Reply_.createdAt), createdBefore))
    }
    if (createdAfter != null) {
      restrictions.add(
        criteriaBuilder.greaterThanOrEqualTo(
          root.get(Reply_.createdAt),
          createdAfter
        )
      )
    }
    if (modifiedBefore != null) {
      restrictions.add(
        criteriaBuilder.lessThanOrEqualTo(
          root.get(Reply_.modifiedAt),
          modifiedBefore
        )
      )
    }
    if (modifiedAfter != null) {
      restrictions.add(
        criteriaBuilder.greaterThanOrEqualTo(
          root.get(Reply_.modifiedAt),
          modifiedAfter
        )
      )
    }
    fieldFilters?.filters?.stream()?.forEach(Consumer { fieldFilter: FieldFilter ->
      val valuePredicate =
        getFieldFilterValuePredicate(criteriaBuilder, criteria, root, fieldFilter)
      if (fieldFilter.operator == FieldFilterOperator.NOT_EQUALS) {
        restrictions.add(
          criteriaBuilder.or(
            valuePredicate,
            criteriaBuilder.not(
              criteriaBuilder.`in`(root)
                .value(createFieldPresentQuery(criteriaBuilder, criteria, fieldFilter.field))
            )
          )
        )
      } else {
        restrictions.add(valuePredicate)
      }
    })
    criteria.select(root)
    criteria.where(criteriaBuilder.and(*restrictions.toTypedArray()))
    criteria.orderBy(criteriaBuilder.asc(root.get(Reply_.createdAt)))
    val query: TypedQuery<Reply> = entityManager.createQuery(criteria)
    return query.resultList
  }

  /**
   * Returns value predicate for field filter query
   *
   * @param criteriaBuilder criteria builder
   * @param criteria criteria
   * @param root root
   * @param fieldFilter filter
   * @return value predicate for field filter query
   */
  private fun getFieldFilterValuePredicate(
    criteriaBuilder: CriteriaBuilder,
    criteria: CriteriaQuery<Reply>,
    root: Root<Reply>,
    fieldFilter: FieldFilter
  ): Predicate {
    val valueSubquery: Subquery<Reply> = if (fieldFilter.dataType == StoreDataType.LIST) {
      createListFieldFilterSubquery(criteriaBuilder, criteria, fieldFilter)
    } else {
      val rootClass = getFieldFilterSubqueryRootClass(fieldFilter.dataType)
      createFieldFilterSubquery(
        rootClass = rootClass,
        fieldFilter = fieldFilter,
        criteriaBuilder = criteriaBuilder,
        criteria = criteria,
        valueFieldFunction = { fieldRoot ->
          getFieldFilterSubqueryReplyField(
            storeDataType = fieldFilter.dataType,
            fieldRoot = fieldRoot
          )
        }
      )
    }

    return criteriaBuilder.`in`(root).value(valueSubquery)
  }

  /**
   * Updates reply revision field
   *
   * @param reply reply
   * @param revision revision time
   * @return updated reply
   */
  fun updateRevision(reply: Reply, revision: OffsetDateTime?): Reply? {
    reply.revision = revision
    return persist(reply)
  }

  /**
   * Updates authorization resource id
   *
   * @param reply reply
   * @param resourceId authorization resource id
   * @return updated reply
   */
  fun updateResourceId(reply: Reply, resourceId: UUID?): Reply {
    reply.resourceId = resourceId
    return persist(reply)
  }

  /**
   * Creates subquery for quering existing fields by name
   *
   * @param criteriaBuilder criteria builder
   * @param criteria criteria
   * @param field field name
   * @return subquery for quering existing fields by name
   */
  private fun createFieldPresentQuery(
    criteriaBuilder: CriteriaBuilder,
    criteria: CriteriaQuery<Reply>,
    field: String
  ): Subquery<Reply> {
    val fieldSubquery: Subquery<Reply> = criteria.subquery(
      Reply::class.java
    )
    val root: Root<ReplyField> = fieldSubquery.from<ReplyField>(
      ReplyField::class.java
    )
    fieldSubquery.select(root.get(ReplyField_.reply))
    fieldSubquery.where(criteriaBuilder.equal(root.get(ReplyField_.name), field))
    return fieldSubquery
  }

  /**
   * Returns field filter reply field
   *
   * @param storeDataType store data type
   * @param fieldRoot field root
   * @return field filter reply field
   */
  private fun getFieldFilterSubqueryReplyField(
      storeDataType: StoreDataType,
      fieldRoot: Root<out ReplyField>
  ): Expression<*>? {
    return when (storeDataType) {
      StoreDataType.STRING -> return (fieldRoot as Root<StringReplyField>).get(
          StringReplyField_.value)
      StoreDataType.BOOLEAN -> return (fieldRoot as Root<BooleanReplyField>).get(
          BooleanReplyField_.value)
      StoreDataType.NUMBER -> return (fieldRoot as Root<NumberReplyField>).get(
          NumberReplyField_.value)
      else -> {
        logger.error("Could not resolve reply field for {}", storeDataType)
        null
      }
    }
  }

  /**
   * Resolves field filter root class
   *
   * @param storeDataType store data type
   * @return field filter root class
   */
  private fun getFieldFilterSubqueryRootClass(storeDataType: StoreDataType): Class<out ReplyField>? {
    return when (storeDataType) {
      StoreDataType.STRING -> return StringReplyField::class.java
      StoreDataType.BOOLEAN -> return BooleanReplyField::class.java
      StoreDataType.NUMBER -> return NumberReplyField::class.java
      else -> {
        logger.error("Could not resolve root class for {}", storeDataType)
        null
      }
    }
  }

  /**
   * Creates a field filter subquery
   *
   * @param criteriaBuilder criteria builder
   * @param criteria criteria
   * @param filters filters
   * @return field filter subquery
   */
  private fun createListFieldFilterSubquery(
    criteriaBuilder: CriteriaBuilder,
    criteria: CriteriaQuery<Reply>,
    filter: FieldFilter
  ): Subquery<Reply> {
    val fieldSubquery: Subquery<Reply> = criteria.subquery(
      Reply::class.java
    )
    val root: Root<ListReplyFieldItem> = fieldSubquery.from(
      ListReplyFieldItem::class.java
    )
    val fieldJoin: Join<ListReplyFieldItem, ListReplyField> = root.join(ListReplyFieldItem_.field)
    val valuePredicate: Predicate =
      if (filter.operator == FieldFilterOperator.NOT_EQUALS) criteriaBuilder.notEqual(
        root.get(ListReplyFieldItem_.value), filter.value
      ) else criteriaBuilder.equal(root.get(ListReplyFieldItem_.value), filter.value)
    fieldSubquery.select(fieldJoin.get(ListReplyField_.reply))
    fieldSubquery.where(
      valuePredicate,
      criteriaBuilder.equal(fieldJoin.get(ListReplyField_.name), filter.field)
    )
    return fieldSubquery
  }

  /**
   * Creates a field filter subquery
   *
   * @param rootClass root class
   * @param fieldFilter filter
   * @param criteriaBuilder criteria builder
   * @param criteria criteria
   * @param valueFieldFunction function for resolving value field
   * @return field filter subquery
   */
  private fun <T : ReplyField?> createFieldFilterSubquery(
      rootClass: Class<T>?,
      fieldFilter: FieldFilter,
      criteriaBuilder: CriteriaBuilder,
      criteria: CriteriaQuery<Reply>,
      valueFieldFunction: Function<Root<T>, Expression<*>?>
  ): Subquery<Reply> {
    val fieldSubquery: Subquery<Reply> = criteria.subquery(
      Reply::class.java
    )
    val fieldRoot: Root<T> = fieldSubquery.from<T>(rootClass)
    fieldSubquery.select(fieldRoot.get(ReplyField_.reply))

    val valueField = valueFieldFunction.apply(fieldRoot)

    val valuePredicate: Predicate =
      if (fieldFilter.operator == FieldFilterOperator.EQUALS) criteriaBuilder.equal(
        valueField,
        fieldFilter.value
      ) else criteriaBuilder.notEqual(valueField, fieldFilter.value)

    fieldSubquery.where(
      criteriaBuilder.and(
        criteriaBuilder.equal(
          fieldRoot.get(ReplyField_.name),
          fieldFilter.field
        ), valuePredicate
      )
    )

    return fieldSubquery
  }
}