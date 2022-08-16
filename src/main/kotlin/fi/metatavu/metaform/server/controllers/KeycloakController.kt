package fi.metatavu.metaform.server.controllers

import com.fasterxml.jackson.databind.ObjectMapper
import fi.metatavu.metaform.api.spec.model.MetaformMemberRole
import fi.metatavu.metaform.keycloak.client.apis.GroupApi
import fi.metatavu.metaform.keycloak.client.apis.UserApi
import fi.metatavu.metaform.keycloak.client.apis.UsersApi
import fi.metatavu.metaform.keycloak.client.infrastructure.ApiClient
import fi.metatavu.metaform.server.exceptions.AuthzException
import fi.metatavu.metaform.server.exceptions.KeycloakDuplicatedUserException
import fi.metatavu.metaform.server.exceptions.KeycloakException
import fi.metatavu.metaform.server.exceptions.MetaformMemberRoleNotFoundException
import fi.metatavu.metaform.server.keycloak.AuthorizationScope
import fi.metatavu.metaform.server.keycloak.KeycloakClientUtils
import fi.metatavu.metaform.server.keycloak.KeycloakControllerToken
import fi.metatavu.metaform.server.keycloak.NotNullResteasyJackson2Provider
import fi.metatavu.metaform.server.rest.AbstractApi
import org.eclipse.microprofile.config.inject.ConfigProperty
import org.jboss.resteasy.client.jaxrs.ResteasyClient
import org.keycloak.OAuth2Constants
import org.keycloak.admin.client.ClientBuilderWrapper
import org.keycloak.admin.client.Keycloak
import org.keycloak.admin.client.KeycloakBuilder
import org.keycloak.admin.client.resource.RealmResource
import org.keycloak.authorization.client.AuthzClient
import org.keycloak.authorization.client.Configuration
import org.keycloak.representations.idm.ClientRepresentation
import org.keycloak.representations.idm.GroupRepresentation
import org.keycloak.representations.idm.UserRepresentation
import org.keycloak.representations.idm.authorization.*
import org.slf4j.Logger
import java.util.*
import javax.enterprise.context.ApplicationScoped
import javax.inject.Inject
import javax.ws.rs.InternalServerErrorException

/**
 * Controller for Keycloak related operations.
 *
 * Currently, this class uses two different Keycloak admin clients. The RESTEasy based client has been deprecated in
 * favor of OpenAPI generated, so when developing the class further all new operations should use the OpenAPI-based
 * client.
 */
@ApplicationScoped
class KeycloakController {

    @Inject
    @ConfigProperty(name = "metaforms.keycloak.admin.realm")
    lateinit var realm: String

    @Inject
    @ConfigProperty(name = "metaforms.keycloak.admin.admin_client_id")
    lateinit var clientId: String

    @Inject
    @ConfigProperty(name = "metaforms.keycloak.admin.secret")
    lateinit var clientSecret: String

    @Inject
    @ConfigProperty(name = "metaforms.keycloak.admin.user")
    lateinit var apiAdminUser: String

    @Inject
    @ConfigProperty(name = "metaforms.keycloak.admin.password")
    lateinit var apiAdminPassword: String

    @Inject
    @ConfigProperty(name = "metaforms.keycloak.admin.host")
    lateinit var authServerUrl: String

    @Inject
    lateinit var logger: Logger

    @Inject
    lateinit var objectMapper: ObjectMapper

    @Inject
    lateinit var keycloakControllerToken: KeycloakControllerToken

    @Inject
    lateinit var keycloakClientUtils: KeycloakClientUtils

    private val apiBasePath: String
        get() {
            return "${authServerUrl}admin/realms"
        }

    private val usersApi: UsersApi
        get() {
            ApiClient.accessToken = keycloakControllerToken.getAccessToken()?.accessToken
            return UsersApi(basePath = apiBasePath)
        }

