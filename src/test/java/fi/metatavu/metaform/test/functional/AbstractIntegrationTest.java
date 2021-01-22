package fi.metatavu.metaform.test.functional;

import static io.restassured.RestAssured.given;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

import io.restassured.response.ValidatableResponse;
import fi.metatavu.metaform.client.api.*;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.After;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.client.WireMock;

import feign.Feign.Builder;
import fi.metatavu.feign.UmaErrorDecoder;
import fi.metatavu.metaform.client.ApiClient;
import fi.metatavu.metaform.client.model.Metaform;
import fi.metatavu.metaform.client.model.Reply;


/**
 * Abstract base class for integration tests
 * 
 * @author Heikki Kurhinen
 * @author Antti Leppä
 */
@SuppressWarnings ("squid:S1192")
public abstract class AbstractIntegrationTest extends AbstractTest {
  
  protected static final String REALM_1 = "test-1";
  protected static final String BASE_URL = "/v1";
  protected static final String AUTH_SERVER_URL = "http://test-keycloak:8080";
  protected static final String DEFAULT_UI_CLIENT_ID = "ui";
  protected static final String DEFAULT_UI_CLIENT_SECRET = "22614bd2-6a85-441c-857d-7606f4359e5b";
  protected static final UUID REALM1_USER_1_ID = UUID.fromString("b6039e55-3758-4252-9858-a973b0988b63");

  @After
  public void properlyCleaned() {
    List<String> tables = Arrays.asList(
      "Attachment",
      "AttachmentReplyField",
      "AttachmentReplyFieldItem",
      "BooleanReplyField",
      "ExportTheme",
      "ExportThemeFile",
      "ListReplyField",
      "ListReplyFieldItem",
      "Metaform",
      "NumberReplyField",
      "Reply",
      "ReplyField",
      "StringReplyField",
      "TableReplyField",
      "TableReplyFieldNumberRowCell",
      "TableReplyFieldRow",
      "TableReplyFieldRowCell",
      "TableReplyFieldStringRowCell"
    );
    
    for (String table : tables) {
      assertCount(String.format("%s not properly cleaned after test", table), String.format("SELECT count(id) as count FROM %s", table), 0); 
    }
  }
  
  /**
   * Returns API base path
   * 
   * @return API base path
   */
  protected String getBasePath() {
   return String.format("http://%s:%d", getHost(), getPort());
  }
  
  /**
   * Returns API host
   * 
   * @return API host
   */
  protected String getHost() {
    return System.getProperty("it.host");
  }
  
  /**
   * Returns API port
   * 
   * @return API port
   */
  protected Integer getPort() {
    return NumberUtils.createInteger(System.getProperty("it.port.http"));
  }

  /**
   * Flushes JPA cache
   */
  protected void flushCache() {
    given()
      .baseUri(getBasePath())
      .get("/system/jpa/cache/flush")
      .then();
  }
  
  /**
   * Reads a Metaform from JSON file
   * 
   * @param form file name
   * @return Metaform object
   * @throws IOException throws IOException when JSON reading fails
   */
  protected Metaform readMetaform(String form) throws IOException {
    ObjectMapper objectMapper = getObjectMapper();
    String path = String.format("fi/metatavu/metaform/testforms/%s.json", form);
    ClassLoader classLoader = getClass().getClassLoader();
    try (InputStream formStream = classLoader.getResourceAsStream(path)) {    
      return objectMapper.readValue(formStream, Metaform.class);
    }
  }
  
  /**
   * Returns replies API authenticated by the given access token
   * 
   * @param accessToken token
   * @return replies API authenticated by the given access token
   */
  protected RepliesApi getRepliesApi(String accessToken) {
    ApiClient apiClient = getApiClient(accessToken);
    return apiClient.buildClient(RepliesApi.class);
  }

  /**
   * Returns AuditLogEntriesApi authenticated by the given access token
   *
   * @param accessTokenaccess token
   * @return replies API authenticated by the given access token
   */
  protected AuditLogEntriesApi getAuditLogEntriesApi(String accessToken) {
    ApiClient apiClient = getApiClient(accessToken);
    return apiClient.buildClient(AuditLogEntriesApi.class);
  }
  /**
   * Returns drafts API authenticated by the given access token
   *
   * @param accessToken token
   * @return drafts API authenticated by the given access token
   */
  protected DraftsApi getDraftsApi(String accessToken) {
    ApiClient apiClient = getApiClient(accessToken);
    return apiClient.buildClient(DraftsApi.class);
  }

