package fi.metatavu.metaform.server.freemarker;

import java.util.UUID;

import org.apache.commons.lang3.StringUtils;

/**
 * Freemarker template source
 * 
 * @author Antti Leppä
 * @author Heikki Kurhinen
 */
public enum TemplateSource {
  
  EMAIL_CONTENT ("email-content-"),
  EMAIL_SUBJECT ("email-subject-");
  
  TemplateSource(String prefix) {
    this.prefix = prefix;
  }
  
  /**
   * Resolve source from template name
   * 
   * @param name name
   * @return source
   */
  public static TemplateSource resolve(String name) {
    for (TemplateSource templateSource : values()) {
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
