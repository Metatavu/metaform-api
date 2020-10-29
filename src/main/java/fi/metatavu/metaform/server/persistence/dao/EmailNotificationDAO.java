package fi.metatavu.metaform.server.persistence.dao;

import java.util.List;
import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import fi.metatavu.metaform.server.persistence.model.Metaform;
import fi.metatavu.metaform.server.persistence.model.notifications.EmailNotification;
import fi.metatavu.metaform.server.persistence.model.notifications.EmailNotification_;

/**
 * DAO class for EmailNotification entity
 * 
 * @author Antti Leppä
 */
@ApplicationScoped
public class EmailNotificationDAO extends AbstractDAO<EmailNotification> {
  
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
  public EmailNotification create(UUID id, Metaform metaform, String subjectTemplate, String contentTemplate, String notifyIf) {
    EmailNotification emailNotification = new EmailNotification(); 
    emailNotification.setId(id);
    emailNotification.setMetaform(metaform);
    emailNotification.setSubjectTemplate(subjectTemplate);
    emailNotification.setContentTemplate(contentTemplate);
    emailNotification.setNotifyIf(notifyIf);
    return persist(emailNotification);
  }
  
  /**
   * Lists email notifications by Metaform
   * 
   * @param metaform Metaform
   * @return list of email notifications
   */
  public List<EmailNotification> listByMetaform(Metaform metaform) {
    EntityManager entityManager = getEntityManager();
    
    CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
    CriteriaQuery<EmailNotification> criteria = criteriaBuilder.createQuery(EmailNotification.class);
    Root<EmailNotification> root = criteria.from(EmailNotification.class);
    criteria.select(root);
    criteria.where(criteriaBuilder.equal(root.get(EmailNotification_.metaform), metaform));
    
    TypedQuery<EmailNotification> query = entityManager.createQuery(criteria);
    
    return query.getResultList();
  }

  /**
   * Updates subject template of email notification
   * 
   * @param emailNotification email notification
   * @param subjectTemplate subject template
   * @return updated email notification
   */
  public EmailNotification updateSubjectTemplate(EmailNotification emailNotification, String subjectTemplate) {
    emailNotification.setSubjectTemplate(subjectTemplate);
    return persist(emailNotification);
  }
  
  /**
   * Updates content template of email notification
   * 
   * @param emailNotification email notification
   * @param contentTemplate content template
   * @return updated email notification
   */
  public EmailNotification updateContentTemplate(EmailNotification emailNotification, String contentTemplate) {
    emailNotification.setContentTemplate(contentTemplate);
    return persist(emailNotification);
  }
  
  /**
   * Updates nofify if JSON of email notification
   * 
   * @param emailNotification email notification
   * @param notifyIf notify if JSON
   * @return updated email notification
   */
  public EmailNotification updateNotifyIf(EmailNotification emailNotification, String notifyIf) {
    emailNotification.setNotifyIf(notifyIf);
    return persist(emailNotification);
  }
  
}
