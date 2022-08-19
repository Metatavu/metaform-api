package fi.metatavu.metaform.server.controllers

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import com.github.slugify.Slugify
import fi.metatavu.metaform.api.spec.model.MetaformField
import fi.metatavu.metaform.api.spec.model.MetaformVisibility
import fi.metatavu.metaform.api.spec.model.PermissionGroups
import fi.metatavu.metaform.server.exceptions.AuthzException
import fi.metatavu.metaform.server.exceptions.MalformedMetaformJsonException
import fi.metatavu.metaform.server.exceptions.ResourceNotFoundException
import fi.metatavu.metaform.server.utils.MetaformUtils
import fi.metatavu.metaform.server.keycloak.AuthorizationScope
import fi.metatavu.metaform.server.metaform.SlugValidation
import fi.metatavu.metaform.server.permissions.GroupMemberPermission
import fi.metatavu.metaform.server.permissions.PermissionController
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
import org.keycloak.representations.idm.UserRepresentation
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

    @Inject
    lateinit var permissionController: PermissionController

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
            visibility: MetaformVisibility,
            title: String?,
            slug: String? = null,
            data: String
    ): Metaform {
        return metaformDAO.create(
            id = UUID.randomUUID(),
            slug = slug ?: createSlug(title),
            exportTheme = exportTheme,
            visibility = visibility,
            allowAnonymous = allowAnonymous,
            data = data
        ).let {
            keycloakController.createMetaformManagementGroup(it.id!!)
            it
        }
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
     * Finds Metaform by slug
     *
     * @param slug Metaform id
     * @return Metaform
     */
    fun findMetaformBySlug(slug: String): Metaform? {
        return metaformDAO.findBySlug(slug)
    }

    /**
     * Lists Metaforms
     *
     * @return list of Metaforms
     */
    fun listMetaforms(visibility: MetaformVisibility? = null): List<Metaform> {
        visibility ?: return metaformDAO.listAll()
        return metaformDAO.listByVisibility(visibility)
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
            visibility: MetaformVisibility,
            data: String,
            allowAnonymous: Boolean?,
            slug: String?
    ): Metaform {
        metaformDAO.updateData(metaform, data)
        metaformDAO.updateAllowAnonymous(metaform, allowAnonymous)
        metaformDAO.updateExportTheme(metaform, exportTheme)
        metaformDAO.updateSlug(metaform, slug ?: metaform.slug)
        metaformDAO.updateVisibility(metaform, visibility)
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

        val metaformMembers = keycloakController.listMetaformMemberAdmin(metaform.id!!) +
                keycloakController.listMetaformMemberManager(metaform.id!!)
        metaformMembers.forEach { keycloakController.deleteMetaformMember(UUID.fromString(it.id)) }

        val auditLogEntries = auditLogEntryDAO.listByMetaform(metaform)
        auditLogEntries.forEach { auditLogEntry: AuditLogEntry -> auditLogEntryController.deleteAuditLogEntry(auditLogEntry) }
        metaformDAO.delete(metaform)
        keycloakController.deleteMetaformManagementGroup(metaform.id!!)
    }

    /**
     * Serializes Metaform into JSON
     *
     * @param metaform Metaform
     * @return serialized Metaform
     */
    @Throws(MalformedMetaformJsonException::class)
    fun serializeMetaform(metaform: fi.metatavu.metaform.api.spec.model.Metaform): String {
        val objectMapper = ObjectMapper()
        try {
            return objectMapper.writeValueAsString(metaform)
        } catch (e: JsonProcessingException) {
            throw MalformedMetaformJsonException("Failed to serialize draft data", e)
        }
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
     * @param metaformId metaform id
     * @param slug slug
     * @return validation result
     */
    fun validateSlug(metaformId: UUID?, slug: String): SlugValidation {
        val foundMetaform = metaformDAO.findBySlug(slug)

        if (!slug.matches(Regex("^[a-z\\d]+(?:[-, _][a-z\\d]+)*$"))) {
            return SlugValidation.INVALID
        } else if (foundMetaform != null && foundMetaform.id != metaformId) {
            return SlugValidation.DUPLICATED
        }
        return SlugValidation.VALID
    }

    /**
     * Handles reply post persist tasks. Tasks include adding to user groups permissions and notifying users about the reply
     *
     * @param replyCreated whether the reply was just created
     * @param metaform metaform
     * @param reply reply
     * @param replyEntity reply entity
     * param loggedUserId logged user id
     */
    @Throws(AuthzException::class)
    fun handleReplyPostPersist(
            replyCreated: Boolean,
            metaform: Metaform,
            reply: Reply,
            replyEntity: fi.metatavu.metaform.api.spec.model.Reply,
            groupMemberPermissions: Set<GroupMemberPermission>,
            loggedUserId: UUID
    ) {
        val adminClient = keycloakController.adminClient
        val keycloakClient = try {
            keycloakController.getKeycloakClient(adminClient)
        } catch (e: AuthzException) {
            throw e
        }

        val resourceName = replyController.getReplyResourceName(reply)
        val notifiedUserIds =
                if (replyCreated) emptySet()
                else keycloakController.getResourcePermittedUsers(
                        adminClient,
                        keycloakClient,
                    reply.resourceId ?: throw ResourceNotFoundException("Resource not found"),
                        resourceName,
                        listOf(AuthorizationScope.REPLY_NOTIFY)
                )

        val resourceId =  permissionController.updateReplyPermissions(
            reply = reply,
            groupMemberPermissions = groupMemberPermissions
        )

        val notifyUserIds = keycloakController
            .getResourcePermittedUsers(
                adminClient,
                keycloakClient,
                resourceId,
                resourceName,
                listOf(AuthorizationScope.REPLY_NOTIFY)
            )
            .filter { notifyUserId: UUID -> !notifiedUserIds.contains(notifyUserId) }
            .minus(loggedUserId)
            .toSet()

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
     * Validates incoming Metaform
     *
     * @param payload metaform data
     * @return validation error or null if metaform is valid
     */
    fun validateMetaform(payload: fi.metatavu.metaform.api.spec.model.Metaform): Boolean {
        val keys = MetaformUtils.getMetaformFields(payload).map(MetaformField::name)

        val duplicates = keys
            .filter { key: String? -> Collections.frequency(keys, key) > 1 }
            .distinct()

        return duplicates.isEmpty()
    }

    /**
     * Validates permission groups
     *
     * @param permissionGroups permission groups
     * @return whether given list of permission groups is valid
     */
    fun validatePermissionGroups(permissionGroups: List<PermissionGroups>): Boolean {
        for (permissionGroup in permissionGroups) {
            val editGroupIds = permissionGroup.editGroupIds ?: emptyList()
            val viewGroupIds = permissionGroup.viewGroupIds ?: emptyList()
            val notifyGroupIds = permissionGroup.notifyGroupIds ?: emptyList()
            val groupIds = editGroupIds.plus(viewGroupIds).plus(notifyGroupIds)

            for (groupId in groupIds) {
                try {
                    if (keycloakController.findGroup(id = groupId) == null) {
                        return false
                    }
                } catch (e: Exception) {
                    return false
                }
            }
        }

        return true
    }
}