    private val userApi: UserApi
        get() {
            ApiClient.accessToken = keycloakControllerToken.getAccessToken()?.accessToken
            return UserApi(basePath = apiBasePath)
        }

    private val groupApi: GroupApi
        get() {
            ApiClient.accessToken = keycloakControllerToken.getAccessToken()?.accessToken
            return GroupApi(basePath = apiBasePath)
        }

    /**
     * Resolves Keycloak client configuration for a realm
     *
     * @return configuration or null if configuration could not be created
     */
    val configuration: Configuration
        get() {
            val clientCredentials: MutableMap<String, Any> = HashMap()
            clientCredentials["secret"] = clientSecret
            clientCredentials["provider"] = "secret"
            clientCredentials["realm-admin-user"] = apiAdminUser
            clientCredentials["realm-admin-pass"] = apiAdminPassword
            val result = Configuration()
            result.authServerUrl = authServerUrl
            result.realm = realm
            result.resource = clientId
            result.credentials = clientCredentials
            return result
        }

    /**
     * Filters out resource ids without permission
     *
     * @param resourceIds resource ids
     * @param authorizationScope scope
     * @return filtered list
     */
    fun getPermittedResourceIds(tokenString: String, resourceIds: Set<UUID>, authorizationScope: AuthorizationScope): Set<UUID> {
        try {
            val authzClient = getAuthzClient()
            val request = AuthorizationRequest()
            resourceIds.forEach{ resourceId: UUID -> request.addPermission(resourceId.toString(), authorizationScope.scopeName) }
            val response = authzClient.authorization(tokenString).authorize(request)
            val irt = authzClient.protection().introspectRequestingPartyToken(response?.token)
            val permissions = irt?.permissions
            return permissions
                    ?.map { obj: Permission -> obj.resourceId }
                    ?.map { name: String? -> UUID.fromString(name) }
                    ?.toSet() ?: emptySet()

        } catch (e: Exception) {
            logger.error("Failed to get permission from Keycloak", e)
        }
        return emptySet()
    }

    /**
     * Returns all configured realms
     *
     * @return all configured realms
     */
    fun getConfiguredRealms(): List<String> {
        return listOf(realm)
    }

    /**
     * Constructs authz client for a realm
     *
     * @return created authz client or null if client could not be created
     */
    protected fun getAuthzClient(): AuthzClient {
        return AuthzClient.create(configuration)
    }

    /**
     * Creates admin client for config
     *
     * @return admin client
     */
    val adminClient: Keycloak
        get() {
            val credentials = configuration.credentials
            val clientSecret = credentials["secret"] as String?
            val adminUser = credentials["realm-admin-user"] as String?
            val adminPass = credentials["realm-admin-pass"] as String?
            val token = keycloakControllerToken.getAccessToken()
            val clientBuilder = ClientBuilderWrapper.create(null, false)
            clientBuilder.register(NotNullResteasyJackson2Provider(), 100)
            logger.trace("Using {} as admin user", adminUser)
            if (token == null) {
                logger.error("Could not retrieve admin client access token")
            }

            return KeycloakBuilder.builder()
                .serverUrl(configuration.authServerUrl)
                .realm(configuration.realm)
                .grantType(OAuth2Constants.PASSWORD)
                .clientId(configuration.resource)
                .clientSecret(clientSecret)
                .username(adminUser)
                .password(adminPass)
                .resteasyClient(clientBuilder.build() as ResteasyClient)
                .authorization("Bearer ${token?.accessToken}")
                .build()
        }

    /**
     * Resolves Keycloak API client
     *
     * @param keycloak admin client
     * @return Keycloak client
     */
    @Throws(AuthzException::class)
    fun getKeycloakClient(keycloak: Keycloak): ClientRepresentation {
        return findClient(keycloak, configuration.resource) ?: throw AuthzException("Keycloak client not found")
    }

