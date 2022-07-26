package fi.metatavu.metaform.server.test.functional.tests

import fi.metatavu.metaform.api.client.models.ExportTheme
import fi.metatavu.metaform.api.client.models.ExportThemeFile
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
import java.util.*

/**
 * Tests for exporting themes
 */
@QuarkusTest
@QuarkusTestResource.List(
        QuarkusTestResource(MysqlResource::class),
        QuarkusTestResource(KeycloakResource::class)
)
@TestProfile(GeneralTestProfile::class)
class ExportThemeTestsIT : AbstractTest() {
    @Test
    @Throws(Exception::class)
    fun createExportTheme() {
        TestBuilder().use { testBuilder ->
            val metaform = testBuilder.systemAdmin.exportThemes.createSimpleExportTheme()
            val childPayload = ExportTheme("child theme", null, metaform.id, "locales")
            val childTheme: ExportTheme = testBuilder.systemAdmin.exportThemes.createExportTheme(childPayload)
            Assertions.assertNotNull(childTheme)
            val foundChildTheme: ExportTheme = testBuilder.systemAdmin.exportThemes.findExportTheme(childTheme.id)
            Assertions.assertNotNull(foundChildTheme)
            Assertions.assertEquals(childTheme.toString(), foundChildTheme.toString())
            Assertions.assertEquals("child theme", foundChildTheme.name)
            Assertions.assertEquals("locales", foundChildTheme.locales)
            Assertions.assertEquals(metaform.id, foundChildTheme.parentId)
        }
    }

    @Test
    @Throws(Exception::class)
    fun findExportTheme() {
        TestBuilder().use { testBuilder ->
            val exportTheme: ExportTheme = testBuilder.systemAdmin.exportThemes.createSimpleExportTheme()
            Assertions.assertNotNull(exportTheme)
            Assertions.assertNotNull(exportTheme.name)
            val foundExportTheme: ExportTheme = testBuilder.systemAdmin.exportThemes.findExportTheme(exportTheme.id)
            Assertions.assertNotNull(foundExportTheme)
            Assertions.assertEquals(exportTheme.name, foundExportTheme.name)
        }
    }

    @Test
    @Throws(Exception::class)
    fun listExportTheme() {
        TestBuilder().use { testBuilder ->
            val exportTheme1: ExportTheme = testBuilder.systemAdmin.exportThemes.createSimpleExportTheme("theme 1")
            val exportTheme2: ExportTheme = testBuilder.systemAdmin.exportThemes.createSimpleExportTheme("theme 2")
            val exportThemes: List<ExportTheme> = testBuilder.systemAdmin.exportThemes.listExportThemes()
            Assertions.assertEquals(2, exportThemes.size)
            Assertions.assertEquals(exportTheme1.toString(), exportThemes[0].toString())
            Assertions.assertEquals(exportTheme2.toString(), exportThemes[1].toString())
        }
    }

    @Test
    @Throws(Exception::class)
    fun updateExportTheme() {
        TestBuilder().use { testBuilder ->
            val metaform = testBuilder.systemAdmin.exportThemes.createSimpleExportTheme()
            val exportTheme: ExportTheme = testBuilder.systemAdmin.exportThemes.createSimpleExportTheme("not updated")
            Assertions.assertNotNull(exportTheme)
            Assertions.assertEquals("not updated", exportTheme.name)
            Assertions.assertNull(exportTheme.locales)
            Assertions.assertNull(exportTheme.parentId)
            val newExportTheme = ExportTheme("updated", null, metaform.id, "locales")
            val updatedTheme: ExportTheme = testBuilder.systemAdmin.exportThemes.updateExportTheme(exportTheme.id, newExportTheme)
            Assertions.assertNotNull(updatedTheme)
            Assertions.assertEquals("updated", updatedTheme.name)
            Assertions.assertEquals("locales", updatedTheme.locales)
            Assertions.assertEquals(metaform.id, updatedTheme.parentId)
        }
    }

