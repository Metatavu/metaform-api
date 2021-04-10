package fi.metatavu.metaform.test.functional.builder.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.kotlin.KotlinModule;
import com.squareup.moshi.*;
import com.squareup.moshi.adapters.EnumJsonAdapter;
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory;
import fi.metatavu.jaxrs.test.functional.builder.AbstractTestBuilder;
import fi.metatavu.jaxrs.test.functional.builder.auth.AccessTokenProvider;
import fi.metatavu.metaform.api.client.apis.MetaformsApi;
import fi.metatavu.metaform.api.client.infrastructure.ApiClient;
import fi.metatavu.metaform.api.client.infrastructure.ClientException;
import fi.metatavu.metaform.api.client.models.Draft;
import fi.metatavu.metaform.api.client.models.Metaform;
import fi.metatavu.metaform.api.client.models.MetaformFieldType;
import fi.metatavu.metaform.api.spec.model.ExportTheme;
import fi.metatavu.metaform.test.TestSettings;
import fi.metatavu.metaform.test.functional.builder.TestBuilder;
import org.apache.commons.io.IOUtils;
import org.json.JSONException;

import static org.junit.Assert.*;

/**
 * Test builder resource for metaforms
 *
 * @author Antti Leppä
 */
public class MetaformTestBuilderResource extends ApiTestBuilderResource<Metaform, MetaformsApi> {

  private AccessTokenProvider accessTokenProvider;
  /**
   * Constructor
   *
   * @param testBuilder test builder
   * @param apiClient   initialized API client
   */
  public MetaformTestBuilderResource(AbstractTestBuilder<ApiClient> testBuilder, AccessTokenProvider accessTokenProvider, ApiClient apiClient) {
    super(testBuilder, apiClient);
    this.accessTokenProvider = accessTokenProvider;
  }

  /**
   * Creates new metaform
   *
   * @param payload payload
   * @return created metaform
   */
  public Metaform create(Metaform payload) {
    return addClosable(getApi().createMetaform(payload));
  }


  /**
   * Finds a metaform
   *
   * @param metaformId metaform id
   * @param replyId    Id of reply the form is loaded for. Reply id needs to be defined when unanonymous form is authenticated with owner key  (optional)
   * @param ownerKey   Reply owner key (optional)
   * @return found metaform
   */
  public Metaform findMetaform(UUID metaformId, UUID replyId, String ownerKey) {
    return getApi().findMetaform(metaformId, replyId, ownerKey);
  }

  /**
   * Finds a metaform
   *
   * @param metaformId metaform id
   * @return found metaform
   */
  public Metaform findMetaform(UUID metaformId) {
    return findMetaform(metaformId, null, null);
  }

  /**
   * Updates a metaform into the API
   *
   * @param body body payload
   */
  public Metaform updateMetaform(UUID id ,Metaform body) {
    return getApi().updateMetaform(id, body);
  }

  public Metaform[] list () {
    return getApi().listMetaforms();
  }

  /**
   * Deletes a metaform from the API
   *
   * @param metaform metaform to be deleted
   */
  public void delete(Metaform metaform) {
    assertNotNull(metaform.getId());
    getApi().deleteMetaform(metaform.getId());
    removeCloseable(closable -> {
      if (closable instanceof Metaform) {
        return !metaform.getId().equals(((Metaform) closable).getId());
      }

      return false;
    });
  }

  /**
   * Asserts metaform count within the system
   *
   * @param expected expected count
   */
  public void assertCount(int expected) {
    assertEquals(expected, getApi().listMetaforms().length);
  }

  /**
   * Asserts find status fails with given status code
   *
   * @param expectedStatus expected status code
   * @param metaformId     metaform id
   * @param replyId        Id of reply the form is loaded for. Reply id needs to be defined when unanonymous form is authenticated with owner key  (optional)
   * @param ownerKey       Reply owner key (optional)
   */
  public void assertFindFailStatus(int expectedStatus, UUID metaformId, UUID replyId, String ownerKey) {
    try {
      getApi().findMetaform(metaformId, replyId, ownerKey);
      fail(String.format("Expected find to fail with status %d", expectedStatus));
    } catch (ClientException e) {
      assertEquals(expectedStatus, e.getStatusCode());
    }
  }

