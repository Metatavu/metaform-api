package fi.metatavu.metaform.server.exporttheme;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;

import fi.metatavu.metaform.server.persistence.model.ExportThemeFile;
import freemarker.cache.TemplateLoader;

/**
 * Freemarker template loader for loading templates from database
 * 
 * @author Antti Leppä
 */
@ApplicationScoped
public class ExportThemeFreemarkerTemplateLoader implements TemplateLoader {
  
  @Inject
  private Logger logger;

  @Inject
  private ExportThemeController exportThemeController;
  
  @Override
  public Object findTemplateSource(String name) {
    return name;
  }

  @Override
  public long getLastModified(Object templateSource) {
    String path = (String) templateSource;
    ExportThemeFile exportThemeFile = findExportThemeFile(path);
    if (exportThemeFile != null) {
      return exportThemeFile.getModifiedAt().toInstant().toEpochMilli();
    }
    
    return 0;
  }

  @Override
  public Reader getReader(Object templateSource, String encoding) throws IOException {
    String path = (String) templateSource;
    ExportThemeFile exportThemeFile = findExportThemeFile(path);
    if (exportThemeFile != null) {
      return new StringReader(exportThemeFile.getContent());
    }
    
    logger.warn("Could not find export theme file {}", path);
    
    return new StringReader(String.format("!! export theme file %s not found !!", path));
  }
  
  private ExportThemeFile findExportThemeFile(String path) {
    
    
    return exportThemeController.findExportThemeFile(path);
  }

  @Override
  public void closeTemplateSource(Object templateSource) throws IOException {
    // Template loader is id, so no need to close
  }
  
}
