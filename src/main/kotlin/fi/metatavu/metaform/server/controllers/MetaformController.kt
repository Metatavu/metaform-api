package fi.metatavu.metaform.server.controllers

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
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
import fi.metatavu.metaform.server.persistence.model.*
import fi.metatavu.metaform.server.persistence.model.notifications.EmailNotification
import io.quarkus.scheduler.Scheduled
import org.apache.commons.lang3.StringUtils
import org.keycloak.admin.client.Keycloak
import org.keycloak.admin.client.resource.UserResource
import org.keycloak.representations.idm.UserRepresentation
import java.util.*
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import jakarta.transaction.Transactional
import org.slf4j.Logger

/**
 * Metaform controller
 *
 * @author Antti Leppä
 */
@Transactional
@ApplicationScoped
class MetaformController {

    @Inject
    lateinit var emailNotificationController: EmailNotificationController

    @Inject
    lateinit var replyController: ReplyController

    @Inject
    lateinit var metaformDAO: MetaformDAO

    @Inject
    lateinit var draftController: DraftController

    @Inject
    lateinit var auditLogEntryDAO: AuditLogEntryDAO

    @Inject
    lateinit var auditLogEntryController: AuditLogEntryController

    @Inject
    lateinit var metaformKeycloakController: MetaformKeycloakController

    @Inject
    lateinit var permissionController: PermissionController

    @Inject
    lateinit var logger: Logger

    @Inject
    lateinit var metaformVersionController: MetaformVersionController

