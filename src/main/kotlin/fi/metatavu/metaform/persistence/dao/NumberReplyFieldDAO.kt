package fi.metatavu.metaform.persistence.dao

import fi.metatavu.metaform.persistence.model.NumberReplyField
import fi.metatavu.metaform.persistence.model.Reply
import java.util.*
import javax.enterprise.context.ApplicationScoped

/**
 * DAO class for NumberReplyField entity
 *
 * @author Antti Leppä
 */
@ApplicationScoped
class NumberReplyFieldDAO : ReplyFieldDAO<NumberReplyField>() {

  /**
   * Creates new NumberReplyField
   *
   * @param id id
   * @param reply reply
   * @param name field name
   * @param value field value
   * @return created field
   */
  fun create(id: UUID, reply: Reply, name: String, value: Double): NumberReplyField {
    val replyField = NumberReplyField()
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
  fun updateValue(replyField: NumberReplyField, value: Double): NumberReplyField{
    replyField.value = value
    return persist(replyField)
  }
}