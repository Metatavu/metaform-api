package fi.metatavu.metaform.server.rest

import fi.metatavu.metaform.api.spec.model.BadRequest
import fi.metatavu.metaform.server.controllers.ReplyController
import fi.metatavu.metaform.server.keycloak.AuthorizationScope
import fi.metatavu.metaform.server.controllers.KeycloakController
import fi.metatavu.metaform.server.persistence.model.Reply
import org.apache.commons.lang3.StringUtils
import org.eclipse.microprofile.jwt.JsonWebToken
import org.jboss.resteasy.spi.HttpRequest
import java.io.ByteArrayInputStream
import java.io.IOException
import java.io.InputStream
import java.io.UnsupportedEncodingException
import java.time.OffsetDateTime
import java.util.*
import javax.enterprise.context.RequestScoped
import javax.inject.Inject
import javax.ws.rs.core.Context
import javax.ws.rs.core.Response
import javax.ws.rs.core.SecurityContext

/**
 * Abstract base class for all API services
 *
 * @author Antti Leppä
 */
@RequestScoped
abstract class AbstractApi {

    @Context
    lateinit var request: HttpRequest

    @Inject
    lateinit var jsonWebToken: JsonWebToken

    @Context
    lateinit var securityContext: SecurityContext

    @Inject
    lateinit var keycloakController: KeycloakController

    @Inject
    lateinit var replyController: ReplyController

    /**
     * Returns request locale
     * 
     * @return request locale
     */
    protected val locale: Locale
        get() {
            val httpHeaders = request.httpHeaders
            val acceptableLanguages = httpHeaders.acceptableLanguages
            return if (acceptableLanguages.isEmpty()) Locale.getDefault() else acceptableLanguages[0]
        }

    /**
     * Returns logged user id
     *
     * @return logged user id
     */
    protected val loggedUserId: UUID?
        get() = if (jsonWebToken.subject == null) {
            null
        } else UUID.fromString(jsonWebToken.subject)

    /**
     * Constructs ok response
     *
     * @param entity payload
     * @return response
     */
    protected fun createOk(entity: Any?): Response {
        return Response
                .status(Response.Status.OK)
                .entity(entity)
                .build()
    }

    /**
     * Constructs no content response
     *
     * @return response
     */
    protected fun createNoContent(): Response {
        return Response
                .status(Response.Status.NO_CONTENT)
                .build()
    }

    /**
     * Constructs an error response
     *
     * @param status status code
     * @param message message
     * @return error response
     */
    private fun createError(status: Response.Status, message: String?): Response {
        val entity = BadRequest(
                message = message,
                code = status.statusCode
        )

        return Response
                .status(status)
                .entity(entity)
                .build()
    }

    /**
     * Creates not allowed message with given parameters
     *
     * @param action target action
     * @param target target
     * @return not allowed message
     */
    protected fun createNotAllowedMessage(action: String, target: String): String {
        return "You are not allowed to $action $target"
    }

    /**
     * Creates Anonymous not allowed message with given parameters
     *
     * @param action target action
     * @param target target
     * @return not allowed message
     */
    protected fun createAnonNotAllowedMessage(action: String, target: String): String {
        return "Anonymous users are not allowed to $action $target"
    }

    /**
     * Creates not found message with given parameters
     *
     * @param target target of the find method
     * @param id ID of the target
     * @return not found message
     */
    protected fun createNotFoundMessage(target: String, id: UUID): String {
        return "$target with ID $id could not be found"
    }

    /**
     * Creates invalid message with given parameters
     *
     * @param target target of the find method
     * @return invalid target message
     */
    protected fun createInvalidMessage(target: String): String {
        return "Invalid $target"
    }

    /**
     * Constructs internal server error response
     *
     * @param message message
     * @return not belong message
     */
    protected fun createNotBelongMessage(target: String): String {
        return "$target does not belong to metaform"
    }

    /**
     * Creates duplicated message with given parameters
     *
     * @param target target of the find method
     * @return duplicated target message
     */
    protected fun createDuplicatedMessage(target: String): String {
        return "Duplicated $target"
    }

    /**
     * Constructs bad request response
     *
     * @param message message
     * @return response
     */
    protected fun createBadRequest(message: String?): Response {
        return createError(Response.Status.BAD_REQUEST, message)
    }

    /**
     * Constructs not found response
     *
     * @param message message
     * @return response
     */
    protected fun createNotFound(message: String): Response {
        return createError(Response.Status.NOT_FOUND, message)
    }

    /**
     * Constructs conflict response
     *
     * @param message message
     * @return response
     */
    protected fun createConflict(message: String): Response {
        return createError(Response.Status.CONFLICT, message)
    }

    /**
     * Constructs not implemented response
     *
     * @param message message
     * @return response
     */
    protected fun createNotImplemented(message: String): Response {
        return createError(Response.Status.NOT_IMPLEMENTED, message)
    }

    /**
     * Constructs internal server error response
     *
     * @param message message
     * @return response
     */
    protected fun createInternalServerError(message: String?): Response {
        return createError(Response.Status.INTERNAL_SERVER_ERROR, message)
    }

    /**
     * Constructs forbidden response
     *
     * @param message message
     * @return response
     */
    protected fun createForbidden(message: String): Response {
        return createError(Response.Status.FORBIDDEN, message)
    }

    /**
     * Creates streamed response from string using a UTF-8 encoding
     *
     * @param data data
     * @param type content type
     * @return Response
     */
    fun streamResponse(data: String, type: String): Response {
        return streamResponse(data, "UTF-8", type)
    }

