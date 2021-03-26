package fi.metatavu.metaform.server.rest.translate;


import fi.metatavu.metaform.api.spec.model.Attachment;

import javax.enterprise.context.ApplicationScoped;

/**
 * Translator for attachments
 * 
 * @author Antti Leppä
 */
@ApplicationScoped
public class AttachmentTranslator {
  
  /**
   * Translates JPA attachment object into REST attachment object
   * 
   * @param attachment JPA attachment object
   * @return REST attachment
   */
  public Attachment translateAttachment(fi.metatavu.metaform.server.persistence.model.Attachment attachment) {
    if (attachment == null) {
      return null;
    }

    Attachment result = new Attachment();
    result.setContentType(attachment.getContentType());
    result.setId(attachment.getId());
    result.setName(attachment.getName());
    
    return result;
  }
  
}
