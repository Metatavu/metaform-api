package fi.metatavu.metaform.test.functional.builder.resources;

import dasniko.testcontainers.keycloak.KeycloakContainer;
import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MockServerContainer;
import org.testcontainers.utility.DockerImageName;

import java.util.HashMap;
import java.util.Map;

/**
 * Resource for wiremock container
 */
public class WireMockResource implements QuarkusTestResourceLifecycleManager {
  public static GenericContainer<?> container =
    new GenericContainer<>("rodolpheche/wiremock")
      .withExposedPorts(8080);

  @Override
  public Map<String, String> start() {
    container.start();

    HashMap config = new HashMap<String, String>();
    config.put("wiremock.port", container.getMappedPort(8080).toString());
    config.put("mailgun.api_url", "http://localhost:"+container.getMappedPort(8080).toString()+"/mgapi");
    config.put("mailgun.domain", "domain.example.com");
    config.put("mailgun.api_key", "fakekey");
    config.put("mailgun.sender_email", "metaform-test@example.com");
    config.put("mailgun.sender_name", "Metaform Test");
    return config;
  }

  @Override
  public void stop() {
    container.stop();
  }

}
