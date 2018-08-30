package fi.metatavu.metaform.server.email;

import java.util.UUID;

import org.apache.commons.lang3.StringUtils;

/**
 * Freemarker template source
 * 
 * @author Antti Leppä
 * @author Heikki Kurhinen
 */
public enum EmailTemplateSource {
  
  EMAIL_CONTENT ("email-content-"),
  EMAIL_SUBJECT ("email-subject-");
  
  EmailTemplateSource(String prefix) {
    this.prefix = prefix;
  }
  
  /**
   * Resolve source from template name
   * 
   * @param name name
   * @return source
   */
  public static EmailTemplateSource resolve(String name) {
    for (EmailTemplateSource templateSource : values()) {
      if (StringUtils.startsWith(name, templateSource.prefix)) {
        return templateSource;
      }
    }
    
    return null;
  }
  
  /**
   * Returns prefix
   * 
   * @return prefix
   */
  public String getPrefix() {
    return prefix;
  }

  /**
   * Returns template name for id
   * 
   * @param id id
   * @return template name for id
   */
  public String getName(UUID id) {
    return String.format("%s%s", getPrefix(), id);
  }

  private String prefix;
  
}
