package fi.metatavu.metaform.server.permissions

import fi.metatavu.metaform.server.exceptions.AuthzException
import fi.metatavu.metaform.server.keycloak.AuthorizationScope
import fi.metatavu.metaform.server.keycloak.KeycloakClientUtils
import org.apache.commons.io.IOUtils
import org.eclipse.microprofile.config.inject.ConfigProperty
import org.keycloak.admin.client.Keycloak
import org.keycloak.representations.idm.ClientRepresentation
import org.keycloak.representations.idm.authorization.*
import org.keycloak.representations.idm.authorization.GroupPolicyRepresentation.GroupDefinition
import org.slf4j.Logger
import java.io.IOException
import java.io.InputStream
import java.util.*
import java.util.stream.Collectors
import javax.enterprise.context.ApplicationScoped
import javax.inject.Inject

/**
 * Keycloak authorization controller
 */
@ApplicationScoped
class AuthzController {

    @Inject
    @ConfigProperty(name = "metaforms.keycloak.admin.realm")
    lateinit var realm: String

    @Inject
    @ConfigProperty(name = "metaforms.keycloak.admin.admin_client_id")
    lateinit var clientId: String

    @Inject
    lateinit var logger: Logger

    @Inject
    lateinit var keycloakClientUtils: KeycloakClientUtils

    /**
     * Creates protected resource into Keycloak
     *
     * @param keycloak Keycloak instance
     * @param ownerId resource owner id
     * @param name resource's human-readable name
     * @param uri resource's uri
     * @param type resource's type
     * @param scopes resource's scopes
     * @return created resource
     */
    fun createProtectedResource(keycloak: Keycloak, ownerId: UUID?, name: String, uri: String, type: String, scopes: List<AuthorizationScope>): UUID {
        val client = getClient(keycloak = keycloak)
        val resources = keycloak.realm(realm).clients()[client.id].authorization().resources()
        val scopeRepresentations = scopes
            .map(AuthorizationScope::scopeName)
            .map { ScopeRepresentation(it) }
            .toSet()

        val resource = ResourceRepresentation(name, scopeRepresentations, uri, type)
        if (ownerId != null) {
            resource.setOwner(ownerId.toString())
            resource.ownerManagedAccess = true
        }

        val resourceId = keycloakClientUtils.getCreateResponseId(resources.create(resource))
        if (resourceId != null) {
            return resourceId
        }

        val foundResources = resources.findByName(name, ownerId?.toString())
        if (foundResources.isEmpty()) {
           throw AuthzException("Failed to create protected resource")
        }

        if (foundResources.size > 1) {
            throw AuthzException("Found more than one resource with name '$name'")
        }

        return UUID.fromString(foundResources[0].id)
    }

    /**
     * Creates new scope permission for resource
     *
     * @param keycloak keycloak admin client
     * @param resourceId resource id
     * @param scopes authorization scopes
     * @param name name
     * @param policyIds policies
     * @param decisionStrategy decision strategy
     */
    fun upsertScopePermission(
        keycloak: Keycloak,
        resourceId: UUID,
        scopes: Collection<AuthorizationScope>,
        name: String?,
        decisionStrategy: DecisionStrategy?,
        policyIds: Collection<UUID>
    ) {
        val client = getClient(keycloak = keycloak)
        val scopeResource = keycloak.realm(realm).clients()[client.id].authorization().permissions().scope()
        val existingPermission = scopeResource.findByName(name)
        val representation = ScopePermissionRepresentation()

        representation.decisionStrategy = decisionStrategy
        representation.logic = Logic.POSITIVE
        representation.name = name
        representation.type = "scope"
        representation.resources = setOf(resourceId.toString())
        representation.scopes = scopes.map(AuthorizationScope::scopeName).toSet()
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
                keycloak.realm(realm).clients()[client.id].authorization().permissions().scope()
                    .findById(existingPermission.id)
                    .update(representation)
            }
        } finally {
            response.close()
        }
    }

    /**
     * Creates group policy
     *
     * @param keycloak Keycloak admin client
     * @param policyName policy's name
     * @param groupId group id
     * @return created policy id
     */
    fun createGroupPolicy(
        keycloak: Keycloak,
        policyName: String,
        groupId: UUID
    ): UUID {
        val client = getClient(keycloak = keycloak)
        val groupPolicy = GroupPolicyRepresentation()
        groupPolicy.name = policyName
        groupPolicy.groups = setOf(GroupDefinition(groupId.toString()))
        val response = keycloak.realm(realm).clients()[client.id].authorization().policies().group().create(groupPolicy)
        return keycloakClientUtils.getCreateResponseId(response) ?: throw AuthzException("Failed to create group policy")
    }

    /**
     * Deletes group policy
     *
     * @param keycloak Keycloak admin client
     * @param policyName policy's name
     */
    fun deleteGroupPolicy(
        keycloak: Keycloak,
        policyName: String
    ) {
        val policyId = getPolicyIdByName(
            keycloak = keycloak,
            name = policyName
        )

        val client = getClient(keycloak = keycloak)

        keycloak.realm(realm).clients()[client.id].authorization().policies().policy(policyId.toString()).remove()
    }

    /**
     * Find policy id by name
     *
     * @param keycloak Keycloak admin client
     * @param name group name
     * @return list of group policy ids
     */
    fun getPolicyIdByName(keycloak: Keycloak, name: String): UUID? {
        return getPolicyIdsByNames(keycloak, mutableListOf(name))
            .firstOrNull()
    }

    /**
     * Lists policy ids by names
     *
     * @param keycloak Keycloak admin client
     * @param policyNames group names
     * @return list of group policy ids
     */
    fun getPolicyIdsByNames(keycloak: Keycloak, policyNames: List<String>): Set<UUID> {
        val keycloakRealm = keycloak.realm(realm)
        val client = getClient(keycloak = keycloak)

        val policies = keycloakRealm.clients()[client.id].authorization().policies()

        return policyNames.stream()
            .map { name: String? -> policies.findByName(name) }
            .filter { obj: PolicyRepresentation? -> Objects.nonNull(obj) }
            .map { obj: PolicyRepresentation -> obj.id }
            .map { name: String? -> UUID.fromString(name) }
            .collect(Collectors.toSet())
    }

    /**
     * Returns client from Keycloak
     *
     * @param keycloak Keycloak admin client
     * @return client
     */
    private fun getClient(keycloak: Keycloak): ClientRepresentation {
        return keycloak.realm(realm).clients().findByClientId(clientId).firstOrNull()
            ?: throw AuthzException("Keycloak client not found")
    }

}