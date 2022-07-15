package fi.metatavu.metaform.server.controllers

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import fi.metatavu.metaform.server.exceptions.FailStoreFailedException
import fi.metatavu.metaform.server.exceptions.KeycloakClientNotFoundException
import fi.metatavu.metaform.server.keycloak.AuthorizationScope
import fi.metatavu.metaform.server.keycloak.NotNullResteasyJackson2Provider
import org.apache.commons.io.IOUtils
import org.apache.commons.lang3.StringUtils
import org.apache.http.NameValuePair
import org.apache.http.client.entity.UrlEncodedFormEntity
import org.apache.http.client.methods.HttpPost
import org.apache.http.impl.client.HttpClients
import org.apache.http.message.BasicNameValuePair
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
import java.io.IOException
import java.io.InputStream
import java.util.*
import java.util.regex.Pattern
import java.util.stream.Collectors
import javax.enterprise.context.ApplicationScoped
import javax.inject.Inject
import javax.ws.rs.InternalServerErrorException
import javax.ws.rs.core.Response


@ApplicationScoped
class KeycloakController {

    @ConfigProperty(name = "metaforms.keycloak.admin.realm")
    private lateinit var realm: String

    @ConfigProperty(name = "metaforms.keycloak.admin.admin_client_id")
    private lateinit var clientId: String

    @ConfigProperty(name = "metaforms.keycloak.admin.secret")
    private lateinit var clientSecret: String

    @ConfigProperty(name = "metaforms.keycloak.admin.user")
    private lateinit var apiAdminUser: String

    @ConfigProperty(name = "metaforms.keycloak.admin.password")
    private lateinit var apiAdminPassword: String

    @ConfigProperty(name = "metaforms.keycloak.admin.host")
    private lateinit var authServerUrl: String