    /**
     * Creates new Metaform
     *
     * @param exportTheme export theme
     * @param allowAnonymous allow anonymous
     * @param title title
     * @param data form JSON
     * @param creatorId creator id
     * @return Metaform
     */
    fun createMetaform(
            exportTheme: ExportTheme?,
            allowAnonymous: Boolean,
            visibility: MetaformVisibility,
            title: String?,
            slug: String? = null,
            data: String,
            creatorId: UUID
    ): Metaform {
        return metaformDAO.create(
            id = UUID.randomUUID(),
            slug = slug ?: createSlug(title),
            exportTheme = exportTheme,
            visibility = visibility,
            allowAnonymous = allowAnonymous,
            data = data,
            creatorId = creatorId
        ).let {
            metaformKeycloakController.createMetaformManagementGroup(it.id!!)
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
        return metaformDAO.listByVisibility(
            visibility = visibility,
            deleted = false
        )
    }

    /**
     * Lists Metaforms that are marked as deleted so that a scheduled job can delete them
     */
    fun listDeletedMetaforms(): List<Metaform> {
        return metaformDAO.listByVisibility(
            visibility = null,
            deleted = true
        )
    }


    /**
     * Updates Metaform
     *
     * @param metaform Metaform
     * @param data form JSON
     * @param allowAnonymous allow anonymous
     * @param slug slug
     * @param lastModifierId last modifier id
     * @return Updated Metaform
     */
    fun updateMetaform(
        metaform: Metaform,
        exportTheme: ExportTheme?,
        visibility: MetaformVisibility,
        data: String,
        allowAnonymous: Boolean?,
        slug: String?,
        lastModifierId: UUID
    ): Metaform {
        metaformDAO.updateData(metaform, data, lastModifierId)
        metaformDAO.updateAllowAnonymous(metaform, allowAnonymous, lastModifierId)
        metaformDAO.updateExportTheme(metaform, exportTheme, lastModifierId)
        metaformDAO.updateSlug(metaform, slug ?: metaform.slug, lastModifierId)
        metaformDAO.updateVisibility(metaform, visibility, lastModifierId)
        return metaform
    }

    /**
     * Delete Metaform
     *
     * @param metaform Metaform
     */
    fun deleteMetaform(metaform: Metaform) {
        val replies = replyController.listReplies(metaform = metaform, includeRevisions = true)
        replies.forEach { reply: Reply -> replyController.deleteReply(reply) }

        val drafts = draftController.listByMetaform(metaform, null, null)
        drafts.forEach { draft: Draft -> draftController.deleteDraft(draft) }

        val metaformMembers = metaformKeycloakController.listMetaformMemberAdmin(
            metaformId = metaform.id!!,
            first = null ,
            max = null
        ) + metaformKeycloakController.listMetaformMemberManager(
            metaformId = metaform.id!!,
            first = null,
            max = null
        )

        metaformMembers.forEach { metaformKeycloakController.deleteMetaformMember(UUID.fromString(it.id), metaform.id!!) }

        val emailNotifications = emailNotificationController.listEmailNotificationByMetaform(
            metaform = metaform,
            firstResult = null,
            maxResults = null
        )

        emailNotifications.forEach { emailNotification: EmailNotification -> emailNotificationController.deleteEmailNotification(emailNotification) }

        val auditLogEntries = auditLogEntryDAO.listByMetaform(metaform)
        auditLogEntries.forEach { auditLogEntry: AuditLogEntry -> auditLogEntryController.deleteAuditLogEntry(auditLogEntry) }

        metaformDAO.delete(metaform)
        metaformKeycloakController.deleteMetaformManagementGroup(metaform.id!!)
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
        objectMapper.registerModule(JavaTimeModule())

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
        val adminClient = metaformKeycloakController.adminClient
        val keycloakClient = try {
            metaformKeycloakController.getKeycloakClient(adminClient)
        } catch (e: AuthzException) {
            throw e
        }

        val resourceName = replyController.getReplyResourceName(reply)

        val notifiedUserIds = if (replyCreated) {
            emptySet()
        } else {
            metaformKeycloakController.getResourcePermittedUsers(
                keycloak = adminClient,
                client = keycloakClient,
                resourceId = reply.resourceId ?: throw ResourceNotFoundException("Resource not found"),
                resourceName =  resourceName,
                scopes = listOf(AuthorizationScope.REPLY_NOTIFY)
            )}

        val resourceId =  permissionController.updateReplyPermissions(
            reply = reply,
            groupMemberPermissions = groupMemberPermissions,
            allowAnonymous = metaform.allowAnonymous ?: false
        )

        val notifyUserIds = metaformKeycloakController
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

        emailNotificationController.listEmailNotificationByMetaform(
            metaform = metaform,
            firstResult = null,
            maxResults = null)
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
     * Deletes replies from forms that are marked as deleted
     */
    @Scheduled(every="\${metaforms.deletion.interval}", delayed = "\${metaforms.deletion.delay}")
    fun scheduledMetaformDelete() {
        val metaform = listDeletedMetaforms().firstOrNull() ?: return

        val replies = replyController.listReplies(
            metaform = metaform,
            includeRevisions = true,
            firstResult = 0,
            maxResults = 10
        )

        if (replies.isNotEmpty()) {
            logger.info("Deleting ${replies.size} replies from form ${metaform.id}")
            replies.forEach { replyController.deleteReply(it) }
            return
        }

        val drafts = draftController.listByMetaform(
            metaform = metaform,
            firstResult = 0,
            maxResults = 10
        )

        if (drafts.isNotEmpty()) {
            logger.info("Deleting ${drafts.size} drafts from form ${metaform.id}")
            drafts.forEach { draftController.deleteDraft(it) }
            return
        }

        val metaformAdminMembers = metaformKeycloakController.listMetaformMemberAdmin(
            metaformId = metaform.id!!,
            first = 0,
            max = 10
        )

        if (metaformAdminMembers.isNotEmpty()) {
            logger.info("Deleting ${metaformAdminMembers.size} admin members from form ${metaform.id}")
            metaformAdminMembers.forEach { metaformKeycloakController.deleteMetaformMember(UUID.fromString(it.id!!), metaform.id!!) }
            return
        }

        val metaformManagerMembers = metaformKeycloakController.listMetaformMemberManager(
            metaformId = metaform.id!!,
            first = 0,
            max= 10
        )

        if (metaformManagerMembers.isNotEmpty()) {
            logger.info("Deleting ${metaformManagerMembers.size} admin members from form ${metaform.id}")
            metaformManagerMembers.forEach { metaformKeycloakController.deleteMetaformMember(UUID.fromString(it.id!!), metaform.id!!) }
            return
        }

        val versions = metaformVersionController.listMetaformVersionsByMetaform(
            metaform = metaform,
            firstResult = 0,
            maxResults = 10
        )

        if (versions.isNotEmpty()) {
            logger.info("Deleting ${versions.size} versions from form ${metaform.id}")
            versions.forEach { metaformVersionController.deleteMetaformVersion(it) }
            return
        }

        val emailNotifications = emailNotificationController.listEmailNotificationByMetaform(
            metaform = metaform,
            firstResult = 0,
            maxResults = 10
        )

        if (emailNotifications.isNotEmpty()) {
            logger.info("Deleting ${emailNotifications.size} email notifications from form ${metaform.id}")
            emailNotifications.forEach { emailNotificationController.deleteEmailNotification(it) }
            return
        }

        val auditLogEntriers = auditLogEntryController.listAuditLogEntries(
            metaform = metaform,
            replyId = null,
            userId = null,
            createdBefore = null,
            createdAfter = null,
            firstResult = 0,
            maxResults = 10
        )

        if (auditLogEntriers.isNotEmpty()) {
            logger.info("Deleting ${auditLogEntriers.size} audit log entries from form ${metaform.id}")
            auditLogEntriers.forEach { auditLogEntryController.deleteAuditLogEntry(it) }
            return
        }

        metaformDAO.delete(metaform)
        metaformKeycloakController.deleteMetaformManagementGroup(metaform.id!!)
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
        val usersResource = keycloak.realm(metaformKeycloakController.configuration.realm).users()
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
                    if (metaformKeycloakController.findGroup(id = groupId) == null) {
                        return false
                    }
                } catch (e: Exception) {
                    return false
                }
            }
        }

        return true
    }

    /**
     * Marks a form as deleted so that a scheduled job deletes it later
     *
     * @param metaform metaform to be deleted
     */
    fun updateMetaformDeleted(metaform: Metaform) {
        metaformDAO.updateMetaformDeleted(metaform)
    }
}