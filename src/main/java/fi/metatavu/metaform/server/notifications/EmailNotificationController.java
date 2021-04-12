package fi.metatavu.metaform.server.notifications;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import fi.metatavu.metaform.api.spec.model.FieldRule;
import fi.metatavu.metaform.api.spec.model.Reply;
import org.slf4j.Logger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import fi.metatavu.metaform.server.email.EmailFreemarkerRenderer;
import fi.metatavu.metaform.server.email.EmailProvider;
import fi.metatavu.metaform.server.email.EmailTemplateSource;
import fi.metatavu.metaform.server.email.mailgun.MailFormat;
import fi.metatavu.metaform.server.metaforms.FieldRuleEvaluator;
import fi.metatavu.metaform.server.persistence.dao.EmailNotificationDAO;
import fi.metatavu.metaform.server.persistence.dao.EmailNotificationEmailDAO;
import fi.metatavu.metaform.server.persistence.model.Metaform;
import fi.metatavu.metaform.server.persistence.model.notifications.EmailNotification;
import fi.metatavu.metaform.server.persistence.model.notifications.EmailNotificationEmail;
import fi.metatavu.metaform.server.rest.translate.EmailNotificationTranslator;

/**
 * Email notification controller
 * 
 * @author Antti Leppä
 */
@ApplicationScoped
public class EmailNotificationController {

  private static final Locale DEFAULT_LOCALE = new Locale("fi");

  @Inject
  private Logger logger;

  @Inject
  private EmailNotificationTranslator emailNotificationTranslator;
  
  @Inject
  private EmailProvider emailProvider;

  @Inject
  private EmailFreemarkerRenderer freemarkerRenderer;

  @Inject
  private EmailNotificationDAO emailNotificationDAO;
  
  @Inject
  private EmailNotificationEmailDAO emailNotificationEmailDAO;

  /**
   * Creates email notification
   * 
   * @param metaform metaform
   * @param subjectTemplate subject template
   * @param contentTemplate content template
   * @param emails list of email addresses
   * @param notifyIf notify if rule or null if not defined
   * @return created email notification
   * @throws JsonProcessingException thrown when notify if JSON processing fails
   */
  public EmailNotification createEmailNotification(Metaform metaform, String subjectTemplate, String contentTemplate, List<String> emails, FieldRule notifyIf) throws JsonProcessingException {
    EmailNotification emailNotification = emailNotificationDAO.create(UUID.randomUUID(), metaform, subjectTemplate, contentTemplate, serializeFieldRule(notifyIf));
    emails.stream().forEach(email -> emailNotificationEmailDAO.create(UUID.randomUUID(), emailNotification, email));
    return emailNotification;
  }

  /**
   * Finds a email notification
   * 
   * @param id id
   * @return a email notification
   */
  public EmailNotification findEmailNotificationById(UUID id) {
    return emailNotificationDAO.findById(id);
  }

  /**
   * Lists a email notifications from Metaform
   * 
   * @param Metaform metaform
   * @return a email notification
   */
  public List<EmailNotification> listEmailNotificationByMetaform(Metaform metaform) {
    return emailNotificationDAO.listByMetaform(metaform);
  }
  
  /**
   * Updates email notification
   * 
   * @param emailNotification email notification
   * @param subjectTemplate subject template
   * @param contentTemplate content template
   * @param emails list of email addresses
   * @param notifyIf notify if rule or null if not defined
   * @return updated email notification
   * @throws JsonProcessingException thrown when notify if JSON processing fails
   */
  public EmailNotification updateEmailNotification(EmailNotification emailNotification, String subjectTemplate, String contentTemplate, List<String> emails, FieldRule notifyIf) throws JsonProcessingException {
    emailNotificationDAO.updateSubjectTemplate(emailNotification, subjectTemplate);
    emailNotificationDAO.updateContentTemplate(emailNotification, contentTemplate);
    emailNotificationDAO.updateNotifyIf(emailNotification, serializeFieldRule(notifyIf));
    deleteNotificationEmails(emailNotification);
    emails.stream().forEach(email -> emailNotificationEmailDAO.create(UUID.randomUUID(), emailNotification, email));
    return emailNotification;
  }

