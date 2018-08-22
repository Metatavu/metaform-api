package fi.metatavu.metaform.server.persistence.dao;

import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;

import fi.metatavu.metaform.server.persistence.model.Attachment;

/**
 * DAO class for Attachment entity
 * 
 * @author Antti Leppä
 * @author Heikki Kurhinen
 */
@ApplicationScoped
public class AttachmentDAO extends AbstractDAO<Attachment> {
  
  /**
  * Creates new attachment
  *
  * @param id id
  * @param name name
  * @param content content
  * @param contentType contentType
  * @return created attachment
  * @param lastModifier modifier
  */
  public Attachment create(UUID id, String name, byte[] content, String contentType, UUID userId) {
    Attachment attachment = new Attachment();
    attachment.setId(id);
    attachment.setName(name);
    attachment.setContent(content);
    attachment.setContentType(contentType);
    attachment.setUserId(userId);
    return persist(attachment);
  }

  /**
  * Updates name
  *
  * @param name name
  * @return updated attachment
  */
  public Attachment updateName(Attachment attachment, String name) {
    attachment.setName(name);
    return persist(attachment);
  }

  /**
  * Updates content
  *
  * @param content content
  * @return updated attachment
  */
  public Attachment updateContent(Attachment attachment, byte[] content) {
    attachment.setContent(content);
    return persist(attachment);
  }

  /**
  * Updates contentType
  *
  * @param contentType contentType
  * @return updated attachment
  */
  public Attachment updateContentType(Attachment attachment, String contentType) {
    attachment.setContentType(contentType);
    return persist(attachment);
  }

}
