package fi.metatavu.metaform.server.persistence.dao

import fi.metatavu.metaform.server.persistence.model.Reply
import fi.metatavu.metaform.server.persistence.model.StringReplyField
import java.util.*
import jakarta.enterprise.context.ApplicationScoped

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
}