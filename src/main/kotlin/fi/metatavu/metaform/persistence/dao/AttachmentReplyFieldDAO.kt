package fi.metatavu.metaform.persistence.dao

import fi.metatavu.metaform.persistence.model.AttachementReplyField
import fi.metatavu.metaform.persistence.model.Reply
import java.util.*
import javax.enterprise.context.ApplicationScoped

/**
 * DAO class for AttachmentReplyField entity
 *
 * @author Antti Leppä
 */
@ApplicationScoped
class AttachmentReplyFieldDAO : ReplyFieldDAO<AttachementReplyField>() {
  /**
   * Creates new AttachmentReplyField
   *
   * @param id id
   * @param reply reply
   * @param name field name
   * @return created field
   */
  fun create(id: UUID?, reply: Reply, name: String): AttachementReplyField? {
    val replyField = AttachementReplyField()
    replyField.id = id
    replyField.name = name
    replyField.reply = reply
    return persist(replyField)
  }
}