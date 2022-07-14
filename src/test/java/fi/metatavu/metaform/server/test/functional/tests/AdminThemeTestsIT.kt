import fi.metatavu.metaform.server.persistence.model.Metaform
import fi.metatavu.metaform.server.test.functional.AbstractTest
import fi.metatavu.metaform.server.test.functional.builder.TestBuilder
import fi.metatavu.metaform.server.test.functional.builder.resources.KeycloakResource
import fi.metatavu.metaform.server.test.functional.builder.resources.MysqlResource
import fi.metatavu.metaform.server.test.functional.tests.GeneralTestProfile
import io.quarkus.test.common.QuarkusTestResource
import io.quarkus.test.junit.QuarkusTest
import io.quarkus.test.junit.TestProfile
import org.junit.Assert.assertNotNull
import org.junit.Test

@QuarkusTest
@QuarkusTestResource.List(
        QuarkusTestResource(MysqlResource::class),
        QuarkusTestResource(KeycloakResource::class)
)
@TestProfile(GeneralTestProfile::class)
class AdminThemeTestsIT : AbstractTest() {
    @Test
    @Throws(Exception::class)
    // Create admin theme (id: UUID, data: Any?, name: String, slug: String, creatorId: UUID, lastModifierId: UUID)
    fun createAdminThemeTest() {
        TestBuilder().use { builder ->
            val adminTheme = builder.metaformAdmin.adminThemes.create(
                    data = "",
                    name = "Test admin theme",
                    slug = "test-admin-theme"
            )
        }
    }
}