    @Test
    @Throws(Exception::class)
    fun deleteExportTheme() {
        TestBuilder().use { testBuilder ->
            val payload = ExportTheme("to be deleted", null, null, null)
            val exportTheme: ExportTheme = testBuilder.systemAdmin.exportThemes.createExportTheme(payload)
            Assertions.assertEquals(1, testBuilder.systemAdmin.exportThemes.listExportThemes().size)
            Assertions.assertEquals(testBuilder.systemAdmin.exportThemes.findExportTheme(exportTheme.id).toString(), exportTheme.toString())
            testBuilder.systemAdmin.exportThemes.deleteExportTheme(exportTheme.id!!)
            Assertions.assertEquals(0, testBuilder.systemAdmin.exportThemes.listExportThemes().size)
            testBuilder.systemAdmin.exportThemes.assertSearchFailStatus(404, exportTheme.id)
        }
    }

    @Test
    @Throws(Exception::class)
    fun createExportThemeFile() {
        TestBuilder().use { testBuilder ->
            val metaform = testBuilder.systemAdmin.exportThemes.createSimpleExportTheme()
            val exportThemeFile: ExportThemeFile = testBuilder.systemAdmin.exportFiles.createSimpleExportThemeFile(metaform.id!!, "path/to/file", "file content")
            Assertions.assertNotNull(exportThemeFile)
            Assertions.assertEquals("path/to/file", exportThemeFile.path)
            Assertions.assertEquals("file content", exportThemeFile.content)
            Assertions.assertEquals(metaform.id, exportThemeFile.themeId)
        }
    }

    @Test
    @Throws(Exception::class)
    fun findExportThemeFile() {
        TestBuilder().use { testBuilder ->
            val metaform = testBuilder.systemAdmin.exportThemes.createSimpleExportTheme()
            val createThemeFile: ExportThemeFile = testBuilder.systemAdmin.exportFiles.createSimpleExportThemeFile(metaform.id!!, "path/to/file", "file content 1")
            Assertions.assertNotNull(createThemeFile)
            val foundFile: ExportThemeFile = testBuilder.systemAdmin.exportFiles.findExportThemeFile(metaform.id, createThemeFile.id!!)
            Assertions.assertEquals(createThemeFile.toString(), foundFile.toString())
        }
    }

    @Test
    @Throws(Exception::class)
    fun listExportThemeFile() {
        TestBuilder().use { testBuilder ->
            val metaform = testBuilder.systemAdmin.exportThemes.createSimpleExportTheme()
            Assertions.assertEquals(0, testBuilder.systemAdmin.exportFiles.listExportThemeFiles(metaform.id!!).size)
            val exportThemeFile1: ExportThemeFile = testBuilder.systemAdmin.exportFiles.createSimpleExportThemeFile(metaform.id, "path/to/file1", "file content 1")
            val exportThemeFile2: ExportThemeFile = testBuilder.systemAdmin.exportFiles.createSimpleExportThemeFile(metaform.id, "path/to/file2", "file content 2")
            val themeFiles: List<ExportThemeFile> = testBuilder.systemAdmin.exportFiles.listExportThemeFiles(metaform.id)
            Assertions.assertEquals(2, themeFiles.size)
            Assertions.assertEquals(exportThemeFile1.toString(), themeFiles[0].toString())
            Assertions.assertEquals(exportThemeFile2.toString(), themeFiles[1].toString())
        }
    }

    @Test
    @Throws(Exception::class)
    fun updateExportThemeFile() {
        TestBuilder().use { testBuilder ->
            val metaform = testBuilder.systemAdmin.exportThemes.createSimpleExportTheme()
            val exportTheme = testBuilder.systemAdmin.exportFiles.createSimpleExportThemeFile(metaform.id!!, "not/updated", "not updated")
            Assertions.assertEquals("not/updated", exportTheme.path)
            Assertions.assertEquals("not updated", exportTheme.content)
            Assertions.assertEquals(metaform.id, exportTheme.themeId)
            val newThemeFile = ExportThemeFile(
                    "is/updated", exportTheme.themeId, "is updated", exportTheme.id)
            val (path1, themeId1, content1) = testBuilder.systemAdmin.exportFiles.updateExportThemeFile(metaform.id, exportTheme.id!!, newThemeFile)
            Assertions.assertEquals("is/updated", path1)
            Assertions.assertEquals("is updated", content1)
            Assertions.assertEquals(metaform.id, themeId1)
        }
    }

