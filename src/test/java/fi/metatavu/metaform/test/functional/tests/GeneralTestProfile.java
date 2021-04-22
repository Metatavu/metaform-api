package fi.metatavu.metaform.test.functional.tests;

import io.quarkus.test.junit.QuarkusTestProfile;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Test profile settings common values for all tests
 */
public class GeneralTestProfile implements QuarkusTestProfile {

  @Override
  public Map<String, String> getConfigOverrides() {
    Map<String, String> properties = new HashMap<String, String>();
    properties.put("runmode", "TEST");
    properties.put("metaform.uploads.folder", "/tmp");
    properties.put("quarkus.liquibase.contexts", "test");
    return properties;
  }
}
