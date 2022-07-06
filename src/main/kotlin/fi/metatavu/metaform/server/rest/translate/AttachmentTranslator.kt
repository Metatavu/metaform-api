package fi.metatavu.metaform.server.rest.translate

import fi.metatavu.metaform.api.spec.model.Attachment
import javax.enterprise.context.ApplicationScoped

/**
 * Translator for attachments
 *
 * @author Antti Leppä
 */
@ApplicationScoped
class AttachmentTranslator {

  /**
   * Translates JPA attachment object into REST attachment object
   *
   * @param attachment JPA attachment object
   * @return REST attachment
   */
  fun translate(attachment: fi.metatavu.metaform.server.persistence.model.Attachment): Attachment {
//    TODO check metadata translator
    return Attachment(
            id = attachment.id,
            contentType = attachment.contentType,
            name = attachment.name
    )
  }
}