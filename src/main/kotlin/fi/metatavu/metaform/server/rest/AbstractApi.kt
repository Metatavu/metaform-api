package fi.metatavu.metaform.server.rest

import fi.metatavu.metaform.api.spec.model.BadRequest
import fi.metatavu.metaform.server.controllers.CardAuthKeycloakController
import fi.metatavu.metaform.server.controllers.ReplyController
import fi.metatavu.metaform.server.keycloak.AuthorizationScope
import fi.metatavu.metaform.server.controllers.MetaformKeycloakController
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
    lateinit var metaformKeycloakController: MetaformKeycloakController

    @Inject
    lateinit var cardAuthKeycloakController: CardAuthKeycloakController

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
     * Creates user not own resource message
     *
     * @param target target
     * @return not owned message
     */
    protected fun createNotOwnedMessage(target: String): String {
        return "User do not own this $target"
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
     * Creates slug not found message with given parameters
     *
     * @param target target of the find method
     * @param slug slug
     * @return not found message
     */
    protected fun createSlugNotFoundMessage(target: String, slug: String): String {
        return "$target with slug $slug could not be found"
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
     * @param target target
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
    private fun streamResponse(type: String, inputStream: InputStream, contentLength: Int): Response {
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
        get() = !hasRealmRole(METAFORM_USER_ROLE, SYSTEM_ADMIN_ROLE)

    /**
     * Returns whether logged user is realm user (system / metaform admins are all users)
     *
     * @return whether logged user is realm user
     */
    protected val isRealmUser: Boolean
        get() = hasRealmRole(METAFORM_USER_ROLE)

    /**
     * Returns whether logged user is realm system admin
     *
     * @return whether logged user is system admin
     */
    protected val isRealmSystemAdmin: Boolean
        get() = hasRealmRole(SYSTEM_ADMIN_ROLE)

    /**
     * Returns whether the logged user is metaform admin or with higher permission
     *
     * @return whether logged user is Metaform admin
     */
    fun isMetaformAdmin(metaformId: UUID): Boolean {
        if (isRealmSystemAdmin) return true
        return metaformKeycloakController.isMetaformAdmin(metaformId, loggedUserId!!)
    }

    /**
     * Returns whether the logged user is any metaform admin or with higher permission
     *
     * @return whether logged user is any Metaform admin
     */
    val isMetaformAdminAny: Boolean
        get() {
            if (isRealmSystemAdmin) return true
            return metaformKeycloakController.isMetaformAdminAny(loggedUserId!!)
        }

    /**
     * Returns whether the logged user is metaform manager or with higher permission
     *
     * @return whether logged user is realm Metaform manager
     */
    fun isMetaformManager(metaformId: UUID): Boolean {
        if (isRealmSystemAdmin) return true
        if (metaformKeycloakController.isMetaformAdmin(metaformId, loggedUserId!!)) return true
        return metaformKeycloakController.isMetaformManager(metaformId, loggedUserId!!)
    }

    /**
     * Returns whether the logged user is any metaform manager or with higher permission
     *
     * @return whether logged user is any Metaform manager
     */
    val isMetaformManagerAny: Boolean
        get() {
            if (isRealmSystemAdmin) return true
            if (metaformKeycloakController.isMetaformAdminAny(loggedUserId!!)) return true
            return metaformKeycloakController.isMetaformManagerAny(loggedUserId!!)
        }

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
        if (isRealmSystemAdmin) {
            return true
        }
        if (reply?.resourceId == null) {
            return false
        }
        if (replyController.isValidOwnerKey(reply, ownerKey)) {
            return true
        }
        if (isAnonymous) {
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
        val permittedResourceIds = metaformKeycloakController.getPermittedResourceIds(tokenString, setOf(resourceId), authorizationScope)
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
        const val METAFORM_USER_ROLE = "metaform-user"
        const val METAFORM_MANAGER_ROLE_NAME = "metaform-manager"
        const val SYSTEM_ADMIN_ROLE = "system-admin"
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
        const val EXPORT_THEME = "export theme"
        const val EXPORT_THEME_FILE = "export theme file"
        const val ATTACHMENT = "attachment"
        const val REPLY_MODE = "reply mode"
        const val DRAFT = "draft"
        const val AUDIT_LOG_ENTRY = "audit log entry"
        const val METAFORM_MEMBER = "metaform member"
        const val METAFORM_MEMBER_GROUP = "metaform member group"
        const val EMAIL_NOTIFICATION = "email notification"
        const val METAFORM_VERSION = "metaform version"
        const val ANONYMOUS_USERS_METAFORM_MESSAGE = "Anonymous users are not allowed on this Metaform"
        const val USER = "user"

        private const val INTERNAL_SERVER_ERROR = "Internal Server Error"
    }
}