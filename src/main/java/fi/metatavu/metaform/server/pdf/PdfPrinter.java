package fi.metatavu.metaform.server.pdf;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import org.slf4j.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.IOUtils;
import org.w3c.dom.Document;
import org.xhtmlrenderer.pdf.ITextRenderer;
import org.xml.sax.SAXException;

import com.itextpdf.text.DocumentException;

@ApplicationScoped
public class PdfPrinter {
  
  @Inject
  private Logger logger;

  /**
   * Renders html stream as pdf stream
   * 
   * @param htmlStream html stream
   * @param pdfStream pdf stream
   * @throws PdfRenderException error thrown on unsuccesfull render
   */
  public void printHtmlAsPdf(InputStream htmlStream, OutputStream pdfStream) throws PdfRenderException {
    try {      
      printHtmlAsPdf(IOUtils.toByteArray(htmlStream), pdfStream);
    } catch (IOException e) {
      throw new PdfRenderException("Pdf rendering failed", e);
    }
  }

  /**
   * Renders html stream as pdf stream
   * 
   * @param htmlData html data
   * @param pdfStream pdf stream
   * @throws PdfRenderException error thrown on unsuccesfull render
   */
  private void printHtmlAsPdf(byte[] htmlData, OutputStream pdfStream) throws PdfRenderException {
    try (InputStream htmlStream = new ByteArrayInputStream (htmlData)) {      
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      factory.setNamespaceAware(true);
      DocumentBuilder builder = factory.newDocumentBuilder();
      Document inputDoc =  builder.parse(htmlStream);
      
      ITextRenderer renderer = new ITextRenderer();

      ChainingReplacedElementFactory chainingReplacedElementFactory = new ChainingReplacedElementFactory();
      chainingReplacedElementFactory.addReplacedElementFactory(new Base64ImageReplacedElementFactory());
      renderer.getSharedContext().setReplacedElementFactory(chainingReplacedElementFactory);

      renderer.setDocument(inputDoc, "");
      renderer.layout();
      renderer.createPDF(pdfStream);

    } catch (IOException | DocumentException | SAXException | ParserConfigurationException e) {
      String html = new String(htmlData, StandardCharsets.UTF_8);
      logger.error("Failed to render PDF from HTML {}", html);
      throw new PdfRenderException("Pdf rendering failed", e);
    }
  }
  
  
}
