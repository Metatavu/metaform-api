package fi.metatavu.metaform.server.persistence.dao

import fi.metatavu.metaform.server.persistence.model.*
import java.util.*
import jakarta.enterprise.context.ApplicationScoped
import jakarta.persistence.criteria.Join

/**
 * DAO class for AttachmentReplyFieldItem entity
 *
 * @author Antti Leppä
 */
@ApplicationScoped
class AttachmentReplyFieldItemDAO : AbstractDAO<AttachmentReplyFieldItem>() {

  /**
   * Creates new attachment reply field item
   *
   * @param id         id
   * @param field      field
   * @param attachment attachment
   * @return created AttachmentReplyFieldItem
   */
  fun create(
    id: UUID?,
    field: AttachmentReplyField,
    attachment: Attachment?
  ): AttachmentReplyFieldItem {
    val attachmentReplyFieldItem = AttachmentReplyFieldItem()
    attachmentReplyFieldItem.id = id
    attachmentReplyFieldItem.field = field
    attachmentReplyFieldItem.attachment = attachment
    return persist(attachmentReplyFieldItem)
  }

  /**
   * Attachments reply field items by field
   *
   * @param field attachment reply field
   * @return attachment of items
   */
  fun listByField(field: AttachmentReplyField): List<AttachmentReplyFieldItem> {
    val criteriaBuilder = entityManager.criteriaBuilder
    val criteria = criteriaBuilder.createQuery(
      AttachmentReplyFieldItem::class.java
    )
    val root = criteria.from(
      AttachmentReplyFieldItem::class.java
    )
    criteria.select(root)
    criteria.where(criteriaBuilder.equal(root.get(AttachmentReplyFieldItem_.field), field))
    return entityManager.createQuery(criteria).resultList
  }

  /**
   * Finds attachment reply field item by attachment
   *
   * @param attachment attachment to find reply field item for
   * @return AttachmentReplyFieldItem
   */
  fun findByAttachment(attachment: Attachment): AttachmentReplyFieldItem? {
    val criteriaBuilder = entityManager.criteriaBuilder
    val criteria = criteriaBuilder.createQuery(
      AttachmentReplyFieldItem::class.java
    )
    val root = criteria.from(
      AttachmentReplyFieldItem::class.java
    )
    criteria.select(root)
    criteria.where(
      criteriaBuilder.equal(root.get(AttachmentReplyFieldItem_.attachment), attachment)
    )
    criteria.groupBy(root[AttachmentReplyFieldItem_.attachment])
    return entityManager.createQuery(criteria).singleResult
  }

  /**
   * List attachment ids by field
   *
   * @param field attachment reply field
   * @return attachment of items
   */
  fun listAttachmentIdsByField(field: AttachmentReplyField): List<UUID> {
    val criteriaBuilder = entityManager.criteriaBuilder
    val criteria = criteriaBuilder.createQuery(
      UUID::class.java
    )
    val root = criteria.from(
      AttachmentReplyFieldItem::class.java
    )
    val attachmentJoin: Join<AttachmentReplyFieldItem, Attachment> =
      root.join(AttachmentReplyFieldItem_.attachment)
    criteria.select(attachmentJoin.get(Attachment_.id))
    criteria.where(criteriaBuilder.equal(root.get(AttachmentReplyFieldItem_.field), field))
    return entityManager.createQuery(criteria).resultList
  }

  fun updateAttachment(
    attachmentReplyFieldItem: AttachmentReplyFieldItem,
    attachment: Attachment?
  ): AttachmentReplyFieldItem {
    attachmentReplyFieldItem.attachment = attachment
    return persist(attachmentReplyFieldItem)
  }
}