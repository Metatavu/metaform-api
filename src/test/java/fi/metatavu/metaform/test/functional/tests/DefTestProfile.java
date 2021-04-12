package fi.metatavu.metaform.test.functional.tests;

import io.quarkus.test.junit.QuarkusTestProfile;

import java.util.Collections;
import java.util.Map;

public class DefTestProfile implements QuarkusTestProfile {
  @Override
  public Map<String, String> getConfigOverrides() {
    return Collections.singletonMap("runmode","TEST");
  }
}
