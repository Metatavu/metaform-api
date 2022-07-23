package fi.metatavu.metaform.server.test.functional.tests

import fi.metatavu.metaform.api.client.models.ExportTheme
import fi.metatavu.metaform.api.client.models.ExportThemeFile
import fi.metatavu.metaform.server.test.functional.AbstractTest
import fi.metatavu.metaform.server.test.functional.builder.TestBuilder
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
    fun createExportThemeTest() {
        TestBuilder().use { testBuilder ->
            val metaform = testBuilder.metaformAdmin.exportThemes.createSimpleExportTheme()
            val childPayload = ExportTheme("child theme", null, metaform.id, "locales")
            val childTheme: ExportTheme = testBuilder.metaformAdmin.exportThemes.createExportTheme(childPayload)
            Assertions.assertNotNull(childTheme)
            val foundChildTheme: ExportTheme = testBuilder.metaformAdmin.exportThemes.findExportTheme(childTheme.id)
            Assertions.assertNotNull(foundChildTheme)
            Assertions.assertEquals(childTheme.toString(), foundChildTheme.toString())
            Assertions.assertEquals("child theme", foundChildTheme.name)
            Assertions.assertEquals("locales", foundChildTheme.locales)
            Assertions.assertEquals(metaform.id, foundChildTheme.parentId)
        }
    }

    @Test
    @Throws(Exception::class)
    fun findExportThemeTest() {
        TestBuilder().use { testBuilder ->
            val exportTheme: ExportTheme = testBuilder.metaformAdmin.exportThemes.createSimpleExportTheme()
            Assertions.assertNotNull(exportTheme)
            Assertions.assertNotNull(exportTheme.name)
            val foundExportTheme: ExportTheme = testBuilder.metaformAdmin.exportThemes.findExportTheme(exportTheme.id)
            Assertions.assertNotNull(foundExportTheme)
            Assertions.assertEquals(exportTheme.name, foundExportTheme.name)
        }
    }

    @Test
    @Throws(Exception::class)
    fun listExportThemeTest() {
        TestBuilder().use { testBuilder ->
            val exportTheme1: ExportTheme = testBuilder.metaformAdmin.exportThemes.createSimpleExportTheme("theme 1")
            val exportTheme2: ExportTheme = testBuilder.metaformAdmin.exportThemes.createSimpleExportTheme("theme 2")
            val exportThemes: List<ExportTheme> = testBuilder.metaformAdmin.exportThemes.listExportThemes()
            Assertions.assertEquals(2, exportThemes.size)
            Assertions.assertEquals(exportTheme1.toString(), exportThemes[0].toString())
            Assertions.assertEquals(exportTheme2.toString(), exportThemes[1].toString())
        }
    }

    @Test
    @Throws(Exception::class)
    fun updateExportThemeTest() {
        TestBuilder().use { testBuilder ->
            val metaform = testBuilder.metaformAdmin.exportThemes.createSimpleExportTheme()
            val exportTheme: ExportTheme = testBuilder.metaformAdmin.exportThemes.createSimpleExportTheme("not updated")
            Assertions.assertNotNull(exportTheme)
            Assertions.assertEquals("not updated", exportTheme.name)
            Assertions.assertNull(exportTheme.locales)
            Assertions.assertNull(exportTheme.parentId)
            val newExportTheme = ExportTheme("updated", null, metaform.id, "locales")
            val updatedTheme: ExportTheme = testBuilder.metaformAdmin.exportThemes.updateExportTheme(exportTheme.id, newExportTheme)
            Assertions.assertNotNull(updatedTheme)
            Assertions.assertEquals("updated", updatedTheme.name)
            Assertions.assertEquals("locales", updatedTheme.locales)
            Assertions.assertEquals(metaform.id, updatedTheme.parentId)
        }
    }

    @Test
    @Throws(Exception::class)
    fun deleteExportThemeTest() {
        TestBuilder().use { testBuilder ->
            val payload = ExportTheme("to be deleted", null, null, null)
            val exportTheme: ExportTheme = testBuilder.metaformAdmin.exportThemes.createExportTheme(payload)
            Assertions.assertEquals(1, testBuilder.metaformAdmin.exportThemes.listExportThemes().size)
            Assertions.assertEquals(testBuilder.metaformAdmin.exportThemes.findExportTheme(exportTheme.id).toString(), exportTheme.toString())
            testBuilder.metaformAdmin.exportThemes.deleteExportTheme(exportTheme.id!!)
            Assertions.assertEquals(0, testBuilder.metaformAdmin.exportThemes.listExportThemes().size)
            testBuilder.metaformAdmin.exportThemes.assertSearchFailStatus(404, exportTheme.id)
        }
    }

    @Test
    @Throws(Exception::class)
    fun createExportThemeFileTest() {
        TestBuilder().use { testBuilder ->
            val metaform = testBuilder.metaformAdmin.exportThemes.createSimpleExportTheme()
            val exportThemeFile: ExportThemeFile = testBuilder.metaformAdmin.exportFiles.createSimpleExportThemeFile(metaform.id!!, "path/to/file", "file content")
            Assertions.assertNotNull(exportThemeFile)
            Assertions.assertEquals("path/to/file", exportThemeFile.path)
            Assertions.assertEquals("file content", exportThemeFile.content)
            Assertions.assertEquals(metaform.id, exportThemeFile.themeId)
        }
    }

    @Test
    @Throws(Exception::class)
    fun findExportThemeFileTest() {
        TestBuilder().use { testBuilder ->
            val metaform = testBuilder.metaformAdmin.exportThemes.createSimpleExportTheme()
            val createThemeFile: ExportThemeFile = testBuilder.metaformAdmin.exportFiles.createSimpleExportThemeFile(metaform.id!!, "path/to/file", "file content 1")
            Assertions.assertNotNull(createThemeFile)
            val foundFile: ExportThemeFile = testBuilder.metaformAdmin.exportFiles.findExportThemeFile(metaform.id, createThemeFile.id!!)
            Assertions.assertEquals(createThemeFile.toString(), foundFile.toString())
        }
    }

    @Test
    @Throws(Exception::class)
    fun listExportThemeFileTest() {
        TestBuilder().use { testBuilder ->
            val metaform = testBuilder.metaformAdmin.exportThemes.createSimpleExportTheme()
            Assertions.assertEquals(0, testBuilder.metaformAdmin.exportFiles.listExportThemeFiles(metaform.id!!).size)
            val exportThemeFile1: ExportThemeFile = testBuilder.metaformAdmin.exportFiles.createSimpleExportThemeFile(metaform.id, "path/to/file1", "file content 1")
            val exportThemeFile2: ExportThemeFile = testBuilder.metaformAdmin.exportFiles.createSimpleExportThemeFile(metaform.id, "path/to/file2", "file content 2")
            val themeFiles: List<ExportThemeFile> = testBuilder.metaformAdmin.exportFiles.listExportThemeFiles(metaform.id)
            Assertions.assertEquals(2, themeFiles.size)
            Assertions.assertEquals(exportThemeFile1.toString(), themeFiles[0].toString())
            Assertions.assertEquals(exportThemeFile2.toString(), themeFiles[1].toString())
        }
    }

    @Test
    @Throws(Exception::class)
    fun updateExportThemeFileTest() {
        TestBuilder().use { testBuilder ->
            val metaform = testBuilder.metaformAdmin.exportThemes.createSimpleExportTheme()
            val exportTheme = testBuilder.metaformAdmin.exportFiles.createSimpleExportThemeFile(metaform.id!!, "not/updated", "not updated")
            Assertions.assertEquals("not/updated", exportTheme.path)
            Assertions.assertEquals("not updated", exportTheme.content)
            Assertions.assertEquals(metaform.id, exportTheme.themeId)
            val newThemeFile = ExportThemeFile(
                    "is/updated", exportTheme.themeId, "is updated", exportTheme.id)
            val (path1, themeId1, content1) = testBuilder.metaformAdmin.exportFiles.updateExportThemeFile(metaform.id, exportTheme.id!!, newThemeFile)
            Assertions.assertEquals("is/updated", path1)
            Assertions.assertEquals("is updated", content1)
            Assertions.assertEquals(metaform.id, themeId1)
        }
    }

    @Test
    @Throws(Exception::class)
    fun deleteExportThemeFileTest() {
        TestBuilder().use { testBuilder ->
            val (_, id) = testBuilder.metaformAdmin.exportThemes.createSimpleExportTheme()

            //  try {
            val payload = ExportThemeFile(
                    "to/be/deleted", id!!, "to be deleted", null)
            val exportThemeFile: ExportThemeFile = testBuilder.metaformAdmin.exportFiles.createExportThemeFile(id, payload)
            Assertions.assertEquals(1, testBuilder.metaformAdmin.exportFiles.listExportThemeFiles(id).size)
            Assertions.assertEquals(testBuilder.metaformAdmin.exportFiles.findExportThemeFile(id, exportThemeFile.id!!).toString(), exportThemeFile.toString())
            testBuilder.metaformAdmin.exportFiles.deleteExportThemeFile(id, exportThemeFile.id)
            Assertions.assertEquals(0, testBuilder.metaformAdmin.exportFiles.listExportThemeFiles(id).size)
            testBuilder.metaformAdmin.exportFiles.assertFindFailStatus(404, id, exportThemeFile.id)
        }
    }

    @Test
    @Throws(Exception::class)
    fun createExportThemePermissionDeniedTest() {
        TestBuilder().use { testBuilder ->
            val exportTheme = ExportTheme("name", null, null, null)
            testBuilder.test1.exportThemes.assertCreateFailStatus(403, exportTheme)
        }
    }

    @Test
    @Throws(Exception::class)
    fun findExportThemePermissionDeniedTest() {
        TestBuilder().use { testBuilder -> testBuilder.test1.exportThemes.assertSearchFailStatus(403, UUID.randomUUID()) }
    }

    @Test
    @Throws(Exception::class)
    fun listExportThemePermissionDeniedTest() {
        TestBuilder().use { testBuilder -> testBuilder.test1.exportThemes.assertListFailStatus(403) }
    }

    @Test
    @Throws(Exception::class)
    fun updateExportThemePermissionDeniedTest() {
        TestBuilder().use { testBuilder ->
            val exportTheme = ExportTheme("name", null, null, null)
            testBuilder.test1.exportThemes.assertUpdateFailStatus(403, UUID.randomUUID(), exportTheme)
        }
    }

    @Test
    @Throws(Exception::class)
    fun deleteExportThemePermissionDeniedTest() {
        TestBuilder().use { testBuilder -> testBuilder.test1.exportThemes.assertDeleteFailStatus(403, UUID.randomUUID()) }
    }

    @Test
    @Throws(Exception::class)
    fun createExportThemeFilePermissionDeniedTest() {
        TestBuilder().use { testBuilder ->
            val (_, id) = testBuilder.metaformAdmin.exportThemes.createSimpleExportTheme()
            val payload = ExportThemeFile("path", id!!, "content", null)
            testBuilder.test1.exportFiles.assertCreateFailStatus(403, payload)
        }
    }

    @Test
    @Throws(Exception::class)
    fun findExportThemeFilePermissionDeniedTest() {
        TestBuilder().use { testBuilder ->
            val (_, id) = testBuilder.metaformAdmin.exportThemes.createSimpleExportTheme()
            testBuilder.test1.exportFiles.assertFindExportThemeFileFailStatus(403, id, UUID.randomUUID())
        }
    }

    @Test
    @Throws(Exception::class)
    fun listExportThemeFilePermissionDeniedTest() {
        TestBuilder().use { testBuilder ->
            val (_, id) = testBuilder.metaformAdmin.exportThemes.createSimpleExportTheme()
            testBuilder.test1.exportFiles.assertListFailStatus(403, id)
        }
    }

    @Test
    @Throws(Exception::class)
    fun updateExportThemeFilePermissionDeniedTest() {
        TestBuilder().use { testBuilder ->
            val (_, id) = testBuilder.metaformAdmin.exportThemes.createSimpleExportTheme()
            val payload = ExportThemeFile("path", id!!, "content", null)
            testBuilder.test1.exportFiles.assertUpdateFailStatus(403, id, UUID.randomUUID(), payload)
        }
    }

    @Test
    @Throws(Exception::class)
    fun deleteExportThemeFilePermissionDeniedTest() {
        TestBuilder().use { testBuilder -> testBuilder.test1.exportFiles.assertDeleteFailStatus(403, UUID.randomUUID(), UUID.randomUUID()) }
    }
}