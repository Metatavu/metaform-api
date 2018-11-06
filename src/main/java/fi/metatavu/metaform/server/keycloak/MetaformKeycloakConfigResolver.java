package fi.metatavu.metaform.server.keycloak;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.keycloak.adapters.KeycloakConfigResolver;
import org.keycloak.adapters.KeycloakDeployment;
import org.keycloak.adapters.KeycloakDeploymentBuilder;
import org.keycloak.adapters.spi.HttpFacade.Request;
import org.keycloak.representations.adapters.config.AdapterConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Keycloak config resolver that resolves used config file by the realm id parameter in the path
 * 
 * @author Antti Leppä
 */
public class MetaformKeycloakConfigResolver implements KeycloakConfigResolver {
  
  private static Logger logger = LoggerFactory.getLogger(MetaformKeycloakConfigResolver.class.getName());
  private static Pattern pattern = Pattern.compile("(\\/v1\\/realms\\/)(.*?)\\/");
  
  @Override
  public KeycloakDeployment resolve(Request request) {
    String path = request.getRelativePath();
    if (path == null) {
      logger.error("Could not resolve Keycloak config because path was null");
      return null;
    }
    
    Matcher matcher = pattern.matcher(path);
    
    if (matcher.find()) {
      String realmName = matcher.group(2);
      if (realmName == null) {
        logger.warn("Could not resolve Keycloak config because realm id was not set");
        return null;
      }
      
      File configFile = KeycloakConfigProvider.getConfigFile(realmName);
      
      FileInputStream configStream;
      try {
        configStream = new FileInputStream(configFile);
        try {
          return KeycloakDeploymentBuilder.build(configStream);
        } finally {
          configStream.close();
        }
      } catch (IOException e) {
        logger.warn("Failed to read config file", e);
      }

      return null;
    }
    
    AdapterConfig adapterConfig = new AdapterConfig();
    adapterConfig.setRealm("unauthorized");
    adapterConfig.setResource("unauthorized");
    adapterConfig.setAuthServerUrl("http://localhost:123");
    
    return KeycloakDeploymentBuilder.build(adapterConfig);
  }

}