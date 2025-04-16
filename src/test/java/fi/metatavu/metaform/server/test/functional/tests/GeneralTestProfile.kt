package fi.metatavu.metaform.server.test.functional.tests

import io.quarkus.test.junit.QuarkusTestProfile

/**
 * Test profile settings common values for all tests
 */
class GeneralTestProfile : QuarkusTestProfile {
    override fun getConfigOverrides(): Map<String, String> {
        val properties: MutableMap<String, String> = HashMap()
        properties["runmode"] = "TEST"
        properties["metaform.uploads.folder"] = "/tmp"
        properties["quarkus.liquibase.contexts"] = "test"
        properties["metaforms.keycloak.card.identity.provider"] = "oidc"
        properties["metaforms.features.auditlog"] = "true"
        properties["metaforms.features.cardauth"] = "true"
        properties["metaforms.environment"] = "DEVELOPMENT"
        properties["metaforms.deletion.interval"] = "3s"
        properties["metaforms.deletion.delay"] = "1s"
        return properties
    }
}