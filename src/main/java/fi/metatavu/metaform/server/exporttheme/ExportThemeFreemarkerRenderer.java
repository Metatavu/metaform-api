package fi.metatavu.metaform.server.exporttheme;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Locale;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;

import freemarker.ext.beans.BeansWrapperBuilder;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import freemarker.template.Version;

/**
 * Freemarker renderer
 * 
 * @author Antti Leppä
 */
@ApplicationScoped
public class ExportThemeFreemarkerRenderer {
  
  private static final Version VERSION = Configuration.VERSION_2_3_23;

  @Inject
  private Logger logger;
  
  @Inject
  private ExportThemeFreemarkerTemplateLoader freemarkerTemplateLoader;
  
  private Configuration configuration;
  
  /**
   * Initializes renderer
   */
  @PostConstruct
  public void init() {
    configuration = new Configuration(VERSION); 
    configuration.setTemplateLoader(freemarkerTemplateLoader);
    configuration.setDefaultEncoding("UTF-8");
    configuration.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER); 
    configuration.setLogTemplateExceptions(false);
    configuration.setObjectWrapper(new BeansWrapperBuilder(VERSION).build());
    configuration.setLocalizedLookup(false);
  }
  
  /**
   * Renders a freemarker template
   * 
   * @param templateName name of the template
   * @param dataModel data model 
   * @param locale locale
   * @return rendered template
   */
  public String render(String templateName, Object dataModel, Locale locale) {
    Template template = getTemplate(templateName, locale);
    if (template == null) {
      if (logger.isErrorEnabled()) {
        logger.error(String.format("Could not find template %s", templateName));
      }
      
      return null;
    }
    
    Writer out = new StringWriter();
    template.setLocale(locale);
    
    try {
      template.process(dataModel, out);
    } catch (TemplateException | IOException e) {
      logger.error("Failed to render template", e);
    }
    
    return out.toString();
  }
  
  private Template getTemplate(String name, Locale locale) {
    try {
      return configuration.getTemplate(name, locale);
    } catch (IOException e) {
      logger.error("Failed to load template", e);
    }
    
    return null;
  }
  
}
