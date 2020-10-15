package fi.metatavu.metaform.server.rest;

import java.util.UUID;
import java.util.stream.Collectors;

import javax.ejb.Stateful;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.core.Response;

import _fi.metatavu.metaform.server.rest.api.ExportThemesApi;
import fi.metatavu.metaform.server.exporttheme.ExportThemeController;
import fi.metatavu.metaform.server.rest.model.ExportTheme;
import fi.metatavu.metaform.server.rest.model.ExportThemeFile;
import fi.metatavu.metaform.server.rest.translate.ExportThemeTranslator;

/**
 * Export themes REST Service implementation
 * 
 * @author Antti Leppä
 */
@RequestScoped
@Stateful
public class ExportThemesApiImpl extends AbstractApi implements ExportThemesApi {
  
  private static final String THEME_DOES_NOT_EXIST = "Theme %s does not exist";
  private static final String YOU_ARE_NOT_ALLOWED_TO_UPDATE_THEMES = "You are not allowed to update themes";
  
  @Inject
  private ExportThemeController exportThemeController;

  @Inject
  private ExportThemeTranslator exportThemeTranslator;

  @Override
  public Response createExportTheme(ExportTheme payload) {
    if (!isRealmMetaformSuper()) {
      return createForbidden(YOU_ARE_NOT_ALLOWED_TO_UPDATE_THEMES);
    }
    
    UUID parentId = payload.getParentId();
    fi.metatavu.metaform.server.persistence.model.ExportTheme parent = null;
    UUID loggedUserId = getLoggerUserId();
    
    if (parentId != null) {
      parent = exportThemeController.findExportTheme(parentId);
      if (parent == null) {
        return createBadRequest(String.format(THEME_DOES_NOT_EXIST, parentId));
      }
    }

    fi.metatavu.metaform.server.persistence.model.ExportTheme exportTheme = exportThemeController.createExportTheme(payload.getLocales(), parent, payload.getName(), loggedUserId);
    
    return createOk(exportThemeTranslator.translateExportTheme(exportTheme));
  }

  @Override
  public Response createExportThemeFile(UUID exportThemeId, ExportThemeFile payload) {
    if (!isRealmMetaformSuper()) {
      return createForbidden(YOU_ARE_NOT_ALLOWED_TO_UPDATE_THEMES);
    }
 
    UUID themeId = payload.getThemeId();
    UUID loggedUserId = getLoggerUserId();
    
    fi.metatavu.metaform.server.persistence.model.ExportTheme theme = exportThemeController.findExportTheme(themeId);
    if (theme == null) {
      return createBadRequest(String.format(THEME_DOES_NOT_EXIST, themeId));
    }

    fi.metatavu.metaform.server.persistence.model.ExportThemeFile themeFile = exportThemeController.createExportThemeFile(theme, payload.getPath(), payload.getContent(), loggedUserId);
    
    return createOk(exportThemeTranslator.translateExportThemeFile(themeFile));
  }

  @Override
  public Response deleteExportTheme(UUID exportThemeId) {
    if (!isRealmMetaformSuper()) {
      return createForbidden(YOU_ARE_NOT_ALLOWED_TO_UPDATE_THEMES);
    }

    fi.metatavu.metaform.server.persistence.model.ExportTheme theme = exportThemeController.findExportTheme(exportThemeId);
    if (theme == null) {
      return createNotFound(NOT_FOUND_MESSAGE);
    }
    
    exportThemeController.deleteTheme(theme);

    return createNoContent();
  }

  @Override
  public Response deleteExportThemeFile(UUID exportThemeId, UUID exportThemeFileId) {
    if (!isRealmMetaformSuper()) {
      return createForbidden(YOU_ARE_NOT_ALLOWED_TO_UPDATE_THEMES);
    }

    fi.metatavu.metaform.server.persistence.model.ExportTheme theme = exportThemeController.findExportTheme(exportThemeId);
    if (theme == null) {
      return createNotFound(NOT_FOUND_MESSAGE);
    }
    
    fi.metatavu.metaform.server.persistence.model.ExportThemeFile exportThemeFile = exportThemeController.findExportThemeFile(exportThemeFileId);
    if (exportThemeFile == null) {
      return createNotFound(NOT_FOUND_MESSAGE);
    }
    
    if (!exportThemeFile.getTheme().getId().equals(theme.getId())) {
      return createNotFound(NOT_FOUND_MESSAGE);
    }
    
    exportThemeController.deleteThemeFile(exportThemeFile);

    return createNoContent();
  }

