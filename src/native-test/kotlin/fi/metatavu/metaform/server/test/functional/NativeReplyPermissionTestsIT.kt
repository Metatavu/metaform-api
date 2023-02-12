package fi.metatavu.metaform.server.test.functional

import fi.metatavu.metaform.server.test.functional.builder.resources.MailgunResource
import fi.metatavu.metaform.server.test.functional.builder.resources.MetaformKeycloakResource
import fi.metatavu.metaform.server.test.functional.builder.resources.MysqlResource
import fi.metatavu.metaform.server.test.functional.builder.resources.PdfRendererResource
import fi.metatavu.metaform.server.test.functional.tests.GeneralTestProfile
import fi.metatavu.metaform.server.test.functional.tests.ReplyPermissionTestsIT
import io.quarkus.test.common.QuarkusTestResource
import io.quarkus.test.junit.QuarkusIntegrationTest
import io.quarkus.test.junit.TestProfile

/**
 * Native tests for ReplyPermission
 */
@QuarkusIntegrationTest
@QuarkusTestResource.List(
    QuarkusTestResource(MysqlResource::class),
    QuarkusTestResource(MetaformKeycloakResource::class),
    QuarkusTestResource(PdfRendererResource::class),
    QuarkusTestResource(MailgunResource::class)
)
@TestProfile(GeneralTestProfile::class)
class NativeReplyPermissionTestsIT: ReplyPermissionTestsIT() {
}