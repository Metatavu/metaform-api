package fi.metatavu.metaform.server.liquibase.changes;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.keycloak.authorization.client.AuthzClient;
import org.keycloak.representations.idm.authorization.ResourceRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fi.metatavu.metaform.server.keycloak.AuthorizationScope;
import liquibase.database.Database;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.CustomChangeException;
import liquibase.exception.DatabaseException;

/**
 * Custom change for creating authorization resources for all replies
 * 
 * @author Antti Leppä
 */
public class CreateReplyAuthzResources extends AbstractAuthzCustomChange {
  
  private static final Logger logger = LoggerFactory.getLogger(CreateReplyAuthzResources.class);
  private static final List<AuthorizationScope> SCOPES = Arrays.asList(AuthorizationScope.REPLY_VIEW, AuthorizationScope.REPLY_EDIT);
  private static final String RESOURCE_TYPE = "urn:metaform:resources:reply";
  private static final String REPLY_RESOURCE_URI_TEMPLATE = "/v1/realms/%s/metaforms/%s/replies/%s";
  private static final String REPLY_RESOURCE_NAME_TEMPLATE = "reply-%s";

  @Override
  public void execute(Database database) throws CustomChangeException {
    JdbcConnection connection = (JdbcConnection) database.getConnection();

    try (PreparedStatement statement = connection.prepareStatement("SELECT id, realmId FROM metaform")) {
      try (ResultSet resultSet = statement.executeQuery()) {
        while (resultSet.next()) {
          String id = resultSet.getString(1);
          String realmName = resultSet.getString(2);
          createMetaformResources(connection, id, realmName);
        }
      }
    } catch (Exception e) {
      throw new CustomChangeException(e);
    } 
  }

  /**
   * Create resources for a single form
   * 
   * @param connection JDBC connection
   * @param metaformId metaform id
   * @param realmName realm name
   * @throws CustomChangeException when migration fails
   */
  private void createMetaformResources(JdbcConnection connection, String metaformId, String realmName) throws CustomChangeException {
    AuthzClient authzClient = getAuthzClient(realmName);
    int count = 0;
    
    try (PreparedStatement statement = connection.prepareStatement("SELECT id, userId FROM reply WHERE metaform_id = ?")) {
      statement.setObject(1, createPgUuid(metaformId), Types.OTHER);
      
      try (ResultSet resultSet = statement.executeQuery()) {
        while (resultSet.next()) {
          String replyId = resultSet.getString(1);
          UUID userId = UUID.fromString(resultSet.getString(2));
          String name = getReplyResourceName(replyId);
          String uri = getReplyResourceUri(realmName, metaformId, replyId);
              
          try {
            ResourceRepresentation resource = createProtectedResource(authzClient, userId, name, uri, RESOURCE_TYPE, SCOPES);
            updateReplyResourceId(connection, replyId, resource.getId());
            count++;
          } catch (Exception e) {
            String keycloakErrorMessage = getKeycloakErrorMessage(e);
            if (StringUtils.isNotBlank(keycloakErrorMessage)) {
              logger.warn("Skipped reply {} from realm {} because of error: {}", replyId, realmName, keycloakErrorMessage);
              continue;
            }
            
            throw new CustomChangeException(String.format("Creating user %s into realm %s failed", userId, realmName), e);
          }
        }
      }
    } catch (DatabaseException | SQLException e) {
      throw new CustomChangeException(e);
    }
    
    appendConfirmationMessage(String.format("Created %d resources into realm %s", count, realmName));
  }

  /**
   * Updates reply resource id into database
   * 
   * @param connection JDBC connection
   * @param replyId reply id
   * @param resourceId resource id
   * @throws CustomChangeException when migration fails
   */
  private void updateReplyResourceId(JdbcConnection connection, String replyId, String resourceId) throws CustomChangeException {
    try (PreparedStatement statement = connection.prepareStatement("UPDATE reply set resourceid = ? WHERE id = ?")) {
      statement.setObject(1, createPgUuid(resourceId), Types.OTHER);
      statement.setObject(2, createPgUuid(replyId), Types.OTHER);
      statement.execute();
    } catch (Exception e) {
      throw new CustomChangeException(e);
    } 
  }
  
  /**
   * Returns resource name for a reply
   * 
   * @param replyId replyId
   * @return resource name
   */
  private String getReplyResourceName(String replyId) {
    if (replyId == null) {
      return null;
    }
    
    return String.format(REPLY_RESOURCE_NAME_TEMPLATE, replyId);
  }
  
  /**
   * Returns resource URI for reply
   * 
   * @param realmName realm name
   * @param metaformId Metaform id
   * @param replyId reply id
   * @return resource URI
   */
  private String getReplyResourceUri(String realmName, String metaformId, String replyId) {
    return String.format(REPLY_RESOURCE_URI_TEMPLATE, realmName, metaformId, replyId);
  }

}
