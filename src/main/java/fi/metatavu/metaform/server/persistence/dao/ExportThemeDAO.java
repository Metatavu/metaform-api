package fi.metatavu.metaform.server.persistence.dao;

import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;

import fi.metatavu.metaform.server.persistence.model.ExportTheme;

/**
 * DAO class for ExportThemeFile entity
 * 
 * @author Antti Leppä
 */
@ApplicationScoped
public class ExportThemeDAO extends AbstractDAO<ExportTheme> {

  /**
  * Creates new exportTheme
  *
  * @param locales locales
  * @param parent parent
  * @param name name
  * @param lastModifier modifier
  * @return created exportTheme
  */
  public ExportTheme create(String locales, ExportTheme parent, String name, UUID lastModifier) {
    ExportTheme exportTheme = new ExportTheme();
    exportTheme.setLocales(locales);
    exportTheme.setParent(parent);
    exportTheme.setName(name);
    exportTheme.setLastModifier(lastModifier);
    return persist(exportTheme);
  }

  /**
  * Updates locales
  *
  * @param locales locales
  * @param lastModifier modifier
  * @return updated exportTheme
  */
  public ExportTheme updateLocales(ExportTheme exportTheme, String locales, UUID lastModifier) {
    exportTheme.setLastModifier(lastModifier);
    exportTheme.setLocales(locales);
    return persist(exportTheme);
  }

  /**
  * Updates parent
  *
  * @param parent parent
  * @param lastModifier modifier
  * @return updated exportTheme
  */
  public ExportTheme updateParent(ExportTheme exportTheme, ExportTheme parent, UUID lastModifier) {
    exportTheme.setLastModifier(lastModifier);
    exportTheme.setParent(parent);
    return persist(exportTheme);
  }

  /**
  * Updates name
  *
  * @param name name
  * @param lastModifier modifier
  * @return updated exportTheme
  */
  public ExportTheme updateName(ExportTheme exportTheme, String name, UUID lastModifier) {
    exportTheme.setLastModifier(lastModifier);
    exportTheme.setName(name);
    return persist(exportTheme);
  }

}