    /**
     * Returns list of permitted users for a resource with given scopes
     *
     * @param keycloak keycloak client instance
     * @param client client
     * @param resourceId resource id
     * @param resourceName resource name
     * @param scopes scopes
     * @return set of user ids
     */
    fun getResourcePermittedUsers(keycloak: Keycloak, client: ClientRepresentation, resourceId: UUID, resourceName: String, scopes: List<AuthorizationScope>): Set<UUID> {
        return getPermittedUsers(keycloak.realm(realm), client, resourceId, resourceName, scopes)
    }

    /**
     * Creates authorization scopes into Keycloak
     *
     * @param keycloak Keycloak admin client
     * @param realmName realm name
     * @param client client
     * @param scopes scopes to be created
     * @return authorization scopes
     */
    fun createAuthorizationScopes(keycloak: Keycloak, realmName: String?, client: ClientRepresentation, scopes: List<AuthorizationScope>): List<UUID> {
        val scopesResource = keycloak.realm(realmName).clients()[client.id].authorization().scopes()
        return scopes
                .map(AuthorizationScope::scopeName)
                .map { ScopeRepresentation(it) }
                .map { scope -> scopesResource.create(scope) }
                .mapNotNull { keycloakClientUtils.getCreateResponseId(it) }
    }

    /**
     * Returns list of permitted users for a resource with given scopes
     *
     * @param realm realm name
     * @param client client
     * @param resourceId resource id
     * @param resourceName resource name
     * @param scopes scopes
     * @return set of user ids
     */
    private fun getPermittedUsers(realm: RealmResource, client: ClientRepresentation, resourceId: UUID, resourceName: String, scopes: List<AuthorizationScope>): Set<UUID> {
        val policies = evaluatePolicies(realm, client, resourceId, resourceName, scopes)
        return policies.entries
                .filter { DecisionEffect.PERMIT == it.value }
                .map { it.key }
                .toSet()
    }

    /**
     * Evaluates policies resource for realm users
     *
     * @param realm realm name
     * @param client client
     * @param resourceId resource id
     * @param resourceName resource name
     * @param scopes scopes
     * @return map of results where key is user id and value is decision
     */
    private fun evaluatePolicies(realm: RealmResource, client: ClientRepresentation, resourceId: UUID, resourceName: String, scopes: List<AuthorizationScope>): Map<UUID, DecisionEffect> {
        val result: MutableMap<UUID, DecisionEffect> = HashMap()
        var firstResult = 0
        val maxResults = 10
        while (firstResult < 1000) {
            val users = realm.users().list(firstResult, maxResults)
            for (user in users) {
                val userId = user.id
                result[UUID.fromString(userId)] = evaluatePolicy(realm, client, resourceId, resourceName, userId, scopes)
            }
            if (users.isEmpty() || users.size < maxResults) {
                break
            }
            firstResult += maxResults
        }
        return result
    }

    /**
     * Evaluates policy for a resource
     *
     * @param realm realm name
     * @param client client
     * @param resourceId resource id
     * @param resourceName resource name
     * @param userId user's id
     * @param scopes scopes
     * @return decision
     */
    private fun evaluatePolicy(realm: RealmResource, client: ClientRepresentation, resourceId: UUID, resourceName: String, userId: String, scopes: List<AuthorizationScope>): DecisionEffect {
        return try {
            val evaluationRequest = createEvaluationRequest(client, resourceId, resourceName, userId, scopes)
            val response = realm.clients()[client.id].authorization().policies().evaluate(evaluationRequest)
            response.status
        } catch (e: InternalServerErrorException) {
            logger.error("Failed to evaluate resource {} policy for {}", resourceName, userId, e)
            DecisionEffect.DENY
        }
    }

