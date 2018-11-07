package fi.metatavu.metaform.server.liquibase.changes;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.HashMap;
import java.util.Map;

import liquibase.database.Database;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.CustomChangeException;
import liquibase.exception.DatabaseException;

/**
 * Custom change for creating authorization resources for all replies
 * 
 * @author Antti Leppä
 */
public class FormSlugs extends AbstractAuthzCustomChange {
  
  @Override
  public void execute(Database database) throws CustomChangeException {
    JdbcConnection connection = (JdbcConnection) database.getConnection();

    Map<String, Integer> ids = new HashMap<>();
    
    try (PreparedStatement statement = connection.prepareStatement("SELECT id, realmId FROM metaform")) {
      try (ResultSet resultSet = statement.executeQuery()) {
        while (resultSet.next()) {
          String id = resultSet.getString(1);
          String realmName = resultSet.getString(2);
          
          if (!ids.containsKey(realmName)) {
            ids.put(realmName, 1);
          } else {
            ids.put(realmName, ids.get(realmName) + 1);
          }
          
          String slug = String.format("form-%d", ids.get(realmName));
          
          updateFormSlug(connection, id, slug);
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
  private void updateFormSlug(JdbcConnection connection, String metaformId, String slug) throws CustomChangeException {
    try (PreparedStatement statement = connection.prepareStatement("UPDATE metaform set slug = ? WHERE id = ?")) {
      statement.setString(1, slug);
      statement.setObject(2, createPgUuid(metaformId), Types.OTHER);      
      statement.execute();
    } catch (DatabaseException e) {
      throw new CustomChangeException(e);
    } catch (SQLException e) {
      throw new CustomChangeException(e);
    }
    
    appendConfirmationMessage(String.format("Updated metaform %s slug to %s", metaformId, slug));
  }

}
