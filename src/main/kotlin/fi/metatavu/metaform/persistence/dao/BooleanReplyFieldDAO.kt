package fi.metatavu.metaform.persistence.dao

import fi.metatavu.metaform.persistence.model.BooleanReplyField
import fi.metatavu.metaform.persistence.model.Reply
import java.util.*
import javax.enterprise.context.ApplicationScoped

/**
 * DAO class for BooleanReplyField entity
 *
 * @author Antti Leppä
 */
@ApplicationScoped
class BooleanReplyFieldDAO : ReplyFieldDAO<BooleanReplyField>() {

  /**
   * Creates new BooleanReplyField
   *
   * @param id id
   * @param reply reply
   * @param name field name
   * @param value field value
   * @return created field
   */
  fun create(id: UUID?, reply: Reply, name: String, value: Boolean): BooleanReplyField {
    val replyField = BooleanReplyField()
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
  fun updateValue(replyField: BooleanReplyField, value: Boolean): BooleanReplyField {
    replyField.value = value
    return persist(replyField)
  }
}