package fi.metatavu.metaform.server.permissions

import fi.metatavu.metaform.api.spec.model.Metaform
import fi.metatavu.metaform.api.spec.model.MetaformField
import fi.metatavu.metaform.server.controllers.*
import fi.metatavu.metaform.server.keycloak.AuthorizationScope
import fi.metatavu.metaform.server.keycloak.ResourceType
import fi.metatavu.metaform.server.persistence.model.Reply
import org.keycloak.admin.client.Keycloak
import org.keycloak.representations.idm.GroupRepresentation
import org.keycloak.representations.idm.authorization.DecisionStrategy
import java.util.*
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject

/**
 * Permission controller
 *
 * @author Antti Leppä
 */
@ApplicationScoped
class PermissionController {

    @Inject
    lateinit var metaformKeycloakController: MetaformKeycloakController

    @Inject
    lateinit var authzController: AuthzController

    @Inject
    lateinit var replyController: ReplyController

    /**
     * Returns form's default group member permissions as list
     *
     * @param metaform metaform
     * @return default group member permissions as list
     */
    fun getDefaultGroupMemberPermissions(
        metaform: Metaform
    ): Set<GroupMemberPermission> {
        val defaultPermissionGroups = metaform.defaultPermissionGroups
        val result = mutableSetOf<GroupMemberPermission>()

        defaultPermissionGroups ?: return result

        defaultPermissionGroups.editGroupIds?.let { groupIds ->
            result.addAll(groupIds.map {
                GroupMemberPermission(scope = AuthorizationScope.REPLY_EDIT, memberGroupId = it)
            })
        }

        defaultPermissionGroups.viewGroupIds?.let { groupIds ->
            result.addAll(groupIds.map {
                GroupMemberPermission(scope = AuthorizationScope.REPLY_VIEW, memberGroupId = it)
            })
        }

        defaultPermissionGroups.notifyGroupIds?.let { groupIds ->
            result.addAll(groupIds.map {
                GroupMemberPermission(scope = AuthorizationScope.REPLY_NOTIFY, memberGroupId = it)
            })
        }

        return result
    }

    /**
     * Returns field permission context groups from field and given field reply value
     *
     * @param field field
     * @param fieldValue field value
     * @return Set of group member permissions
     */
    fun getFieldGroupMemberPermissions(
        field: MetaformField,
        fieldValue: Any?
    ): Set<GroupMemberPermission> {
        val result = mutableSetOf<GroupMemberPermission>()
        val options = field.options ?: return result

        if (fieldValue is String) {
            val option = options.find { it.name == fieldValue } ?: return result
            val permissionGroups = option.permissionGroups ?: return result

            permissionGroups.editGroupIds?.let { groupIds ->
                result.addAll(groupIds.map {
                    GroupMemberPermission(scope = AuthorizationScope.REPLY_EDIT, memberGroupId = it)
                })
            }

            permissionGroups.viewGroupIds?.let { groupIds ->
                result.addAll(groupIds.map {
                    GroupMemberPermission(scope = AuthorizationScope.REPLY_VIEW, memberGroupId = it)
                })
            }

            permissionGroups.notifyGroupIds?.let { groupIds ->
                result.addAll(groupIds.map {
                    GroupMemberPermission(scope = AuthorizationScope.REPLY_NOTIFY, memberGroupId = it)
                })
            }
        }

        return result
    }

    /**
     * Create member group policy into Keycloak
     *
     * @param group group
     */
    fun createMemberGroupPolicy(group: GroupRepresentation) {
        val policyName = getGroupPolicyName(
            groupId = UUID.fromString(group.id)
        )

        authzController.createGroupPolicy(
            keycloak = metaformKeycloakController.adminClient,
            policyName = policyName,
            groupId = UUID.fromString(group.id)
        )
    }

    /**
     * Updates reply permissions
     *
     * @param reply reply
     * @param groupMemberPermissions group member permissions to be added
     * @param allowAnonymous allow anonymous
     */
    fun updateReplyPermissions(
        reply: Reply,
        groupMemberPermissions: Set<GroupMemberPermission>,
        allowAnonymous: Boolean
    ): UUID {
        val keycloak = metaformKeycloakController.adminClient
        val resourceId = reply.resourceId ?: createReplyProtectedResource(
            keycloak = keycloak,
            reply = reply
        )

        for (scope in AuthorizationScope.values()) {
            val groupIds = groupMemberPermissions.filter { it.scope == scope }.map { it.memberGroupId }
            val policyNames = groupIds.map(this::getGroupPolicyName)

            val policyIds = authzController.getPolicyIdsByNames(
                keycloak = keycloak,
                policyNames = policyNames
            ).toMutableSet()

            if (!allowAnonymous && scope != AuthorizationScope.REPLY_NOTIFY) {
                policyIds.addAll(authzController.getPolicyIdsByNames(
                    keycloak, listOf(
                        OWNER_POLICY_NAME
                    )
                ))
            }

            authzController.upsertScopePermission(
                keycloak = keycloak,
                resourceId = resourceId,
                scopes = setOf(scope),
                name = getReplyPermissionName(reply, scope.scopeName.lowercase(Locale.getDefault())),
                decisionStrategy = DecisionStrategy.AFFIRMATIVE,
                policyIds = policyIds
            )
        }

        return resourceId
    }

    /**
     * Creates protected resource for a reply
     *
     * @param keycloak keycloak admin client
     * @param reply reply
     * @return created resource id
     */
    private fun createReplyProtectedResource(keycloak: Keycloak, reply: Reply): UUID {
        val resourceId = authzController.createProtectedResource(
            name = replyController.getReplyResourceName(reply),
            uri = replyController.getReplyResourceUri(reply),
            type = ResourceType.REPLY.urn,
            keycloak = keycloak,
            ownerId = reply.userId,
            scopes = REPLY_SCOPES
        )

        replyController.updateResourceId(reply, resourceId)

        return resourceId
    }

    /**
     * Returns groups permission name for a reply
     *
     * @param reply reply
     * @param name permission name
     * @return resource name
     */
    private fun getReplyPermissionName(reply: Reply, name: String): String {
        return String.format(REPLY_PERMISSION_NAME_TEMPLATE, reply.id, name)
    }

    /**
     * Returns group policy name
     *
     * @param groupId group id
     * @return group policy name
     */
    private fun getGroupPolicyName(groupId: UUID): String {
        return String.format(GROUP_POLICY_NAME, groupId)
    }

    companion object {
        private const val REPLY_PERMISSION_NAME_TEMPLATE = "permission-%s-%s"
        private const val OWNER_POLICY_NAME = "owner"
        private const val GROUP_POLICY_NAME = "group-%s"
        private val REPLY_SCOPES = listOf(AuthorizationScope.REPLY_VIEW, AuthorizationScope.REPLY_EDIT, AuthorizationScope.REPLY_NOTIFY)
    }

}