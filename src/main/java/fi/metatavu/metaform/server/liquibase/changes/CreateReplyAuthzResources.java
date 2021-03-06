package fi.metatavu.metaform.server.liquibase.changes;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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
  private static final String REPLY_RESOURCE_URI_TEMPLATE = "/v1/metaforms/%s/replies/%s";
  private static final String REPLY_RESOURCE_NAME_TEMPLATE = "reply-%s";

  @Override
  public void execute(Database database) throws CustomChangeException {
    JdbcConnection connection = (JdbcConnection) database.getConnection();

    try (PreparedStatement statement = connection.prepareStatement("SELECT id FROM metaform")) {
      try (ResultSet resultSet = statement.executeQuery()) {
        while (resultSet.next()) {
          String id = resultSet.getString(1);
          createMetaformResources(connection, id);
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
   * @throws CustomChangeException when migration fails
   */
  @SuppressWarnings ("squid:S1141")
  private void createMetaformResources(JdbcConnection connection, String metaformId) throws CustomChangeException {
    AuthzClient authzClient = getAuthzClient();
    int count = 0;
    
    try (PreparedStatement statement = connection.prepareStatement("SELECT id, userId FROM reply WHERE metaform_id = ?")) {
      statement.setBytes(1, getUUIDBytes(UUID.fromString(metaformId)));
      
      try (ResultSet resultSet = statement.executeQuery()) {
        while (resultSet.next()) {
          String replyId = resultSet.getString(1);
          UUID userId = UUID.fromString(resultSet.getString(2));
          String name = getReplyResourceName(replyId);
          String uri = getReplyResourceUri(metaformId, replyId);
              
          try {
            ResourceRepresentation resource = createProtectedResource(authzClient, userId, name, uri, RESOURCE_TYPE, SCOPES);
            updateReplyResourceId(connection, replyId, resource.getId());
            count++;
          } catch (Exception e) {
            String keycloakErrorMessage = getKeycloakErrorMessage(e);
            if (StringUtils.isNotBlank(keycloakErrorMessage)) {
              logger.warn("Skipped reply {} from realm {} because of error: {}", replyId, keycloakErrorMessage);
              continue;
            }
            
            throw new CustomChangeException(String.format("Creating user %s into realm %s failed", userId), e);
          }
        }
      }
    } catch (DatabaseException | SQLException e) {
      throw new CustomChangeException(e);
    }
    
    appendConfirmationMessage(String.format("Created %d resources", count));
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
      statement.setBytes(1, getUUIDBytes(UUID.fromString(resourceId)));
      statement.setBytes(2, getUUIDBytes(UUID.fromString(replyId)));
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
   * @param metaformId Metaform id
   * @param replyId reply id
   * @return resource URI
   */
  private String getReplyResourceUri(String metaformId, String replyId) {
    return String.format(REPLY_RESOURCE_URI_TEMPLATE, metaformId, replyId);
  }

}
