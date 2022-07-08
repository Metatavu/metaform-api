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
        return properties
    }
}