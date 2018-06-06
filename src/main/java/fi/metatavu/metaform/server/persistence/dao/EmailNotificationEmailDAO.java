package fi.metatavu.metaform.server.persistence.dao;

import java.util.List;
import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;
import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import fi.metatavu.metaform.server.persistence.model.notifications.EmailNotification;
import fi.metatavu.metaform.server.persistence.model.notifications.EmailNotificationEmail;
import fi.metatavu.metaform.server.persistence.model.notifications.EmailNotificationEmail_;

/**
 * DAO class for EmailNotificationEmail entity
 * 
 * @author Antti Leppä
 */
@ApplicationScoped
public class EmailNotificationEmailDAO extends AbstractDAO<EmailNotificationEmail> {
  
  /**
   * Creates new email notification email
   * 
   * @param id id
   * @param emailNotification Email notification
   * @return created EmailNotificationEmail
   */
  public EmailNotificationEmail create(UUID id, EmailNotification emailNotification, String email) {
    EmailNotificationEmail emailNotificationEmail = new EmailNotificationEmail(); 
    emailNotificationEmail.setId(id);
    emailNotificationEmail.setEmailNotification(emailNotification);
    emailNotificationEmail.setEmail(email);
    return persist(emailNotificationEmail);
  }
  
  /**
   * Lists email notification emails by email notification
   * 
   * @param emailNotification Email Notification
   * @return list of email notifications
   */
  public List<EmailNotificationEmail> listByEmailNotification(EmailNotification emailNotification) {
    EntityManager entityManager = getEntityManager();
    
    CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
    CriteriaQuery<EmailNotificationEmail> criteria = criteriaBuilder.createQuery(EmailNotificationEmail.class);
    Root<EmailNotificationEmail> root = criteria.from(EmailNotificationEmail.class);
    criteria.select(root);
    criteria.where(criteriaBuilder.equal(root.get(EmailNotificationEmail_.emailNotification), emailNotification));
    
    return entityManager.createQuery(criteria).getResultList();
  }

  
}
