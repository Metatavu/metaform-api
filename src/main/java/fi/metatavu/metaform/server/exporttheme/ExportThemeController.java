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
 * Controller for export theme related operations
 * 
 * @author Antti Leppä
 */
@ApplicationScoped
public class ExportThemeController {

  @Inject
  private ExportThemeDAO exportThemeDAO;

  @Inject
  private ExportThemeFileDAO exportThemeFileDAO;
  
  /**
   * Creates new export theme
   * 
   * @param locales locales string
   * @param parent parent theme
   * @param name theme name
   * @param lastModifier last modifier
   * @return created export theme
   */
  public ExportTheme createExportTheme(String locales, ExportTheme parent, String name, UUID lastModifier) {
    return exportThemeDAO.create(UUID.randomUUID(), locales, parent, name, lastModifier, lastModifier);
  }

  /**
   * Update export theme
   * 
   * @param exportTheme export theme
   * @param locales locales string
   * @param parent parent theme
   * @param name theme name
   * @param lastModifier last modifier
   * @return updated export theme
   */
  public ExportTheme updateExportTheme(ExportTheme exportTheme, String locales, ExportTheme parent, String name, UUID lastModifier) {
    exportThemeDAO.updateLocales(exportTheme, locales, lastModifier);
    exportThemeDAO.updateName(exportTheme, name, lastModifier);
    exportThemeDAO.updateParent(exportTheme, parent, lastModifier);
    
    return exportTheme;
  }

  /**
   * Finds a export theme by id
   * 
   * @param id id
   * @return found export theme or null if not found
   */
  public ExportTheme findExportTheme(UUID id) {
    return exportThemeDAO.findById(id);
  }

  /**
   * Finds a export theme by name
   * 
   * @param name name
   * @return found export theme or null if not found
   */
  public ExportTheme findExportTheme(String name) {
    return exportThemeDAO.findByName(name);
  }
  
  /**
   * List export themes
   * 
   * @return export themes
   */
  public List<ExportTheme> listExportThemes() {
    return exportThemeDAO.listAll();
  }

  /**
   * Deletes an export theme
   * 
   * @param theme theme to be deleted
   */
  public void deleteTheme(ExportTheme theme) {
    exportThemeDAO.delete(theme);
  }

  /**
   * Deletes an export theme file
   * 
   * @param themeFile file
   */
  public void deleteThemeFile(ExportThemeFile themeFile) {
    exportThemeFileDAO.delete(themeFile);
  }

  /**
   * Creates new theme file
   * 
   * @param theme theme
   * @param path file path
   * @param content file contents
   * @param lastModifier last modifier
   * @return created file
   */
  public ExportThemeFile createExportThemeFile(ExportTheme theme, String path, String content, UUID lastModifier) {
    return exportThemeFileDAO.create(UUID.randomUUID(), theme, path, content, lastModifier, lastModifier);
  }
  
  /**
   * Finds theme file by id
   * 
   * @param id id
   * @return found file or null if not found
   */
  public ExportThemeFile findExportThemeFile(UUID id) {
    return exportThemeFileDAO.findById(id);
  }
  
  /**
   * Finds theme file by full path
   * 
   * @param path path including theme
   * @return found file or null if not found
   */
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
  
  /**
   * Finds theme file by theme name and path
   * 
   * @param themeName theme name
   * @param path path within theme
   * @return found file or null if not found
   */
  public ExportThemeFile findExportThemeFile(String themeName, String path) {
    ExportTheme exportTheme = findExportTheme(themeName);
    if (exportTheme == null) {
      return null;
    }
    
    return findExportThemeFile(exportTheme, path);
  }
  
  /**
   * Finds theme file by theme and path
   * 
   * @param theme theme
   * @param path path within theme
   * @return found file or null if not found
   */
  public ExportThemeFile findExportThemeFile(ExportTheme theme, String path) {
    return exportThemeFileDAO.findByThemeAndPath(theme, path);
  }

  /**
   * Lists theme files
   * 
   * @param theme thme
   * @return theme files
   */
  public List<ExportThemeFile> listExportThemeFiles(ExportTheme theme) {
    return exportThemeFileDAO.listByTheme(theme);
  }

  /**
   * Updates theme file
   * 
   * @param exportThemeFile theme file
   * @param path path
   * @param content content
   * @param lastModifier last modifier
   * @return updated theme file
   */
  public ExportThemeFile updateExportThemeFile(ExportThemeFile exportThemeFile, String path, String content, UUID lastModifier) {
    exportThemeFileDAO.updatePath(exportThemeFile, path, lastModifier);
    exportThemeFileDAO.updateContent(exportThemeFile, content, lastModifier);
    return exportThemeFile;
  }
  
}
