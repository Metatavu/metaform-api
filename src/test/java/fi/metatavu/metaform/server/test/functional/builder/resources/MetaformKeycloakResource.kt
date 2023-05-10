package fi.metatavu.metaform.server.test.functional.builder.resources

import dasniko.testcontainers.keycloak.KeycloakContainer
import io.quarkus.test.common.QuarkusTestResourceLifecycleManager
import kotlin.collections.HashMap

/**
 * Starts test container for Metaform Keycloak
 */
class MetaformKeycloakResource : QuarkusTestResourceLifecycleManager {
    override fun start(): Map<String, String> {
        keycloak.start()
        val config: MutableMap<String, String> = HashMap()
        config["quarkus.oidc.auth-server-url"] = String.format("%srealms/test-1", keycloak.authServerUrl)
        config["quarkus.oidc.client-id"] = "metaform-api"
        config["metaforms.keycloak.admin.host"] = keycloak.authServerUrl
        config["metaforms.keycloak.admin.realm"] = "test-1"
        config["metaforms.keycloak.admin.user"] = "realm-admin"
        config["metaforms.keycloak.admin.password"] = "test"
        config["metaforms.keycloak.admin.admin_client_id"] = "metaform-api"
        config["metaforms.keycloak.admin.secret"] = "378833f9-dde8-4443-84ca-edfa26e2f0ee"
        config["metaforms.features.auditlog"] = "true"
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
            .withRealmImportFile("exported-metaform-kc.json")
    }
}