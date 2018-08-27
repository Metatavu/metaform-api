package fi.metatavu.metaform.server.exporttheme;

import java.util.List;
import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import fi.metatavu.metaform.server.persistence.dao.ExportThemeDAO;
import fi.metatavu.metaform.server.persistence.dao.ExportThemeFileDAO;
import fi.metatavu.metaform.server.persistence.model.ExportTheme;
import fi.metatavu.metaform.server.persistence.model.ExportThemeFile;

/**
 * Controller for exportTheme related operations
 * 
 * @author Antti Leppä
 */
@ApplicationScoped
public class ExportThemeController {

  @Inject
  private ExportThemeDAO exportThemeDAO;

  @Inject
  private ExportThemeFileDAO exportThemeFileDAO;
  
  public ExportTheme createExportTheme(String locales, ExportTheme parent, String name, UUID lastModifier) {
    return exportThemeDAO.create(UUID.randomUUID(), locales, parent, name, lastModifier, lastModifier);
  }

  public ExportTheme updateExportTheme(ExportTheme exportTheme, String locales, ExportTheme parent, String name, UUID lastModifier) {
    exportThemeDAO.updateLocales(exportTheme, locales, lastModifier);
    exportThemeDAO.updateName(exportTheme, name, lastModifier);
    exportThemeDAO.updateParent(exportTheme, parent, lastModifier);
    
    return exportTheme;
  }

  public ExportTheme findExportTheme(UUID id) {
    return exportThemeDAO.findById(id);
  }

  public ExportTheme findExportTheme(String name) {
    return exportThemeDAO.findByName(name);
  }
  
  public List<ExportTheme> listExportThemes() {
    return exportThemeDAO.listAll();
  }

  public void deleteTheme(ExportTheme theme) {
    exportThemeDAO.delete(theme);
  }

  public ExportThemeFile createExportThemeFile(ExportTheme theme, String path, String content, UUID lastModifier) {
    return exportThemeFileDAO.create(UUID.randomUUID(), theme, path, content, lastModifier, lastModifier);
  }
  
  public ExportThemeFile findExportThemeFile(UUID id) {
    return exportThemeFileDAO.findById(id);
  }
  
  public ExportThemeFile findExportThemeFile(String path) {
    String[] parts = StringUtils.split(path, '/');
    if (parts.length < 2) {
      return null;
    }
    
    ExportTheme exportTheme = findExportTheme(parts[0]);
    if (exportTheme == null) {
      return null;
    }
    
    String filePath = StringUtils.join(ArrayUtils.remove(parts, 0), "/");
    ExportThemeFile result = findExportThemeFile(exportTheme, filePath);
    if (result == null && exportTheme.getParent() != null) {
      return findExportThemeFile(String.format("%s/%s", exportTheme.getParent().getName(), filePath));
    }
    
    return result;
  }
  
  public ExportThemeFile findExportThemeFile(String themeName, String path) {
    ExportTheme exportTheme = findExportTheme(themeName);
    if (exportTheme == null) {
      return null;
    }
    
    return findExportThemeFile(exportTheme, path);
  }
  
  public ExportThemeFile findExportThemeFile(ExportTheme theme, String path) {
    return exportThemeFileDAO.findByThemeAndPath(theme, path);
  }

  public List<ExportThemeFile> listExportThemeFiles(ExportTheme theme) {
    return exportThemeFileDAO.listByTheme(theme);
  }

  public ExportThemeFile updateExportThemeFile(ExportThemeFile exportThemeFile, String path, String content, UUID lastModifier) {
    exportThemeFileDAO.updatePath(exportThemeFile, path, lastModifier);
    exportThemeFileDAO.updateContent(exportThemeFile, content, lastModifier);
    return exportThemeFile;
  }
  
}
