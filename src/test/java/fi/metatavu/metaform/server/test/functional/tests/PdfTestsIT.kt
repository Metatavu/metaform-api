package fi.metatavu.metaform.server.test.functional.tests

import fi.metatavu.metaform.api.client.models.Metaform
import fi.metatavu.metaform.api.client.models.Reply
import fi.metatavu.metaform.server.files.File
import fi.metatavu.metaform.server.rest.ReplyMode
import fi.metatavu.metaform.server.test.functional.AbstractTest
import fi.metatavu.metaform.server.test.functional.ApiTestSettings.Companion.apiBasePath
import fi.metatavu.metaform.server.test.functional.builder.PermissionScope
import fi.metatavu.metaform.server.test.functional.builder.TestBuilder
import fi.metatavu.metaform.server.test.functional.builder.auth.TestBuilderAuthentication
import fi.metatavu.metaform.server.test.functional.builder.resources.MetaformKeycloakResource
import fi.metatavu.metaform.server.test.functional.builder.resources.MysqlResource
import fi.metatavu.metaform.server.test.functional.builder.resources.PdfRendererResource
import io.quarkus.test.common.QuarkusTestResource
import io.quarkus.test.junit.QuarkusTest
import io.quarkus.test.junit.TestProfile
import io.restassured.RestAssured
import org.apache.commons.lang3.ArrayUtils
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.util.*
import kotlin.Any
import kotlin.Array
import kotlin.Exception
import kotlin.String
import kotlin.Throws
import kotlin.arrayOf

/**
 * Tests that test Metaform replies
 */
@QuarkusTest
@QuarkusTestResource.List(
    QuarkusTestResource(MysqlResource::class),
    QuarkusTestResource(MetaformKeycloakResource::class),
    QuarkusTestResource(PdfRendererResource::class)
)
@TestProfile(GeneralTestProfile::class)
class PdfTestsIT : AbstractTest() {

    @Test
    @Throws(Exception::class)
    fun testPdfFromBaseTheme() {
        TestBuilder().use { testBuilder ->

            //val metaform = testBuilder.systemAdmin.metaforms.createFromJsonFile("simple")

            val exportTheme = testBuilder.systemAdmin.exportThemes.createSimpleExportTheme("simple")
            assertNotNull(exportTheme)

            val doc = "<!DOCTYPE html><html><head><meta charset=\"UTF-8\"></meta><title>title</title></head><body>content</body></html>"

            val classLoader = javaClass.classLoader
            val url = classLoader.getResource("resources/export-themes/base/reply/pdf.ftl")
            if (url != null) {
                testBuilder.systemAdmin.exportFiles.createSimpleExportThemeFile(exportTheme.id!!, url.path.toString(), doc)
            }



            //testBuilder.systemAdmin.exportFiles.createSimpleExportThemeFile(exportTheme.id!!, "reply/pdf.ftl", "<!DOCTYPE html><html><head><meta charset=\"UTF-8\"></meta><title>title</title></head><body>content</body></html>")

            /*
            val classLoader = javaClass.classLoader
            val url = classLoader.getResource("pdf.ftl")
            return if (url != null) {

            } else {

            }
            */

            /*
            val exportTheme = testBuilder.systemAdmin.exportThemes.createSimpleExportTheme()
            testBuilder.systemAdmin.exportFiles.createSimpleExportThemeFile(exportTheme.id!!, "reply/pdf.ftl", "<!DOCTYPE html><html><head><meta charset=\"UTF-8\"></meta><title>title</title></head><body>content</body></html>")
            */

            /*
            (AbstractTest) assertPdfContains
            */

        }
    }

    private fun testPdfFieldByType(pdfFile: File, testFieldName: String, testFieldValue: String) {

        when (testFieldName) {
            "text" -> {}
            "number" -> {}
            "email" -> {}
            "memo" -> {}
            "date" -> {}
            "time" -> {}
            "date-time" -> {}
            "radio" -> {}
            "select" -> {}
            "autocomplete" -> {}
            "table" -> {}
            "boolean" -> {}
            "checklist" -> {}
            "files" -> {}
            "html" -> {}
            "hidden" -> {}
            "submit" -> {}
            }
        }

        /*
        val document = PDDocument.load(ByteArrayInputStream(data))
        val pdfText = PDFTextStripper().getText(document)
        document.close()
        Assert.assertTrue(String.format("PDF text (%s) does not contain expected text %s", pdfText, expected), StringUtils.contains(pdfText, expected))
        */
    }
}