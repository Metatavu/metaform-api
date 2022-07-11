package fi.metatavu.metaform.server.liquibase.changes

import liquibase.database.Database
import liquibase.database.jvm.JdbcConnection
import liquibase.exception.CustomChangeException
import liquibase.exception.DatabaseException
import java.sql.SQLException
import java.util.*

/**
 * Custom change for creating authorization resources for all replies
 *
 * @author Antti Leppä
 */
class FormSlugs : AbstractAuthzCustomChange() {
    @Throws(CustomChangeException::class)
    override fun execute(database: Database) {
        val connection = database.connection as JdbcConnection
        val ids: MutableMap<String, Int> = HashMap()
        try {
            connection.prepareStatement("SELECT id, realmId FROM metaform").use { statement ->
                statement.executeQuery().use { resultSet ->
                    while (resultSet.next()) {
                        val id = resultSet.getString(1)
                        val realmName = resultSet.getString(2)
                        if (!ids.containsKey(realmName)) {
                            ids[realmName] = 1
                        } else {
                            ids[realmName] = ids[realmName]!! + 1
                        }
                        val slug = String.format("form-%d", ids[realmName])
                        updateFormSlug(connection, id, slug)
                    }
                }
            }
        } catch (e: Exception) {
            throw CustomChangeException(e)
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
    @Throws(CustomChangeException::class)
    private fun updateFormSlug(connection: JdbcConnection, metaformId: String, slug: String) {
        try {
            connection.prepareStatement("UPDATE metaform set slug = ? WHERE id = ?").use { statement ->
                statement.setString(1, slug)
                statement.setBytes(2, getUUIDBytes(UUID.fromString(metaformId)))
                statement.execute()
            }
        } catch (e: SQLException) {
            throw CustomChangeException(e)
        } catch (e: DatabaseException) {
            throw CustomChangeException(e)
        }
        appendConfirmationMessage(String.format("Updated metaform %s slug to %s", metaformId, slug))
    }
}