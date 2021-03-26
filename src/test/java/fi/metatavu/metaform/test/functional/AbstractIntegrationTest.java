package fi.metatavu.metaform.test.functional;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import fi.metatavu.metaform.api.client.models.Metaform;
import fi.metatavu.metaform.api.client.models.Reply;
import fi.metatavu.metaform.test.TestSettings;
import io.restassured.response.ValidatableResponse;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.eclipse.microprofile.config.ConfigProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.sql.*;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;
import static org.junit.Assert.*;
import static org.junit.Assert.fail;

public class AbstractIntegrationTest {
  private static Logger logger = LoggerFactory.getLogger(AbstractIntegrationTest.class.getName());
  public static final UUID REALM1_USER_1_ID = UUID.fromString("b6039e55-3758-4252-9858-a973b0988b63");
  public static final UUID REALM1_USER_2_ID = UUID.fromString("5ec6c56a-f618-4038-ab62-098b0db50cd5");

  /**
   * Flushes JPA cache
   */
  protected void flushCache() {
    given()
      .baseUri(TestSettings.basePath)
      .get("/system/jpa/cache/flush")
      .then();
  }

  /**
   * Executes an update statement into test database
   *
   * @param sql sql
   * @param params params
   */
  protected void executeUpdate(String sql, Object... params) {
    executeInsert(sql, params);
  }

  /**
   * Executes an insert statement into test database
   *
   * @param sql sql
   * @param params params
   */
  protected void executeInsert(String sql, Object... params) {
    try (Connection connection = getConnection()) {
      connection.setAutoCommit(true);
      try (PreparedStatement statement = connection.prepareStatement(sql)) {
        applyStatementParams(statement, params);
        statement.execute();
      }
    } catch (Exception e) {
      logger.error("Failed to execute insert", e);
      fail(e.getMessage());
    }
  }

  /**
   * Returns test database connection
   *
   * @return test database connection
   */
  private Connection getConnection() {
    String username = ConfigProvider.getConfig().getValue("quarkus.datasource.username", String.class);
    String password = ConfigProvider.getConfig().getValue("quarkus.datasource.password", String.class);
    String url = ConfigProvider.getConfig().getValue("quarkus.datasource.jdbc.url", String.class);
    try {
      String driver = ConfigProvider.getConfig().getValue("jdbc.driver", String.class);

      Class.forName(driver).newInstance();
    } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
      logger.error("Failed to load JDBC driver", e);
      fail(e.getMessage());
    }

    try {
      return DriverManager.getConnection(url, username, password);
    } catch (SQLException e) {
      logger.error("Failed to get connection", e);
      fail(e.getMessage());
    }

    return null;
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
        HttpPost post = new HttpPost(String.format("%s/fileUpload", TestSettings.basePath));
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
   * @param expectedStatus expected status code
   * @throws IOException throw then request fails
   */
  private void assertUploadStatus(String fileRef, int expectedStatus) throws IOException {
    HttpClientBuilder clientBuilder = HttpClientBuilder.create();
    try (CloseableHttpClient client = clientBuilder.build()) {
      HttpGet get = new HttpGet(String.format("%s/fileUpload?fileRef=%s", TestSettings.basePath, fileRef));
      HttpResponse response = client.execute(get);
      assertEquals(expectedStatus, response.getStatusLine().getStatusCode());
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
      HttpGet get = new HttpGet(String.format("%s/fileUpload?fileRef=%s&meta=true", TestSettings.basePath, fileRef));
      HttpResponse response = client.execute(get);
      try (InputStream contentStream = response.getEntity().getContent()) {
        return getObjectMapper().readValue(contentStream, FileUploadMeta.class);
      }
    }
  }

