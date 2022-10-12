package fi.metatavu.metaform.server.controllers

import fi.metatavu.metaform.keycloak.client.apis.UsersApi
import fi.metatavu.metaform.keycloak.client.infrastructure.ApiClient
import fi.metatavu.metaform.server.keycloak.KeycloakConfiguration
import fi.metatavu.metaform.server.keycloak.KeycloakControllerToken
import fi.metatavu.metaform.server.keycloak.KeycloakSource
import org.eclipse.microprofile.config.inject.ConfigProperty
import org.slf4j.Logger
import javax.enterprise.context.ApplicationScoped
import javax.inject.Inject

/**
 * Controller for Card Auth Keycloak related operations.
 */
@ApplicationScoped
class CardAuthKeycloakController {

    @Inject
    @ConfigProperty(name = "card.auth.keycloak.admin.realm")
    lateinit var realm: String

    @Inject
    @ConfigProperty(name = "card.auth.keycloak.admin.admin_client_id")
    lateinit var clientId: String

    @Inject
    @ConfigProperty(name = "card.auth.keycloak.admin.secret")
    lateinit var clientSecret: String

    @Inject
    @ConfigProperty(name = "card.auth.keycloak.admin.user")
    lateinit var apiAdminUser: String

    @Inject
    @ConfigProperty(name = "card.auth.keycloak.admin.password")
    lateinit var apiAdminPassword: String

    @Inject
    @ConfigProperty(name = "card.auth.keycloak.admin.host")
    lateinit var authServerUrl: String

    @Inject
    lateinit var logger: Logger

    @Inject
    lateinit var keycloakControllerToken: KeycloakControllerToken

    private val keycloakConfiguration: KeycloakConfiguration
        get() {
            return KeycloakConfiguration(
                realm = realm,
                clientId = clientId,
                clientSecret = clientSecret,
                apiAdminUser = apiAdminUser,
                apiAdminPassword = apiAdminPassword,
                authServerUrl = authServerUrl
            )
        }

    private val apiBasePath: String
        get() {
            return "${authServerUrl}admin/realms"
        }

    private val usersApi: UsersApi
        get() {
            ApiClient.accessToken = keycloakControllerToken.getAccessToken(keycloakConfiguration, KeycloakSource.CARD_AUTH)?.accessToken
            return UsersApi(basePath = apiBasePath)
        }

    /**
     * Finds users by search param
     *
     * @param search search
     * @param firstResult firstResult
     * @param maxResults maxResults
     * @return List of found users
     */
    fun searchUsers(
        search: String?,
        maxResults: Int? = 100
    ): List<fi.metatavu.metaform.keycloak.client.models.UserRepresentation> {
        return usersApi.realmUsersGet(
            realm = realm,
            search = search,
            lastName = null,
            firstName = null,
            email = null,
            username = null,
            emailVerified = null,
            idpAlias = null,
            idpUserId = null,
            first = 0,
            max = maxResults,
            enabled = null,
            briefRepresentation = false,
            exact = false,
            q = null
        )
    }
}