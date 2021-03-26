package fi.metatavu.metaform.server.rest.translate;

import fi.metatavu.metaform.server.persistence.model.ExportTheme;
import fi.metatavu.metaform.server.persistence.model.ExportThemeFile;

import javax.enterprise.context.ApplicationScoped;


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
  public fi.metatavu.metaform.api.spec.model.ExportTheme translateExportTheme(fi.metatavu.metaform.server.persistence.model.ExportTheme exportTheme) {
    if (exportTheme == null) {
      return null;
    }
    
    fi.metatavu.metaform.server.persistence.model.ExportTheme parent = exportTheme.getParent();
    
    fi.metatavu.metaform.api.spec.model.ExportTheme result = new fi.metatavu.metaform.api.spec.model.ExportTheme();
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
  public fi.metatavu.metaform.api.spec.model.ExportThemeFile translateExportThemeFile(fi.metatavu.metaform.server.persistence.model.ExportThemeFile exportThemeFile) {
    if (exportThemeFile == null) {
      return null;
    }
    
    fi.metatavu.metaform.server.persistence.model.ExportTheme theme = exportThemeFile.getTheme();
    
    fi.metatavu.metaform.api.spec.model.ExportThemeFile result = new fi.metatavu.metaform.api.spec.model.ExportThemeFile();
    result.setId(exportThemeFile.getId());
    result.setContent(exportThemeFile.getContent());
    result.setPath(exportThemeFile.getPath());
    result.setThemeId(theme != null ? theme.getId() : null);
    
    return result;
  }
}
