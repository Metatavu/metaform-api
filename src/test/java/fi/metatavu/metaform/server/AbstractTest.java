package fi.metatavu.metaform.server;

import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TestName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * Abstract base class for all tests
 * 
 * @author Antti Leppä
 * @author Heikki Kurhinen
 */
public abstract class AbstractTest {
  
  private static Logger logger = LoggerFactory.getLogger(AbstractTest.class.getName());
  
  @Rule
  public TestName testName = new TestName();
  
  @Before
  @SuppressWarnings ("squid:S106")
  public void printName() {
    System.out.println(String.format("> %s", testName.getMethodName()));
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
   * Reads JSON src into Map
   * 
   * @param src input
   * @return map
   * @throws IOException throws IOException when there is error when reading the input 
   */
  protected Map<String, Object> readJsonMap(InputStream src) throws IOException {
    return getObjectMapper().readValue(src, new TypeReference<Map<String, Object>>() {});
  }

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
   * Executes an insert statement into test database
   * 
   * @param sql sql
   * @param params params
   */
  protected void executeInsert(String sql, Object... params) {
    try (Connection connection = getConnection()) {
      connection.setAutoCommit(true);
      PreparedStatement statement = connection.prepareStatement(sql);
      try {
        applyStatementParams(statement, params);
        statement.execute();
      } finally {
        statement.close();
      }
    } catch (Exception e) {
      logger.error("Failed to execute insert", e);
      fail(e.getMessage());
    }
  }
  
  /**
   * Executes a delete statement
   * 
   * @param sql sql 
   * @param params params
   */
  protected void executeDelete(String sql, Object... params) {
    try (Connection connection = getConnection()) {
      connection.setAutoCommit(true);
      PreparedStatement statement = connection.prepareStatement(sql);
      try {
        applyStatementParams(statement, params);
        statement.execute();
      } finally {
        statement.close();
      }
    } catch (Exception e) {
      logger.error("Failed to execute delete", e);
      fail(e.getMessage());
    }
  }

  /**
   * Returns test database connection
   * 
   * @return test database connection
   */
  private Connection getConnection() {
    String username = System.getProperty("it.jdbc.username");
    String password = System.getProperty("it.jdbc.password");
    String url = System.getProperty("it.jdbc.url");
    try {
      Class.forName(System.getProperty("it.jdbc.driver")).newInstance();
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
      } else {
        statement.setObject(i + 1, params[i]);
      }
    }
  }
 
}
