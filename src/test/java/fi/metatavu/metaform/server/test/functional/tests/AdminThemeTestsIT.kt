package fi.metatavu.metaform.server.test.functional.tests

import fi.metatavu.metaform.api.client.models.AdminTheme
import fi.metatavu.metaform.server.test.functional.AbstractTest
import fi.metatavu.metaform.server.test.functional.builder.TestBuilder
import fi.metatavu.metaform.server.test.functional.builder.resources.KeycloakResource
import fi.metatavu.metaform.server.test.functional.builder.resources.MysqlResource
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
            val adminTheme: AdminTheme = builder.metaformAdmin.adminThemes.create(
                    data = "data-create",
                    name = "Test admin theme",
                    slug = "test-admin-theme-create"
            )

            Assertions.assertNotNull(adminTheme)
            Assertions.assertEquals("Test admin theme", adminTheme.name)
            Assertions.assertEquals("test-admin-theme-create", adminTheme.slug)
            Assertions.assertEquals("data-create", adminTheme.data)
            
            val foundAdminTheme: AdminTheme = builder.metaformAdmin.adminThemes.findById(adminTheme.id!!)
            Assertions.assertNotNull(foundAdminTheme)
            Assertions.assertEquals(adminTheme.id, foundAdminTheme.id)
            Assertions.assertEquals(adminTheme.name, foundAdminTheme.name)
            Assertions.assertEquals(adminTheme.slug, foundAdminTheme.slug)
            
        }
    }

    @Test
    @Throws(Exception::class)
    fun updateAdminThemeTest() {
        TestBuilder().use { builder ->
            val adminTheme = builder.metaformAdmin.adminThemes.create(
                    data = "data-update",
                    name = "Test admin theme",
                    slug = "test-admin-theme-update"
            )
            val secondAdminTheme = builder.metaformAdmin.adminThemes.create(
                    data = "data2-update",
                    name = "Test admin theme2",
                    slug = "test-admin-theme2-update"
            )

            val updatedAdminTheme = builder.metaformAdmin.adminThemes.update(adminTheme.id!!, secondAdminTheme)
            Assertions.assertNotNull(updatedAdminTheme)
            Assertions.assertEquals(secondAdminTheme.data, updatedAdminTheme.data)
            Assertions.assertEquals(secondAdminTheme.name, updatedAdminTheme.name)
            Assertions.assertEquals(secondAdminTheme.slug, updatedAdminTheme.slug)
        }
    }
    
    @Test
    @Throws(Exception::class)
    fun listAdminThemesTest() {
        TestBuilder().use { builder ->
            val adminTheme: AdminTheme = builder.metaformAdmin.adminThemes.create(
                    data = "data-list",
                    name = "Test admin theme",
                    slug = "test-admin-theme-list"
            )
            val secondAdminTheme: AdminTheme = builder.metaformAdmin.adminThemes.create(
                    data = "data2-list",
                    name = "Test admin theme",
                    slug = "test-admin-theme2-list"
            )

            Assertions.assertNotNull(adminTheme)
            Assertions.assertNotNull(secondAdminTheme)
            val adminThemes: List<AdminTheme> = builder.metaformAdmin.adminThemes.list()
            Assertions.assertNotNull(adminThemes)
            Assertions.assertEquals(2, adminThemes.size)
        }
    }

    @Test
    @Throws(Exception::class)
    fun deleteAdminThemeTest() {
        TestBuilder().use { builder ->
            val adminTheme: AdminTheme = builder.metaformAdmin.adminThemes.create(
                    data = "data-delete",
                    name = "Test admin theme",
                    slug = "test-admin-theme-delete"
            )

            Assertions.assertNotNull(adminTheme)
            builder.metaformAdmin.adminThemes.delete(adminTheme.id!!)
            Assertions.assertEquals(0, builder.metaformAdmin.adminThemes.list().size)
            builder.metaformAdmin.adminThemes.assertSearchFailStatus(404, adminTheme.id)
        }
    }

    @Test
    @Throws(Exception::class)
    fun createAdminThemeNotFoundTest() {
        TestBuilder().use { builder ->
            val adminTheme: AdminTheme = builder.metaformAdmin.adminThemes.create(
                    data = "data-create-not-found",
                    name = "Test admin theme",
                    slug = "test-admin-theme-create-not-found"
            )

            Assertions.assertNotNull(adminTheme)
            builder.metaformAdmin.adminThemes.assertSearchFailStatus(404, randomUUID())
        }
    }

    @Test
    @Throws(Exception::class)
    fun testUpdateAdminThemeDuplicatedSlug() {
        TestBuilder().use { builder ->
            val adminTheme: AdminTheme = builder.metaformAdmin.adminThemes.create(
                    data = "data-update-duplicated-slug",
                    name = "Test admin theme",
                    slug = "test-admin-theme-update-duplicated-slug"
            )

            val secondAdminTheme: AdminTheme = builder.metaformAdmin.adminThemes.create(
                    data = "data2-update-duplicated-slug",
                    name = "Test admin theme2",
                    slug = "test-admin-theme-update-duplicated-slug"
            )

            Assertions.assertNotNull(adminTheme)
            builder.metaformAdmin.adminThemes.assertUpdateFailStatus(409, adminTheme.id!!, secondAdminTheme)
        }
    }
}