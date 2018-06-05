package fi.metatavu.metaform.server.email;

import fi.metatavu.metaform.server.email.mailgun.MailFormat;

/**
 * Interface that describes a single email provider
 * 
 * @author Heikki Kurhinen
 * @author Antti Leppä
 */
public interface EmailProvider {
  
  /**
   * Sends an email
   * 
   * @param toEmail recipient's email address
   * @param subject email's subject
   * @param content email's content
   * @param format email format
   */
  public void sendMail(String toEmail, String subject, String content, MailFormat format);

}

