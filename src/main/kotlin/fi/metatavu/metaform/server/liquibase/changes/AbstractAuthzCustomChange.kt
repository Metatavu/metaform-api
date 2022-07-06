package fi.metatavu.metaform.server.liquibase.changes

import fi.metatavu.metaform.server.keycloak.AuthorizationScope
import org.apache.commons.io.IOUtils
import org.apache.commons.lang3.StringUtils
import org.keycloak.authorization.client.AuthzClient
import org.keycloak.authorization.client.util.HttpResponseException
import org.keycloak.representations.idm.authorization.ResourceRepresentation
import org.keycloak.representations.idm.authorization.ScopeRepresentation
import java.io.IOException
import java.io.InputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.*
import javax.ws.rs.BadRequestException

/**
 * Abstract base class for custom Liquibase changes regarding authz operations
 *
 * @author Antti Leppä
 */
abstract class AbstractAuthzCustomChange : AbstractCustomChange() {
    /**
     * Creates protected resource into Keycloak
     *
     * @param ownerId resource owner id
     * @param name resource's human-readable name
     * @param uri resource's uri
     * @param type resource's type
     * @param scopes resource's scopes
     *
     * @return created resource
     */
    protected fun createProtectedResource(client: AuthzClient, ownerId: UUID, name: String?, uri: String?, type: String, scopes: List<AuthorizationScope>): ResourceRepresentation {
        val scopeRepresentations: Set<ScopeRepresentation> = scopes
                .map(AuthorizationScope::scopeName)
                .map { ScopeRepresentation(it) }
                .toSet()
        val resource = ResourceRepresentation(name, scopeRepresentations, uri, type)
        resource.setOwner(ownerId.toString())
        resource.ownerManagedAccess = true
        return client.protection().resource().create(resource)
    }

    /**
     * Resolves Keycloak error message from exception
     *
     * @param e exception
     * @return error message
     */
    protected fun getKeycloakErrorMessage(e: Throwable?): String? {
        val httpResponseException = unwrapHttpException(e)
        if (httpResponseException != null) {
            var message: String? = String(httpResponseException.bytes)
            if (StringUtils.isBlank(message)) {
                message = httpResponseException.message
            }
            if (StringUtils.isBlank(message)) {
                message = httpResponseException.reasonPhrase
            }
            return if (StringUtils.isNotBlank(message)) {
                message
            } else "Unknown error"
        }
        val badRequestException = unwrapBadRequestException(e)
        if (badRequestException != null) {
            val body = badRequestException.response.entity as InputStream
            var message = toString(body)
            if (StringUtils.isBlank(message)) {
                message = badRequestException.message
            }
            if (StringUtils.isNotBlank(message)) {
                return message
            }
        }
        return "Unknown error"
    }

    /**
     * Gets the contents of an as a String
     *
     * @param inputStream
     * @return string
     */
    private fun toString(inputStream: InputStream): String? {
        try {
            return IOUtils.toString(inputStream, "UTF-8")
        } catch (e: IOException) {
            // Just eat IO exceptions
        }
        return null
    }

    /**
     * Unwraps HttpResponseException from Keycloak
     *
     * @param e Exception
     * @return unwrapped exception
     */
    protected fun unwrapHttpException(e: Throwable?): HttpResponseException? {
        if (e == null) {
            return null
        }
        return if (e is HttpResponseException) {
            e
        } else unwrapHttpException(e.cause)
    }

    /**
     * Unwraps HttpResponseException from Keycloak
     *
     * @param e Exception
     * @return unwrapped exception
     */
    protected fun unwrapBadRequestException(e: Throwable?): BadRequestException? {
        if (e == null) {
            return null
        }
        return if (e is BadRequestException) {
            e
        } else unwrapBadRequestException(e.cause)
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
}