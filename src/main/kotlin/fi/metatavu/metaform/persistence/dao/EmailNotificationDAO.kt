package fi.metatavu.metaform.persistence.dao

import fi.metatavu.metaform.persistence.model.Metaform
import fi.metatavu.metaform.persistence.model.notifications.EmailNotification
import fi.metatavu.metaform.persistence.model.notifications.EmailNotification_
import java.util.*
import javax.enterprise.context.ApplicationScoped
import javax.persistence.EntityManager
import javax.persistence.TypedQuery
import javax.persistence.criteria.CriteriaBuilder
import javax.persistence.criteria.CriteriaQuery

/**
 * DAO class for EmailNotification entity
 *
 * @author Antti Leppä
 */
@ApplicationScoped
class EmailNotificationDAO : AbstractDAO<EmailNotification>() {

  /**
   * Creates new email notification
   *
   * @param id id
   * @param metaform Metaform
   * @param subjectTemplate subject template
   * @param contentTemplate content template
   * @param notifyIf notify if JSON
   * @param creatorId creator id
   * @param lastModifierId creator id
   * @return created email notification
   */
  fun create(
    id: UUID?,
    metaform: Metaform,
    subjectTemplate: String?,
    contentTemplate: String?,
    notifyIf: String?,
    creatorId: UUID,
    lastModifierId: UUID
  ): EmailNotification {
    val emailNotification = EmailNotification()
    emailNotification.id = id
    emailNotification.metaform = metaform
    emailNotification.subjectTemplate = subjectTemplate
    emailNotification.contentTemplate = contentTemplate
    emailNotification.notifyIf = notifyIf
    emailNotification.creatorId = creatorId
    emailNotification.lastModifierId = lastModifierId
    return persist(emailNotification)
  }

  /**
   * Lists email notifications by Metaform
   *
   * @param metaform Metaform
   * @return list of email notifications
   */
  fun listByMetaform(metaform: Metaform): List<EmailNotification> {
    val entityManager: EntityManager = getEntityManager()
    val criteriaBuilder: CriteriaBuilder = entityManager.criteriaBuilder
    val criteria: CriteriaQuery<EmailNotification> = criteriaBuilder.createQuery(
      EmailNotification::class.java
    )
    val root = criteria.from(
      EmailNotification::class.java
    )
    criteria.select(root)
    criteria.where(criteriaBuilder.equal(root.get(EmailNotification_.metaform), metaform))
    val query: TypedQuery<EmailNotification> = entityManager.createQuery(criteria)
    return query.resultList
  }

  /**
   * Updates subject template of email notification
   *
   * @param emailNotification email notification
   * @param subjectTemplate subject template
   * @param lastModifierId modifier id
   * @return updated email notification
   */
  fun updateSubjectTemplate(
    emailNotification: EmailNotification,
    subjectTemplate: String?,
    lastModifierId: UUID,
  ): EmailNotification {
    emailNotification.subjectTemplate = subjectTemplate
    emailNotification.lastModifierId = lastModifierId
    return persist(emailNotification)
  }

  /**
   * Updates content template of email notification
   *
   * @param emailNotification email notification
   * @param contentTemplate content template
   * @param lastModifierId modifier id
   * @return updated email notification
   */
  fun updateContentTemplate(
    emailNotification: EmailNotification,
    contentTemplate: String?,
    lastModifierId: UUID,
  ): EmailNotification {
    emailNotification.contentTemplate = contentTemplate
    emailNotification.lastModifierId = lastModifierId
    return persist(emailNotification)
  }

  /**
   * Updates nofify if JSON of email notification
   *
   * @param emailNotification email notification
   * @param notifyIf notify if JSON
   * @param lastModifierId modifier id
   * @return updated email notification
   */
  fun updateNotifyIf(
    emailNotification: EmailNotification,
    notifyIf: String?,
    lastModifierId: UUID,
  ): EmailNotification {
    emailNotification.notifyIf = notifyIf
    emailNotification.lastModifierId = lastModifierId
    return persist(emailNotification)
  }
}