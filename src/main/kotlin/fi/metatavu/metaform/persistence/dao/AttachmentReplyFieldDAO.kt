package fi.metatavu.metaform.persistence.dao

import fi.metatavu.metaform.persistence.model.AttachmentReplyField
import fi.metatavu.metaform.persistence.model.Reply
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