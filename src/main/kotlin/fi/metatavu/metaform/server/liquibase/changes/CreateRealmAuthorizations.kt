package fi.metatavu.metaform.server.liquibase.changes

import fi.metatavu.metaform.server.exceptions.KeycloakClientNotFoundException
import fi.metatavu.metaform.server.exceptions.ResourceNotFoundException
import fi.metatavu.metaform.server.keycloak.AuthorizationScope
import fi.metatavu.metaform.server.controllers.KeycloakController
import fi.metatavu.metaform.server.keycloak.ResourceType
import liquibase.database.Database
import liquibase.exception.CustomChangeException
import org.keycloak.representations.idm.authorization.DecisionStrategy
import javax.enterprise.context.ApplicationScoped
import javax.inject.Inject

/**
 * Custom change for creating authorization resources for all replies
 *
 * @author Antti Leppä
 */
@ApplicationScoped
class CreateRealmAuthorizations : AbstractAuthzCustomChange() {
    @Inject
    lateinit var keycloakController: KeycloakController

    @Throws(CustomChangeException::class)
    override fun execute(database: Database) {
        for (realmName in keycloakController.getConfiguredRealms()) {
            createRealmAuthorizations(realmName)
        }
    }

    /**
     * create realm authorizations
     *
     * @param realmName realm name
     * @throws CustomChangeException when migration fails
     */
    @Throws(CustomChangeException::class, KeycloakClientNotFoundException::class)
    private fun createRealmAuthorizations(realmName: String?) {
        try {
            val adminClient = keycloakController.getAdminClient()
            val keycloakClient = keycloakController.getKeycloakClient(adminClient)
            keycloakController.createAuthorizationScopes(adminClient, realmName, keycloakClient, AuthorizationScope.values().toList())
            val defaultPolicyId = keycloakController.getPolicyIdByName(adminClient, keycloakClient, "Default Policy")!!
            val scopes: List<AuthorizationScope> = listOf(AuthorizationScope.REPLY_CREATE, AuthorizationScope.REPLY_VIEW)
            val resourceId = keycloakController.createProtectedResource(adminClient, keycloakClient, null, "replies", "/v1/metaforms/{metaformId}/replies", ResourceType.REPLY.urn, scopes)
                    ?: throw ResourceNotFoundException("Resource not found")
            keycloakController.upsertScopePermission(adminClient, keycloakClient, resourceId, scopes, "replies", DecisionStrategy.UNANIMOUS, setOf(defaultPolicyId))
            appendConfirmationMessage(String.format("Created default permissions into realm %s", realmName))
        } catch (e: Exception) {
            val keycloakErrorMessage = getKeycloakErrorMessage(e)
            throw CustomChangeException(String.format("Realm %s migration failed with following error: %s", realmName, keycloakErrorMessage
                    ?: e.message), e)
        }
    }
}