  /**
   * Asserts find status fails with given status code
   *
   * @param expectedStatus expected status code
   * @param metaformId     metaform id
   */
  public void assertFindFailStatus(int expectedStatus, UUID metaformId) {
    assertFindFailStatus(expectedStatus, metaformId, null, null);
  }

  /**
   * Asserts create status fails with given status code
   *
   * @param expectedStatus expected status code
   * @param payload        payload
   */
  public void assertCreateFailStatus(int expectedStatus, Metaform payload) {
    try {
      getApi().createMetaform(payload);
      fail(String.format("Expected create to fail with status %d", expectedStatus));
    } catch (ClientException e) {
      assertEquals(expectedStatus, e.getStatusCode());
    }
  }

  /**
   * Asserts update status fails with given status code
   *
   * @param expectedStatus expected status code
   * @param metaform       metaform
   */
  public void assertUpdateFailStatus(int expectedStatus, Metaform metaform) {
    try {
      getApi().updateMetaform(metaform.getId(), metaform);
      fail(String.format("Expected update to fail with status %d", expectedStatus));
    } catch (ClientException e) {
      assertEquals(expectedStatus, e.getStatusCode());
    }
  }

  /**
   * Asserts delete status fails with given status code
   *
   * @param expectedStatus expected status code
   * @param metaform       metaform
   */
  public void assertDeleteFailStatus(int expectedStatus, Metaform metaform) {
    try {
      getApi().deleteMetaform(metaform.getId());
      fail(String.format("Expected delete to fail with status %d", expectedStatus));
    } catch (ClientException e) {
      assertEquals(expectedStatus, e.getStatusCode());
    }
  }

  /**
   * Asserts list status fails with given status code
   *
   * @param expectedStatus expected status code
   */
  public void assertListFailStatus(int expectedStatus) {
    try {
      getApi().listMetaforms();
      fail(String.format("Expected list to fail with status %d", expectedStatus));
    } catch (ClientException e) {
      assertEquals(expectedStatus, e.getStatusCode());
    }
  }

  /**
   * Asserts that actual metaform equals expected metaform when both are serialized into JSON
   *
   * @param expected expected metaform
   * @param actual   actual metaform
   * @throws JSONException thrown when JSON serialization error occurs
   * @throws IOException   thrown when IO Exception occurs
   */
  public void assertMetaformsEqual(Metaform expected, Metaform actual) throws IOException, JSONException {
    assertJsonsEqual(expected, actual);
  }

  @Override
  public void clean(Metaform metaform) {
    getApi().deleteMetaform(metaform.getId());
  }

  /**
   * Reads a Metaform from JSON file
   *
   * @param form file name
   * @return Metaform object
   * @throws IOException throws IOException when JSON reading fails
    */
  public Metaform readMetaform(String form) throws IOException {
    return MetaformsReader.Companion.readMetaform(form);

  }
  /**
   * Reads a Metaform from JSON file
   *
   * @param form file name
   * @return Metaform object
   * @throws IOException throws IOException when JSON reading fails
   */
 /* public Metaform readMetaform(String form) throws IOException {
    ObjectMapper objectMapper = getObjectMapper();
    String path = String.format("fi/metatavu/metaform/testforms/%s.json", form);
    ClassLoader classLoader = getClass().getClassLoader();
    try (InputStream formStream = classLoader.getResourceAsStream(path)) {
      return objectMapper.readValue(formStream, Metaform.class);
    }
  }*/
/*=   String path = String.format("fi/metatavu/metaform/testforms/%s.json", form);
    ClassLoader classLoader = getClass().getClassLoader();
    try (InputStream formStream = classLoader.getResourceAsStream(path)) {
      try (Reader targetReader = new InputStreamReader(formStream)) {
        return new Gson().fromJson(targetReader, Metaform.class);
      }
    }*/
  /**
   * Returns object mapper with default modules and settings
   *
   * @return object mapper
   */
  protected ObjectMapper getObjectMapper() {
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.registerModule(new JavaTimeModule());
    objectMapper.registerModule(new KotlinModule());
    objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    return objectMapper;
  }

  @Override
  protected MetaformsApi getApi() {
    try {
      ApiClient.Companion.setAccessToken(accessTokenProvider.getAccessToken());
    } catch (IOException e) {
      e.printStackTrace();
    }
    return new MetaformsApi(TestSettings.basePath);
  }


}