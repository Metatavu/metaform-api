package fi.metatavu.metaform.server.rest.translate;

import java.io.IOException;
import org.slf4j.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

import fi.metatavu.metaform.server.notifications.EmailNotificationController;
import fi.metatavu.metaform.server.rest.model.EmailNotification;
import fi.metatavu.metaform.server.rest.model.FieldRule;

/**
 * Translator for email notifications
 * 
 * @author Antti Leppä
 * @author Heikki Kurhinen
 */
@ApplicationScoped
public class EmailNotificationTranslator {

  @Inject
  private Logger logger;
  
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
    result.setNotifyIf(deserializeFieldRule(emailNotification.getNotifyIf()));
    
    return result;
  }
  
  /**
   * Deserializes the field rule JSON
   * 
   * @param json field rule JSON
   * @return field rule object
   */
  private FieldRule deserializeFieldRule(String json) {
    if (StringUtils.isBlank(json)) {
      return null;
    }
    
    ObjectMapper objectMapper = new ObjectMapper();
    try {
      return objectMapper.readValue(json, FieldRule.class);
    } catch (IOException e) {
      logger.error("Failed to read notify if rule", e);
    }
    
    return null;
  }
  
}
