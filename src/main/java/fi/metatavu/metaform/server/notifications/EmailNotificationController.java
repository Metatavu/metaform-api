package fi.metatavu.metaform.server.notifications;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import fi.metatavu.metaform.server.email.EmailProvider;
import fi.metatavu.metaform.server.email.mailgun.MailFormat;
import fi.metatavu.metaform.server.freemarker.FreemarkerRenderer;
import fi.metatavu.metaform.server.freemarker.TemplateSource;
import fi.metatavu.metaform.server.persistence.dao.EmailNotificationDAO;
import fi.metatavu.metaform.server.persistence.dao.EmailNotificationEmailDAO;
import fi.metatavu.metaform.server.persistence.model.Metaform;
import fi.metatavu.metaform.server.persistence.model.notifications.EmailNotification;
import fi.metatavu.metaform.server.persistence.model.notifications.EmailNotificationEmail;
import fi.metatavu.metaform.server.rest.model.Reply;

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
  private EmailProvider emailProvider;

  @Inject
  private FreemarkerRenderer freemarkerRenderer;

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
   * @return created email notification
   */
  public EmailNotification createEmailNotification(Metaform metaform, String subjectTemplate, String contentTemplate, List<String> emails) {
    EmailNotification emailNotification = emailNotificationDAO.create(UUID.randomUUID(), metaform, subjectTemplate, contentTemplate);
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
   * @return updated email notification
   */
  public EmailNotification updateEmailNotification(EmailNotification emailNotification, String subjectTemplate, String contentTemplate, List<String> emails) {
    emailNotificationDAO.updateSubjectTemplate(emailNotification, subjectTemplate);
    emailNotificationDAO.updateContentTemplate(emailNotification, contentTemplate);
    deleteNotificationEmails(emailNotification);
    emails.stream().forEach(email -> emailNotificationEmailDAO.create(UUID.randomUUID(), emailNotification, email));
    return emailNotification;
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
   * Sends email notifications
   * 
   * @param reply reply posted
   */
  public void sendEmailNotifications(Metaform metaform, Reply reply) {
    listEmailNotificationEmails(metaform).stream().forEach(emailNotificationEmail -> sendEmailNotification(reply, emailNotificationEmail));   
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
   * Returns list of email notification emails for a metaform
   * 
   * @param metaform metaform
   * @return list of email notification emails for a metaform
   */
  private List<EmailNotificationEmail> listEmailNotificationEmails(Metaform metaform) {
    return listEmailNotifications(metaform).stream()
      .map(emailNotification -> emailNotificationEmailDAO.listByEmailNotification(emailNotification))
      .flatMap(List::stream)
      .collect(Collectors.toList());
  }
  
  /**
   * Lists email notifications by Metaform
   * 
   * @param metaform Metaform
   * @return list of email notifications
   */
  private List<EmailNotification> listEmailNotifications(Metaform metaform) {
    return emailNotificationDAO.listByMetaform(metaform);
  }
  
  /**
   * Send email notfication
   * 
   * @param reply reply posted
   * @param emailNotificationEmail notification email
   */
  private void sendEmailNotification(Reply reply, EmailNotificationEmail emailNotificationEmail) {
    String email = emailNotificationEmail.getEmail();
    UUID id = emailNotificationEmail.getEmailNotification().getId();
    Map<String, Object> data = toFreemarkerData(reply);
    
    String subject = freemarkerRenderer.render(TemplateSource.EMAIL_SUBJECT.getName(id), data, DEFAULT_LOCALE);
    String content = freemarkerRenderer.render(TemplateSource.EMAIL_CONTENT.getName(id), data, DEFAULT_LOCALE);
    
    emailProvider.sendMail(email, subject, content, MailFormat.HTML);
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
    try {
      return objectMapper.readValue(objectMapper.writeValueAsString(reply), new TypeReference<Map<String, Object>>() { });
    } catch (IOException e) {
      logger.error("Failed to convert reply into freemarker data", e);
    }
    
    return null;
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