    @Test
    @Throws(Exception::class)
    fun deleteExportThemeFile() {
        TestBuilder().use { testBuilder ->
            val (_, id) = testBuilder.systemAdmin.exportThemes.createSimpleExportTheme()

            val payload = ExportThemeFile(
                    "to/be/deleted", id!!, "to be deleted", null)
            val exportThemeFile: ExportThemeFile = testBuilder.systemAdmin.exportFiles.createExportThemeFile(id, payload)
            Assertions.assertEquals(1, testBuilder.systemAdmin.exportFiles.listExportThemeFiles(id).size)
            Assertions.assertEquals(testBuilder.systemAdmin.exportFiles.findExportThemeFile(id, exportThemeFile.id!!).toString(), exportThemeFile.toString())
            testBuilder.systemAdmin.exportFiles.deleteExportThemeFile(id, exportThemeFile.id)
            Assertions.assertEquals(0, testBuilder.systemAdmin.exportFiles.listExportThemeFiles(id).size)
            testBuilder.systemAdmin.exportFiles.assertFindFailStatus(404, id, exportThemeFile.id)
        }
    }

    @Test
    @Throws(Exception::class)
    fun createExportThemePermission() {
        TestBuilder().use { testBuilder ->
            testBuilder.permissionTestByScopes(
                scope = PermissionScope.SYSTEM_ADMIN,
                apiCaller = { authentication: TestBuilderAuthentication, _: Int ->
                    authentication.exportThemes.createExportTheme(
                        ExportTheme(
                            name = "name",
                            parentId = null,
                            locales = null
                        )
                    )
                },
            )
        }
    }

    @Test
    @Throws(Exception::class)
    fun findExportThemePermission() {
        TestBuilder().use { testBuilder ->
            val exportTheme = testBuilder.systemAdmin.exportThemes.createExportTheme(
                ExportTheme(
                    name = "name",
                    parentId = null,
                    locales = null
                )
            )
            testBuilder.permissionTestByScopes(
                scope = PermissionScope.SYSTEM_ADMIN,
                apiCaller = { authentication: TestBuilderAuthentication, _: Int ->
                    authentication.exportThemes.findExportTheme(exportTheme.id!!)
                },
            )
        }
    }

    @Test
    @Throws(Exception::class)
    fun listExportThemePermission() {
        TestBuilder().use { testBuilder ->
            testBuilder.systemAdmin.exportThemes.createExportTheme(
                ExportTheme(
                    name = "name",
                    parentId = null,
                    locales = null
                )
            )
            testBuilder.permissionTestByScopes(
                scope = PermissionScope.SYSTEM_ADMIN,
                apiCaller = { authentication: TestBuilderAuthentication, _: Int ->
                    authentication.exportThemes.listExportThemes()
                },
            )
        }
    }

    @Test
    @Throws(Exception::class)
    fun updateExportThemePermission() {
        TestBuilder().use { testBuilder ->
            val exportTheme = testBuilder.systemAdmin.exportThemes.createExportTheme(
                ExportTheme(
                    name = "name",
                    parentId = null,
                    locales = null
                )
            )
            testBuilder.permissionTestByScopes(
                scope = PermissionScope.SYSTEM_ADMIN,
                apiCaller = { authentication: TestBuilderAuthentication, _: Int ->
                    authentication.exportThemes.updateExportTheme(exportTheme.id, exportTheme)
                },
            )
        }
    }

    @Test
    @Throws(Exception::class)
    fun deleteExportThemePermission() {
        TestBuilder().use { testBuilder ->
            testBuilder.permissionTestByScopes(
                scope = PermissionScope.SYSTEM_ADMIN,
                apiCaller = { authentication: TestBuilderAuthentication, index: Int ->
                    val exportTheme = testBuilder.systemAdmin.exportThemes.createExportTheme(
                        ExportTheme(
                            name = String.format("name%d", index),
                            parentId = null,
                            locales = null
                        )
                    )
                    authentication.exportThemes.deleteExportTheme(exportTheme.id!!,)
                },
            )
        }
    }

