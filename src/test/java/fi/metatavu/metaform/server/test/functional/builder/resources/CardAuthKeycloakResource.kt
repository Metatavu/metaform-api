package fi.metatavu.metaform.server.test.functional.builder.resources

import dasniko.testcontainers.keycloak.KeycloakContainer
import io.quarkus.test.common.QuarkusTestResourceLifecycleManager

/**
 * Starts test container for Card Auth Keycloak
 */
class CardAuthKeycloakResource: QuarkusTestResourceLifecycleManager {

    override fun start(): Map<String, String> {
        keycloak.start()
        val config: MutableMap<String, String> = HashMap()
        config["card.auth.keycloak.admin.host"] = keycloak.authServerUrl
        config["card.auth.keycloak.admin.realm"] = "test-1"
        config["card.auth.keycloak.admin.user"] = "realm-admin"
        config["card.auth.keycloak.admin.password"] = "test"
        config["card.auth.keycloak.admin.admin_client_id"] = "metaform-api"
        config["card.auth.keycloak.admin.secret"] = "378833f9-dde8-4443-84ca-edfa26e2f0ee"
        return config
    }

    override fun stop() {
        keycloak.stop()
    }

    companion object {
        val serverAdminUser = "admin"
        val serverAdminPass = "admin"
        val keycloak: KeycloakContainer = KeycloakContainer()
            .withAdminUsername(serverAdminUser)
            .withAdminPassword(serverAdminPass)
            .withRealmImportFile("exported-card-auth-kc.json")
    }
}