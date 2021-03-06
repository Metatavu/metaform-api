package fi.metatavu.metaform.server.persistence.dao;

import java.util.List;
import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;
import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Root;

import fi.metatavu.metaform.server.persistence.model.Attachment;
import fi.metatavu.metaform.server.persistence.model.AttachmentReplyField;
import fi.metatavu.metaform.server.persistence.model.AttachmentReplyFieldItem;
import fi.metatavu.metaform.server.persistence.model.AttachmentReplyFieldItem_;
import fi.metatavu.metaform.server.persistence.model.Attachment_;

/**
 * DAO class for AttachmentReplyFieldItem entity
 * 
 * @author Antti Leppä
 */
@ApplicationScoped
public class AttachmentReplyFieldItemDAO extends AbstractDAO<AttachmentReplyFieldItem> {
  
  /**
   * Creates new attachment reply field item
   * 
   * @param id id
   * @param field field
   * @param attachment attachment
   * @return created AttachmentReplyFieldItem
   */
  public AttachmentReplyFieldItem create(UUID id, AttachmentReplyField field, Attachment attachment) {
    AttachmentReplyFieldItem attachmentReplyFieldItem = new AttachmentReplyFieldItem(); 
    attachmentReplyFieldItem.setId(id);
    attachmentReplyFieldItem.setField(field);
    attachmentReplyFieldItem.setAttachment(attachment);
    return persist(attachmentReplyFieldItem);
  }
  
  /**
   * Attachments reply field items by field
   * 
   * @param field attachment reply field
   * @return attachment of items
   */
  public List<AttachmentReplyFieldItem> listByField(AttachmentReplyField field) {
    EntityManager entityManager = getEntityManager();
    
    CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
    CriteriaQuery<AttachmentReplyFieldItem> criteria = criteriaBuilder.createQuery(AttachmentReplyFieldItem.class);
    Root<AttachmentReplyFieldItem> root = criteria.from(AttachmentReplyFieldItem.class);
    criteria.select(root);
    criteria.where(criteriaBuilder.equal(root.get(AttachmentReplyFieldItem_.field), field));
    
    return entityManager.createQuery(criteria).getResultList();
  }

  /**
   * Finds attachment reply field item by attachment
   * 
   * @param attachment attachment to find reply field item for
   * @return AttachmentReplyFieldItem
   */
  public AttachmentReplyFieldItem findByAttachment(Attachment attachment) {
    EntityManager entityManager = getEntityManager();
    
    CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
    CriteriaQuery<AttachmentReplyFieldItem> criteria = criteriaBuilder.createQuery(AttachmentReplyFieldItem.class);
    Root<AttachmentReplyFieldItem> root = criteria.from(AttachmentReplyFieldItem.class);
    criteria.select(root);
    criteria.where(criteriaBuilder.equal(root.get(AttachmentReplyFieldItem_.attachment), attachment));
    
    return entityManager.createQuery(criteria).getSingleResult();
  }
  
  /**
   * List attachment ids by field
   * 
   * @param field attachment reply field
   * @return attachment of items
   */
  public List<UUID> listAttachmentIdsByField(AttachmentReplyField field) {
    EntityManager entityManager = getEntityManager();
    
    CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
    
    CriteriaQuery<UUID> criteria = criteriaBuilder.createQuery(UUID.class);
    Root<AttachmentReplyFieldItem> root = criteria.from(AttachmentReplyFieldItem.class);
    Join<AttachmentReplyFieldItem, Attachment> attachmentJoin = root.join(AttachmentReplyFieldItem_.attachment);
    
    criteria.select(attachmentJoin.get(Attachment_.id));
    criteria.where(criteriaBuilder.equal(root.get(AttachmentReplyFieldItem_.field), field));
    
    return entityManager.createQuery(criteria).getResultList();
  }

  public AttachmentReplyFieldItem updateAttachment(AttachmentReplyFieldItem attachmentReplyFieldItem, Attachment attachment) {
    attachmentReplyFieldItem.setAttachment(attachment);
    return persist(attachmentReplyFieldItem);
  }

}
