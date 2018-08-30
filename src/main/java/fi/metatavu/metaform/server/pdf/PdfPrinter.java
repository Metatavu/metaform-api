package fi.metatavu.metaform.server.pdf;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.enterprise.context.ApplicationScoped;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xhtmlrenderer.pdf.ITextRenderer;
import org.xml.sax.SAXException;

import com.itextpdf.text.DocumentException;

@ApplicationScoped
public class PdfPrinter {

  /**
   * Renders html stream as pdf stream
   * 
   * @param htmlStream html stream
   * @param pdfStream pdf stream
   * @throws PdfRenderException error thrown on unsuccesfull render
   */
  public void printHtmlAsPdf(InputStream htmlStream, OutputStream pdfStream) throws PdfRenderException {
    try {
      
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
      throw new PdfRenderException("Pdf rendering failed", e);
    }
  }
  
}