  @Override
  public Response findExportTheme(UUID exportThemeId) {
    if (!isRealmMetaformSuper()) {
      return createForbidden(YOU_ARE_NOT_ALLOWED_TO_UPDATE_THEMES);
    }

    fi.metatavu.metaform.server.persistence.model.ExportTheme exportTheme = exportThemeController.findExportTheme(exportThemeId);
    if (exportTheme == null) {
      return createNotFound(NOT_FOUND_MESSAGE);
    }

    return createOk(exportThemeTranslator.translateExportTheme(exportTheme));
  }

  @Override
  public Response findExportThemeFile(UUID exportThemeId, UUID exportThemeFileId) {
    if (!isRealmMetaformSuper()) {
      return createForbidden(YOU_ARE_NOT_ALLOWED_TO_UPDATE_THEMES);
    }

    fi.metatavu.metaform.server.persistence.model.ExportTheme theme = exportThemeController.findExportTheme(exportThemeId);
    if (theme == null) {
      return createNotFound(NOT_FOUND_MESSAGE);
    }
    
    fi.metatavu.metaform.server.persistence.model.ExportThemeFile exportThemeFile = exportThemeController.findExportThemeFile(exportThemeFileId);
    if (exportThemeFile == null) {
      return createNotFound(NOT_FOUND_MESSAGE);
    }
    
    if (!exportThemeFile.getTheme().getId().equals(theme.getId())) {
      return createNotFound(NOT_FOUND_MESSAGE);
    }

    return createOk(exportThemeTranslator.translateExportThemeFile(exportThemeFile));
  }

  @Override
  public Response listExportThemeFiles(UUID exportThemeId) {
    if (!isRealmMetaformSuper()) {
      return createForbidden(YOU_ARE_NOT_ALLOWED_TO_UPDATE_THEMES);
    }

    fi.metatavu.metaform.server.persistence.model.ExportTheme theme = exportThemeController.findExportTheme(exportThemeId);
    if (theme == null) {
      return createNotFound(NOT_FOUND_MESSAGE);
    }
    
    return createOk(exportThemeController.listExportThemeFiles(theme).stream()
      .map(exportThemeTranslator::translateExportThemeFile)
      .collect(Collectors.toList()));
  }

  @Override
  public Response listExportThemes() {
    if (!isRealmMetaformSuper()) {
      return createForbidden(YOU_ARE_NOT_ALLOWED_TO_UPDATE_THEMES);
    }

    return createOk(exportThemeController.listExportThemes().stream()
      .map(exportThemeTranslator::translateExportTheme)
      .collect(Collectors.toList()));
  }

  @Override
  public Response updateExportTheme(UUID exportThemeId, ExportTheme payload) {
    if (!isRealmMetaformSuper()) {
      return createForbidden(YOU_ARE_NOT_ALLOWED_TO_UPDATE_THEMES);
    }

    fi.metatavu.metaform.server.persistence.model.ExportTheme theme = exportThemeController.findExportTheme(exportThemeId);
    if (theme == null) {
      return createNotFound(NOT_FOUND_MESSAGE);
    }
    
    UUID parentId = payload.getParentId();
    fi.metatavu.metaform.server.persistence.model.ExportTheme parent = null;
    UUID loggedUserId = getLoggerUserId();
    
    if (parentId != null) {
      parent = exportThemeController.findExportTheme(parentId);
      if (parent == null) {
        return createBadRequest(String.format(THEME_DOES_NOT_EXIST, parentId));
      }
    }

    return createOk(exportThemeTranslator.translateExportTheme(exportThemeController.updateExportTheme(theme, payload.getLocales(), parent, payload.getName(), loggedUserId)));
  }

  @Override
  public Response updateExportThemeFile(UUID exportThemeId, UUID exportThemeFileId, ExportThemeFile payload) {
    if (!isRealmMetaformSuper()) {
      return createForbidden(YOU_ARE_NOT_ALLOWED_TO_UPDATE_THEMES);
    }

    fi.metatavu.metaform.server.persistence.model.ExportTheme theme = exportThemeController.findExportTheme(exportThemeId);
    UUID loggedUserId = getLoggerUserId();
    
    if (theme == null) {
      return createNotFound(NOT_FOUND_MESSAGE);
    }
    
    fi.metatavu.metaform.server.persistence.model.ExportThemeFile exportThemeFile = exportThemeController.findExportThemeFile(exportThemeFileId);
    if (exportThemeFile == null) {
      return createNotFound(NOT_FOUND_MESSAGE);
    }
    
    if (!exportThemeFile.getTheme().getId().equals(theme.getId())) {
      return createNotFound(NOT_FOUND_MESSAGE);
    }
    
    exportThemeController.updateExportThemeFile(exportThemeFile, payload.getPath(), payload.getContent(), loggedUserId);

    return createOk(exportThemeTranslator.translateExportThemeFile(exportThemeFile));
  }
  
}
