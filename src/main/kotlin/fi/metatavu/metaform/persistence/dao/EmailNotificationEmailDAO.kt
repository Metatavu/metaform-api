package fi.metatavu.metaform.persistence.dao

import fi.metatavu.metaform.persistence.model.notifications.EmailNotification
import fi.metatavu.metaform.persistence.model.notifications.EmailNotificationEmail
import fi.metatavu.metaform.persistence.model.notifications.EmailNotificationEmail_
import java.util.*
import javax.enterprise.context.ApplicationScoped
import javax.persistence.EntityManager
import javax.persistence.criteria.CriteriaBuilder
import javax.persistence.criteria.CriteriaQuery
import javax.persistence.criteria.Root

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
    val entityManager: EntityManager = getEntityManager()
    val criteriaBuilder: CriteriaBuilder = entityManager.getCriteriaBuilder()
    val criteria: CriteriaQuery<EmailNotificationEmail> =
      criteriaBuilder.createQuery<EmailNotificationEmail>(
        EmailNotificationEmail::class.java
      )
    val root: Root<EmailNotificationEmail> = criteria.from<EmailNotificationEmail>(
      EmailNotificationEmail::class.java
    )
    criteria.select(root)
    criteria.where(
      criteriaBuilder.equal(
        root.get(EmailNotificationEmail_.emailNotification),
        emailNotification
      )
    )
    return entityManager.createQuery<EmailNotificationEmail>(criteria).getResultList()
  }
}