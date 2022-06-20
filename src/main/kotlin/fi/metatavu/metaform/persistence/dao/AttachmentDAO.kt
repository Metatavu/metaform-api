package fi.metatavu.metaform.persistence.dao

import fi.metatavu.metaform.persistence.model.Attachment
import java.util.*
import javax.enterprise.context.ApplicationScoped

/**
 * DAO class for Attachment entity
 *
 * @author Antti Leppä
 * @author Heikki Kurhinen
 */
@ApplicationScoped
class AttachmentDAO : AbstractDAO<Attachment>() {

  /**
   * Creates new attachment
   *
   * @param id id
   * @param name name
   * @param content content
   * @param contentType contentType
   * @param userId user id
   * @param creatorId creator id
   * @param lastModifierId creator id
   * @return created apptachment
   */
  fun create(
    id: UUID?,
    name: String,
    content: ByteArray,
    contentType: String,
    userId: UUID,
    creatorId: UUID,
    lastModifierId: UUID,
  ): Attachment {
    val attachment = Attachment()
    attachment.id = id
    attachment.name = name
    attachment.content = content
    attachment.contentType = contentType
    attachment.userId = userId
    attachment.creatorId = creatorId
    attachment.lastModifierId = lastModifierId
    return persist(attachment)
  }

  /**
   * Updates name
   *
   * @param name name
   * @param lastModifierId modifier id
   * @return updated attachment
   */
  fun updateName(attachment: Attachment, lastModifierId: UUID, name: String): Attachment {
    attachment.name = name
    attachment.lastModifierId = lastModifierId
    return persist(attachment)
  }

  /**
   * Updates content
   *
   * @param content content
   * @param lastModifierId modifier id
   * @return updated attachment
   */
  fun updateContent(
    attachment: Attachment,
    lastModifierId: UUID,
    content: ByteArray,
  ): Attachment {
    attachment.content = content
    attachment.lastModifierId = lastModifierId
    return persist(attachment)
  }

  /**
   * Updates contentType
   *
   * @param contentType contentType
   * @param lastModifierId modifier id
   * @return updated attachment
   */
  fun updateContentType(
    attachment: Attachment,
    lastModifierId: UUID,
    contentType: String,
  ): Attachment {
    attachment.contentType = contentType
    attachment.lastModifierId = lastModifierId
    return persist(attachment)
  }
}