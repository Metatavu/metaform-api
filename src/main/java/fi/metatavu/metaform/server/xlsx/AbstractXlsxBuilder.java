package fi.metatavu.metaform.server.xlsx;

import java.io.IOException;
import java.io.OutputStream;
import java.time.OffsetDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.poi.common.usermodel.HyperlinkType;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Hyperlink;
import org.apache.poi.ss.usermodel.Row;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Builder for Excel spreadsheets
 * 
 * @author Antti Leppä
 */
public abstract class AbstractXlsxBuilder<B extends org.apache.poi.ss.usermodel.Workbook, S extends org.apache.poi.ss.usermodel.Sheet> implements AutoCloseable {

  private static Logger logger = LoggerFactory.getLogger(AbstractXlsxBuilder.class);

  private B workbook;
  private Map<String, S> sheets;
  private Map<String, Row> rows;
  private Map<String, Cell> cells;
  private Map<String, CellSource> cellSources;
  private CellStyle dateTimeCellStyle;
  
  /**
   * Constructor
   * 
   * @param workbook instance
   */
  public AbstractXlsxBuilder(B workbook) {
    this.workbook = workbook;
    this.sheets = new HashMap<>();
    this.rows = new HashMap<>();
    this.cells = new HashMap<>();
    this.cellSources = new HashMap<>();

    CreationHelper createHelper = workbook.getCreationHelper();

    this.dateTimeCellStyle = workbook.createCellStyle();
    this.dateTimeCellStyle.setDataFormat(createHelper.createDataFormat().getFormat("DD.MM.YYYY HH:MM"));
    
    workbook.setForceFormulaRecalculation(true);
  }

  /**
   * Returns sheet ids
   * 
   * @return sheet ids
   */
  public Set<String> getSheetIds() {
    return sheets.keySet();
  }

  /**
   * Returns sheet by sheetId
   * 
   * @param sheetId sheet id
   * @return sheet or null if not found
   */
  public S getSheet(String sheetId) {
    return sheets.get(sheetId);
  }

  /**
   * Returns sheet by sheetId
   * 
   * @param sheetId sheet id
   * @return sheet or null if not found
   */
  public S getSheetByName(String sheetName) {
    return getSheetIds().stream()
      .map(this::getSheet)  
      .filter(sheet -> sheet.getSheetName().equals(sheetName))
      .findFirst()
      .orElse(null);
  }

  /**
   * Returns sheet id by name
   * 
   * @param sheetName sheet name
   * @return sheet id or null if not found
   */
  public String getSheetIdByName(String sheetName) {
    String[] ids = getSheetIds().toArray(new String[0]);

    for (int i = 0; i < ids.length; i++) {
      String id = ids[i];
      if (this.getSheet(id).getSheetName().equals(sheetName)) {
        return id;
      }
    }

    return null;
  }

  /**
   * Finds a row by sheet id and row number
   * 
   * @param sheetId sheet id
   * @param rowNumber row number
   * @return row or null if not found
   */
  public Row getRow(String sheetId, int rowNumber) {
    return rows.get(String.format("%s-%s", sheetId, rowNumber));
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
    String key = getCellKey(sheetId, rowNumber, columnNumber);
    return cells.get(key);
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
    String key = getCellKey(sheetId, rowNumber, columnNumber);
    return cellSources.get(key);
  }

  /**
   * Returns sheet row count
   * 
   * @param sheetId sheetId
   * @return sheet row count
   */
  public int getRowCount(String sheetId) {
    S sheet = this.getSheet(sheetId);
    return sheet != null ? sheet.getPhysicalNumberOfRows() : 0;
  }

  /**
   * Returns column count in a row
   * 
   * @param sheetId sheetId
   * @param rowNumber row number
   * @return column count in a row
   */
  public int getColumnCount(String sheetId, int rowNumber) {  
    Row row = getRow(sheetId, rowNumber);
    return row != null ? row.getPhysicalNumberOfCells() : 0;
  }

  /**
   * Creates new sheet
   * 
   * @param label sheet label
   * @return sheet id
   */
  @SuppressWarnings("unchecked")
  public String createSheet(String label) {
    String id = UUID.randomUUID().toString();
    sheets.put(id, (S) workbook.createSheet(label));
    return id;
  }  
  
