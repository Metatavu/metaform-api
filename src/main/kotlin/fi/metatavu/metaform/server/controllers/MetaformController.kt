package fi.metatavu.metaform.server.controllers

import com.github.slugify.Slugify
import fi.metatavu.metaform.api.spec.model.MetaformField
import fi.metatavu.metaform.server.exceptions.KeycloakClientNotFoundException
import fi.metatavu.metaform.server.exceptions.ResourceNotFoundException
import fi.metatavu.metaform.server.utils.MetaformUtils
import fi.metatavu.metaform.server.keycloak.AuthorizationScope
import fi.metatavu.metaform.server.keycloak.ResourceType
import fi.metatavu.metaform.server.persistence.dao.AuditLogEntryDAO
import fi.metatavu.metaform.server.persistence.dao.MetaformDAO
import fi.metatavu.metaform.server.persistence.dao.ReplyDAO
import fi.metatavu.metaform.server.persistence.model.AuditLogEntry
import fi.metatavu.metaform.server.persistence.model.ExportTheme
import fi.metatavu.metaform.server.persistence.model.Metaform
import fi.metatavu.metaform.server.persistence.model.Reply
import fi.metatavu.metaform.server.persistence.model.notifications.EmailNotification
import org.apache.commons.lang3.StringUtils
import org.keycloak.admin.client.Keycloak
import org.keycloak.admin.client.resource.UserResource
import org.keycloak.representations.idm.ClientRepresentation
import org.keycloak.representations.idm.UserRepresentation
import org.keycloak.representations.idm.authorization.DecisionStrategy
import java.util.*
import javax.enterprise.context.ApplicationScoped
import javax.inject.Inject

/**
 * Metaform controller
 *
 * @author Antti Leppä
 */
@ApplicationScoped
class MetaformController {

    @Inject
    lateinit var emailNotificationController: EmailNotificationController

    @Inject
    lateinit var replyController: ReplyController

    @Inject
    lateinit var metaformDAO: MetaformDAO

    @Inject
    lateinit var replyDAO: ReplyDAO

    @Inject
    lateinit var auditLogEntryDAO: AuditLogEntryDAO

    @Inject
    lateinit var auditLogEntryController: AuditLogEntryController

    @Inject
    lateinit var keycloakController: KeycloakController

    /**
     * Creates new Metaform
     *
     * @param exportTheme export theme
     * @param allowAnonymous allow anonymous
     * @param title title
     * @param data form JSON
     * @return Metaform
     */
    fun createMetaform(
            exportTheme: ExportTheme?,
            allowAnonymous: Boolean,
            title: String?,
            slug: String? = null,
            data: String
    ): Metaform {
        return metaformDAO.create(
                id = UUID.randomUUID(),
                slug = slug ?: createSlug(title),
                exportTheme = exportTheme,
                allowAnonymous = allowAnonymous,
                data = data
        )
    }

    /**
     * Finds Metaform by id
     *
     * @param id Metaform id
     * @return Metaform
     */
    fun findMetaformById(id: UUID): Metaform? {
        return metaformDAO.findById(id)
    }

    /**
     * Lists Metaforms
     *
     * @return list of Metaforms
     */
    fun listMetaforms(): List<Metaform> {
        return metaformDAO.listAll()
    }

    /**
     * Updates Metaform
     *
     * @param metaform Metaform
     * @param data form JSON
     * @param allowAnonymous allow anonymous
     * @param slug slug
     * @return Updated Metaform
     */
    fun updateMetaform(
            metaform: Metaform,
            exportTheme: ExportTheme?,
            data: String,
            allowAnonymous: Boolean?,
            slug: String
    ): Metaform {
        metaformDAO.updateData(metaform, data)
        metaformDAO.updateAllowAnonymous(metaform, allowAnonymous)
        metaformDAO.updateExportTheme(metaform, exportTheme)
        metaformDAO.updateSlug(metaform, slug)
        return metaform
    }

    /**
     * Delete Metaform
     *
     * @param metaform Metaform
     */
    fun deleteMetaform(metaform: Metaform) {
        val replies = replyDAO.listByMetaform(metaform)
        replies.forEach { reply: Reply -> replyController.deleteReply(reply) }

        val auditLogEntries = auditLogEntryDAO.listByMetaform(metaform)
        auditLogEntries.forEach { auditLogEntry: AuditLogEntry -> auditLogEntryController.deleteAuditLogEntry(auditLogEntry) }
        metaformDAO.delete(metaform)
    }

    /**
     * Generates unique slug within a realm for a Metaform
     *
     * @param title title
     * @return unique slug
     */
    private fun createSlug(title: String?): String {
        val slugify = Slugify()
        val prefix = if (StringUtils.isNotBlank(title)) slugify.slugify(title) else "form"
        var count = 0
        do {
            val slug = if (count > 0) String.format("%s-%d", prefix, count) else prefix

            metaformDAO.findBySlug(slug) ?: return slug
            count++
        } while (true)
    }

