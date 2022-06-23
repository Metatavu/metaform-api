package fi.metatavu.metaform.server.persistence.dao

import fi.metatavu.metaform.server.persistence.model.AttachmentReplyField
import fi.metatavu.metaform.server.persistence.model.Reply
import java.util.*
import javax.enterprise.context.ApplicationScoped

/**
 * DAO class for AttachmentReplyField entity
 *
 * @author Antti Leppä
 */
@ApplicationScoped
class AttachmentReplyFieldDAO : ReplyFieldDAO<AttachmentReplyField>() {
  /**
   * Creates new AttachmentReplyField
   *
   * @param id id
   * @param reply reply
   * @param name field name
   * @return created field
   */
  fun create(id: UUID?, reply: Reply, name: String): AttachmentReplyField? {
    val replyField = AttachmentReplyField()
    replyField.id = id
    replyField.name = name
    replyField.reply = reply
    return persist(replyField)
  }
}