package fi.metatavu.metaform.test.functional.builder.impl;

import fi.metatavu.jaxrs.test.functional.builder.AbstractTestBuilder;
import fi.metatavu.jaxrs.test.functional.builder.auth.AccessTokenProvider;
import fi.metatavu.metaform.api.client.apis.ExportThemeFilesApi;
import fi.metatavu.metaform.api.client.infrastructure.ApiClient;
import fi.metatavu.metaform.api.client.infrastructure.ClientException;
import fi.metatavu.metaform.api.client.models.ExportThemeFile;
import fi.metatavu.metaform.api.client.models.Metaform;
import fi.metatavu.metaform.test.TestSettings;

import java.io.IOException;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * Test resource for Export theme files API
 */
public class ExportThemeFilesTestBuilderResource extends ApiTestBuilderResource<ExportThemeFile, ExportThemeFilesApi> {

  private AccessTokenProvider accessTokenProvider;

  public ExportThemeFilesTestBuilderResource(
    AbstractTestBuilder<ApiClient> testBuilder,
    AccessTokenProvider accessTokenProvider,
    ApiClient apiClient) {
    super(testBuilder, apiClient);
    this.accessTokenProvider = accessTokenProvider;
  }

  @Override
  protected ExportThemeFilesApi getApi() {
    try {
      ApiClient.Companion.setAccessToken(accessTokenProvider.getAccessToken());
    } catch (IOException e) {
      e.printStackTrace();
    }
    return new ExportThemeFilesApi(TestSettings.basePath);
  }

  /**
   * Creates export theme file from paylost
   *
   * @param themeId
   * @param path
   * @param content
   * @return
   */
  public ExportThemeFile createSimpleExportThemeFile(UUID themeId, String path, String content) {
    ExportThemeFile payload = new ExportThemeFile(path, themeId, content, null);
    return createExportThemeFile(themeId, payload);
  }

  /**
   * Creates export theme file
   *
   * @param themeId parent theme
   * @param payload payload
   * @return created export theme file
   */
  public ExportThemeFile createExportThemeFile(UUID themeId, ExportThemeFile payload) {
    ExportThemeFile exportThemeFile = getApi().createExportThemeFile(themeId, payload);
    return addClosable(exportThemeFile);
  }

  @Override
  public void clean(ExportThemeFile exportThemeFile) throws Exception {
    System.out.println("cleaning export theme files "+exportThemeFile.getId());
    getApi().deleteExportThemeFile(exportThemeFile.getThemeId(), exportThemeFile.getId());
  }

  /**
   * Finds export theme file
   *
   * @param exportThemeId exportThemeId
   * @param exportThemeFieldId exportThemeFieldId
   * @return found export theme file
   */
  public ExportThemeFile findExportThemeFile(UUID exportThemeId, UUID exportThemeFieldId) {
    return getApi().findExportThemeFile(exportThemeId, exportThemeFieldId);
  }

  /**
   * List export theme files by export theme id
   *
   * @param id export theme id
   * @return list of export theme files
   */
  public List<ExportThemeFile> listExportThemeFiles(UUID id) {
    return Arrays.asList(getApi().listExportThemeFiles(id));
  }

  /**
   * Updates export theme file
   *
   * @param exportThemeId exportThemeId
   * @param exportThemeFileId exportThemeFileId
   * @param createdThemeFile createdThemeFile
   * @return updated export theme file
   */
  public ExportThemeFile updateExportThemeFile(UUID exportThemeId, UUID exportThemeFileId, ExportThemeFile createdThemeFile) {
    return getApi().updateExportThemeFile(exportThemeId, exportThemeFileId, createdThemeFile);
  }

  /**
   * Delete export theme file
   *
   * @param themeId themeId
   * @param exportThemeFile exportThemeFile
   */
  public void deleteExportThemeFile(UUID themeId, ExportThemeFile exportThemeFile) {
    getApi().deleteExportThemeFile(themeId, Objects.requireNonNull(exportThemeFile.getId()));
    removeCloseable(closable -> {
      if (closable instanceof ExportThemeFile) {
        return exportThemeFile.getId().equals(((ExportThemeFile) closable).getId());
      }

      return false;
    });
  }

  /**
   * Asserts search status
   *
   * @param status expected status
   * @param exportThemeId exportThemeId
   * @param exportThemeFileId exportThemeFileId
   */
  public void assertFindFailStatus(int status, UUID exportThemeId, UUID exportThemeFileId) {
    try {
      getApi().findExportThemeFile(exportThemeId, exportThemeFileId);
      fail(String.format("Expected find to fail with status %d", status));
    } catch (ClientException e) {
      assertEquals(status, e.getStatusCode());
    }
  }

  public void assertCreateFailStatus(int status, ExportThemeFile exportThemeFile) {
    try {
      getApi().createExportThemeFile(exportThemeFile.getThemeId(), exportThemeFile);
      fail(String.format("Expected find to fail with status %d", status));
    } catch (ClientException e) {
      assertEquals(status, e.getStatusCode());
    }
  }

  public void assertFindExportThemeFileFailStatus(int status, UUID exportThemeId, UUID exportThemeFileId) {
    try {
      getApi().findExportThemeFile(exportThemeId, exportThemeFileId);
      fail(String.format("Expected find to fail with status %d", status));
    } catch (ClientException e) {
      assertEquals(status, e.getStatusCode());
    }
  }

  public void assertListFailStatus(int status, UUID exportThemeId) {
    try {
      getApi().listExportThemeFiles(exportThemeId);
      fail(String.format("Expected find to fail with status %d", status));
    } catch (ClientException e) {
      assertEquals(status, e.getStatusCode());
    }
  }

  public void assertUpdateFailStatus(int status, UUID exportThemeId, UUID exportThemeFileId, ExportThemeFile exportTheme) {
    try {
      getApi().updateExportThemeFile(exportThemeId, exportThemeFileId, exportTheme);
      fail(String.format("Expected find to fail with status %d", status));
    } catch (ClientException e) {
      assertEquals(status, e.getStatusCode());
    }
  }

  public void assertDeleteFailStatus(int status, UUID exportThemeId, UUID exportThemeFileId) {
    try {
      getApi().deleteExportThemeFile(exportThemeId, exportThemeFileId);
      fail(String.format("Expected find to fail with status %d", status));
    } catch (ClientException e) {
      assertEquals(status, e.getStatusCode());
    }
  }
}

