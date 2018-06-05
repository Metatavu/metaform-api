package fi.metatavu.metaform.server.rest.translate;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import fi.metatavu.metaform.server.notifications.EmailNotificationController;
import fi.metatavu.metaform.server.rest.model.EmailNotification;

/**
 * Translator for email notifications
 * 
 * @author Antti Leppä
 * @author Heikki Kurhinen
 */
@ApplicationScoped
public class EmailNotificationTranslator {
  
  @Inject
  private EmailNotificationController emailNotificationController;

  /**
   * Translates JPA email notification object into REST email notification object
   * 
   * @param emailNotification JPA emailNotification object
   * @return REST emailNotification
   */
  public EmailNotification translateEmailNotification(fi.metatavu.metaform.server.persistence.model.notifications.EmailNotification emailNotification) {
    if (emailNotification == null) {
      return null;
    }
    
    EmailNotification result = new EmailNotification();
    result.setEmails(emailNotificationController.getEmailNotificationEmails(emailNotification));
    result.setId(emailNotification.getId());
    result.setSubjectTemplate(emailNotification.getSubjectTemplate());
    result.setContentTemplate(emailNotification.getContentTemplate());
    
    return result;
  }
  
}
