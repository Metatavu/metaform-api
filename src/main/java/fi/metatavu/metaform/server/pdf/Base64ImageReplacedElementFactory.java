package fi.metatavu.metaform.server.pdf;

import java.io.IOException;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Element;
import org.xhtmlrenderer.extend.FSImage;
import org.xhtmlrenderer.extend.ReplacedElement;
import org.xhtmlrenderer.extend.ReplacedElementFactory;
import org.xhtmlrenderer.extend.UserAgentCallback;
import org.xhtmlrenderer.layout.LayoutContext;
import org.xhtmlrenderer.pdf.ITextFSImage;
import org.xhtmlrenderer.pdf.ITextImageElement;
import org.xhtmlrenderer.render.BlockBox;
import org.xhtmlrenderer.simple.extend.FormSubmissionListener;

import com.itextpdf.text.BadElementException;
import com.itextpdf.text.Image;

public class Base64ImageReplacedElementFactory implements ReplacedElementFactory {

  /**
   * Creates a replacement image
   *
   * @param c layout context
   * @param box block box
   * @param uac user agent callback
   * @param cssWidth css width
   * @param cssHeight css height
   * @return replaced image
   */
  public ReplacedElement createReplacedElement(LayoutContext c, BlockBox box, UserAgentCallback uac, int cssWidth, int cssHeight) {
    Element element = box.getElement();
    if (element == null) {
      return null;
    }

    String nodeName = element.getNodeName();
    if (nodeName.equals("img")) {
      
      try {
        String attribute = element.getAttribute("src");
        FSImage fsImage = buildImage(attribute, uac);

        if (fsImage != null) {
          if (cssWidth != -1 || cssHeight != -1) {
            fsImage.scale(cssWidth, cssHeight);
          }
          return new ITextImageElement(fsImage);
        }
      } catch (BadElementException | IOException e) {
        return null;
      }

    }
    return null;
  }

  protected FSImage buildImage(String srcAttr, UserAgentCallback uac) throws IOException, BadElementException {
    FSImage fsImage;
    if (srcAttr.startsWith("data:image/")) {
      String b64encoded = StringUtils.substringAfter(srcAttr, "base64,");
      fsImage = new ITextFSImage(Image.getInstance(Base64.decodeBase64(b64encoded)));
    } else {
      fsImage = uac.getImageResource(srcAttr).getImage();
    }
    return fsImage;
  }

  public void remove(Element e) {
    // Do nothing
  }

  public void reset() {
    // Do nothing
  }

  @Override
  public void setFormSubmissionListener(FormSubmissionListener listener) {
    // Do nothing
  }
}