    /**
     * Validate a slug for Metaform
     *
     * @param slug slug
     * @return boolean result for validation
     */
    fun validateSlug(slug: String): Boolean {
        return slug.matches(Regex("^[a-z\\d]+(?:[-, _][a-z\\d]+)*$"))
    }

    /**
     * Unique check for metaform slug
     *
     * @param slug slug
     * @return boolean result for unique check
     */
    fun isSlugUnique(metaformId: UUID?, slug: String): Boolean {
        val foundMetaform = metaformDAO.findBySlug(slug)
        return foundMetaform == null || foundMetaform.id === metaformId
    }

    /**
     * Adds field permission context groups into appropriate lists
     *
     * @param formSlug form slug
     * @param permissionGroups target map
     * @param field field
     * @param fieldValue field value
     */
    fun addPermissionContextGroups(
            permissionGroups: EnumMap<AuthorizationScope, MutableList<String>>,
            formSlug: String?,
            field: MetaformField,
            fieldValue: Any?
    ) {
        val permissionContexts = field.permissionContexts
        if (permissionContexts != null && fieldValue is String) {
            val permissionGroupName = getReplySecurityContextGroup(formSlug, field.name, fieldValue as String?)
            if (permissionContexts.editGroup == true) {
                permissionGroups[AuthorizationScope.REPLY_EDIT]!!.add(permissionGroupName)
            }
            if (permissionContexts.viewGroup == true) {
                permissionGroups[AuthorizationScope.REPLY_VIEW]!!.add(permissionGroupName)
            }
            if (permissionContexts.notifyGroup == true) {
                permissionGroups[AuthorizationScope.REPLY_NOTIFY]!!.add(permissionGroupName)
            }
        }
    }

    /**
     * Creates reply security context group name
     *
     * @param formSlug form slug
     * @param fieldName field name
     * @param fieldValue field value
     * @return reply security context group name
     */
    fun getReplySecurityContextGroup(formSlug: String?, fieldName: String?, fieldValue: String?): String {
        return String.format(REPLY_GROUP_NAME_TEMPLATE, formSlug, fieldName, fieldValue)
    }

    /**
     * Handles reply post persist tasks. Tasks include adding to user groups permissions and notifying users about the reply
     *
     * @param replyCreated whether the reply was just created
     * @param metaform metaform
     * @param reply reply
     * @param replyEntity reply entity
     * @param newPermissionGroups added permission groups
     */
    @Throws(KeycloakClientNotFoundException::class)
    fun handleReplyPostPersist(
            replyCreated: Boolean,
            metaform: Metaform,
            reply: Reply,
            replyEntity: fi.metatavu.metaform.api.spec.model.Reply,
            newPermissionGroups: EnumMap<AuthorizationScope, MutableList<String>>
    ) {
        val adminClient = keycloakController.getAdminClient()
        val keycloakClient = try {
            keycloakController.getKeycloakClient(adminClient)
        } catch (e: KeycloakClientNotFoundException) {
            throw e
        }
        var resourceId = reply.resourceId
        val resourceName = replyController.getReplyResourceName(reply)
        val notifiedUserIds =
                if (replyCreated) emptySet()
                else keycloakController.getResourcePermittedUsers(
                        adminClient,
                        keycloakClient,
                        resourceId ?: throw ResourceNotFoundException("Resource not found"),
                        resourceName,
                        listOf(AuthorizationScope.REPLY_NOTIFY)
                )
        resourceId = updateReplyPermissions(adminClient, keycloakClient, reply, newPermissionGroups)
        val notifyUserIds = keycloakController.getResourcePermittedUsers(
                adminClient,
                keycloakClient,
                resourceId ?: throw ResourceNotFoundException("Resource not found"),
                resourceName,
                listOf(AuthorizationScope.REPLY_NOTIFY)
        ).filter { notifyUserId: UUID -> !notifiedUserIds.contains(notifyUserId) }.toSet()

        emailNotificationController.listEmailNotificationByMetaform(metaform)
            .forEach{ emailNotification: EmailNotification ->
                sendReplyEmailNotification(
                        adminClient,
                        replyCreated,
                        emailNotification,
                        replyEntity,
                        notifyUserIds
                )
            }
    }

