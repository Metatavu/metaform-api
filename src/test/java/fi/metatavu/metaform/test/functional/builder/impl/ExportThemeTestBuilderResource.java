package fi.metatavu.metaform.test.functional.builder.impl;

import fi.metatavu.jaxrs.test.functional.builder.AbstractTestBuilder;
import fi.metatavu.jaxrs.test.functional.builder.auth.AccessTokenProvider;
import fi.metatavu.metaform.api.client.apis.ExportThemesApi;
import fi.metatavu.metaform.api.client.infrastructure.ApiClient;
import fi.metatavu.metaform.api.client.infrastructure.ClientException;
import fi.metatavu.metaform.api.client.models.ExportTheme;
import fi.metatavu.metaform.test.TestSettings;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * Test builder resource for Export Themes API
 */
public class ExportThemeTestBuilderResource extends ApiTestBuilderResource<ExportTheme, ExportThemesApi> {

  private final AccessTokenProvider accessTokenProvider;

  public ExportThemeTestBuilderResource(
    AbstractTestBuilder<ApiClient> testBuilder,
    AccessTokenProvider accessTokenProvider,
    ApiClient apiClient) {
    super(testBuilder, apiClient);
    this.accessTokenProvider = accessTokenProvider;
  }

  @Override
  protected ExportThemesApi getApi() {
    try {
      ApiClient.Companion.setAccessToken(accessTokenProvider.getAccessToken());
    } catch (IOException e) {
      e.printStackTrace();
    }
    return new ExportThemesApi(TestSettings.basePath);
  }

  @Override
  public void clean(ExportTheme exportTheme) {
    getApi().deleteExportTheme(exportTheme.getId());
  }

  /**
   * Creates simple export theme
   *
   * @return export theme
   * @throws IOException thrown when request fails
   */
  public ExportTheme createSimpleExportTheme() throws IOException {
    return createSimpleExportTheme("simple");
  }

  /**
   * Creates simple export theme
   *
   * @param name name
   * @return export theme
   */
  public ExportTheme createSimpleExportTheme(String name) {
    ExportTheme payload = new ExportTheme(name, null, null, null);
    return createExportTheme(payload);
  }

  /**
   * Creates export theme
   *
   * @param payload payload
   * @return export theme
   */
  public ExportTheme createExportTheme(ExportTheme payload) {
    ExportTheme exportTheme = getApi().createExportTheme(payload);
    return addClosable(exportTheme);
  }

  /**
   * Finds export theme by id
   *
   * @param exportThemeId exportThemeId
   * @return found export theme
   */
  public ExportTheme findExportTheme(UUID exportThemeId) {
    return getApi().findExportTheme(exportThemeId);
  }

  /**
   * Lists all export themes
   *
   * @return export theme list
   */
  public List<ExportTheme> listExportThemes() {
    return Arrays.asList(getApi().listExportThemes());
  }

  /**
   * Updates export theme
   *
   * @param exportThemeId exportThemeId
   * @param exportTheme   exportTheme
   * @return updated export theme
   */
  public ExportTheme updateExportTheme(UUID exportThemeId, ExportTheme exportTheme) {
    return getApi().updateExportTheme(exportThemeId, exportTheme);
  }

  /**
   * Deletes export theme
   *
   * @param exportTheme export theme to delete
   */
  public void deleteExportTheme(ExportTheme exportTheme) {
    getApi().deleteExportTheme(Objects.requireNonNull(exportTheme.getId()));
    removeCloseable(closable -> {
      if (closable instanceof ExportTheme) {
        return exportTheme.getId().equals(((ExportTheme) closable).getId());
      }

      return false;
    });
  }

  /**
   * Asserts expected status for search
   *
   * @param status        expected status
   * @param exportThemeId exportThemeId
   */
  public void assertSearchFailStatus(int status, UUID exportThemeId) {
    try {
      getApi().findExportTheme(exportThemeId);
      fail(String.format("Expected find to fail with status %d", status));
    } catch (ClientException e) {
      assertEquals(status, e.getStatusCode());
    }
  }

  /**
   * Asserts expected status for creation
   *
   * @param status      expected status
   * @param exportTheme exportTheme
   */
  public void assertCreateFailStatus(int status, ExportTheme exportTheme) {
    try {
      getApi().createExportTheme(exportTheme);
      fail(String.format("Expected find to fail with status %d", status));
    } catch (ClientException e) {
      assertEquals(status, e.getStatusCode());
    }
  }

  /**
   * Asserts list to fail with given status
   *
   * @param status expected status
   */
  public void assertListFailStatus(int status) {
    try {
      getApi().listExportThemes();
      fail(String.format("Expected find to fail with status %d", status));
    } catch (ClientException e) {
      assertEquals(status, e.getStatusCode());
    }
  }

  /**
   * Asserts update to fail with given status
   *
   * @param status        expected status
   * @param exportThemeId export theme id
   */
  public void assertUpdateFailStatus(int status, UUID exportThemeId, ExportTheme exportTheme) {
    try {
      getApi().updateExportTheme(exportThemeId, exportTheme);
      fail(String.format("Expected find to fail with status %d", status));
    } catch (ClientException e) {
      assertEquals(status, e.getStatusCode());
    }
  }

  /**
   * Asserts delete to fail with given status
   *
   * @param status        expected status
   * @param exportThemeId export theme id
   */
  public void assertDeleteFailStatus(int status, UUID exportThemeId) {
    try {
      getApi().deleteExportTheme(exportThemeId);
      fail(String.format("Expected find to fail with status %d", status));
    } catch (ClientException e) {
      assertEquals(status, e.getStatusCode());
    }
  }
}
