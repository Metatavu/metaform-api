package fi.metatavu.metaform.server.persistence.dao

import fi.metatavu.metaform.server.persistence.model.ListReplyField
import fi.metatavu.metaform.server.persistence.model.Reply
import java.util.*
import jakarta.enterprise.context.ApplicationScoped

/**
 * DAO class for ListReplyField entity
 *
 * @author Antti Leppä
 */
@ApplicationScoped
class ListReplyFieldDAO : ReplyFieldDAO<ListReplyField>() {

  /**
   * Creates new ListReplyField
   *
   * @param id id
   * @param reply reply
   * @param name field name
   * @return created field
   */
  fun create(id: UUID, reply: Reply, name: String): ListReplyField {
    val replyField = ListReplyField()
    replyField.id = id
    replyField.name = name
    replyField.reply = reply
    return persist(replyField)
  }
}