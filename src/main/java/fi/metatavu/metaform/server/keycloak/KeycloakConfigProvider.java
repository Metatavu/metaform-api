package fi.metatavu.metaform.server.keycloak;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.keycloak.authorization.client.Configuration;
import org.keycloak.util.JsonSerialization;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Keycloak realm config provider
 * 
 * @author Antti Leppä
 */
public class KeycloakConfigProvider {

  private static Logger logger = LoggerFactory.getLogger(KeycloakConfigProvider.class.getName());

  /**
   * Returns all configured realms
   * 
   * @return all configured realms
   */
  public static List<String> getConfiguredRealms() {
    File configPath = new File(System.getProperty("metaform-api.config-path"));
    
    return Arrays.stream(configPath.listFiles())
      .map((file) -> {
        System.out.println(file.getAbsolutePath());
        
        return file;
      })
      .map(KeycloakConfigProvider::getConfig)
      .map(Configuration::getRealm)
      .filter(Objects::nonNull)
      .collect(Collectors.toList());
  }
  /**
   * Resolves Keycloak config file for a realm
   * 
   * @param realmName Keycloak realm name
   * @return config file or null if not found
   */
  public static File getConfigFile(String realmName) {
    String configParentSetting = System.getProperty("metaform-api.config-path");
    if (configParentSetting == null) {
      logger.error("Config parent setting not set");
      return null;
    }

    File configParent = new File(configParentSetting);
    if (!configParent.exists()) {
      logger.error("Config parent setting folder does not exist");
      return null;
    }

    File configFile = new File(configParent, String.format("%s.json", realmName));
    if (!configFile.exists()) {
      logger.warn(String.format("Keycloak config not found for realm %s", realmName));
      return null;
    }
    
    return configFile;
  }
    
  /**
   * Resolves Keycloak client configuration for a realm
   * 
   * @param realmName realm
   * @return configuration or null if configuration could not be created
   */
  public static Configuration getConfig(String realmName) {
    File configFile = KeycloakConfigProvider.getConfigFile(realmName);
    if (configFile != null) {
      return getConfig(configFile);
    }
    
    return null;
  }

  private static Configuration getConfig(File configFile) {
    try (FileInputStream inputStream = new FileInputStream(configFile)) {
      return JsonSerialization.readValue(inputStream, Configuration.class);
    } catch (IOException e) {
      logger.error("Failed to load Keycloak config {}", e);
    }
    
    return null;
  }  
  
}
