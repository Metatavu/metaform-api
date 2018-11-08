package fi.metatavu.metaform.server;

import static io.restassured.RestAssured.given;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.Map;
import java.util.UUID;
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

import feign.Feign.Builder;
import fi.metatavu.metaform.ApiClient;
import fi.metatavu.metaform.client.EmailNotificationsApi;
import fi.metatavu.metaform.client.ExportThemesApi;
import fi.metatavu.metaform.client.Metaform;
import fi.metatavu.metaform.client.MetaformsApi;
import fi.metatavu.metaform.client.RepliesApi;
import fi.metatavu.metaform.client.Reply;
import fi.metatavu.metaform.client.ReplyData;
import fi.metatavu.metaform.client.AttachmentsApi;


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
  protected static final String AUTH_SERVER_URL = "http://localhost:8280";
  protected static final String DEFAULT_UI_CLIENT_ID = "ui";
  protected static final String DEFAULT_UI_CLIENT_SECRET = "22614bd2-6a85-441c-857d-7606f4359e5b";
  protected static final UUID REALM1_USER_1_ID = UUID.fromString("b6039e55-3758-4252-9858-a973b0988b63");
  
  @After
  public void metaformsCleaned() {
    int metaformCount = executeSelectSingle("SELECT count(id) as count FROM Metaform", (resultSet) -> {
      try {
        return resultSet.getInt("count");
      } catch (SQLException e) {
        fail(e.getMessage());
        return null;
      }
    });
    
    assertEquals("Metaforms not properly cleaned after test", 0, metaformCount); 
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
   * Returns WireMock port
   * 
   * @return WireMock port
   */
  protected int getWireMockPort() {
    return getPort() + 1;
  }
  
  /**
   * Returns WireMock base path
   * 
   * @return WireMock base path
   */
  protected String getWireMockBasePath() {
    return String.format("http://%s:%d", getHost(), getWireMockPort());
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
   * @param accessTokenaccess token
   * @return replies API authenticated by the given access token
   */
  protected RepliesApi getRepliesApi(String accessToken) {
    ApiClient apiClient = getApiClient(accessToken);
    return apiClient.buildClient(RepliesApi.class);
  }

  /**
   * Returns metaforms API authenticated by the given access token
   * 
   * @param accessTokenaccess token
   * @return metaforms API authenticated by the given access token
   */
  protected MetaformsApi getMetaformsApi(String accessToken) {
    ApiClient apiClient = getApiClient(accessToken);
    return apiClient.buildClient(MetaformsApi.class);
  }

  /**
   * Returns metaforms API authenticated by the given access token
   * 
   * @param accessTokenaccess token
   * @return metaforms API authenticated by the given access token
   */
  protected AttachmentsApi getAttachmentsApi(String accessToken) {
    ApiClient apiClient = getApiClient(accessToken);
    return apiClient.buildClient(AttachmentsApi.class);
  }

  /**
   * Returns EmailNotificationsApi authenticated by the given access token
   * 
   * @param accessTokenaccess token
   * @return metaforms API authenticated by the given access token
   */
  protected EmailNotificationsApi getEmailNotificationsApi(String accessToken) {
    ApiClient apiClient = getApiClient(accessToken);
    return apiClient.buildClient(EmailNotificationsApi.class);
  }
  
  /**
   * Returns exportThemes API authenticated by the given access token
   * 
   * @param accessTokenaccess token
   * @return exportThemes API authenticated by the given access token
   */
  protected ExportThemesApi getExportThemesApi(String accessToken) {
    ApiClient apiClient = getApiClient(accessToken);
    return apiClient.buildClient(ExportThemesApi.class);
  }
  
  
  /**
   * Returns API client authenticated by the given access token
   * 
   * @param accessTokenaccess token
   * @return API client authenticated by the given access token
   */
  private ApiClient getApiClient(String accessToken) {
    String authorization = String.format("Bearer %s", accessToken);
    ApiClient apiClient = new ApiClient("bearer", authorization);
    
    Builder feignBuilder = apiClient.getFeignBuilder();
    feignBuilder.errorDecoder(new UmaErrorDecoder(authorization, apiClient));
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
   * @param realm realm
   * @return an access token
   * @throws IOException thrown on communication failure
   */
  protected String getAnonymousToken(String realm) throws IOException {
    String path = String.format("/auth/realms/%s/protocol/openid-connect/token", realm);
    
    String password = String.format("%s:%s", DEFAULT_UI_CLIENT_ID, DEFAULT_UI_CLIENT_SECRET);
    String passwordEncoded = Base64.encodeBase64String(password.getBytes("UTF-8"));
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
    String senderEmail = "metaform-test@example.com";
    String senderName = "Metaform Test";

    executeInsert("INSERT INTO SystemSetting (id, settingkey, value) VALUES (?, ?, ?)", UUID.randomUUID(), "mailgun-apiurl", String.format("%s/%s",getWireMockBasePath(), path));
    executeInsert("INSERT INTO SystemSetting (id, settingkey, value) VALUES (?, ?, ?)", UUID.randomUUID(), "mailgun-domain", domain);
    executeInsert("INSERT INTO SystemSetting (id, settingkey, value) VALUES (?, ?, ?)", UUID.randomUUID(), "mailgun-apikey", apiKey);
    executeInsert("INSERT INTO SystemSetting (id, settingkey, value) VALUES (?, ?, ?)", UUID.randomUUID(), "mailgun-sender-email", senderEmail);
    executeInsert("INSERT INTO SystemSetting (id, settingkey, value) VALUES (?, ?, ?)", UUID.randomUUID(), "mailgun-sender-name", senderName);
    
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
    executeDelete("DELETE FROM SystemSetting WHERE settingKey in ('mailgun-apiurl', 'mailgun-domain', 'mailgun-apikey', 'mailgun-sender-email', 'mailgun-sender-name')");
  }

  /**
   * Creates a reply object with given data
   * 
   * @param replyData reply data
   * @return reply object with given data
   */
  protected Reply createReplyWithData(ReplyData replyData) {
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
   * @param fileRef
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
   * Asserts that given file upload does not exist 
   * 
   * @param fileRef fileRef
   * @param expectedStatus expected status code
   * @throws IOException throw then request fails
   */
  private void assertUploadStatus(String fileRef, int expectedStatus) throws IOException, ClientProtocolException {
    HttpClientBuilder clientBuilder = HttpClientBuilder.create();
    try (CloseableHttpClient client = clientBuilder.build()) {
      HttpGet get = new HttpGet(String.format("%s/fileUpload?fileRef=%s", getBasePath(), fileRef));
      HttpResponse response = client.execute(get);
      assertEquals(expectedStatus, response.getStatusLine().getStatusCode());
    }
  }
  
  
}