  /**
   * Insert a column before given column
   * 
   * @param sheetId sheet id
   * @param referenceColumnIndex reference column index
   */
  public void insertColumnBefore(String sheetId, int referenceColumnIndex) {
    int rowCount = this.getRowCount(sheetId);
    for (int rowIndex = rowCount - 1; rowIndex >= 0; rowIndex--) {
      int columnCount = this.getColumnCount(sheetId, rowIndex);
      for (int oldColumnIndex = columnCount - 1; oldColumnIndex >= referenceColumnIndex; oldColumnIndex--) {
        int newColumnIndex = oldColumnIndex + 1;
        String oldCellKey = getCellKey(sheetId, rowIndex, oldColumnIndex);

        Cell oldCell = cells.get(oldCellKey);
        
        if (oldCell != null) {
          CellSource cellSource = cellSources.get(oldCellKey);
          
          Hyperlink hyperlink = oldCell.getHyperlink();
          if (hyperlink != null) {
            setCellLink(sheetId, rowIndex, newColumnIndex, hyperlink.getType(), hyperlink.getAddress(), oldCell.getStringCellValue(), cellSource);
          } else {
            switch (oldCell.getCellType()) {
              case BOOLEAN:
                setCellValue(sheetId, rowIndex, newColumnIndex, oldCell.getBooleanCellValue(), cellSource);
              break;
              case FORMULA:
                setCellFormula(sheetId, rowIndex, newColumnIndex, oldCell.getCellFormula(), cellSource);
              break;
              case NUMERIC:
                setCellValue(sheetId, rowIndex, newColumnIndex, oldCell.getNumericCellValue(), cellSource);
              break;
              case STRING:
                setCellValue(sheetId, rowIndex, newColumnIndex, oldCell.getStringCellValue(), cellSource);
              break;
              default:
              break;
            }
          }
          
          cells.remove(oldCellKey);
          cellSources.remove(oldCellKey);
          getRow(sheetId, rowIndex).removeCell(oldCell);
        }
      }
    }
  }

  /**
   * Insert a column after given column
   * 
   * @param sheetId sheet id
   * @param referenceColumnIndex reference column index
   */
  public void insertColumnAfter(String sheetId, int referenceColumnIndex) {
    insertColumnBefore(sheetId, referenceColumnIndex + 1);
  }

  /**
   * Sets a cell value
   * 
   * @param sheetId sheet id
   * @param rowNumber row number
   * @param columnNumber column number
   * @param value value as string
   * @param cellSource cell source
   * @return cell
   */
  public Cell setCellValue(String sheetId, int rowNumber, int columnNumber, String value, CellSource cellSource) {
    Cell cell = findOrCreateCell(sheetId, rowNumber, columnNumber);
    if (cell != null) {
      cell.setCellValue(value);
    }

    setCellSource(sheetId, rowNumber, columnNumber, cellSource);
    
    return cell;
  }

  /**
   * Sets a cell value
   * 
   * @param sheetId sheet id
   * @param rowNumber row number
   * @param columnNumber column number
   * @param value value as offset date time
   * @param cellSource cell source
   * @return cell
   */
  public Cell setCellValue(String sheetId, int rowNumber, int columnNumber, OffsetDateTime value, CellSource cellSource) {
    return setCellValue(sheetId, rowNumber, columnNumber, Date.from(value.toInstant()), cellSource);
  }
  
  /**
   * Sets a cell value
   * 
   * @param sheetId sheet id
   * @param rowNumber row number
   * @param columnNumber column number
   * @param value value as date
   * @param cellSource cell source
   * @return cell
   */
  public Cell setCellValue(String sheetId, int rowNumber, int columnNumber, Date value, CellSource cellSource) {
    Cell cell = findOrCreateCell(sheetId, rowNumber, columnNumber);
    if (cell != null) {
      cell.setCellValue(value);
      cell.setCellStyle(this.dateTimeCellStyle);
    }

    setCellSource(sheetId, rowNumber, columnNumber, cellSource);

    return cell;
  }

  /**
   * Sets a cell value
   * 
   * @param sheetId sheet id
   * @param rowNumber row number
   * @param columnNumber column number
   * @param value value as boolean
   * @param cellSource cell source
   * @return cell
   */
  public Cell setCellValue(String sheetId, int rowNumber, int columnNumber, Boolean value, CellSource cellSource) {
    Cell cell = findOrCreateCell(sheetId, rowNumber, columnNumber);
    if (cell != null) {
      cell.setCellValue(value);
    }
    
    setCellSource(sheetId, rowNumber, columnNumber, cellSource);
    
    return cell;
  }

  /**
   * Sets a cell value
   * 
   * @param sheetId sheet id
   * @param rowNumber row number
   * @param columnNumber column number
   * @param value value as double
   * @param cellSource cell source
   * @return cell
   */
  public Cell setCellValue(String sheetId, int rowNumber, int columnNumber, Double value, CellSource cellSource) {
    Cell cell = findOrCreateCell(sheetId, rowNumber, columnNumber);
    if (cell != null) {
      cell.setCellValue(value);
    }

    setCellSource(sheetId, rowNumber, columnNumber, cellSource);

    return cell;
  }

