package fi.metatavu.metaform.server.pdf

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import fi.metatavu.metaform.server.exceptions.PdfRenderException
import org.apache.commons.io.IOUtils
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.HttpClients
import org.eclipse.microprofile.config.inject.ConfigProperty
import org.slf4j.Logger
import java.io.ByteArrayInputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.nio.charset.StandardCharsets
import javax.enterprise.context.ApplicationScoped
import javax.inject.Inject

@ApplicationScoped
class PdfPrinter {

    @Inject
    lateinit var logger: Logger

    @ConfigProperty (name = "metaform.pdf-renderer.url")
    private lateinit var pdfRendererUrl: String

    /**
     * Renders html stream as pdf stream
     *
     * @param htmlStream html stream
     * @param pdfStream pdf stream
     * @throws PdfRenderException error thrown on unsuccessful render
     */
    @Throws(PdfRenderException::class)
    fun printHtmlAsPdf(htmlStream: InputStream?, pdfStream: OutputStream) {
        try {
            printHtmlAsPdf(IOUtils.toByteArray(htmlStream), pdfStream)
        } catch (e: IOException) {
            throw PdfRenderException("Pdf rendering failed", e)
        }
    }

    /**
     * Renders html stream as pdf stream
     *
     * @param htmlData html data
     * @param pdfStream pdf stream
     * @throws PdfRenderException error thrown on unsuccessful render
     */
    @Throws(PdfRenderException::class)
    private fun printHtmlAsPdf(htmlData: ByteArray, pdfStream: OutputStream) {
        try {
            ByteArrayInputStream(htmlData).use {
                HttpClients.createDefault().use { client ->
                    val httpPost = HttpPost(pdfRendererUrl)
                    httpPost.entity = StringEntity(jacksonObjectMapper().writeValueAsString(mapOf(
                        "html" to htmlData.toString(StandardCharsets.UTF_8)
                    )))

                    client.execute(httpPost).use { response ->
                        if (response.statusLine.statusCode != 200) {
                            val errorMessage = IOUtils.toString(response.entity.content, StandardCharsets.UTF_8)
                            logger.error("Failed obtain access token: {}", errorMessage)
                            throw PdfRenderException(errorMessage)
                        } else {
                            response.entity.content.use { inputStream ->
                                IOUtils.copy(inputStream, pdfStream)
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            val html = String(htmlData, StandardCharsets.UTF_8)
            logger.error("Failed to render PDF from HTML {}", html)
            throw PdfRenderException("Pdf rendering failed", e)
        }
    }

}