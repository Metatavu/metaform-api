package fi.metatavu.metaform.server.persistence.dao

import fi.metatavu.metaform.server.persistence.model.Reply
import fi.metatavu.metaform.server.persistence.model.TableReplyField
import java.util.*
import javax.enterprise.context.ApplicationScoped

/**
 * DAO class for TableReplyField entity
 *
 * @author Antti Leppä
 */
@ApplicationScoped
class TableReplyFieldDAO : ReplyFieldDAO<TableReplyField>() {
  /**
   * Creates new TableReplyField
   *
   * @param id id
   * @param reply reply
   * @param name field name
   * @param value field value
   * @return created field
   */
  fun create(id: UUID?, reply: Reply, name: String): TableReplyField {
    val replyField = TableReplyField()
    replyField.id = id
    replyField.name = name
    replyField.reply = reply
    return persist(replyField)
  }
}