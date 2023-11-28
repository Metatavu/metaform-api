package fi.metatavu.metaform.server.persistence.dao

import fi.metatavu.metaform.server.persistence.model.Attachment
import java.util.*
import jakarta.enterprise.context.ApplicationScoped

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
   * @return created attachment
   */
  fun create(
    id: UUID,
    name: String,
    content: ByteArray,
    contentType: String,
    userId: UUID
  ): Attachment {
    val attachment = Attachment()
    attachment.id = id
    attachment.name = name
    attachment.content = content
    attachment.contentType = contentType
    attachment.userId = userId
    return persist(attachment)
  }

  /**
   * Updates name
   *
   * @param attachment attachment
   * @param name name
   * @return updated attachment
   */
  fun updateName(
    attachment: Attachment,
    name: String
  ): Attachment {
    attachment.name = name
    return persist(attachment)
  }

  /**
   * Updates content
   *
   * @param attachment attachment
   * @param content content
   * @return updated attachment
   */
  fun updateContent(
    attachment: Attachment,
    content: ByteArray
  ): Attachment {
    attachment.content = content
    return persist(attachment)
  }

  /**
   * Updates contentType
   *
   * @param attachment attachment
   * @param contentType contentType
   * @return updated attachment
   */
  fun updateContentType(
    attachment: Attachment,
    contentType: String
  ): Attachment {
    attachment.contentType = contentType
    return persist(attachment)
  }
}