    /**
     * Creates Keycloak policy evaluation request
     *
     * @param client client
     * @param resourceId resource id
     * @param resourceName resource name
     * @param userId userId
     * @param scopes scopes
     * @return created request
     */
    private fun createEvaluationRequest(client: ClientRepresentation, resourceId: UUID, resourceName: String, userId: String, scopes: Collection<AuthorizationScope>): PolicyEvaluationRequest {
        val resourceScopes = scopes.map(AuthorizationScope::scopeName).map { ScopeRepresentation(it) }.toSet()
        val resource = ResourceRepresentation(resourceName, resourceScopes)
        resource.id = resourceId.toString()
        val evaluationRequest = PolicyEvaluationRequest()
        evaluationRequest.clientId = client.id
        evaluationRequest.resources = listOf(resource)
        evaluationRequest.userId = userId
        return evaluationRequest
    }

    /**
     * Finds a Keycloak client by realm and clientId
     *
     * @param keycloak keycloak admin client
     * @param clientId clientId
     * @return client or null if not found
     */
    private fun findClient(keycloak: Keycloak, clientId: String): ClientRepresentation? {
        val clients = keycloak.realm(realm).clients().findByClientId(clientId)
        return if (clients.isNotEmpty()) {
            clients[0]
        } else null
    }

    /**
     * Gets user group
     *
     * @param userId user id
     * @return list of group
     */
    fun getUserGroups(userId: String): List<GroupRepresentation> {
        return adminClient.realm(realm).users()[userId].groups()
    }

    /**
     * Adds a user to a group
     *
     * @param memberGroupId member group id
     * @param memberId member id
     */
    fun userJoinGroup(memberGroupId: String, memberId: String) {
        adminClient.realm(realm).users()[memberId].joinGroup(memberGroupId)
    }

    /**
     * Pops a user from group
     *
     * @param memberGroupId member group id
     * @param memberId member idp
     */
    fun userLeaveGroup(memberGroupId: String, memberId: String) {
        adminClient.realm(realm).users()[memberId].leaveGroup(memberGroupId)
    }

    /**
     * Gets metaform admin group name
     *
     * @param metaformId metaform id
     * @return metaform admin group name
     */
    fun getMetaformAdminGroupName(metaformId: UUID): String {
        return String.format(ADMIN_GROUP_NAME_TEMPLATE, metaformId)
    }

    /**
     * Gets metaform manager group name
     *
     * @param metaformId metaform id
     * @return metaform manager group name
     */
    fun getMetaformManagerGroupName(metaformId: UUID): String {
        return String.format(MANAGER_GROUP_NAME_TEMPLATE, metaformId)
    }

    /**
     * Creates metaform admin and manager group name
     *
     * @param metaformId metaform id
     */
    fun createMetaformManagementGroup(metaformId: UUID) {
        adminClient.realm(realm).groups().add(GroupRepresentation().apply { name = getMetaformAdminGroupName(metaformId) })
        adminClient.realm(realm).groups().add(GroupRepresentation().apply { name = getMetaformManagerGroupName(metaformId) })
    }

    /**
     * Deletes metaform admin and manager group name
     *
     * @param metaformId metaform id
     */
    fun deleteMetaformManagementGroup(metaformId: UUID) {
        val adminGroup = getMetaformAdminGroup(metaformId)
        val managerGroup = getMetaformManagerGroup(metaformId)
        adminClient.realm(realm).groups().group(adminGroup.id).remove()
        adminClient.realm(realm).groups().group(managerGroup.id).remove()
    }

    /**
     * Gets metaform manager group
     *
     * @param metaformId metaform id
     * @return metaform manager group
     */
    fun getMetaformManagerGroup(metaformId: UUID): GroupRepresentation {
        return adminClient.realm(realm).groups()
            .groups(getMetaformManagerGroupName(metaformId), 0, 1)
            .first()
    }

    /**
     * Gets metaform admin group
     *
     * @param metaformId metaform id
     * @return metaform admin group
     */
    fun getMetaformAdminGroup(metaformId: UUID): GroupRepresentation {
        return adminClient.realm(realm).groups()
            .groups(getMetaformAdminGroupName(metaformId), 0, 1)
            .first()
    }

