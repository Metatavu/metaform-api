package fi.metatavu.metaform.server.test.functional.builder.impl;

import fi.metatavu.jaxrs.test.functional.builder.AbstractTestBuilder;
import fi.metatavu.jaxrs.test.functional.builder.auth.AccessTokenProvider;
import fi.metatavu.metaform.api.client.apis.ExportThemeFilesApi;
import fi.metatavu.metaform.api.client.infrastructure.ApiClient;
import fi.metatavu.metaform.api.client.infrastructure.ClientException;
import fi.metatavu.metaform.api.client.models.ExportThemeFile;
import fi.metatavu.metaform.server.test.TestSettings;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * Test builder resource for Export theme files API
 */
public class ExportThemeFilesTestBuilderResource extends ApiTestBuilderResource<ExportThemeFile, ExportThemeFilesApi> {

  private final AccessTokenProvider accessTokenProvider;

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

  @Override
  public void clean(ExportThemeFile exportThemeFile) throws IOException {
    getApi().deleteExportThemeFile(exportThemeFile.getThemeId(), exportThemeFile.getId());
  }

  /**
   * Creates export theme file from payload
   *
   * @param themeId theme id
   * @param path    path
   * @param content content
   * @return generated export theme file
   */
  public ExportThemeFile createSimpleExportThemeFile(UUID themeId, String path, String content) throws IOException {
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
  public ExportThemeFile createExportThemeFile(UUID themeId, ExportThemeFile payload) throws IOException {
    ExportThemeFile exportThemeFile = getApi().createExportThemeFile(themeId, payload);
    return addClosable(exportThemeFile);
  }

  /**
   * Finds export theme file
   *
   * @param exportThemeId      exportThemeId
   * @param exportThemeFieldId exportThemeFieldId
   * @return found export theme file
   */
  public ExportThemeFile findExportThemeFile(UUID exportThemeId, UUID exportThemeFieldId) throws IOException {
    return getApi().findExportThemeFile(exportThemeId, exportThemeFieldId);
  }

  /**
   * List export theme files by export theme id
   *
   * @param id export theme id
   * @return list of export theme files
   */
  public List<ExportThemeFile> listExportThemeFiles(UUID id) throws IOException {
    return Arrays.asList(getApi().listExportThemeFiles(id));
  }

  /**
   * Updates export theme file
   *
   * @param exportThemeId     exportThemeId
   * @param exportThemeFileId exportThemeFileId
   * @param createdThemeFile  createdThemeFile
   * @return updated export theme file
   */
  public ExportThemeFile updateExportThemeFile(UUID exportThemeId, UUID exportThemeFileId, ExportThemeFile createdThemeFile) throws IOException {
    return getApi().updateExportThemeFile(exportThemeId, exportThemeFileId, createdThemeFile);
  }

  /**
   * Delete export theme file
   *
   * @param themeId         themeId
   * @param exportThemeFile exportThemeFile
   */
  public void deleteExportThemeFile(UUID themeId, ExportThemeFile exportThemeFile) throws IOException {
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
   * @param status            expected status
   * @param exportThemeId     exportThemeId
   * @param exportThemeFileId exportThemeFileId
   */
  public void assertFindFailStatus(int status, UUID exportThemeId, UUID exportThemeFileId) throws IOException {
    try {
      getApi().findExportThemeFile(exportThemeId, exportThemeFileId);
      fail(String.format("Expected find to fail with status %d", status));
    } catch (ClientException e) {
      assertEquals(status, e.getStatusCode());
    }
  }

  /**
   * Asserts export theme file creation to fail with given status
   *
   * @param status          expected status
   * @param exportThemeFile new export theme file payload
   */
  public void assertCreateFailStatus(int status, ExportThemeFile exportThemeFile) throws IOException {
    try {
      getApi().createExportThemeFile(exportThemeFile.getThemeId(), exportThemeFile);
      fail(String.format("Expected find to fail with status %d", status));
    } catch (ClientException e) {
      assertEquals(status, e.getStatusCode());
    }
  }

  /**
   * Asserts finding export theme file to fail with given status
   *
   * @param status            expected status
   * @param exportThemeId     export theme id
   * @param exportThemeFileId export theme file id
   */
  public void assertFindExportThemeFileFailStatus(int status, UUID exportThemeId, UUID exportThemeFileId) throws IOException {
    try {
      getApi().findExportThemeFile(exportThemeId, exportThemeFileId);
      fail(String.format("Expected find to fail with status %d", status));
    } catch (ClientException e) {
      assertEquals(status, e.getStatusCode());
    }
  }

  /**
   * Asserts listing export theme files to fail with given status
   *
   * @param status        expected status
   * @param exportThemeId export theme id
   */
  public void assertListFailStatus(int status, UUID exportThemeId) throws IOException {
    try {
      getApi().listExportThemeFiles(exportThemeId);
      fail(String.format("Expected find to fail with status %d", status));
    } catch (ClientException e) {
      assertEquals(status, e.getStatusCode());
    }
  }

  /**
   * Asserts updating export theme file to fail
   *
   * @param status            expected status
   * @param exportThemeId     export theme id
   * @param exportThemeFileId export theme file id
   * @param exportThemeFile   new export theme file
   */
  public void assertUpdateFailStatus(int status, UUID exportThemeId, UUID exportThemeFileId, ExportThemeFile exportThemeFile) throws IOException {
    try {
      getApi().updateExportThemeFile(exportThemeId, exportThemeFileId, exportThemeFile);
      fail(String.format("Expected find to fail with status %d", status));
    } catch (ClientException e) {
      assertEquals(status, e.getStatusCode());
    }
  }

  /**
   * Asserts deleting export theme file to fail
   *
   * @param status            expected status
   * @param exportThemeId     export theme id
   * @param exportThemeFileId export theme file id
   */
  public void assertDeleteFailStatus(int status, UUID exportThemeId, UUID exportThemeFileId) throws IOException {
    try {
      getApi().deleteExportThemeFile(exportThemeId, exportThemeFileId);
      fail(String.format("Expected find to fail with status %d", status));
    } catch (ClientException e) {
      assertEquals(status, e.getStatusCode());
    }
  }
}

