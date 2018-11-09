package fi.metatavu.metaform.server.keycloak;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.keycloak.adapters.KeycloakConfigResolver;
import org.keycloak.adapters.KeycloakDeployment;
import org.keycloak.adapters.KeycloakDeploymentBuilder;
import org.keycloak.adapters.spi.HttpFacade.Request;
import org.keycloak.representations.adapters.config.AdapterConfig;
import org.keycloak.representations.adapters.config.PolicyEnforcerConfig;
import org.keycloak.representations.adapters.config.PolicyEnforcerConfig.EnforcementMode;
import org.keycloak.representations.adapters.config.PolicyEnforcerConfig.MethodConfig;
import org.keycloak.representations.adapters.config.PolicyEnforcerConfig.PathConfig;
import org.keycloak.representations.adapters.config.PolicyEnforcerConfig.UserManagedAccessConfig;
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
          AdapterConfig adapterConfig = KeycloakDeploymentBuilder.loadAdapterConfig(configStream);          
          adapterConfig.setPolicyEnforcerConfig(createPolicyEnforcerConfig());
          return KeycloakDeploymentBuilder.build(adapterConfig);
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

  /**
   * Creates policy enforcer config
   * 
   * @return policy enforcer config
   */
  private PolicyEnforcerConfig createPolicyEnforcerConfig() {
    PolicyEnforcerConfig result = new PolicyEnforcerConfig();
    result.setEnforcementMode(EnforcementMode.PERMISSIVE);
    result.setUserManagedAccess(new UserManagedAccessConfig());
    result.setPaths(Arrays.asList(createRepliesPolicyEnforcerConfigPath(), createReplyPolicyEnforcerConfigPath()));
    return result;
  }
  
  /**
   * Creates replies policy enforcer path config
   * 
   * @return replies policy enforcer path config
   */
  private PathConfig createRepliesPolicyEnforcerConfigPath() {
    return createPolicyEnforcerConfigPath("Replies", "/v1/realms/{realmId}/metaforms/{metaformId}/replies", Arrays.asList(
      createPolicyEnforcerMethodConfig("GET", Arrays.asList("reply:view")),
      createPolicyEnforcerMethodConfig("POST", Arrays.asList("reply:create"))
    ));
  }
  
  /**
   * Creates reply policy enforcer path config
   * 
   * @return reply policy enforcer path config
   */
  private PathConfig createReplyPolicyEnforcerConfigPath() {
    return createPolicyEnforcerConfigPath("Reply", "/v1/realms/{realmId}/metaforms/{metaformId}/replies/{id}", Arrays.asList(
      createPolicyEnforcerMethodConfig("GET", Arrays.asList("reply:view")),
      createPolicyEnforcerMethodConfig("PUT", Arrays.asList("reply:edit")),
      createPolicyEnforcerMethodConfig("DELETE", Arrays.asList("reply:edit"))
    ));
  }
  
  /**
   * Creates policy enforcer method config
   * 
   * @param method method
   * @param scopes scopes
   * @return policy enforcer method config
   */
  private MethodConfig createPolicyEnforcerMethodConfig(String method, List<String> scopes) {
    MethodConfig result = new MethodConfig();
    result.setMethod(method);
    result.setScopes(scopes);
    return result;
  }
  
  /**
   * Creates policy enforcer config path
   * 
   * @param name name
   * @param path path
   * @param methods methods
   * @return create path config
   */
  private PathConfig createPolicyEnforcerConfigPath(String name, String path, List<MethodConfig> methods) {
    PathConfig result = new PathConfig();
    result.setPath(path);
    result.setName(name);
    result.setMethods(methods);
    return result;
  }

}