    /**
     * Check if a user is metaform admin
     *
     * @param metaformId metaform id
     * @param userId user id
     * @return boolean indicate is admin or not
     */
    fun isMetaformAdmin(metaformId: UUID, userId: UUID): Boolean {
        return getUserGroups(userId.toString())
            .map { group -> group.name }
            .contains(getMetaformAdminGroupName(metaformId))
    }

    /**
     * Check if a user is any metaform admin
     *
     * @param userId user id
     * @return boolean indicate is admin or not
     */
    fun isMetaformAdminAny(userId: UUID): Boolean {
        return getUserGroups(userId.toString())
            .any { group -> group.name.contains(ADMIN_GROUP_NAME_SUFFIX) }
    }

    /**
     * Check if a user is metaform manager
     *
     * @param metaformId metaform id
     * @param userId user id
     * @return boolean indicate is manager or not
     */
    fun isMetaformManager(metaformId: UUID, userId: UUID): Boolean {
        return getUserGroups(userId.toString())
            .map { group -> group.name }
            .contains(getMetaformManagerGroupName(metaformId))
    }

    /**
     * Check if a user is any metaform manager
     *
     * @param userId user id
     * @return boolean indicate is manager or not
     */
    fun isMetaformManagerAny(userId: UUID): Boolean {
        return getUserGroups(userId.toString())
            .any { group -> group.name.contains(MANAGER_GROUP_NAME_SUFFIX) }
    }

