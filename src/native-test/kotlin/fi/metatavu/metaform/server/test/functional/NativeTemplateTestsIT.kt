package fi.metatavu.metaform.server.test.functional

import fi.metatavu.metaform.server.test.functional.builder.resources.*
import fi.metatavu.metaform.server.test.functional.tests.GeneralTestProfile
import fi.metatavu.metaform.server.test.functional.tests.TemplateTestsIT
import io.quarkus.test.common.QuarkusTestResource
import io.quarkus.test.junit.QuarkusIntegrationTest
import io.quarkus.test.junit.TestProfile

/**
 * Native tests for Drafts API
 */
@QuarkusIntegrationTest
@QuarkusTestResource.List(
        QuarkusTestResource(MysqlResource::class),
        QuarkusTestResource(MetaformKeycloakResource::class),
        QuarkusTestResource(PdfRendererResource::class)
)
@TestProfile(GeneralTestProfile::class)
class NativeTemplateTestsIT: TemplateTestsIT() {
}