package fi.metatavu.metaform.server.rest.translate;

import javax.enterprise.context.ApplicationScoped;

import fi.metatavu.metaform.server.rest.model.ExportTheme;
import fi.metatavu.metaform.server.rest.model.ExportThemeFile;

/**
 * Translator for export themes
 * 
 * @author Antti Leppä
 */
@ApplicationScoped
public class ExportThemeTranslator {
  
  /**
   * Translates JPA ExportTheme object into REST ExportTheme object
   * 
   * @param exportTheme JPA ExportTheme object
   * @return REST ExportTheme
   */
  public ExportTheme translateExportTheme(fi.metatavu.metaform.server.persistence.model.ExportTheme exportTheme) {
    if (exportTheme == null) {
      return null;
    }
    
    fi.metatavu.metaform.server.persistence.model.ExportTheme parent = exportTheme.getParent();
    
    ExportTheme result = new ExportTheme();
    result.setId(exportTheme.getId());
    result.setLocales(exportTheme.getLocales());
    result.setName(exportTheme.getName());
    result.setParentId(parent != null ? parent.getId() : null);
    
    return result;
  }

  /**
   * Translates JPA ExportThemeFile object into REST ExportThemeFile object
   * 
   * @param exportThemeFile JPA ExportThemeFile object
   * @return REST ExportThemeFile
   */
  public ExportThemeFile translateExportThemeFile(fi.metatavu.metaform.server.persistence.model.ExportThemeFile exportThemeFile) {
    if (exportThemeFile == null) {
      return null;
    }
    
    fi.metatavu.metaform.server.persistence.model.ExportTheme theme = exportThemeFile.getTheme();
    
    ExportThemeFile result = new ExportThemeFile();
    result.setId(exportThemeFile.getId());
    result.setContent(exportThemeFile.getContent());
    result.setPath(exportThemeFile.getPath());
    result.setThemeId(theme != null ? theme.getId() : null);
    
    return result;
  }
}
