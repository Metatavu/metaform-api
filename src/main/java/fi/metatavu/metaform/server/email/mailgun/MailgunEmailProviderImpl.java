package fi.metatavu.metaform.server.email.mailgun;
import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.microprofile.config.ConfigProvider;
import org.eclipse.microprofile.config.inject.ConfigProperty;
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

  @Inject
  @ConfigProperty(name = "mailgun.domain")
  private String domain;

  @Inject
  @ConfigProperty(name = "mailgun.api_key")
  private String apiKey;

  @Inject
  @ConfigProperty(name = "mailgun.sender_name")
  private String senderName;

  @Inject
  @ConfigProperty(name = "mailgun.sender_email")
  private String senderEmail;

  @Inject
  @ConfigProperty(name = "mailgun.api_url")
  private String apiUrl;
  
  @PostConstruct
  public void init() {
    configuration = new Configuration()
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
