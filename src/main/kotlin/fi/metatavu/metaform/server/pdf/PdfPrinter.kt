package fi.metatavu.metaform.server.pdf

import fi.metatavu.metaform.server.exceptions.PdfRenderException
import org.apache.commons.io.IOUtils
import org.slf4j.Logger
// import org.xhtmlrenderer.pdf.ITextRenderer
import java.io.ByteArrayInputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.nio.charset.StandardCharsets
import javax.enterprise.context.ApplicationScoped
import javax.inject.Inject
import javax.xml.parsers.DocumentBuilderFactory

@ApplicationScoped
class PdfPrinter {
    @Inject
    lateinit var logger: Logger

    /**
     * Renders html stream as pdf stream
     *
     * @param htmlStream html stream
     * @param pdfStream pdf stream
     * @throws PdfRenderException error thrown on unsuccesfull render
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
     * @throws PdfRenderException error thrown on unsuccesfull render
     */
    @Throws(PdfRenderException::class)
    private fun printHtmlAsPdf(htmlData: ByteArray, pdfStream: OutputStream) {
        try {
            ByteArrayInputStream(htmlData).use { htmlStream ->
                val factory = DocumentBuilderFactory.newInstance()
                factory.isNamespaceAware = true
                val builder = factory.newDocumentBuilder()
                val inputDoc = builder.parse(htmlStream)
                /**
                val renderer = ITextRenderer()
                renderer.setDocument(inputDoc, "")
                renderer.layout()
                renderer.createPDF(pdfStream)
                **/
            }
        } catch (e: Exception) {
            val html = String(htmlData, StandardCharsets.UTF_8)
            logger.error("Failed to render PDF from HTML {}", html)
            throw PdfRenderException("Pdf rendering failed", e)
        }
    }

}