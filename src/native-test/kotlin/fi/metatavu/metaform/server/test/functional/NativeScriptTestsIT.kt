package fi.metatavu.metaform.server.test.functional

import fi.metatavu.metaform.server.test.functional.builder.resources.*
import fi.metatavu.metaform.server.test.functional.tests.GeneralTestProfile
import fi.metatavu.metaform.server.test.functional.tests.ScriptTestsIT
import io.quarkus.test.common.QuarkusTestResource
import io.quarkus.test.junit.QuarkusIntegrationTest
import io.quarkus.test.junit.TestProfile

/**
 * Native tests for Scripts
 */
@QuarkusIntegrationTest
@QuarkusTestResource.List(
    QuarkusTestResource(MysqlResource::class),
    QuarkusTestResource(MetaformKeycloakResource::class),
    QuarkusTestResource(CardAuthKeycloakResource::class),
    QuarkusTestResource(PdfRendererResource::class),
    QuarkusTestResource(MailgunResource::class)
)
@TestProfile(GeneralTestProfile::class)
class NativeScriptTestsIT: ScriptTestsIT() {
}