package fi.metatavu.metaform.server.persistence.dao

import fi.metatavu.metaform.server.persistence.model.Metaform
import fi.metatavu.metaform.server.persistence.model.notifications.EmailNotification
import fi.metatavu.metaform.server.persistence.model.notifications.EmailNotification_
import java.util.*
import jakarta.enterprise.context.ApplicationScoped
import jakarta.persistence.TypedQuery
import jakarta.persistence.criteria.CriteriaBuilder
import jakarta.persistence.criteria.CriteriaQuery

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
   * @return created email notification
   */
  fun create(
    id: UUID?,
    metaform: Metaform,
    subjectTemplate: String,
    contentTemplate: String,
    notifyIf: String?
  ): EmailNotification {
    val emailNotification = EmailNotification()
    emailNotification.id = id
    emailNotification.metaform = metaform
    emailNotification.subjectTemplate = subjectTemplate
    emailNotification.contentTemplate = contentTemplate
    emailNotification.notifyIf = notifyIf
    return persist(emailNotification)
  }

  /**
   * Lists email notifications by Metaform
   *
   * @param metaform Metaform
   * @param firstResult first result
   * @param maxResults max results
   * @return list of email notifications
   */
  fun listByMetaform(metaform: Metaform, firstResult: Int?, maxResults: Int?): List<EmailNotification> {
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

    if (firstResult != null) {
      query.firstResult = firstResult
    }

    if (maxResults != null) {
      query.maxResults = maxResults
    }

    return query.resultList
  }

  /**
   * Updates subject template of email notification
   *
   * @param emailNotification email notification
   * @param subjectTemplate subject template
   * @return updated email notification
   */
  fun updateSubjectTemplate(
    emailNotification: EmailNotification,
    subjectTemplate: String
  ): EmailNotification {
    emailNotification.subjectTemplate = subjectTemplate
    return persist(emailNotification)
  }

  /**
   * Updates content template of email notification
   *
   * @param emailNotification email notification
   * @param contentTemplate content template
   * @return updated email notification
   */
  fun updateContentTemplate(
    emailNotification: EmailNotification,
    contentTemplate: String
  ): EmailNotification {
    emailNotification.contentTemplate = contentTemplate
    return persist(emailNotification)
  }

  /**
   * Updates nofify if JSON of email notification
   *
   * @param emailNotification email notification
   * @param notifyIf notify if JSON
   * @return updated email notification
   */
  fun updateNotifyIf(
    emailNotification: EmailNotification,
    notifyIf: String?
  ): EmailNotification {
    emailNotification.notifyIf = notifyIf
    return persist(emailNotification)
  }
}