    @Inject
    private lateinit var logger: Logger

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
    fun getAdminClient(): Keycloak {
        val credentials = configuration.credentials
        val clientSecret = credentials["secret"] as String?
        val adminUser = credentials["realm-admin-user"] as String?
        val adminPass = credentials["realm-admin-pass"] as String?
        val token = getAccessToken(configuration.authServerUrl, configuration.realm, configuration.resource, clientSecret, adminUser, adminPass)
        val clientBuilder = ClientBuilderWrapper.create(null, false)
        clientBuilder.register(NotNullResteasyJackson2Provider(), 100)
        logger.info("Using {} as admin user", adminUser)
        if (StringUtils.isBlank(token)) {
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
                .authorization(String.format("Bearer %s", token))
                .build()
    }

    /**
     * Resolves Keycloak API client
     *
     * @param keycloak admin client
     * @return Keycloak client
     */
    @Throws(KeycloakClientNotFoundException::class)
    fun getKeycloakClient(keycloak: Keycloak): ClientRepresentation {
        return findClient(keycloak, configuration.resource) ?: throw KeycloakClientNotFoundException("Keycloak client not found")
    }

    /**
     * Creates protected resource into Keycloak
     *
     * @param keycloak Keycloak instance
     * @param keycloakClient Keycloak client representation
     * @param ownerId resource owner id
     * @param name resource's human-readable name
     * @param uri resource's uri
     * @param type resource's type
     * @param scopes resource's scopes
     * @return created resource
     */
    fun createProtectedResource(keycloak: Keycloak, keycloakClient: ClientRepresentation, ownerId: UUID?, name: String, uri: String, type: String, scopes: List<AuthorizationScope>): UUID? {
        val realmName: String = configuration.realm
        val resources = keycloak.realm(realmName).clients()[keycloakClient.id].authorization().resources()
        val scopeRepresentations = scopes
                .map(AuthorizationScope::scopeName)
                .map { ScopeRepresentation(it) }
                .toSet()
        val resource = ResourceRepresentation(name, scopeRepresentations, uri, type)
        if (ownerId != null) {
            resource.setOwner(ownerId.toString())
            resource.ownerManagedAccess = true
        }
        val resourceId = getCreateResponseId(resources.create(resource))
        if (resourceId != null) {
            return resourceId
        }
        val foundResources = resources.findByName(name, ownerId?.toString())
        if (foundResources.isEmpty()) {
            return null
        }
        if (foundResources.size > 1) {
            logger.warn("Found more than one resource with name {}", name)
        }
        return UUID.fromString(foundResources[0].id)
    }

    /**
     * Creates new scope permission for resource
     *
     * @param keycloak keycloak admin client
     * @param client client
     * @param resourceId resource id
     * @param scopes authorization scopes
     * @param name name
     * @param policyIds policies
     * @param decisionStrategy
     * @return created permission
     */
    fun upsertScopePermission(
            keycloak: Keycloak,
            client: ClientRepresentation,
            resourceId: UUID,
            scopes: Collection<AuthorizationScope>,
            name: String?,
            decisionStrategy: DecisionStrategy?,
            policyIds: Collection<UUID>
    ) {
        val realmName: String = configuration.realm
        val realm = keycloak.realm(realmName)
        val scopeResource = realm.clients()[client.id].authorization().permissions().scope()
        val existingPermission = scopeResource.findByName(name)
        val representation = ScopePermissionRepresentation()
        representation.decisionStrategy = decisionStrategy
        representation.logic = Logic.POSITIVE
        representation.name = name
        representation.type = "scope"
        representation.resources = setOf(resourceId.toString())
        representation.scopes = scopes
                .map(AuthorizationScope::scopeName)
                .toSet()
        representation.policies = policyIds.stream().map { obj: UUID -> obj.toString() }.collect(Collectors.toSet())
        val response = scopeResource.create(representation)
        try {
            if (existingPermission == null) {
                val status = response.status
                if (status != 201) {
                    var message: String? = "Unknown error"
                    try {
                        message = IOUtils.toString(response.entity as InputStream, "UTF-8")
                    } catch (e: IOException) {
                        logger.warn("Failed read error message", e)
                    }
                    logger.warn("Failed to create scope permission for resource {} with message {}", resourceId, message)
                }
            } else {
                realm.clients()[client.id].authorization().permissions().scope()
                        .findById(existingPermission.id)
                        .update(representation)
            }
        } finally {
            response.close()
        }
    }

    /**
     * Updates groups and group policies into Keycloak
     *
     * @param keycloak admin client
     * @param realmName realm name
     * @param client client
     * @param groupNames groups names
     */
    fun updatePermissionGroups(keycloak: Keycloak, client: ClientRepresentation, groupNames: List<String>) {
        val keycloakRealm = keycloak.realm(realm)
        val groups = keycloakRealm.groups()
        val groupPolicies = keycloakRealm.clients()[client.id].authorization().policies().group()
        val existingGroups = groups.groups()
                .associate { group -> group.name to UUID.fromString(group.id) }
        for (groupName in groupNames) {
            var groupId = existingGroups[groupName]
            if (groupId == null) {
                val groupRepresentation = GroupRepresentation()
                groupRepresentation.name = groupName
                groupId = getCreateResponseId(groups.add(groupRepresentation))
            }
            var policyRepresentation = groupPolicies.findByName(groupName)
            if (policyRepresentation == null && groupId != null) {
                groupPolicies.create(policyRepresentation)
                policyRepresentation = GroupPolicyRepresentation()
                policyRepresentation.name = groupName
                policyRepresentation.decisionStrategy = DecisionStrategy.UNANIMOUS
                policyRepresentation.addGroup(groupId.toString(), true)
                groupPolicies.create(policyRepresentation)
            }
        }
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
     * Find policy id by name
     *
     * @param keycloak Keycloak admin client
     * @param client client
     * @param name group name
     * @return list of group policy ids
     */
    fun getPolicyIdByName(keycloak: Keycloak, client: ClientRepresentation, name: String): UUID? {
        val ids = getPolicyIdsByNames(keycloak, client, mutableListOf(name))
        return if (ids.isEmpty()) {
            null
        } else ids.iterator().next()
    }

    /**
     * Lists policy ids by names
     *
     * @param keycloak Keycloak admin client
     * @param client client
     * @param names group names
     * @return list of group policy ids
     */
    fun getPolicyIdsByNames(keycloak: Keycloak, client: ClientRepresentation, names: MutableList<String>): Set<UUID> {
        val keycloakRealm = keycloak.realm(realm)
        val policies = keycloakRealm.clients()[client.id].authorization().policies()
        return names.stream()
                .map { name: String? -> policies.findByName(name) }
                .filter { obj: PolicyRepresentation? -> Objects.nonNull(obj) }
                .map { obj: PolicyRepresentation -> obj.id }
                .map { name: String? -> UUID.fromString(name) }
                .collect(Collectors.toSet())
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
                .mapNotNull { getCreateResponseId(it) }
    }

    /**
     * Finds user by username
     *
     * @param keycloak Keycloak admin client
     * @param realmName realm name
     * @param username username
     * @return found user or null if not found
     */
    fun findUser(keycloak: Keycloak, realmName: String?, username: String?): UserRepresentation? {
        val result = keycloak.realm(realmName).users().search(username)
        return if (result.isEmpty()) {
            null
        } else result[0]
    }

    /**
     * Resolves an access token for realm, client, username and password
     *
     * @param realm realm
     * @param clientId clientId
     * @param username username
     * @param password password
     * @return an access token
     * @throws IOException thrown on communication failure
     */
    private fun getAccessToken(serverUrl: String, realm: String, clientId: String, clientSecret: String?, username: String?, password: String?): String? {
        val uri = String.format("%s/realms/%s/protocol/openid-connect/token", serverUrl, realm)
        try {
            HttpClients.createDefault().use { client ->
                val httpPost = HttpPost(uri)
                val params: MutableList<NameValuePair> = ArrayList()
                params.add(BasicNameValuePair("client_id", clientId))
                params.add(BasicNameValuePair("grant_type", "password"))
                params.add(BasicNameValuePair("username", username))
                params.add(BasicNameValuePair("password", password))
                params.add(BasicNameValuePair("client_secret", clientSecret))
                httpPost.entity = UrlEncodedFormEntity(params)
                client.execute(httpPost).use { response ->
                    response.entity.content.use { inputStream ->
                        val responseMap = readJsonMap(inputStream)
                        return responseMap["access_token"] as String?
                    }
                }
            }
        } catch (e: IOException) {
            logger.debug("Failed to retrieve access token", e)
        }
        return null
    }

    fun createProtectedResource(ownerId: UUID, name: String?, uri: String?, type: String, scopes: List<AuthorizationScope>): ResourceRepresentation {
        val authzClient = getAuthzClient()
        val scopeRepresentations: Set<ScopeRepresentation> = scopes
                .map(AuthorizationScope::scopeName)
                .map { ScopeRepresentation(it) }
                .toSet()
        val resource = ResourceRepresentation(name, scopeRepresentations, uri, type)
        resource.setOwner(ownerId.toString())
        resource.ownerManagedAccess = true
        return authzClient.protection().resource().create(resource)
    }


    /**
     * Reads JSON src into Map
     *
     * @param src input
     * @return map
     * @throws IOException throws IOException when there is error when reading the input
     */
    @Throws(IOException::class)
    private fun readJsonMap(src: InputStream): Map<String, Any?> {
        val objectMapper = ObjectMapper()
        return objectMapper.readValue(src, object : TypeReference<Map<String, Any?>>() {})
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
     * Finds a id from Keycloak create response
     *
     * @param response response object
     * @return id
     */
    private fun getCreateResponseId(response: Response): UUID? {
        if (response.status != 201) {
            try {
                if (logger.isErrorEnabled) {
                    logger.error("Failed to execute create: {}", IOUtils.toString(response.entity as InputStream, "UTF-8"))
                }
            } catch (e: IOException) {
                logger.error("Failed to extract error message", e)
            }
            return null
        }
        val locationId = getCreateResponseLocationId(response)
        return locationId ?: getCreateResponseBodyId(response)
    }

    /**
     * Attempts to locate id from create location response
     *
     * @param response response
     * @return id or null if not found
     */
    private fun getCreateResponseLocationId(response: Response): UUID? {
        val location = response.getHeaderString("location")
        if (StringUtils.isNotBlank(location)) {
            val pattern = Pattern.compile(".*\\/(.*)$")
            val matcher = pattern.matcher(location)
            if (matcher.find()) {
                return UUID.fromString(matcher.group(1))
            }
        }
        return null
    }

    /**
     * Attempts to locate id from create response body
     *
     * @param response response object
     * @return id or null if not found
     */
    private fun getCreateResponseBodyId(response: Response): UUID? {
        if (response.entity is InputStream) {
            try {
                (response.entity as InputStream).use { inputStream ->
                    val result = readJsonMap(inputStream)
                    if (result["_id"] is String) {
                        return UUID.fromString(result["_id"] as String?)
                    }
                    if (result["id"] is String) {
                        return UUID.fromString(result["id"] as String?)
                    }
                }
            } catch (e: IOException) {
                if (logger.isDebugEnabled) {
                    logger.debug("Failed to locate id from response", e)
                }
            }
        }
        return null
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
     * Check if a user is metaform admin
     *
     * @param metaformId metaform id
     * @param userId user id
     * @return boolean indicate is admin or not
     */
    fun isMetaformAdmin(metaformId: UUID, userId: UUID): Boolean {
//        TODO
        return true
    }

}