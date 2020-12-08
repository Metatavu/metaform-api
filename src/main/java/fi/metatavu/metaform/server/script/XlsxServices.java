package fi.metatavu.metaform.server.script;

import java.text.DateFormat;
import java.text.ParseException;
import java.time.OffsetDateTime;
import java.util.Date;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.poi.common.usermodel.HyperlinkType;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Sheet;

import fi.metatavu.metaform.server.metaforms.ReplyController;
import fi.metatavu.metaform.server.xlsx.CellSource;
import fi.metatavu.metaform.server.xlsx.CellSourceType;
import fi.metatavu.metaform.server.xlsx.XlsxBuilder;

/**
 * Xlsx services
 * 
 * @author Antti Leppä
 */
@ApplicationScoped
public class XlsxServices {

  @Inject
  private FormRuntimeContext formRuntimeContext;

  @Inject
  private ReplyController replyController;

  /**
   * Returns sheet ids
   * 
   * @return sheet ids
   */
  public String[] getSheetIds() {
    XlsxBuilder xlsxBuilder = formRuntimeContext.getXlsxBuilder();
    return xlsxBuilder.getSheetIds().toArray(new String[0]);
  }

  /**
   * Returns sheet by sheetId
   * 
   * @param sheetId sheet id
   * @return sheet or null if not found
   */
  public Sheet getSheet(String sheetId) {
    XlsxBuilder xlsxBuilder = formRuntimeContext.getXlsxBuilder();
    return xlsxBuilder.getSheet(sheetId);
  }

  /**
   * Returns sheet by sheetId
   * 
   * @param sheetId sheet id
   * @return sheet or null if not found
   */
  public Sheet getSheetByName(String sheetName) {
    XlsxBuilder xlsxBuilder = formRuntimeContext.getXlsxBuilder();
    return xlsxBuilder.getSheetByName(sheetName);
  }

  /**
   * Returns sheet id by name
   * 
   * @param sheetName sheet name
   * @return sheet id or null if not found
   */
  public String getSheetIdByName(String sheetName) {
    XlsxBuilder xlsxBuilder = formRuntimeContext.getXlsxBuilder();
    return xlsxBuilder.getSheetIdByName(sheetName);
  }
  
  /**
   * Finds cell by sheet id, row number and column number
   * 
   * @param sheetId sheet id
   * @param rowNumber row number
   * @param columnNumber column number
   * @return cell or null if not found
   */
  public Cell getCell(String sheetId, int rowNumber, int columnNumber) {
    XlsxBuilder xlsxBuilder = formRuntimeContext.getXlsxBuilder();
    return xlsxBuilder.getCell(sheetId, rowNumber, columnNumber);
  }

  /**
   * Insert a column before given column
   * 
   * @param sheetId sheet id
   * @param referenceColumnIndex reference column index
   */
  public void insertColumnBefore(String sheetId, int referenceColumnIndex) {
    XlsxBuilder xlsxBuilder = formRuntimeContext.getXlsxBuilder();
    xlsxBuilder.insertColumnBefore(sheetId, referenceColumnIndex);
  }

  /**
   * Insert a column after given column
   * 
   * @param sheetId sheet id
   * @param referenceColumnIndex reference column index
   */
  public void insertColumnAfter(String sheetId, int referenceColumnIndex) {
    XlsxBuilder xlsxBuilder = formRuntimeContext.getXlsxBuilder();
    xlsxBuilder.insertColumnAfter(sheetId, referenceColumnIndex);
  }

  /**
   * Returns cell source
   * 
   * @param sheetId sheet id
   * @param rowNumber row number
   * @param columnNumber column number
   * @return cell source
   */
  public CellSource getCellSource(String sheetId, int rowNumber, int columnNumber) {
    XlsxBuilder xlsxBuilder = formRuntimeContext.getXlsxBuilder();
    return xlsxBuilder.getCellSource(sheetId, rowNumber, columnNumber);
  }

  /**
   * Sets a cell value
   * 
   * @param sheetId sheet id
   * @param rowNumber row number
   * @param columnNumber column number
   * @param value value as string
   * @return cell
   */
  public Cell setCellValueString(String sheetId, int rowNumber, int columnNumber, String value) {
    XlsxBuilder xlsxBuilder = formRuntimeContext.getXlsxBuilder();
    return xlsxBuilder.setCellValue(sheetId, rowNumber, columnNumber, value, createCellSource());
  }
  
  /**
   * Sets cell link
   * 
   * @param sheetId sheet id
   * @param rowNumber row number
   * @param columnNumber column number
   * @param type link type
   * @param address link address
   * @param cellText cell text
   * @return cell
   */
  public Cell setCellLink(String sheetId, int rowNumber, int columnNumber, HyperlinkType type, String address, String cellText) {
    XlsxBuilder xlsxBuilder = formRuntimeContext.getXlsxBuilder();
    return xlsxBuilder.setCellLink(sheetId, rowNumber, columnNumber, type, address, cellText, createCellSource());
  }

  /**
   * Sets cell formula
   * 
   * @param sheetId sheet id
   * @param rowNumber row number
   * @param columnNumber column number
   * @param formula formula
   * @return cell
   */
  public Cell setCellFormula(String sheetId, int rowNumber, int columnNumber, String formula) {
    XlsxBuilder xlsxBuilder = formRuntimeContext.getXlsxBuilder();
    return xlsxBuilder.setCellFormula(sheetId, rowNumber, columnNumber, formula, createCellSource());
  }

  /**
   * Returns sheet row count
   * 
   * @param sheetId sheetId
   * @return sheet row count
   */
  public int getRowCount(String sheetId) {
    XlsxBuilder xlsxBuilder = formRuntimeContext.getXlsxBuilder();
    return xlsxBuilder.getRowCount(sheetId);
  }

  /**
   * Returns cell count in a row
   * 
   * @param sheetId sheetId
   * @param rowNumber row number
   * @return cell count in a row
   */
  public int getColumnCount(String sheetId, int rowNumber) {  
    XlsxBuilder xlsxBuilder = formRuntimeContext.getXlsxBuilder();
    return xlsxBuilder.getColumnCount(sheetId, rowNumber);
  }
  
  /**
   * Creates a cell source for script generated cell
   * 
   * @return a cell source for script generated cell
   */
  private CellSource createCellSource() {
    return new CellSource(null, CellSourceType.SCRIPT);
  }
  
}