  /**
   * Returns object mapper with default modules and settings
   *
   * @return object mapper
   */
  protected ObjectMapper getObjectMapper() {
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.registerModule(new JavaTimeModule());
    objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    return objectMapper;
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
   * Delete uploaded file from the store
   *
   * @param fileRef fileRef
   * @throws IOException thrown on delete failure
   */
  protected void deleteUpload(String fileRef) throws IOException {
    HttpClientBuilder clientBuilder = HttpClientBuilder.create();
    try (CloseableHttpClient client = clientBuilder.build()) {
      HttpDelete delete = new HttpDelete(String.format("%s/fileUpload?fileRef=%s", TestSettings.basePath, fileRef));
      HttpResponse response = client.execute(delete);
      assertEquals(204, response.getStatusLine().getStatusCode());
    }
  }

  /**
   * Returns offset date time
   *
   * @param year year
   * @param month month
   * @param dayOfMonth day
   * @param zone zone
   * @return offset date time
   */
  protected OffsetDateTime getOffsetDateTime(int year, int month, int dayOfMonth, ZoneId zone) {
    return getZonedDateTime(year, month, dayOfMonth, 0, 0, 0, zone).toOffsetDateTime();
  }

  /**
   * Parses offset date time from string
   *
   * @param string string
   * @return parsed offset date time
   */
  protected OffsetDateTime parseOffsetDateTime(String string) {
    return OffsetDateTime.parse(string);
  }

  /**
   * Returns ISO formatted date string
   *
   * @param year year
   * @param month month
   * @param dayOfMonth day
   * @param zone zone
   * @return ISO formatted date string
   */
  protected String getIsoDateTime(int year, int month, int dayOfMonth, ZoneId zone) {
    return DateTimeFormatter.ISO_DATE_TIME.format(getOffsetDateTime(year, month, dayOfMonth, zone));
  }

  /**
   * Returns zoned date time
   *
   * @param year year
   * @param month month
   * @param dayOfMonth day
   * @param hour hour
   * @param minute minute
   * @param second second
   * @param zone zone
   * @return zoned date time
   */
  protected ZonedDateTime getZonedDateTime(int year, int month, int dayOfMonth, int hour, int minute, int second, ZoneId zone) {
    return ZonedDateTime.of(year, month, dayOfMonth, hour, minute, second, 0, zone);
  }

  /**
   * Applies params into sql statement
   *
   * @param statement statement
   * @param params params
   * @throws SQLException
   */
  private void applyStatementParams(PreparedStatement statement, Object... params)
    throws SQLException {
    for (int i = 0, l = params.length; i < l; i++) {
      Object param = params[i];
      if (param instanceof List) {
        statement.setObject(i + 1, ((List<?>) param).toArray());
      } else if (param instanceof UUID) {
        statement.setBytes(i + 1, getUUIDBytes((UUID) param));
      } else {
        statement.setObject(i + 1, params[i]);
      }
    }
  }

  /**
   * Converts UUID into bytes
   *
   * @param uuid UUID
   * @return bytes
   */
  private byte[] getUUIDBytes(UUID uuid) {
    byte[] result = new byte[16];
    ByteBuffer.wrap(result).order(ByteOrder.BIG_ENDIAN).putLong(uuid.getMostSignificantBits()).putLong(uuid.getLeastSignificantBits());
    return result;
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
      .baseUri(TestSettings.basePath)
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
   * Resolves an access token for realm, client, username and password
   *
   * @param realm realm
   * @param clientId clientId
   * @param username username
   * @param password password
   * @return an access token
   * @throws IOException thrown on communication failure

  protected String getAccessToken(String realm, String clientId, String username, String password) throws IOException {
    String path = String.format("/realms/%s/protocol/openid-connect/token", realm);

    String response = given()
      .baseUri(ConfigProvider.getConfig().getValue("metaforms.keycloak.admin.host", String.class))
      .formParam("client_id", clientId)
      .formParam("grant_type", "password")
      .formParam("username", username)
      .formParam("password", password)
      .formParam("client_secret", DEFAULT_UI_CLIENT_SECRET)
      .post(path)
      .getBody()
      .asString();
    System.out.println("token is "+response);

    Map<String, Object> responseMap = readJsonMap(response);
    String token = (String) responseMap.get("access_token");
    System.out.println("token is "+token);

    assertNotNull(token);

    return token;
  }*/
  /**
   * Reads JSON src into Map
   *
   * @param src input
   * @return map
   * @throws IOException throws IOException when there is error when reading the input
   */
  protected Map<String, Object> readJsonMap(String src) throws IOException {
    return getObjectMapper().readValue(src, new TypeReference<Map<String, Object>>() {});
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
   * Asserts that given object is list and contains same items as the expected list (in any order)
   *
   * @param expected expected list
   * @param actual actual object
   */
  protected void assertListsEqualInAnyOrder(List<?> expected, Object actual) {
    assertTrue(actual instanceof List);
    assertThat((List<?>) actual, containsInAnyOrder(expected.toArray()));
  }

}