  /**
   * Returns metaforms API authenticated by the given access token
   *
   * @param accessToken token
   * @return metaforms API authenticated by the given access token
   */
  protected MetaformsApi getMetaformsApi(String accessToken) {
    ApiClient apiClient = getApiClient(accessToken);
    return apiClient.buildClient(MetaformsApi.class);
  }

  /**
   * Returns metaforms API authenticated by the given access token
   *
   * @param accessToken token
   * @return metaforms API authenticated by the given access token
   */
  protected AttachmentsApi getAttachmentsApi(String accessToken) {
    ApiClient apiClient = getApiClient(accessToken);
    return apiClient.buildClient(AttachmentsApi.class);
  }

  /**
   * Returns EmailNotificationsApi authenticated by the given access token
   *
   * @param accessToken token
   * @return metaforms API authenticated by the given access token
   */
  protected EmailNotificationsApi getEmailNotificationsApi(String accessToken) {
    ApiClient apiClient = getApiClient(accessToken);
    return apiClient.buildClient(EmailNotificationsApi.class);
  }

  /**
   * Returns exportThemes API authenticated by the given access token
   *
   * @param accessToken token
   * @return exportThemes API authenticated by the given access token
   */
  protected ExportThemesApi getExportThemesApi(String accessToken) {
    ApiClient apiClient = getApiClient(accessToken);
    return apiClient.buildClient(ExportThemesApi.class);
  }

  /**
   * Returns exportThemeFiles API authenticated by the given access token
   *
   * @param accessToken token
   * @return exportThemeFiles API authenticated by the given access token
   */
  protected ExportThemeFilesApi getExportThemeFilesApi(String accessToken) {
    ApiClient apiClient = getApiClient(accessToken);
    return apiClient.buildClient(ExportThemeFilesApi.class);
  }

  /**
   * Creates test table row data
   *
   * @param tableText text
   * @param tableNumber number
   * @return created test data row
   */
  protected Map<String, Object> createSimpleTableRow(String tableText, Double tableNumber) {
    Map<String, Object> result = new HashMap<>();

    if (tableText != null) {
      result.put("tabletext", tableText);
    }

    if (tableNumber != null) {
      result.put("tablenumber", tableNumber);
    }

    return result;
  }

  /**
   * Returns API client authenticated by the given access token
   *
   * @param accessToken token
   * @return API client authenticated by the given access token
   */
  private ApiClient getApiClient(String accessToken) {
    String authorization = String.format("Bearer %s", accessToken);
    ApiClient apiClient = new ApiClient("bearer", authorization);
    
    Builder feignBuilder = apiClient.getFeignBuilder();
    Consumer<String> authorizationChange = apiClient::setApiKey;
    feignBuilder.errorDecoder(new UmaErrorDecoder(feignBuilder, authorization, authorizationChange));
    String basePath = String.format("http://%s:%d/v1", getHost(), getPort());
    apiClient.setBasePath(basePath);
    return apiClient;
  }
  
  /**
   * Resolves an access token for realm, username and password
   * 
   * @param realm realm
   * @param username username
   * @param password password
   * @return an access token
   * @throws IOException thrown on communication failure
   */
  protected String getAccessToken(String realm, String username, String password) throws IOException {
    return getAccessToken(realm, DEFAULT_UI_CLIENT_ID, username, password);
  }

  /**
   * Resolves an admin access token for realm
   * 
   * @param realm realm
   * @return an access token
   * @throws IOException thrown on communication failure
   */
  protected String getAdminToken(String realm) throws IOException {
    return getAccessToken(realm, DEFAULT_UI_CLIENT_ID, "metaform-admin", "test"); 
  }

  /**
   * Resolves an super access token for realm
   * 
   * @param realm realm
   * @return an access token
   * @throws IOException thrown on communication failure
   */
  protected String getSuperToken(String realm) throws IOException {
    return getAccessToken(realm, DEFAULT_UI_CLIENT_ID, "metaform-super", "test"); 
  }