    @Test
    @Throws(Exception::class)
    fun createExportThemeFilePermission() {
        TestBuilder().use { testBuilder ->
            val exportTheme = testBuilder.systemAdmin.exportThemes.createSimpleExportTheme()
            testBuilder.permissionTestByScopes(
                scope = PermissionScope.SYSTEM_ADMIN,
                apiCaller = { authentication: TestBuilderAuthentication, _: Int ->
                    authentication.exportFiles.createExportThemeFile(
                        exportTheme.id!!,
                        ExportThemeFile(
                            "path",
                            exportTheme.id,
                            "content",
                            null
                        )
                    )
                },
            )
        }
    }

    @Test
    @Throws(Exception::class)
    fun findExportThemeFilePermission() {
        TestBuilder().use { testBuilder ->
            val exportTheme = testBuilder.systemAdmin.exportThemes.createSimpleExportTheme()
            val exportThemeFile = testBuilder.systemAdmin.exportFiles.createExportThemeFile(
                exportTheme.id!!,
                ExportThemeFile(
                    "path",
                    exportTheme.id,
                    "content",
                    null
                )
            )

            testBuilder.permissionTestByScopes(
                scope = PermissionScope.SYSTEM_ADMIN,
                apiCaller = { authentication: TestBuilderAuthentication, _: Int ->
                    authentication.exportFiles.findExportThemeFile(exportTheme.id, exportThemeFile.id!!)
                },
            )
        }
    }

    @Test
    @Throws(Exception::class)
    fun listExportThemeFilePermission() {
        TestBuilder().use { testBuilder ->
            val exportTheme = testBuilder.systemAdmin.exportThemes.createSimpleExportTheme()
            testBuilder.systemAdmin.exportFiles.createExportThemeFile(
                exportTheme.id!!,
                ExportThemeFile(
                    "path",
                    exportTheme.id,
                    "content",
                    null
                )
            )

            testBuilder.permissionTestByScopes(
                scope = PermissionScope.SYSTEM_ADMIN,
                apiCaller = { authentication: TestBuilderAuthentication, _: Int ->
                    authentication.exportFiles.listExportThemeFiles(exportTheme.id)
                }
            )
        }
    }

    @Test
    @Throws(Exception::class)
    fun updateExportThemeFilePermission() {
        TestBuilder().use { testBuilder ->
            val exportTheme = testBuilder.systemAdmin.exportThemes.createSimpleExportTheme()
            val exportThemeFile = testBuilder.systemAdmin.exportFiles.createExportThemeFile(
                exportTheme.id!!,
                ExportThemeFile(
                    "path",
                    exportTheme.id,
                    "content",
                    null
                )
            )

            testBuilder.permissionTestByScopes(
                scope = PermissionScope.SYSTEM_ADMIN,
                apiCaller = { authentication: TestBuilderAuthentication, _: Int ->
                    authentication.exportFiles.updateExportThemeFile(exportTheme.id, exportThemeFile.id!!, exportThemeFile)
                }
            )
        }
    }

    @Test
    @Throws(Exception::class)
    fun deleteExportThemeFilePermission() {
        TestBuilder().use { testBuilder ->
            val exportTheme = testBuilder.systemAdmin.exportThemes.createSimpleExportTheme()

            testBuilder.permissionTestByScopes(
                scope = PermissionScope.SYSTEM_ADMIN,
                apiCaller = { authentication: TestBuilderAuthentication, index: Int ->
                    val exportThemeFile = testBuilder.systemAdmin.exportFiles.createExportThemeFile(
                        exportTheme.id!!,
                        ExportThemeFile(
                            path = String.format("path%d", index),
                            themeId = exportTheme.id,
                            content = "content"
                        )
                    )
                    authentication.exportFiles.updateExportThemeFile(exportTheme.id, exportThemeFile.id!!, exportThemeFile)
                }
            )
        }
    }
}