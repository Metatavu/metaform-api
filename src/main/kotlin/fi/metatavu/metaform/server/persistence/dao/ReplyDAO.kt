package fi.metatavu.metaform.server.persistence.dao

import fi.metatavu.metaform.api.spec.model.ReplyOrderCriteria
import fi.metatavu.metaform.server.metaform.FieldFilter
import fi.metatavu.metaform.server.metaform.FieldFilterOperator
import fi.metatavu.metaform.server.metaform.FieldFilters
import fi.metatavu.metaform.server.metaform.StoreDataType
import fi.metatavu.metaform.server.persistence.model.*
import java.time.OffsetDateTime
import java.util.*
import java.util.function.Consumer
import java.util.function.Function
import jakarta.enterprise.context.ApplicationScoped
import jakarta.persistence.TypedQuery
import jakarta.persistence.criteria.*

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
    revision: OffsetDateTime?,
    lastModifierId: UUID
  ): Reply {
    val reply = Reply()
    val odtNow = OffsetDateTime.now()
    reply.id = id
    reply.metaform = metaform
    reply.userId = userId
    reply.resourceId = resourceId
    reply.privateKey = privateKey
    reply.revision = revision
    reply.createdAt = odtNow
    reply.lastModifierId = lastModifierId
    reply.modifiedAt = odtNow
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
   * @param orderBy criteria to order by
   * @param latestFirst return the latest result first according to the criteria in orderBy
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
    fieldFilters: FieldFilters?,
    firstResult: Int?,
    maxResults: Int?,
    orderBy: ReplyOrderCriteria,
    latestFirst: Boolean
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

    val attr = root.get(when (orderBy) {
      ReplyOrderCriteria.CREATED -> Reply_.createdAt
      ReplyOrderCriteria.MODIFIED -> Reply_.modifiedAt
    })

    criteria.orderBy(if (latestFirst) {
      criteriaBuilder.desc(attr)
    } else {
      criteriaBuilder.asc(attr)
    })

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
    val query = entityManager.createQuery(criteria)

    if (firstResult != null) {
      query.firstResult = firstResult
    }

    if (maxResults != null) {
      query.maxResults = maxResults
    }

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
    reply.modifiedAt = OffsetDateTime.now()
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
    reply.modifiedAt = OffsetDateTime.now()
    return persist(reply)
  }

  /**
   * Updates last modifier id
   *
   * @param reply reply
   * @param lastModifierId last modifier id
   * @return updated reply
   */
  fun updateLastModifierId(reply: Reply, lastModifierId: UUID): Reply {
    reply.lastModifierId = lastModifierId
    reply.modifiedAt = OffsetDateTime.now()
    return persist(reply)
  }

  /**
   * Gets count of unprocessed replies by Metaform
   *
   * @param metaform metaform
   * @return count of unprocessed replies
   */
  fun countUnprocessedReplies(metaform: Metaform): Long? {
    val criteriaBuilder = entityManager.criteriaBuilder
    val criteria = criteriaBuilder.createQuery(Long::class.java)
    val root = criteria.from(StringReplyField::class.java)
    val replyJoin = root.join(StringReplyField_.reply)

    criteria.select(criteriaBuilder.count(replyJoin))
    criteria.where(
      criteriaBuilder.equal(root.get(StringReplyField_.name), "status"),
      criteriaBuilder.equal(root.get(StringReplyField_.value), "waiting"),
      criteriaBuilder.equal(replyJoin.get(Reply_.metaform), metaform)
    )

    return getSingleResult(entityManager.createQuery(criteria))
  }

  /**
   * Gets date of latest Reply for given Metaform
   *
   * @param metaform metaform
   * @returns date of latest reply
   */
  fun getLastReplyDateByMetaform(metaform: Metaform): OffsetDateTime? {
    val criteriaBuilder = entityManager.criteriaBuilder
    val criteria = criteriaBuilder.createQuery(OffsetDateTime::class.java)
    val root = criteria.from(Reply::class.java)

    criteria.select(criteriaBuilder.greatest(root.get(Reply_.createdAt)))
    criteria.where(criteriaBuilder.equal(root.get(Reply_.metaform), metaform))

    return getSingleResult(entityManager.createQuery(criteria))
  }

  /**
   * Gets average process delay for given Metaforms replies
   *
   * @param metaform metaform
   * @returns average reply process delay
   */
  fun getAverageProcessDelayByMetaform(metaform: Metaform): Double? {
    val criteriaBuilder = entityManager.criteriaBuilder
    val criteria = criteriaBuilder.createQuery(Double::class.java)
    val root = criteria.from(Reply::class.java)

    criteria.select(
            criteriaBuilder.avg(
                    criteriaBuilder.diff(
                            createToSecondsSqlFunction(criteriaBuilder, root.get(Reply_.firstViewedAt)),
                            createToSecondsSqlFunction(criteriaBuilder, root.get(Reply_.createdAt))
                    )
            )
    )

    criteria.where(criteriaBuilder.equal(root.get(Reply_.metaform), metaform))

    return getSingleResult(entityManager.createQuery(criteria))
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

  /**
   * Creates SQL TO_SECONDS function for Criteria Query
   *
   * @param criteriaBuilder criteria builder
   * @param value value to convert to seconds
   * @returns
   */
  private fun createToSecondsSqlFunction(criteriaBuilder: CriteriaBuilder, value: Expression<*>): Expression<Int>? {
    return criteriaBuilder.function(
            "TO_SECONDS",
            Int::class.java,
            value
    )
  }
}