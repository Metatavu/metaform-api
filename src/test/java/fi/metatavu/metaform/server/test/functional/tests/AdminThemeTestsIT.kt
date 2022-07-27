package fi.metatavu.metaform.server.test.functional.tests

import fi.metatavu.metaform.api.client.models.AdminTheme
import fi.metatavu.metaform.server.test.functional.AbstractTest
import fi.metatavu.metaform.server.test.functional.builder.PermissionScope
import fi.metatavu.metaform.server.test.functional.builder.TestBuilder
import fi.metatavu.metaform.server.test.functional.builder.auth.TestBuilderAuthentication
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
    fun createAdminTheme() {
        TestBuilder().use { builder ->
            val adminTheme: AdminTheme = builder.systemAdmin.adminThemes.createSimpleTheme()
            val simpleTheme = builder.systemAdmin.adminThemes.getSimpleTheme()

            Assertions.assertNotNull(adminTheme)
            Assertions.assertEquals(simpleTheme.name, adminTheme.name)
            Assertions.assertEquals(simpleTheme.name, adminTheme.slug)
            Assertions.assertEquals(simpleTheme.data, adminTheme.data)
            
            val foundAdminTheme: AdminTheme = builder.systemAdmin.adminThemes.findById(adminTheme.id!!)
            Assertions.assertNotNull(foundAdminTheme)
            Assertions.assertEquals(adminTheme.id, foundAdminTheme.id)
            Assertions.assertEquals(adminTheme.name, foundAdminTheme.name)
            Assertions.assertEquals(adminTheme.slug, foundAdminTheme.slug)
            
        }
    }

    @Test
    @Throws(Exception::class)
    fun createAdminThemeSameSlug() {
        TestBuilder().use { builder ->
            val simpleTheme: AdminTheme = builder.systemAdmin.adminThemes.getSimpleTheme()

            val adminTheme1: AdminTheme = builder.systemAdmin.adminThemes.createSimpleTheme()
            val adminTheme2: AdminTheme = builder.systemAdmin.adminThemes.createSimpleTheme()

            Assertions.assertEquals(simpleTheme.name, adminTheme1.slug)
            Assertions.assertEquals(String.format("%s-%d", simpleTheme.name, 1), adminTheme2.slug)
        }
    }


    @Test
    @Throws(Exception::class)
    fun createAdminThemeNotFound() {
        TestBuilder().use { builder ->
            val adminTheme: AdminTheme = builder.systemAdmin.adminThemes.createSimpleTheme()

            Assertions.assertNotNull(adminTheme)
            builder.systemAdmin.adminThemes.assertSearchFailStatus(404, randomUUID())
        }
    }

    @Test
    @Throws(Exception::class)
    fun createAdminThemeDuplicatedSlug() {
        TestBuilder().use { builder ->
            val duplicatedSlug = "test-admin-theme-create-duplicated-slug"
            val themeData = builder.systemAdmin.adminThemes.exampleThemeData

            builder.systemAdmin.adminThemes.create(AdminTheme(
                data = themeData,
                name = "Test admin theme",
                slug = duplicatedSlug
            ))

            builder.systemAdmin.adminThemes.assertCreateFailStatus(
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
    fun createAdminThemePermission() {
        TestBuilder().use { testBuilder ->
            testBuilder.permissionTestByScopes(
                scope = PermissionScope.SYSTEM_ADMIN,
                apiCaller = { authentication: TestBuilderAuthentication, _: Int ->
                    authentication.adminThemes.createSimpleTheme()
                }
            )
        }
    }

    @Test
    @Throws(Exception::class)
    fun findAdminThemePermission() {
        TestBuilder().use { testBuilder ->
            val adminTheme = testBuilder.systemAdmin.adminThemes.createSimpleTheme()

            testBuilder.permissionTestByScopes(
                scope = PermissionScope.METAFORM_MANAGER,
                apiCaller = { authentication: TestBuilderAuthentication, _: Int ->
                    authentication.adminThemes.findById(adminTheme.id!!)
                }
            )
        }
    }

    @Test
    @Throws(Exception::class)
    fun updateAdminTheme() {
        TestBuilder().use { builder ->
            val adminTheme = builder.systemAdmin.adminThemes.createSimpleTheme()
            val themeData = mapOf("formData" to "updated value")
            val secondAdminTheme = AdminTheme(
                    name = "Test admin theme updated",
                    slug = "test-admin-theme-updated",
                    data = themeData
            )

            val updatedAdminTheme = builder.systemAdmin.adminThemes.update(adminTheme.id!!, secondAdminTheme)
            Assertions.assertNotNull(updatedAdminTheme)
            val foundAdminTheme = builder.systemAdmin.adminThemes.findById(adminTheme.id)
            Assertions.assertEquals(secondAdminTheme.data, foundAdminTheme.data)
            Assertions.assertEquals(secondAdminTheme.name, foundAdminTheme.name)
            Assertions.assertEquals(secondAdminTheme.slug, foundAdminTheme.slug)
        }
    }

    @Test
    @Throws(Exception::class)
    fun updateAdminThemeDuplicatedSlug() {
        TestBuilder().use { builder ->
            builder.systemAdmin.adminThemes.createSimpleTheme()

            val themeToBeUpdated = builder.systemAdmin.adminThemes.createSimpleTheme()

            builder.systemAdmin.adminThemes.assertUpdateFailStatus(
                409,
                themeToBeUpdated.id!!,
                builder.systemAdmin.adminThemes.getSimpleTheme().copy(
                    slug = "simple-theme"
                )
            )
        }
    }

    @Test
    @Throws(Exception::class)
    fun updateAdminThemePermission() {
        TestBuilder().use { testBuilder ->
            val adminTheme = testBuilder.systemAdmin.adminThemes.createSimpleTheme()

            testBuilder.permissionTestByScopes(
                scope = PermissionScope.SYSTEM_ADMIN,
                apiCaller = { authentication: TestBuilderAuthentication, _: Int ->
                    authentication.adminThemes.update(
                        adminTheme.id!!,
                        adminTheme
                    )
                }
            )
        }
    }

    @Test
    @Throws(Exception::class)
    fun listAdminThemes() {
        TestBuilder().use { builder ->
            val adminTheme: AdminTheme = builder.systemAdmin.adminThemes.createSimpleTheme()
            val secondAdminTheme: AdminTheme = builder.systemAdmin.adminThemes.createSimpleTheme()

            Assertions.assertNotNull(adminTheme)
            Assertions.assertNotNull(secondAdminTheme)
            val adminThemes: List<AdminTheme> = builder.systemAdmin.adminThemes.list()
            Assertions.assertNotNull(adminThemes)
            Assertions.assertEquals(2, adminThemes.size)
        }
    }

    @Test
    @Throws(Exception::class)
    fun listAdminThemePermission() {
        TestBuilder().use { testBuilder ->
            testBuilder.systemAdmin.adminThemes.createSimpleTheme()

            testBuilder.permissionTestByScopes(
                scope = PermissionScope.SYSTEM_ADMIN,
                apiCaller = { authentication: TestBuilderAuthentication, _: Int ->
                    authentication.adminThemes.list()
                }
            )
        }
    }

    @Test
    @Throws(Exception::class)
    fun deleteAdminTheme() {
        TestBuilder().use { builder ->
            val adminTheme: AdminTheme = builder.systemAdmin.adminThemes.createSimpleTheme()

            Assertions.assertNotNull(adminTheme)
            builder.systemAdmin.adminThemes.delete(adminTheme.id!!)
            Assertions.assertEquals(0, builder.systemAdmin.adminThemes.list().size)
            builder.systemAdmin.adminThemes.assertSearchFailStatus(404, adminTheme.id)
        }
    }

    @Test
    @Throws(Exception::class)
    fun deleteAdminThemePermission() {
        TestBuilder().use { testBuilder ->
            val simpleTheme = testBuilder.systemAdmin.adminThemes.getSimpleTheme()

            testBuilder.permissionTestByScopes(
                scope = PermissionScope.SYSTEM_ADMIN,
                apiCaller = { authentication: TestBuilderAuthentication, index: Int ->
                    val adminTheme = testBuilder.systemAdmin.adminThemes.create(simpleTheme.copy(
                        name = String.format("theme%d", index)
                    ))
                    authentication.adminThemes.delete(adminTheme.id!!)
                },
                successStatus = 204
            )
        }
    }
}