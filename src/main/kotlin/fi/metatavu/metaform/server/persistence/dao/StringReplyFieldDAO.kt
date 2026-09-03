package fi.metatavu.metaform.server.persistence.dao

import fi.metatavu.metaform.server.persistence.model.Reply
import fi.metatavu.metaform.server.persistence.model.Reply_
import fi.metatavu.metaform.server.persistence.model.StringReplyField
import fi.metatavu.metaform.server.persistence.model.StringReplyField_
import java.util.*
import jakarta.enterprise.context.ApplicationScoped

data class ReplyStringFieldValue(val replyId: UUID, val name: String, val value: String?)

/**
 * DAO class for StringReplyField entity
 *
 * @author Antti Leppä
 */
@ApplicationScoped
class StringReplyFieldDAO : ReplyFieldDAO<StringReplyField>() {

  /**
   * Creates new StringReplyField
   *
   * @param id id
   * @param reply reply
   * @param name field name
   * @param value field value
   * @return created field
   */
  fun create(id: UUID, reply: Reply, name: String, value: String): StringReplyField {
    val replyField = StringReplyField()
    replyField.id = id
    replyField.name = name
    replyField.reply = reply
    replyField.value = value
    return persist(replyField)
  }

  /**
   * Updates reply field
   *
   * @param replyField reply field
   * @param value value
   * @return updated field
   */
  fun updateValue(replyField: StringReplyField, value: String): StringReplyField {
    replyField.value = value
    return persist(replyField)
  }

  /**
   * Lists string reply field values for the given replies and field names.
   *
   * @param replyIds reply ids
   * @param names field names
   * @return matching reply field values
   */
  fun listValuesByReplyIdsAndNames(replyIds: Collection<UUID>, names: Collection<String>): List<ReplyStringFieldValue> {
    if (replyIds.isEmpty() || names.isEmpty()) {
      return emptyList()
    }

    val criteriaBuilder = entityManager.criteriaBuilder
    val criteria = criteriaBuilder.createTupleQuery()
    val root = criteria.from(StringReplyField::class.java)
    val replyJoin = root.join(StringReplyField_.reply)

    criteria.multiselect(
      replyJoin.get(Reply_.id).alias("replyId"),
      root.get(StringReplyField_.name).alias("name"),
      root.get(StringReplyField_.value).alias("value")
    )
    criteria.where(
      replyJoin.get(Reply_.id).`in`(replyIds),
      root.get(StringReplyField_.name).`in`(names)
    )

    return entityManager.createQuery(criteria).resultList.map {
      ReplyStringFieldValue(
        replyId = it.get("replyId") as UUID,
        name = it.get("name") as String,
        value = it.get("value") as String?
      )
    }
  }
}