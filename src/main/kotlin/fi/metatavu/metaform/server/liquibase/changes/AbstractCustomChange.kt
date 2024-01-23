package fi.metatavu.metaform.server.liquibase.changes

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import fi.metatavu.metaform.api.spec.model.Metaform
import io.quarkus.runtime.annotations.RegisterForReflection
import liquibase.change.custom.CustomTaskChange
import liquibase.database.Database
import liquibase.database.jvm.JdbcConnection
import liquibase.exception.CustomChangeException
import liquibase.exception.DatabaseException
import liquibase.exception.SetupException
import liquibase.exception.ValidationErrors
import liquibase.resource.ResourceAccessor
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.sql.SQLException
import java.util.UUID

/**
 * Abstract base class for custom Liquibase changes
 *
 * @author Antti Leppä
 */
@RegisterForReflection
abstract class AbstractCustomChange : CustomTaskChange {

    private val confirmationMessage = StringBuilder()

    /**
     * Reads metaforms from the database
     *
     * @param connection JDBC connection
     * @param ignoreUnknownProperties whether to ignore unknown JSON properties
     * @return list of metaform ids and metaforms as a pair
     */
    protected fun readMetaforms(
        connection: JdbcConnection,
        ignoreUnknownProperties: Boolean = false
    ): List<Pair<ByteArray, JsonNode>> {
        try {
            connection.prepareStatement("SELECT id, data FROM metaform").use { statement ->
                statement.executeQuery().use { resultSet ->
                    val metaforms = mutableListOf<Pair<ByteArray, JsonNode>>()
                    while (resultSet.next()) {
                        val objectMapper = jacksonObjectMapper()

                        if (ignoreUnknownProperties) {
                            objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                        }

                        objectMapper.registerModule(JavaTimeModule())

                        val id = resultSet.getBytes(1)
                        val data = resultSet.getString(2)
                        metaforms.add(Pair(id, objectMapper.readTree(data)))
                    }

                    return metaforms
                }
            }
        } catch (e: SQLException) {
            throw CustomChangeException(e)
        } catch (e: DatabaseException) {
            throw CustomChangeException(e)
        }
    }

    /**
     * Updates metaform data in the database
     *
     * @param connection JDBC connection
     * @param metaform metaform
     */
    protected fun updateMetaform(connection: JdbcConnection, metaformId: ByteArray, metaform: JsonNode) {
        try {
            connection.prepareStatement("UPDATE metaform SET data = ? WHERE id = ?").use { statement ->
                statement.setString(1, serializeMetaform(metaform))
                statement.setBytes(2, metaformId)
                statement.execute()
            }
        } catch (e: SQLException) {
            throw CustomChangeException(e)
        } catch (e: DatabaseException) {
            throw CustomChangeException(e)
        }
    }

    /**
     * Converts UUID into bytes
     *
     * @param uuid UUID
     * @return bytes
     */
    protected fun getUUIDBytes(uuid: UUID): ByteArray {
        val result = ByteArray(16)
        ByteBuffer.wrap(result).order(ByteOrder.BIG_ENDIAN).putLong(uuid.mostSignificantBits).putLong(uuid.leastSignificantBits)
        return result
    }

    /**
     * Serializes metaform into JSON string
     *
     * @param metaform metaform
     * @return JSON string
     */
    protected fun serializeMetaform(metaform: JsonNode): String {
        val objectMapper = jacksonObjectMapper()
        objectMapper.registerModule(JavaTimeModule())
        return objectMapper.writeValueAsString(metaform)
    }

    /**
     * Appends string to confirmation message
     *
     * @param message message
     */
    protected fun appendConfirmationMessage(message: String?) {
        confirmationMessage.append(message)
    }

    override fun getConfirmationMessage(): String {
        return confirmationMessage.toString()
    }

    @Throws(SetupException::class)
    override fun setUp() {
        // No need to set anything up
    }

    override fun setFileOpener(resourceAccessor: ResourceAccessor) {}
    override fun validate(database: Database): ValidationErrors? {
        return null
    }
}