    /**
     * Finds metaform member
     *
     * @param metaformMemberId metaform member id
     * @return found group or null
     */
    fun findMetaformMember(metaformMemberId: UUID): fi.metatavu.metaform.keycloak.client.models.UserRepresentation? {
        return try {
            return userApi.realmUsersIdGet(realm, metaformMemberId.toString())
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Lists manager metaform members
     *
     * @param metaformId metaform id
     * @return listed users
     */
    fun listMetaformMemberManager(metaformId: UUID): List<fi.metatavu.metaform.keycloak.client.models.UserRepresentation> {
        return groupApi.realmGroupsIdMembersGet(
            realm = realm,
            id = getMetaformManagerGroup(metaformId).id,
            first = null,
            max = null,
            briefRepresentation = false
        )
    }

    /**
     * Lists admin metaform members
     *
     * @param metaformId metaform id
     * @return listed users
     */
    fun listMetaformMemberAdmin(metaformId: UUID): List<fi.metatavu.metaform.keycloak.client.models.UserRepresentation> {
        return groupApi.realmGroupsIdMembersGet(
            realm = realm,
            id = getMetaformAdminGroup(metaformId).id,
            first = null,
            max = null,
            briefRepresentation = false
        )
    }

    /**
     * Finds group by id from Keycloak
     *
     * @param id group id
     * @return found group or null if not found
     */
    fun findGroup(id: UUID): fi.metatavu.metaform.keycloak.client.models.GroupRepresentation? {
        return groupApi.realmGroupsIdGet(realm = realm, id = id.toString())
    }

    /**
     * Delete group from Keycloak
     *
     * @param id group id
     */
    fun deleteGroup(id: UUID) {
        groupApi.realmGroupsIdDelete(realm = realm, id = id.toString())
    }

    /**
     * Finds metaform member group
     *
     * @param metaformId metaform id
     * @param metaformMemberRole metaform member role
     * @param userRepresentation userRepresentation
     * @return found group or null
     */
    @Throws(KeycloakException::class, KeycloakDuplicatedUserException::class)
    fun createMetaformMember(
        metaformId: UUID,
        metaformMemberRole: MetaformMemberRole,
        userRepresentation: UserRepresentation,
    ): fi.metatavu.metaform.keycloak.client.models.UserRepresentation {
        val existingUser = findUserByUsername(username = userRepresentation.username) ?: findUserByEmail(email = userRepresentation.email)

        if (existingUser == null) {
            val groupName = when (metaformMemberRole) {
                MetaformMemberRole.ADMINISTRATOR -> getMetaformAdminGroupName(metaformId)
                MetaformMemberRole.MANAGER -> getMetaformManagerGroupName(metaformId)
            }

            val response = adminClient.realm(realm).users().create(userRepresentation.apply {
                this.isEnabled = true
                this.groups = listOf(groupName)
            })

            if (response.status == 409) {
                throw KeycloakDuplicatedUserException("Duplicated user")
            } else if (response.status != 201) {
                throw KeycloakException(String.format("Request failed with %d", response.status))
            }

            val userId = keycloakClientUtils.getCreateResponseId(response) ?: throw KeycloakException("Failed to get the userId")
            val userRole = adminClient.realm(realm).roles().get(AbstractApi.METAFORM_USER_ROLE).toRepresentation()
            adminClient.realm(realm).users()[userId.toString()].roles().realmLevel().add(listOf(userRole))

            return findMetaformMember(userId) ?: throw KeycloakException("Failed to find the created user")
        } else {
            when (metaformMemberRole) {
                MetaformMemberRole.ADMINISTRATOR -> userJoinGroup(getMetaformAdminGroup(metaformId).id, existingUser.id!!)
                MetaformMemberRole.MANAGER -> userJoinGroup(getMetaformManagerGroup(metaformId).id, existingUser.id!!)
            }

            return existingUser
        }
    }

    /**
     * Finds metaform member group
     *
     * @param metaformId metaform id
     * @param metaformMemberGroupId metaform member group id
     * @return found group or null
     */
    fun findMetaformMemberGroup(metaformId: UUID, metaformMemberGroupId: UUID): GroupRepresentation? {
        val managerGroup = getMetaformManagerGroup(metaformId)

        return adminClient.realm(realm).groups()
            .group(managerGroup.id)
            .toRepresentation()
            .subGroups
            .find { group -> group.id == metaformMemberGroupId.toString() }
    }

    /**
     * Lists member groups for given metaformId
     *
     * @param metaformId metaform id
     * @return member groups for given metaformId
     */
    fun listMetaformMemberGroups(metaformId: UUID): List<GroupRepresentation> {
        val managerGroup = getMetaformManagerGroup(metaformId)

        return adminClient.realm(realm).groups()
            .group(managerGroup.id)
            .toRepresentation()
            .subGroups
    }

    /**
     * Finds metaform member group members
     *
     * @param metaformMemberGroupId metaform member group id
     * @return group members
     */
    fun findMetaformMemberGroupMembers(metaformMemberGroupId: UUID): List<UUID> {
        return adminClient.realm(realm).groups()
            .group(metaformMemberGroupId.toString())
            .members()
            .map { UUID.fromString(it.id) }
    }

    /**
     * Deletes metaform member
     *
     * @param metaformMemberId metaform member id
     */
    fun deleteMetaformMember(metaformMemberId: UUID) {
        adminClient.realm(realm).users().delete(metaformMemberId.toString())
    }

    /**
     * Updates metaform member
     *
     * @param metaformMember metaform member
     * @return updated metaform member
     */
    fun updateMetaformMember(metaformMember: fi.metatavu.metaform.keycloak.client.models.UserRepresentation): fi.metatavu.metaform.keycloak.client.models.UserRepresentation {
        userApi.realmUsersIdPut(
            realm = realm,
            id = metaformMember.id!!,
            userRepresentation = metaformMember
        )

        return metaformMember
    }

    /**
     * Updates user group
     *
     * @param userGroup user group
     * @return updated user group
     */
    fun updateUserGroup(userGroup: GroupRepresentation): GroupRepresentation {
        adminClient.realm(realm).groups().group(userGroup.id).update(userGroup)
        return userGroup
    }

    /**
     * Gets metaform member role
     *
     * @param userId user id
     * @param metaformId metaform id
     * @return metaform member role
     */
    @Throws(MetaformMemberRoleNotFoundException::class)
    fun getMetaformMemberRole(userId: String, metaformId: UUID): MetaformMemberRole {
        val adminGroupName = getMetaformAdminGroupName(metaformId)
        val managerGroupName = getMetaformManagerGroupName(metaformId)
        val userGroupNames = getUserGroups(userId).map { group -> group.name }
        with (userGroupNames) {
            when {
                contains(adminGroupName) -> return MetaformMemberRole.ADMINISTRATOR
                contains(managerGroupName) -> return MetaformMemberRole.MANAGER
                else -> throw MetaformMemberRoleNotFoundException("Metaform member role not found")
            }
        }
    }

    /**
     * Formats keycloak search query
     *
     * @param metaformId metaform Id
     * @param metaformMember metaform member
     * @param newRole new metaform member role
     * @return search query
     */
    fun updateMemberManagementGroup(
        metaformId: UUID,
        metaformMember: fi.metatavu.metaform.keycloak.client.models.UserRepresentation,
        newRole: MetaformMemberRole
    ) {
        val prevRole = getMetaformMemberRole(metaformMember.id!!, metaformId)
        when {
            prevRole == MetaformMemberRole.ADMINISTRATOR && newRole == MetaformMemberRole.MANAGER ->
                userLeaveGroup(getMetaformAdminGroup(metaformId).id, metaformMember.id)
            prevRole == MetaformMemberRole.MANAGER && newRole == MetaformMemberRole.ADMINISTRATOR ->
                userJoinGroup(getMetaformAdminGroup(metaformId).id, metaformMember.id)
            else -> return
        }
    }

    /**
     * Creates metaform member group
     *
     * @param metaformId metaform Id
     * @param memberGroup member group
     * @return created group
     */
    @Throws(KeycloakException::class)
    fun createMetaformMemberGroup(metaformId: UUID, memberGroup: GroupRepresentation): GroupRepresentation {
        val managerGroup = getMetaformManagerGroup(metaformId)
        val response = adminClient.realm(realm).groups().group(managerGroup.id).subGroup(memberGroup)

        if (response.status != 201) {
            throw KeycloakException(String.format("Request failed with %d", response.status))
        }

        val groupId = keycloakClientUtils.getCreateResponseId(response) ?: throw KeycloakException("Failed to get created group id")
        return findMetaformMemberGroup(metaformId, groupId) ?: throw KeycloakException("Failed to find the created group")
    }

    /**
     * Finds user by username
     *
     * @param username username
     * @return found user or null if not found
     */
    private fun findUserByUsername(username: String): fi.metatavu.metaform.keycloak.client.models.UserRepresentation? {
        return usersApi.realmUsersGet(
            realm = realm,
            search = null,
            lastName = null,
            firstName = null,
            email = null,
            username = username,
            emailVerified = null,
            idpAlias = null,
            idpUserId = null,
            first = 0,
            max = 1,
            enabled = null,
            briefRepresentation = false,
            exact = true,
            q = null
        ).firstOrNull()
    }

    /**
     * Finds user by email address
     *
     * @param email email
     * @return found user or null if not found
     */
    private fun findUserByEmail(email: String): fi.metatavu.metaform.keycloak.client.models.UserRepresentation? {
        return usersApi.realmUsersGet(
            realm = realm,
            search = null,
            lastName = null,
            firstName = null,
            email = email,
            username = null,
            emailVerified = null,
            idpAlias = null,
            idpUserId = null,
            first = 0,
            max = 1,
            enabled = null,
            briefRepresentation = false,
            exact = true,
            q = null
        ).firstOrNull()
    }

    companion object {
        private const val ADMIN_GROUP_NAME_TEMPLATE = "%s-admin"
        private const val MANAGER_GROUP_NAME_TEMPLATE = "%s-manager"
        private const val ADMIN_GROUP_NAME_SUFFIX = "admin"
        private const val MANAGER_GROUP_NAME_SUFFIX = "manager"
    }
}