package fi.metatavu.metaform.server.email;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;

import fi.metatavu.metaform.server.persistence.dao.EmailNotificationDAO;
import fi.metatavu.metaform.server.persistence.model.notifications.EmailNotification;
import freemarker.cache.TemplateLoader;

/**
 * Freemarker template loader for loading templates from database
 * 
 * @author Antti Leppä
 */
@ApplicationScoped
public class EmailFreemarkerTemplateLoader implements TemplateLoader {

  @Inject
  private Logger logger;

  @Inject
  private EmailNotificationDAO emailNotificationDAO;
  
  @Override
  public Object findTemplateSource(String name) {
    return name;
  }

  @Override
  @SuppressWarnings ("squid:S1301")
  public long getLastModified(Object templateSource) {
    String name = (String) templateSource;
    EmailTemplateSource source = EmailTemplateSource.resolve(name);
    if (source == null) {
      return 0l;
    }
    
    int localeIndex = name.indexOf('_');
    UUID id = UUID.fromString(name.substring(source.getPrefix().length(), localeIndex));
    
    switch (source) {
      case EMAIL_SUBJECT:
      case EMAIL_CONTENT:
        EmailNotification notificationEmail = emailNotificationDAO.findById(id);
        if (notificationEmail != null) {
          return notificationEmail.getModifiedAt().toInstant().toEpochMilli();
        }
      break;
      default:
        logger.error("Unknown source {}", source);
      break;
    }
    
    return 0;
  }

  @Override
  @SuppressWarnings ("squid:S1301")
  public Reader getReader(Object templateSource, String encoding) throws IOException {
    String name = (String) templateSource;
    EmailTemplateSource source = EmailTemplateSource.resolve(name);
    if (source == null) {
      return null;
    }

    int localeIndex = name.indexOf('_');
    UUID id = UUID.fromString(name.substring(source.getPrefix().length(), localeIndex));
    
    switch (source) {
      case EMAIL_SUBJECT:
      case EMAIL_CONTENT:
        EmailNotification notificationEmail = emailNotificationDAO.findById(id);
        if (notificationEmail != null) {
          return new StringReader(source == EmailTemplateSource.EMAIL_SUBJECT ? notificationEmail.getSubjectTemplate() : notificationEmail.getContentTemplate());
        }
      break;
      default:
        logger.error("Unknown source {}", source);
      break;
    }
    
    return null;
  }

  @Override
  public void closeTemplateSource(Object templateSource) throws IOException {
    // Template loader is id, so no need to close
  }
  
}
