package fi.metatavu.metaform.server.test.functional

import fi.metatavu.metaform.server.test.functional.builder.resources.*
import fi.metatavu.metaform.server.test.functional.tests.GeneralTestProfile
import fi.metatavu.metaform.server.test.functional.tests.ScriptsTestsIT
import io.quarkus.test.common.QuarkusTestResource
import io.quarkus.test.junit.QuarkusIntegrationTest
import io.quarkus.test.junit.TestProfile

/**
 * Native tests for scripts API
 */
@QuarkusIntegrationTest
@QuarkusTestResource.List(
  QuarkusTestResource(MysqlResource::class),
  QuarkusTestResource(MetaformKeycloakResource::class),
)
@TestProfile(GeneralTestProfile::class)
class NativeScriptsTestsIT: ScriptsTestsIT() {
}