  /**
   * Returns whether email notification should be send according to notify if rule
   * 
   * @param emailNotification email notification
   * @param replyEntity reply entity
   * @return whether email notification should be send according to notify if rule
   */
  public boolean evaluateEmailNotificationNotifyIf(EmailNotification emailNotification, Reply replyEntity) {
    return evaluateEmailNotificationNotifyIf(emailNotificationTranslator.translateEmailNotification(emailNotification), replyEntity);
  }
  
  /**
   * Returns whether email notification should be send according to notify if rule
   * 
   * @param emailNotificationEntity email notification entity
   * @param replyEntity reply entity
   * @return whether email notification should be send according to notify if rule
   */
  public boolean evaluateEmailNotificationNotifyIf(fi.metatavu.metaform.api.spec.model.EmailNotification emailNotificationEntity, Reply replyEntity) {
    FieldRule notifyIf = emailNotificationEntity.getNotifyIf();
    if (notifyIf != null) {
      return new FieldRuleEvaluator().evaluate(notifyIf, replyEntity);
    }
    
    return true;
  }
  
  /**
   * Returns list of email addresses in email notification
   * 
   * @param emailNotification email notification
   * @return List of email addresses in email notification
   */
  public List<String> getEmailNotificationEmails(EmailNotification emailNotification) {
    return emailNotificationEmailDAO.listByEmailNotification(emailNotification).stream()
      .map(EmailNotificationEmail::getEmail)
      .collect(Collectors.toList());
  }
  
  /**
   * Send email notification
   * 
   * @param emailNotification email notification
   * @param replyEntity reply posted
   * @param emails notify emails
   */
  public void sendEmailNotification(EmailNotification emailNotification, Reply replyEntity, Set<String> emails) {
    UUID id = emailNotification.getId();
    Map<String, Object> data = toFreemarkerData(replyEntity);
    
    String subject = freemarkerRenderer.render(EmailTemplateSource.EMAIL_SUBJECT.getName(id), data, DEFAULT_LOCALE);
    String content = freemarkerRenderer.render(EmailTemplateSource.EMAIL_CONTENT.getName(id), data, DEFAULT_LOCALE);
    
    emails.stream().forEach(email -> emailProvider.sendMail(email, subject, content, MailFormat.HTML));
  }
  
  /**
   * Delete email notification
   * 
   * @param emailNotification email notification
   */
  public void deleteEmailNotification(EmailNotification emailNotification) {
    deleteNotificationEmails(emailNotification);
    emailNotificationDAO.delete(emailNotification);
  }
  
  /**
   * Converts reply to Freemarker data
   * 
   * @param reply reply
   * @return freemarker data
   */
  private Map<String, Object> toFreemarkerData(Reply reply) {
    if (reply == null) {
      return null;
    }
    
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.registerModule(new JavaTimeModule());
    try {
      return objectMapper.readValue(objectMapper.writeValueAsString(reply), new TypeReference<Map<String, Object>>() { });
    } catch (IOException e) {
      logger.error("Failed to convert reply into freemarker data", e);
    }
    
    return null;
  }
  
  /**
   * Serializes field rule as string
   * 
   * @param fieldRule field rule
   * @return serialized field rule
   * @throws JsonProcessingException
   */
  private String serializeFieldRule(FieldRule fieldRule) throws JsonProcessingException {
    if (fieldRule == null) {
      return null;
    }
    
    ObjectMapper objectMapper = new ObjectMapper();
    return objectMapper.writeValueAsString(fieldRule);
  }

  /**
   * Deletes email notification entities
   * 
   * @param emailNotification email notification
   */
  private void deleteNotificationEmails(EmailNotification emailNotification) {
    List<EmailNotificationEmail> emailNotificationEmails = emailNotificationEmailDAO.listByEmailNotification(emailNotification);
    emailNotificationEmails.stream().forEach(emailNotificationEmailDAO::delete);
  }
  
}
