package fi.metatavu.metaform.server.test.functional.builder.resources;

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.containers.MySQLContainerProvider;

import java.util.HashMap;
import java.util.Map;

/**
 * Starts test container for mysql
 */
public class MysqlResource implements QuarkusTestResourceLifecycleManager {
  private static final String DATABASE = "db";
  private static final String USERNAME = "user";
  private static final String PASSWORD = "pass";

  static final JdbcDatabaseContainer db = new MySQLContainerProvider()
    .newInstance("5.6")
    .withDatabaseName(DATABASE)
    .withUsername(USERNAME)
    .withPassword(PASSWORD);


  @Override
  public Map<String, String> start() {
    db.withCommand(
      "--character-set-server=utf8mb4",
      "--collation-server=utf8mb4_unicode_ci",
      "--lower_case_table_names=1"
    );
    db.start();

    HashMap config = new HashMap<String, String>();
    config.put("quarkus.datasource.username", USERNAME);
    config.put("quarkus.datasource.password", PASSWORD);
    config.put("quarkus.datasource.jdbc.url", db.getJdbcUrl());
    config.put("jdbc.driver", db.getDriverClassName());

    return config;
  }

  @Override
  public void stop() {
    db.stop();
  }
}