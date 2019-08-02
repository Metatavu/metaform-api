package fi.metatavu.metaform.server.script;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import fi.metatavu.metaform.server.metaforms.ReplyController;
import fi.metatavu.metaform.server.pdf.PdfRenderException;

/**
 * PDF services
 * 
 * @author Antti Leppä
 */
@ApplicationScoped
public class PdfServices {

  @Inject
  private FormRuntimeContext formRuntimeContext;

  @Inject
  private ReplyController replyController;
  
  /**
   * Returns reply as PDF
   * 
   * @return PDF data
   * @throws PdfRenderException thrown when rendering fails
   */
  public byte[] getReplyPdf() throws PdfRenderException {
    
    byte[] result = replyController.getReplyPdf(formRuntimeContext.getExportThemeName(), formRuntimeContext.getMetaform(), formRuntimeContext.getReply(), formRuntimeContext.getAttachmentMap(), formRuntimeContext.getLocale());
    
    System.out.println("reply pdf: " + result.length);
    
    return result;
  }
  
}
