package fi.metatavu.metaform.server.persistence.dao

import fi.metatavu.metaform.server.persistence.model.notifications.EmailNotification
import fi.metatavu.metaform.server.persistence.model.notifications.EmailNotificationEmail
import fi.metatavu.metaform.server.persistence.model.notifications.EmailNotificationEmail_
import java.util.*
import jakarta.enterprise.context.ApplicationScoped
import jakarta.persistence.EntityManager
import jakarta.persistence.criteria.CriteriaBuilder
import jakarta.persistence.criteria.CriteriaQuery
import jakarta.persistence.criteria.Root

/**
 * DAO class for EmailNotificationEmail entity
 *
 * @author Antti Leppä
 */
@ApplicationScoped
class EmailNotificationEmailDAO : AbstractDAO<EmailNotificationEmail?>() {
  /**
   * Creates new email notification email
   *
   * @param id id
   * @param emailNotification Email notification
   * @param email email
   * @return created EmailNotificationEmail
   */
  fun create(
    id: UUID,
    emailNotification: EmailNotification,
    email: String
  ): EmailNotificationEmail? {
    val emailNotificationEmail = EmailNotificationEmail()
    emailNotificationEmail.id = id
    emailNotificationEmail.emailNotification = emailNotification
    emailNotificationEmail.email = email
    return persist(emailNotificationEmail)
  }

  /**
   * Lists email notification emails by email notification
   *
   * @param emailNotification Email Notification
   * @return list of email notifications
   */
  fun listByEmailNotification(emailNotification: EmailNotification?): List<EmailNotificationEmail> {
    val criteriaBuilder: CriteriaBuilder = entityManager.criteriaBuilder
    val criteria: CriteriaQuery<EmailNotificationEmail> =
      criteriaBuilder.createQuery(
        EmailNotificationEmail::class.java
      )
    val root: Root<EmailNotificationEmail> = criteria.from(
      EmailNotificationEmail::class.java
    )
    criteria.select(root)
    criteria.where(
      criteriaBuilder.equal(
        root.get(EmailNotificationEmail_.emailNotification),
        emailNotification
      )
    )
    return entityManager.createQuery(criteria).resultList
  }
}