    /**
     * Sends reply email notifications
     * @param keycloak Keycloak admin client
     * @param replyCreated whether the reply was just created
     * @param emailNotification email notification
     * @param replyEntity reply REST entity
     * @param notifyUserIds notify user ids
     */
    private fun sendReplyEmailNotification(keycloak: Keycloak, replyCreated: Boolean, emailNotification: EmailNotification, replyEntity: fi.metatavu.metaform.api.spec.model.Reply, notifyUserIds: Set<UUID>) {
        if (!emailNotificationController.evaluateEmailNotificationNotifyIf(emailNotification, replyEntity)) {
            return
        }
        val directEmails = if (replyCreated) emailNotificationController.getEmailNotificationEmails(emailNotification) else emptyList()
        val usersResource = keycloak.realm(keycloakController.configuration.realm).users()
        val groupEmails = notifyUserIds
                .asSequence()
                .map { obj: UUID -> obj.toString() }
                .map { id: String? -> usersResource[id] }
                .map { obj: UserResource -> obj.toRepresentation() }
                .filter { obj: UserRepresentation? -> Objects.nonNull(obj) }
                .map { obj: UserRepresentation -> obj.email }
        val emails: MutableSet<String> = HashSet(directEmails)
        emails.addAll(groupEmails)
        emailNotificationController.sendEmailNotification(emailNotification, replyEntity, emails.filter { cs: String? -> StringUtils.isNotEmpty(cs) }.toSet())
    }

    /**
     * Returns permission context fields from Metaform REST entity
     *
     * @param metaformEntity metaform REST entity
     * @return permission context fields
     */
    fun getPermissionContextFields(metaformEntity: fi.metatavu.metaform.api.spec.model.Metaform): List<MetaformField> {
        return MetaformUtils.getMetaformFields(metaformEntity)
                .filter { it.permissionContexts != null }
                .filter { it.permissionContexts?.editGroup == true || it.permissionContexts?.viewGroup == true || it.permissionContexts?.notifyGroup == true }
    }

    /**
     * Returns groups permission name for a reply
     *
     * @param reply reply
     * @param name permission name
     * @return resource name
     */
    private fun getReplyPermissionName(reply: Reply?, name: String): String? {
        return if (reply == null) {
            null
        } else String.format(REPLY_PERMISSION_NAME_TEMPLATE, reply.id, name)
    }

    /**
     * Updates reply permissions
     *
     * @param keycloak keycloak
     * @param keycloakClient keycloakClient
     * @param permissionGroups permissionGroups
     * @return resource id
     */
    private fun updateReplyPermissions(keycloak: Keycloak, keycloakClient: ClientRepresentation, reply: Reply, permissionGroups: EnumMap<AuthorizationScope, MutableList<String>>): UUID? {
        val metaform = reply.metaform
        var resourceId = reply.resourceId
        if (resourceId == null) {
            resourceId = keycloakController.createProtectedResource(
                    keycloak,
                    keycloakClient,
                    reply.userId,
                    replyController.getReplyResourceName(reply),
                    replyController.getReplyResourceUri(reply),
                    ResourceType.REPLY.urn,
                    REPLY_SCOPES
            )
            replyController.updateResourceId(reply, resourceId)
        }
        val commonPolicyIds = keycloakController.getPolicyIdsByNames(keycloak, keycloakClient, mutableListOf(METAFORM_ADMIN_POLICY_NAME, OWNER_POLICY_NAME))
        for (scope in AuthorizationScope.values()) {
            val groupNames = permissionGroups[scope] ?: mutableListOf()
            val groupPolicyIds = keycloakController.getPolicyIdsByNames(keycloak, keycloakClient, groupNames)
            val policyIds = HashSet(groupPolicyIds)
            policyIds.addAll(commonPolicyIds)
            keycloakController.upsertScopePermission(
                    keycloak,
                    keycloakClient,
                    resourceId!!,
                    setOf(scope),
                    getReplyPermissionName(reply, scope.scopeName.lowercase(Locale.getDefault())),
                    DecisionStrategy.AFFIRMATIVE,
                    policyIds
            )
        }
        val userPolicyIds = keycloakController.getPolicyIdsByNames(keycloak, keycloakClient, mutableListOf(USER_POLICY_NAME))
        if (metaform.allowAnonymous == true) {
            keycloakController.upsertScopePermission(
                    keycloak,
                    keycloakClient,
                    resourceId!!,
                    setOf(AuthorizationScope.REPLY_VIEW),
                    "require-user",
                    DecisionStrategy.AFFIRMATIVE,
                    userPolicyIds
            )
        }
        return resourceId
    }

    companion object {
        private const val USER_POLICY_NAME = "user"
        private const val OWNER_POLICY_NAME = "owner"
        private const val METAFORM_ADMIN_POLICY_NAME = "metaform-admin"
        private const val REPLY_PERMISSION_NAME_TEMPLATE = "permission-%s-%s"
        private const val REPLY_GROUP_NAME_TEMPLATE = "%s:%s:%s"
        private val REPLY_SCOPES = listOf(AuthorizationScope.REPLY_VIEW, AuthorizationScope.REPLY_EDIT, AuthorizationScope.REPLY_NOTIFY)
    }
}