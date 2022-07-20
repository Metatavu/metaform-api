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
            val adminTheme: AdminTheme = builder.metaformAdmin.adminThemes.createSimpleTheme()
            val simpleTheme = builder.metaformAdmin.adminThemes.getSimpleTheme()

            Assertions.assertNotNull(adminTheme)
            Assertions.assertEquals(simpleTheme.name, adminTheme.name)
            Assertions.assertEquals(simpleTheme.name, adminTheme.slug)
            Assertions.assertEquals(simpleTheme.data, adminTheme.data)
            
            val foundAdminTheme: AdminTheme = builder.metaformAdmin.adminThemes.findById(adminTheme.id!!)
            Assertions.assertNotNull(foundAdminTheme)
            Assertions.assertEquals(adminTheme.id, foundAdminTheme.id)
            Assertions.assertEquals(adminTheme.name, foundAdminTheme.name)
            Assertions.assertEquals(adminTheme.slug, foundAdminTheme.slug)
            
        }
    }
    // TODO same name different slug test

    @Test
    @Throws(Exception::class)
    fun createAdminThemeSameSlugTest() {
        TestBuilder().use { builder ->
            val simpleTheme: AdminTheme = builder.metaformAdmin.adminThemes.getSimpleTheme()

            val adminTheme1: AdminTheme = builder.metaformAdmin.adminThemes.createSimpleTheme()
            val adminTheme2: AdminTheme = builder.metaformAdmin.adminThemes.createSimpleTheme()

            Assertions.assertEquals(simpleTheme.name, adminTheme1.slug)
            Assertions.assertEquals(String.format("%s-%d", simpleTheme.name, 1), adminTheme2.slug)
        }
    }

    @Test
    @Throws(Exception::class)
    fun updateAdminThemeTest() {
        TestBuilder().use { builder ->
            val adminTheme = builder.metaformAdmin.adminThemes.createSimpleTheme()
            val themeData = mapOf("formData" to "updated value")
            val secondAdminTheme = AdminTheme(
                    name = "Test admin theme updated",
                    slug = "test-admin-theme-updated",
                    data = themeData
            )

            val updatedAdminTheme = builder.metaformAdmin.adminThemes.update(adminTheme.id!!, secondAdminTheme)
            Assertions.assertNotNull(updatedAdminTheme)
            val foundAdminTheme = builder.metaformAdmin.adminThemes.findById(adminTheme.id)
            Assertions.assertEquals(secondAdminTheme.data, foundAdminTheme.data)
            Assertions.assertEquals(secondAdminTheme.name, foundAdminTheme.name)
            Assertions.assertEquals(secondAdminTheme.slug, foundAdminTheme.slug)
        }
    }
    @Test
    @Throws(Exception::class)
    fun listAdminThemesTest() {
        TestBuilder().use { builder ->
            val adminTheme: AdminTheme = builder.metaformAdmin.adminThemes.createSimpleTheme()
            val secondAdminTheme: AdminTheme = builder.metaformAdmin.adminThemes.createSimpleTheme()

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
            val adminTheme: AdminTheme = builder.metaformAdmin.adminThemes.createSimpleTheme()

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
            val adminTheme: AdminTheme = builder.metaformAdmin.adminThemes.createSimpleTheme()

            Assertions.assertNotNull(adminTheme)
            builder.metaformAdmin.adminThemes.assertSearchFailStatus(404, randomUUID())
        }
    }

    @Test
    @Throws(Exception::class)
    fun createAdminThemeDuplicatedSlugTest() {
        TestBuilder().use { builder -> 
            val duplicatedSlug = "test-admin-theme-create-duplicated-slug"
            val themeData = builder.metaformAdmin.adminThemes.exampleThemeData

            builder.metaformAdmin.adminThemes.create(AdminTheme(
                    data = themeData,
                    name = "Test admin theme",
                    slug = duplicatedSlug
            ))

            builder.metaformAdmin.adminThemes.assertCreateFailStatus(
                409,
                AdminTheme(
                    name = "Test admin theme created",
                    data = themeData,
                    slug = duplicatedSlug
                )
            )
        }
    }

    @Test
    @Throws(Exception::class)
    fun updateAdminThemeDuplicatedSlugTest() {
        TestBuilder().use { builder ->
            builder.metaformAdmin.adminThemes.createSimpleTheme()

            val themeToBeUpdated = builder.metaformAdmin.adminThemes.createSimpleTheme()

            builder.metaformAdmin.adminThemes.assertUpdateFailStatus(
                409,
                themeToBeUpdated.id!!,
                builder.metaformAdmin.adminThemes.getSimpleTheme().copy(
                    slug = "simple-theme"
                )
            )
        }
    }

    @Test
    @Throws(Exception::class)
    fun createAdminThemeUnauthorizedTest() {
        TestBuilder().use { builder ->
            builder.test1.adminThemes.assertCreateFailStatus(
                403,
                builder.test1.adminThemes.getSimpleTheme()
            )
        }
    }

    @Test
    @Throws(Exception::class)
    fun updateAdminThemeUnauthorizedTest() {
        TestBuilder().use { builder ->
            val adminTheme = builder.metaformAdmin.adminThemes.createSimpleTheme()

            builder.test1.adminThemes.assertUpdateFailStatus(
                403,
                adminTheme.id!!,
                builder.metaformAdmin.adminThemes.getSimpleTheme()
            )
        }
    }

    @Test
    @Throws(Exception::class)
    fun listAdminThemesUnauthorizedTest() {
        TestBuilder().use { builder -> builder.test1.adminThemes.assertListFailStatus(403) }
    }

    @Test
    @Throws(Exception::class)
    fun findAdminThemeUnauthorizedTest() {
        TestBuilder().use { builder ->
            val adminTheme = builder.metaformAdmin.adminThemes.createSimpleTheme()

            builder.test1.adminThemes.assertFindFailStatus(403, adminTheme.id!!)
        }
    }
}