  /**
   * Resolves an anonymous access token for realm
   *
   * @return an access token
   * @throws IOException thrown on communication failure
   */
  protected String getAnonymousToken() throws IOException {
    String path = String.format("/auth/realms/%s/protocol/openid-connect/token", AbstractIntegrationTest.REALM_1);
    
    String password = String.format("%s:%s", DEFAULT_UI_CLIENT_ID, DEFAULT_UI_CLIENT_SECRET);
    String passwordEncoded = Base64.encodeBase64String(password.getBytes(StandardCharsets.UTF_8));
    String authorization = String.format("Basic %s", passwordEncoded);

    String response = given()
      .baseUri(AUTH_SERVER_URL)
      .header("Authorization", authorization)
      .formParam("grant_type", "client_credentials")
      .post(path)
      .getBody()
      .asString();
    
    Map<String, Object> responseMap = readJsonMap(response);
    String token = (String) responseMap.get("access_token");
    assertNotNull(token);
    
    return token;
  }

  /**
   * Resolves an access token for realm, client, username and password
   * 
   * @param realm realm
   * @param clientId clientId
   * @param username username
   * @param password password
   * @return an access token
   * @throws IOException thrown on communication failure
   */
  protected String getAccessToken(String realm, String clientId, String username, String password) throws IOException {
    String path = String.format("/auth/realms/%s/protocol/openid-connect/token", realm);
    
    String response = given()
      .baseUri(AUTH_SERVER_URL)
      .formParam("client_id", clientId)
      .formParam("grant_type", "password")
      .formParam("username", username)
      .formParam("password", password)
      .formParam("client_secret", DEFAULT_UI_CLIENT_SECRET)
      .post(path)
      .getBody()
      .asString();
    
    Map<String, Object> responseMap = readJsonMap(response);
    String token = (String) responseMap.get("access_token");
    assertNotNull(token);
    
    return token;
  }
  
  /**
   * Starts a mailgun mocker
   * 
   * @return mailgun mocker
   */
  protected MailgunMocker startMailgunMocker() {
    String domain = "domain.example.com";
    String path = "mgapi";
    String apiKey = "fakekey";
    MailgunMocker mailgunMocker = new MailgunMocker(String.format("/%s", path), domain, apiKey);
    mailgunMocker.startMock();
    return mailgunMocker;
  }

  /**
   * Stops a malgun mocker
   * 
   * @param mailgunMocker mocker
   */
  protected void stopMailgunMocker(MailgunMocker mailgunMocker) {
    mailgunMocker.stopMock();
  }

  /**
   * Creates a reply object with given data
   * 
   * @param replyData reply data
   * @return reply object with given data
   */
  protected Reply createReplyWithData(Map<String, Object> replyData) {
    Reply reply = new Reply();
    reply.setData(replyData);
    return reply;
  }
  
  /**
   * Calculates contents md5 from a resource
   * 
   * @param resourceName resource name
   * @return resource contents md5
   * @throws IOException thrown when file reading fails
   */
  protected String getResourceMd5(String resourceName) throws IOException {
    ClassLoader classLoader = getClass().getClassLoader();
    try (InputStream fileStream = classLoader.getResourceAsStream(resourceName)) {
      assert fileStream != null;
      return DigestUtils.md5Hex(fileStream);
    }    
  }
  
  /**
   * Uploads resource into file store
   * 
   * @param resourceName resource name
   * @return upload response
   * @throws IOException thrown on upload failure
   */
  protected FileUploadResponse uploadResourceFile(String resourceName) throws IOException {
    ClassLoader classLoader = getClass().getClassLoader();
    
    try (InputStream fileStream = classLoader.getResourceAsStream(resourceName)) {
      HttpClientBuilder clientBuilder = HttpClientBuilder.create();
      try (CloseableHttpClient client = clientBuilder.build()) {
        HttpPost post = new HttpPost(String.format("%s/fileUpload", getBasePath()));
        MultipartEntityBuilder multipartEntityBuilder = MultipartEntityBuilder.create();
        
        multipartEntityBuilder.addBinaryBody("file", fileStream, ContentType.create("image/jpg"), resourceName);
        
        post.setEntity(multipartEntityBuilder.build());
        HttpResponse response = client.execute(post);

        assertEquals(200, response.getStatusLine().getStatusCode());
        
        HttpEntity httpEntity = response.getEntity();

        ObjectMapper objectMapper = new ObjectMapper();
        FileUploadResponse result = objectMapper.readValue(httpEntity.getContent(), FileUploadResponse.class);

        assertNotNull(result);
        assertNotNull(result.getFileRef());
        
        return result;
      }
    }
  }

