package fi.metatavu.metaform.server.keycloak

/**
 * Class for Keycloak Admin Client configuration
 *
 * @property realm Keycloak Realm
 * @property clientId Keycloak Client Id
 * @property clientSecret Keycloak Client Secret
 * @property apiAdminUser Keycloak Api Admin User
 * @property apiAdminPassword Keycloak Api Admin Password
 * @property authServerUrl Keycloak Auth Server Url
 */
class KeycloakConfiguration (
    val realm: String,
    val clientId: String,
    val clientSecret: String,
    val apiAdminUser: String,
    val apiAdminPassword: String,
    val authServerUrl: String
)