  /**
   * Sets a cell value
   * 
   * @param sheetId sheet id
   * @param rowNumber row number
   * @param columnNumber column number
   * @param value value as integer
   * @param cellSource cell source
   * @return cell
   */
  public Cell setCellValue(String sheetId, int rowNumber, int columnNumber, Integer value, CellSource cellSource) {
    Cell cell = findOrCreateCell(sheetId, rowNumber, columnNumber);
    if (cell != null) {
      cell.setCellValue(value);
    }

    setCellSource(sheetId, rowNumber, columnNumber, cellSource);

    return cell;
  }

  /**
   * Sets a cell value
   * 
   * @param sheetId sheet id
   * @param rowNumber row number
   * @param columnNumber column number
   * @param value value as long
   * @param cellSource cell source
   * @return cell
   */
  public Cell setCellValue(String sheetId, int rowNumber, int columnNumber, Long value, CellSource cellSource) {
    Cell cell = findOrCreateCell(sheetId, rowNumber, columnNumber);
    if (cell != null) {
      cell.setCellValue(value);
    }

    setCellSource(sheetId, rowNumber, columnNumber, cellSource);

    return cell;
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
   * @param cellSource cell source
   * @return cell
   */
  public Cell setCellLink(String sheetId, int rowNumber, int columnNumber, HyperlinkType type, String address, String cellText, CellSource cellSource) {
    Cell cell = findOrCreateCell(sheetId, rowNumber, columnNumber);
    if (cell != null) {
      Hyperlink link = workbook.getCreationHelper().createHyperlink(type);
      link.setAddress(address);
      cell.setCellValue(cellText);
      cell.setHyperlink(link);
    }

    setCellSource(sheetId, rowNumber, columnNumber, cellSource);
    
    return cell;
  }

  /**
   * Sets cell formula
   * 
   * @param sheetId sheet id
   * @param rowNumber row number
   * @param columnNumber column number
   * @param formula formula
   * @param cellSource cell source
   * @return cell
   */
  public Cell setCellFormula(String sheetId, int rowNumber, int columnNumber, String formula, CellSource cellSource) {
    Cell cell = findOrCreateCell(sheetId, rowNumber, columnNumber);
    if (cell != null) {
      cell.setCellFormula(formula);
    }
    
    setCellSource(sheetId, rowNumber, columnNumber, cellSource);
    
    return cell;
  }

  /**
   * Writes sheet into stream
   * 
   * @param stream stream
   * @throws IOException thrown when writing fails
   */
  public void write(OutputStream stream) throws IOException {
    workbook.getCreationHelper().createFormulaEvaluator().evaluateAll();
    workbook.write(stream);
  }

  @Override
  public void close() throws Exception {
    workbook.close();
  }

  /**
   * Finds or creates cell by sheet id, row number and column number
   * 
   * @param sheetId sheet id
   * @param rowNumber row number
   * @param columnNumber column number
   * @return cell
   */
  private Cell findOrCreateCell(String sheetId, int rowNumber, int columnNumber) {
    Row row = findOrCreateRow(sheetId, rowNumber);
    if (row == null) {
      return null;
    }

    String key = getCellKey(sheetId, rowNumber, columnNumber);
    if (!cells.containsKey(key)) {
      Cell cell = row.createCell(columnNumber);
      cells.put(key, cell);
    }

    return cells.get(key);
  }

  /**
   * Finds or creates row sheet id and row number
   * 
   * @param sheetId sheet id
   * @param rowNumber row number
   * @return row
   */
  private Row findOrCreateRow(String sheetId, int rowNumber) {
    S sheet = getSheet(sheetId);
    if (sheet == null) {
      logger.error("Could not find sheet {}", sheetId);
      return null;
    }

    String key = String.format("%s-%s", sheetId, rowNumber);

    if (!rows.containsKey(key)) {
      rows.put(key, sheet.createRow(rowNumber));
    }

    return rows.get(key);
  }

  /**
   * Sets cell source
   * 
   * @param sheetId sheet id
   * @param rowNumber row number
   * @param columnNumber column number
   * @param cellSource cell source
   */
  private void setCellSource(String sheetId, int rowNumber, int columnNumber, CellSource cellSource) {
    String key = getCellKey(sheetId, rowNumber, columnNumber);
    cellSources.put(key, cellSource);
  }

  /**
   * Creates a cell map key
   * 
   * @param sheetId sheet id
   * @param rowNumber row number
   * @param columnNumber column number
   * @return a cell map key
   */  
  private String getCellKey(String sheetId, int rowNumber, int columnNumber) {
    return String.format("%s-%d-%d", sheetId, rowNumber, columnNumber);
  }

}
