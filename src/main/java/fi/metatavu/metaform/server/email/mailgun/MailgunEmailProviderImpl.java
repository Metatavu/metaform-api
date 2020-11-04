package fi.metatavu.metaform.server.email.mailgun;
import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

import fi.metatavu.metaform.server.email.EmailProvider;
import net.sargue.mailgun.Configuration;
import net.sargue.mailgun.Mail;
import net.sargue.mailgun.MailBuilder;
import net.sargue.mailgun.Response;

/**
 * Mailgun email provider implementation
 * 
 * @author Heikki Kurhinen
 * @author Antti Leppä
 */
@ApplicationScoped
public class MailgunEmailProviderImpl implements EmailProvider {

  private Configuration configuration;
  
  @Inject
  private Logger logger;
  
  @PostConstruct
  public void init() {
    String domain = System.getenv(MailgunConsts.DOMAIN_SETTING_KEY);
    if (StringUtils.isEmpty(domain)) {
      logger.error("Domain setting is missing");
      return;
    }

    String apiKey = System.getenv(MailgunConsts.API_KEY_SETTING_KEY);
    if (StringUtils.isEmpty(apiKey)) {
      logger.error("API key setting is missing");
      return;
    }

    String senderName = System.getenv(MailgunConsts.SENDER_NAME_SETTING_KEY);
    if (StringUtils.isEmpty(senderName)) {
      logger.error("Sender name setting is missing");
      return;
    }

    String senderEmail = System.getenv(MailgunConsts.SENDER_EMAIL_SETTING_KEY);
    if (StringUtils.isEmpty(senderEmail)) {
      logger.error("Sender emaili setting is missing");
      return;
    }
    
    String apiUrl = System.getenv(MailgunConsts.API_URL_SETTING_KEY);

    Configuration configuration = new Configuration()
      .domain(domain)
      .apiKey(apiKey)
      .from(senderName, senderEmail);
    
    if (StringUtils.isNotEmpty(apiUrl)) {
      configuration.apiUrl(apiUrl);
    }
  }
  
  @Override
  @SuppressWarnings ("squid:S3457")
  public void sendMail(String toEmail, String subject, String content, MailFormat format) {
    logger.info("Sending email to {}", toEmail);
    
    MailBuilder mailBuilder = Mail.using(configuration)
      .to(toEmail)
      .subject(subject);
    
    switch (format) {
      case HTML:
        mailBuilder = mailBuilder.html(content);
      break;
      case PLAIN:
        mailBuilder = mailBuilder.text(content);
      break;
      default:
        logger.error("Unknown mail format {}", format);
        return;
    }
    
    Response response = mailBuilder.build().send();
    if (response.isOk()) {
      logger.info("Send email to {}", toEmail);
    } else {
      logger.info("Sending email to {} failed with message {}", toEmail, response.responseMessage());
    }
  }

}
