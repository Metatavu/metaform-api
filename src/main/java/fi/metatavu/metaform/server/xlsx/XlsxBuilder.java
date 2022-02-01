package fi.metatavu.metaform.server.xlsx;

import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

/**
 * XLSX implementation of builder
 * 
 * @author Antti Leppä
 */
public class XlsxBuilder extends AbstractXlsxBuilder<SXSSFWorkbook, SXSSFSheet> {

  /**
   * Constructor
   */
  public XlsxBuilder() {
    super(new SXSSFWorkbook(10000));
  }

}