    /**
     * Creates streamed response from string using specified encoding
     *
     * @param data data
     * @param type content type
     * @return Response
     */
    fun streamResponse(data: String, charsetName: String, type: String): Response {
        return try {
            streamResponse(data.toByteArray(charset(charsetName)), type)
        } catch (e: UnsupportedEncodingException) {
            Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(INTERNAL_SERVER_ERROR)
                    .build()
        }
    }

    /**
     * Creates streamed response from byte array
     *
     * @param data data
     * @param type content type
     * @return Response
     */
    fun streamResponse(data: ByteArray, type: String): Response {
        try {
            ByteArrayInputStream(data).use { byteStream -> return streamResponse(type, byteStream, data.size) }
        } catch (e: IOException) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(INTERNAL_SERVER_ERROR)
                    .build()
        }
    }

    /**
     * Creates streamed response from input stream
     *
     * @param inputStream data
     * @param type content type
     * @param contentLength content length
     * @return Response
     */
    fun streamResponse(type: String, inputStream: InputStream, contentLength: Int): Response {
        return Response.ok(StreamingOutputImpl(inputStream), type)
                .header("Content-Length", contentLength)
                .build()
    }

    /**
     * Returns whether logged user is anonymous
     *
     * @return whether logged user is anonymous
     */
    protected val isAnonymous: Boolean
        get() = !hasRealmRole(ANSWERER_ROLE, USER_ROLE, ADMIN_ROLE, SUPER_ROLE)

    /**
     * Returns whether logged user is realm user
     *
     * @return whether logged user is realm user
     */
    protected val isRealmUser: Boolean
        get() = hasRealmRole(USER_ROLE)

    /**
     * Returns whether logged user is realm Metaform admin
     *
     * @return whether logged user is realm Metaform admin
     */
    protected val isRealmMetaformAdmin: Boolean
        get() = hasRealmRole(ADMIN_ROLE)

    /**
     * Returns whether logged user is realm Metaform super
     *
     * @return whether logged user is realm Metaform super
     */
    protected val isRealmMetaformSuper: Boolean
        get() = hasRealmRole(SUPER_ROLE)

    /**
     * Returns whether logged user has at least one of specified realm roles
     *
     * @param roles roles
     * 
     * @return whether logged user has specified realm role or not
     */
    protected fun hasRealmRole(vararg roles: String?): Boolean {
        for (i in roles.indices) {
            if (securityContext.isUserInRole(roles[i])) return true
        }
        return false
    }

    /**
     * Returns access token as string
     *
     * @return access token as string
     */
    protected val tokenString: String
        get() = jsonWebToken.rawToken

    /**
     * Returns whether given reply is permitted within given scope
     *
     * @param reply reply
     * @param ownerKey reply owner key
     * @param authorizationScope scope
     * @return whether given reply is permitted within given scope
     */
    fun isPermittedReply(reply: Reply?, ownerKey: String?, authorizationScope: AuthorizationScope): Boolean {
        if (isRealmMetaformAdmin || isRealmMetaformSuper) {
            return true
        }
        if (reply?.resourceId == null) {
            return false
        }
        if (replyController.isValidOwnerKey(reply, ownerKey)) {
            return true
        }
        if (!isRealmUser) {
            return false
        }

        return isPermittedResourceId(reply.resourceId!!, authorizationScope)
    }

    /**
     * Returns whether given resource id is permitted within given scope
     *
     * @param resourceId resource id
     * @param authorizationScope scope
     * @return whether given resource id is permitted within given scope
     */
    private fun isPermittedResourceId(resourceId: UUID, authorizationScope: AuthorizationScope): Boolean {
        val permittedResourceIds = keycloakController.getPermittedResourceIds(tokenString, setOf(resourceId), authorizationScope)
        return permittedResourceIds.size == 1 && resourceId == permittedResourceIds.iterator().next()
    }

    /**
     * Parses date time from string
     *
     * @param timeString
     * @return Parsed offset date time
     */
    protected fun parseTime(timeString: String?): OffsetDateTime? {
        return if (StringUtils.isEmpty(timeString)) {
            null
        } else OffsetDateTime.parse(timeString)
    }

    companion object {
        const val USER_ROLE = "user"
        const val ANSWERER_ROLE = "answerer"
        const val ADMIN_ROLE = "metaform-admin"
        const val SUPER_ROLE = "metaform-super"
        const val VIEW_ALL_REPLIES_ROLE = "metaform-view-all-replies"
        const val VIEW_AUDIT_LOGS_ROLE = "metaform-view-all-audit-logs"
        const val UNAUTHORIZED = "Unauthorized"
        const val LIST = "list"
        const val FIND = "find"
        const val UPDATE = "update"
        const val DELETE = "delete"
        const val CREATE = "create"
        const val EXPORT = "export"
        const val METAFORM = "metaform"
        const val ADMIN_THEME = "admin theme"
        const val REPLY = "reply"
        const val SLUG = "slug"
        const val LOGS = "logs"
        const val EXPORT_THEME = "export theme"
        const val EXPORT_THEME_FILE = "export theme file"
        const val ATTACHMENT = "attachment"
        const val REPLY_MODE = "reply mode"
        const val DRAFT = "draft"
        const val EMAIL_NOTIFICATION = "email notification"
        const val METAFORM_VERSION = "metaform version"
        const val ANONYMOUS_USERS_MESSAGE = "Anonymous users are not allowed on this Metaform"
        const val FAILED_TO_TRANSLATE_METAFORM = "Failed to translate metaform"

        private const val INTERNAL_SERVER_ERROR = "Internal Server Error"
    }
}