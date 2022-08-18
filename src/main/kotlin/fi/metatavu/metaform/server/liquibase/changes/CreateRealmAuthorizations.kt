package fi.metatavu.metaform.server.liquibase.changes

import fi.metatavu.metaform.server.exceptions.AuthzException
import fi.metatavu.metaform.server.keycloak.AuthorizationScope
import fi.metatavu.metaform.server.controllers.KeycloakController
import fi.metatavu.metaform.server.keycloak.ResourceType
import fi.metatavu.metaform.server.permissions.AuthzController
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
@Suppress ("UNUSED")
class CreateRealmAuthorizations : AbstractAuthzCustomChange() {

    @Inject
    lateinit var keycloakController: KeycloakController

    @Inject
    lateinit var authzController: AuthzController

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
    @Throws(CustomChangeException::class, AuthzException::class)
    private fun createRealmAuthorizations(realmName: String?) {
        try {
            val adminClient = keycloakController.adminClient
            val keycloakClient = keycloakController.getKeycloakClient(adminClient)
            keycloakController.createAuthorizationScopes(adminClient, realmName, keycloakClient, AuthorizationScope.values().toList())
            val defaultPolicyId = authzController.getPolicyIdByName(
                keycloak = adminClient,
                name = "Default Policy"
            )!!

            val scopes: List<AuthorizationScope> = listOf(AuthorizationScope.REPLY_CREATE, AuthorizationScope.REPLY_VIEW)
            val resourceId = authzController.createProtectedResource(
                keycloak = adminClient,
                ownerId = null,
                name = "replies",
                uri = "/v1/metaforms/{metaformId}/replies",
                type = ResourceType.REPLY.urn,
                scopes = scopes
            )

            authzController.upsertScopePermission(
                keycloak = adminClient,
                resourceId = resourceId,
                scopes = scopes,
                name = "replies",
                decisionStrategy = DecisionStrategy.UNANIMOUS,
                policyIds = setOf(defaultPolicyId)
            )

            appendConfirmationMessage(String.format("Created default permissions into realm %s", realmName))
        } catch (e: Exception) {
            val keycloakErrorMessage = getKeycloakErrorMessage(e)
            throw CustomChangeException(String.format("Realm %s migration failed with following error: %s", realmName, keycloakErrorMessage
                    ?: e.message), e)
        }
    }
}