  /**
   * Returns file meta for a uploaded file
   * 
   * @param fileRef file ref
   * @return meta
   * @throws IOException thrown on io exception
   */
  protected FileUploadMeta getFileRefMeta(UUID fileRef) throws IOException {
    HttpClientBuilder clientBuilder = HttpClientBuilder.create();
    try (CloseableHttpClient client = clientBuilder.build()) {
      HttpGet get = new HttpGet(String.format("%s/fileUpload?fileRef=%s&meta=true", getBasePath(), fileRef));
      HttpResponse response = client.execute(get);
      try (InputStream contentStream = response.getEntity().getContent()) {
        return getObjectMapper().readValue(contentStream, FileUploadMeta.class);
      }
    }
  }
  
  /**
   * Delete uploaded file from the store
   * 
   * @param fileRef fileRef
   * @throws IOException thrown on delete failure
   */
  protected void deleteUpload(String fileRef) throws IOException {
    HttpClientBuilder clientBuilder = HttpClientBuilder.create();
    try (CloseableHttpClient client = clientBuilder.build()) {
      HttpDelete delete = new HttpDelete(String.format("%s/fileUpload?fileRef=%s", getBasePath(), fileRef));
      HttpResponse response = client.execute(delete);
      assertEquals(204, response.getStatusLine().getStatusCode());
    }
  }

  /**
   * Asserts that given file upload exists
   * 
   * @param fileRef fileRef
   * @throws IOException throw then request fails
   */
  protected void assertUploadFound(String fileRef) throws IOException {
    assertUploadStatus(fileRef, 200);
  }

  /**
   * Asserts that given file upload does not exist 
   * 
   * @param fileRef fileRef
   * @throws IOException throw then request fails
   */
  protected void assertUploadNotFound(String fileRef) throws IOException {
    assertUploadStatus(fileRef, 404);
  }

  /**
   * Assert PDF download status code
   *
   * @param expected expected status code
   * @param accessToken access token
   * @param metaform metaform
   * @param reply reply
   */
  protected void assertPdfDownloadStatus(int expected, String accessToken, Metaform metaform, Reply reply) {
    ValidatableResponse response = given()
      .baseUri(getBasePath())
      .header("Authorization", String.format("Bearer %s", accessToken))
      .get("/v1/metaforms/{metaformId}/replies/{replyId}/export?format=PDF", metaform.getId().toString(), reply.getId().toString())
      .then()
      .assertThat()
      .statusCode(expected);

    if (expected == 200) {
      response.header("Content-Type", "application/pdf");
    }
  }

  /**
   * Asserts that given file upload does not exist 
   * 
   * @param fileRef fileRef
   * @param expectedStatus expected status code
   * @throws IOException throw then request fails
   */
  private void assertUploadStatus(String fileRef, int expectedStatus) throws IOException {
    HttpClientBuilder clientBuilder = HttpClientBuilder.create();
    try (CloseableHttpClient client = clientBuilder.build()) {
      HttpGet get = new HttpGet(String.format("%s/fileUpload?fileRef=%s", getBasePath(), fileRef));
      HttpResponse response = client.execute(get);
      assertEquals(expectedStatus, response.getStatusLine().getStatusCode());
    }
  }

  /**
   * Asserts that given query returns expected value in "count" column
   * 
   * @param message assertion fail message
   * @param sql SQL
   * @param expected expected count
   */
  private void assertCount(String message, String sql, int expected) {
    int metaformCount = executeSelectSingle(sql, (resultSet) -> {
      try {
        return resultSet.getInt("count");
      } catch (SQLException e) {
        fail(e.getMessage());
        return null;
      }
    });
    
    assertEquals(message, expected, metaformCount);
  }
  
  static {
    WireMock.configureFor("localhost", 8888);
  }
  
}
