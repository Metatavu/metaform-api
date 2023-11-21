package fi.metatavu.metaform.server.liquibase.changes

import fi.metatavu.metaform.server.keycloak.AuthorizationScope
import fi.metatavu.metaform.server.controllers.MetaformKeycloakController
import fi.metatavu.metaform.server.permissions.AuthzController
import io.quarkus.runtime.annotations.RegisterForReflection
import liquibase.database.Database
import liquibase.database.jvm.JdbcConnection
import liquibase.exception.CustomChangeException
import liquibase.exception.DatabaseException
import org.apache.commons.lang3.StringUtils
import org.slf4j.LoggerFactory
import java.sql.SQLException
import java.util.*
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject

/**
 * Custom change for creating authorization resources for all replies
 *
 * @author Antti Leppä
 */
@ApplicationScoped
@Suppress ("UNUSED")
@RegisterForReflection
class CreateReplyAuthzResources : AbstractAuthzCustomChange() {

    @Inject
    lateinit var metaformKeycloakController: MetaformKeycloakController

    @Inject
    lateinit var authzController: AuthzController

    @Throws(CustomChangeException::class)
    override fun execute(database: Database) {
        val connection = database.connection as JdbcConnection
        try {
            connection.prepareStatement("SELECT id FROM metaform").use { statement ->
                statement.executeQuery().use { resultSet ->
                    while (resultSet.next()) {
                        val id = resultSet.getString(1)
                        createMetaformResources(connection, id)
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
     * @throws CustomChangeException when migration fails
     */
    @Throws(CustomChangeException::class)
    private fun createMetaformResources(connection: JdbcConnection, metaformId: String) {
        var count = 0
        try {
            connection.prepareStatement("SELECT id, userId FROM reply WHERE metaform_id = ?").use { statement ->
                statement.setBytes(1, getUUIDBytes(UUID.fromString(metaformId)))
                statement.executeQuery().use { resultSet ->
                    while (resultSet.next()) {
                        val replyId = resultSet.getString(1)
                        val userId = UUID.fromString(resultSet.getString(2))
                        val name = getReplyResourceName(replyId)
                        val uri = getReplyResourceUri(metaformId, replyId)
                        try {
                            val resource = authzController.createProtectedResource(
                                keycloak = metaformKeycloakController.adminClient,
                                ownerId = userId,
                                name = name,
                                uri = uri,
                                type = RESOURCE_TYPE,
                                scopes = SCOPES
                            )

                            updateReplyResourceId(connection, replyId, resource.toString())
                            count++
                        } catch (e: Exception) {
                            val keycloakErrorMessage = getKeycloakErrorMessage(e)
                            if (StringUtils.isNotBlank(keycloakErrorMessage)) {
                                logger.warn("Skipped reply {} from realm {} because of error: {}", replyId, keycloakErrorMessage)
                                continue
                            }
                            throw CustomChangeException(String.format("Creating user %s into realm %s failed", userId), e)
                        }
                    }
                }
            }
        } catch (e: DatabaseException) {
            throw CustomChangeException(e)
        } catch (e: SQLException) {
            throw CustomChangeException(e)
        }
        appendConfirmationMessage(String.format("Created %d resources", count))
    }

    /**
     * Updates reply resource id into database
     *
     * @param connection JDBC connection
     * @param replyId reply id
     * @param resourceId resource id
     * @throws CustomChangeException when migration fails
     */
    @Throws(CustomChangeException::class)
    private fun updateReplyResourceId(connection: JdbcConnection, replyId: String, resourceId: String) {
        try {
            connection.prepareStatement("UPDATE reply set resourceid = ? WHERE id = ?").use { statement ->
                statement.setBytes(1, getUUIDBytes(UUID.fromString(resourceId)))
                statement.setBytes(2, getUUIDBytes(UUID.fromString(replyId)))
                statement.execute()
            }
        } catch (e: Exception) {
            throw CustomChangeException(e)
        }
    }

    /**
     * Returns resource name for a reply
     *
     * @param replyId replyId
     * @return resource name
     */
    private fun getReplyResourceName(replyId: String): String {
        return String.format(REPLY_RESOURCE_NAME_TEMPLATE, replyId)
    }

    /**
     * Returns resource URI for reply
     *
     * @param metaformId Metaform id
     * @param replyId reply id
     * @return resource URI
     */
    private fun getReplyResourceUri(metaformId: String, replyId: String): String {
        return String.format(REPLY_RESOURCE_URI_TEMPLATE, metaformId, replyId)
    }

    companion object {
        private val logger = LoggerFactory.getLogger(CreateReplyAuthzResources::class.java)
        private val SCOPES: List<AuthorizationScope> = listOf(AuthorizationScope.REPLY_VIEW, AuthorizationScope.REPLY_EDIT)
        private const val RESOURCE_TYPE = "urn:metaform:resources:reply"
        private const val REPLY_RESOURCE_URI_TEMPLATE = "/v1/metaforms/%s/replies/%s"
        private const val REPLY_RESOURCE_NAME_TEMPLATE = "reply-%s"
    }
}