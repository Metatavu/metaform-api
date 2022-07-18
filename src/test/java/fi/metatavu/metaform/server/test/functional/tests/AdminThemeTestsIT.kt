package fi.metatavu.metaform.server.test.functional.tests

import fi.metatavu.metaform.server.persistence.model.Metaform
import fi.metatavu.metaform.server.test.functional.AbstractTest
import fi.metatavu.metaform.server.test.functional.builder.TestBuilder
import fi.metatavu.metaform.server.test.functional.builder.resources.KeycloakResource
import fi.metatavu.metaform.server.test.functional.builder.resources.MysqlResource
import fi.metatavu.metaform.server.test.functional.tests.GeneralTestProfile
import io.quarkus.test.common.QuarkusTestResource
import io.quarkus.test.junit.QuarkusTest
import io.quarkus.test.junit.TestProfile
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.util.UUID.randomUUID

/**
 * Tests for Admin Theme API
 * @author Otto Hooper
 */
@QuarkusTest
@QuarkusTestResource.List(
        QuarkusTestResource(MysqlResource::class),
        QuarkusTestResource(KeycloakResource::class)
)
@TestProfile(GeneralTestProfile::class)
class AdminThemeTestsIT : AbstractTest() {
    @Test
    @Throws(Exception::class)
    fun createAdminThemeTest() {
        TestBuilder().use { builder ->
            val adminTheme = builder.metaformAdmin.adminThemes.create(
                    data = "data",
                    name = "Test admin theme",
                    slug = "test-admin-theme"
            )
            
            val foundAdminTheme = builder.metaformAdmin.adminThemes.findById(adminTheme.id!!)
            Assertions.assertNotNull(foundAdminTheme)
            Assertions.assertNotNull(foundAdminTheme.id)
            Assertions.assertNotNull(foundAdminTheme.data)
            Assertions.assertNotNull(foundAdminTheme.name)
            Assertions.assertNotNull(foundAdminTheme.slug)
        }
    }

    @Test
    @Throws(Exception::class)
    fun updateAdminThemeTest() {
        TestBuilder().use { builder ->
            val adminTheme = builder.metaformAdmin.adminThemes.create(
                    data = "data",
                    name = "Test admin theme",
                    slug = "test-admin-theme"
            )
            val secondAdminTheme = builder.metaformAdmin.adminThemes.create(
                    data = "data2",
                    name = "Test admin theme2",
                    slug = "test-admin-theme2"
            )

            val updatedAdminTheme = builder.metaformAdmin.adminThemes.update(adminTheme.id!!, secondAdminTheme)
            val foundAdminTheme = builder.metaformAdmin.adminThemes.findById(adminTheme.id!!)
            Assertions.assertNotNull(updatedAdminTheme)
            Assertions.assertNotNull(updatedAdminTheme.id)
            Assertions.assertNotNull(updatedAdminTheme.data)
            Assertions.assertNotNull(updatedAdminTheme.name)
            Assertions.assertNotNull(updatedAdminTheme.slug)
            Assertions.assertEquals(updatedAdminTheme.data, foundAdminTheme.data)
        }
    }
    
    @Test
    @Throws(Exception::class)
    fun listAdminThemesTest() {
        TestBuilder().use { builder ->
            val adminTheme = builder.metaformAdmin.adminThemes.create(
                    data = "data",
                    name = "Test admin theme",
                    slug = "test-admin-theme"
            )
            val secondAdminTheme = builder.metaformAdmin.adminThemes.create(
                    data = "data2",
                    name = "Test admin theme2",
                    slug = "test-admin-theme2"
            )

            Assertions.assertNotNull(adminTheme)
            Assertions.assertNotNull(secondAdminTheme)
            val adminThemes = builder.metaformAdmin.adminThemes.list()
            Assertions.assertNotNull(adminThemes)
            Assertions.assertEquals(4, adminThemes.size)
        }
    }

    @Test
    @Throws(Exception::class)
    fun deleteAdminThemeTest() {
        TestBuilder().use { builder ->
            val adminTheme = builder.metaformAdmin.adminThemes.create(
                    data = "data",
                    name = "Test admin theme",
                    slug = "test-admin-theme"
            )

            Assertions.assertNotNull(adminTheme)
            builder.metaformAdmin.adminThemes.delete(adminTheme.id!!)
            builder.metaformAdmin.adminThemes.assertSearchFailStatus(404, adminTheme.id)
        }
    }

    @Test
    @Throws(Exception::class)
    fun createAdminThemeNotFoundTest() {
        TestBuilder().use { builder ->
            val adminTheme = builder.metaformAdmin.adminThemes.create(
                    data = "data",
                    name = "Test admin theme",
                    slug = "test-admin-theme"
            )

            Assertions.assertNotNull(adminTheme)
            builder.metaformAdmin.adminThemes.assertSearchFailStatus(404, randomUUID())
        }
    }
}