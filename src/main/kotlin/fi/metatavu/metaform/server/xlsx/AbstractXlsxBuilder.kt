package fi.metatavu.metaform.server.xlsx

import org.apache.poi.common.usermodel.HyperlinkType
import org.apache.poi.ss.usermodel.*
import org.slf4j.LoggerFactory
import java.io.IOException
import java.io.OutputStream
import java.time.OffsetDateTime
import java.util.*

abstract class AbstractXlsxBuilder<B : Workbook, S : Sheet>(private var workbook: B) : AutoCloseable  {

    private val logger = LoggerFactory.getLogger(AbstractXlsxBuilder::class.java)

    private var sheets: MutableMap<String, S> = mutableMapOf()
    private var rows: MutableMap<String, Row> = mutableMapOf()
    private var cells: MutableMap<String, Cell> = mutableMapOf()
    private var cellSources: MutableMap<String, CellSource> = mutableMapOf()
    private var dateTimeCellStyle: CellStyle = workbook.createCellStyle()

    /**
     * Constructor
     */
    init {
        val createHelper = workbook.creationHelper

        dateTimeCellStyle.dataFormat = createHelper.createDataFormat().getFormat("DD.MM.YYYY HH:MM")
    }

    /**
     * Returns sheet ids
     *
     * @return sheet ids
     */
    open fun getSheetIds(): Set<String> {
        return sheets.keys
    }

    /**
     * Returns sheet by sheetId
     *
     * @param sheetId sheet id
     * @return sheet or null if not found
     */
    open fun getSheet(sheetId: String): S? {
        return sheets[sheetId]
    }

    /**
     * Returns sheet by sheetId
     *
     * @param sheetId sheet id
     * @return sheet or null if not found
     */
    open fun getSheetByName(sheetName: String): S? {
        return getSheetIds()
                .map { sheetId -> getSheet(sheetId) }
                .firstOrNull { sheet -> sheet?.sheetName == sheetName }
    }

    /**
     * Returns sheet id by name
     *
     * @param sheetName sheet name
     * @return sheet id or null if not found
     */
    open fun getSheetIdByName(sheetName: String): String? {
        val ids = getSheetIds().toTypedArray()
        for (i in ids.indices) {
            val id = ids[i]
            if (getSheet(id)?.sheetName == sheetName) {
                return id
            }
        }
        return null
    }

    /**
     * Finds a row by sheet id and row number
     *
     * @param sheetId sheet id
     * @param rowNumber row number
     * @return row or null if not found
     */
    open fun getRow(sheetId: String?, rowNumber: Int): Row? {
        return rows[String.format("%s-%s", sheetId, rowNumber)]
    }

    /**
     * Finds cell by sheet id, row number and column number
     *
     * @param sheetId sheet id
     * @param rowNumber row number
     * @param columnNumber column number
     * @return cell or null if not found
     */
    open fun getCell(sheetId: String, rowNumber: Int, columnNumber: Int): Cell? {
        val key = getCellKey(sheetId, rowNumber, columnNumber)
        return cells[key]
    }

    /**
     * Returns cell source
     *
     * @param sheetId sheet id
     * @param rowNumber row number
     * @param columnNumber column number
     * @return cell source
     */
    open fun getCellSource(sheetId: String, rowNumber: Int, columnNumber: Int): CellSource? {
        val key = getCellKey(sheetId, rowNumber, columnNumber)
        return cellSources[key]
    }

    /**
     * Returns sheet row count
     *
     * @param sheetId sheetId
     * @return sheet row count
     */
    open fun getRowCount(sheetId: String): Int {
        val sheet: S? = getSheet(sheetId)
        return if (sheet != null) sheet.lastRowNum + 1 else 0
    }

    /**
     * Returns column count in a row
     *
     * @param sheetId sheetId
     * @param rowNumber row number
     * @return column count in a row
     */
    open fun getColumnCount(sheetId: String?, rowNumber: Int): Int {
        val row = getRow(sheetId, rowNumber)
        return row?.lastCellNum?.toInt() ?: 0
    }

    /**
     * Creates new sheet
     *
     * @param label sheet label
     * @return sheet id
     */
    open fun createSheet(label: String): String {
        val id = UUID.randomUUID().toString()
        sheets[id] = workbook.createSheet(label) as S
        return id
    }

    /**
     * Insert a column before given column
     *
     * @param sheetId sheet id
     * @param referenceColumnIndex reference column index
     */
    open fun insertColumnBefore(sheetId: String, referenceColumnIndex: Int) {
        val rowCount = getRowCount(sheetId)
        for (rowIndex in rowCount - 1 downTo 0) {
            val columnCount = getColumnCount(sheetId, rowIndex)
            for (oldColumnIndex in columnCount - 1 downTo referenceColumnIndex) {
                val newColumnIndex = oldColumnIndex + 1
                val oldCellKey = getCellKey(sheetId, rowIndex, oldColumnIndex)
                val oldCell = cells[oldCellKey]
                if (oldCell != null) {
                    val cellSource = cellSources[oldCellKey]!!
                    val hyperlink = oldCell.hyperlink
                    if (hyperlink != null) {
                        setCellLink(sheetId, rowIndex, newColumnIndex, hyperlink.type, hyperlink.address, oldCell.stringCellValue, cellSource)
                    } else {
                        when (oldCell.cellType) {
                            CellType.BOOLEAN -> setCellValue(sheetId, rowIndex, newColumnIndex, oldCell.booleanCellValue, cellSource)
                            CellType.FORMULA -> setCellFormula(sheetId, rowIndex, newColumnIndex, oldCell.cellFormula, cellSource)
                            CellType.NUMERIC -> setCellValue(sheetId, rowIndex, newColumnIndex, oldCell.numericCellValue, cellSource)
                            CellType.STRING -> setCellValue(sheetId, rowIndex, newColumnIndex, oldCell.stringCellValue, cellSource)
                            else -> {}
                        }
                    }
                    cells.remove(oldCellKey)
                    cellSources.remove(oldCellKey)
                    getRow(sheetId, rowIndex)!!.removeCell(oldCell)
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
    open fun insertColumnAfter(sheetId: String, referenceColumnIndex: Int) {
        insertColumnBefore(sheetId, referenceColumnIndex + 1)
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
    open fun setCellValue(sheetId: String, rowNumber: Int, columnNumber: Int, value: String?, cellSource: CellSource): Cell? {
        val cell = findOrCreateCell(sheetId, rowNumber, columnNumber)
        cell?.setCellValue(value)
        setCellSource(sheetId, rowNumber, columnNumber, cellSource)
        return cell
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
    open fun setCellValue(sheetId: String, rowNumber: Int, columnNumber: Int, value: OffsetDateTime, cellSource: CellSource): Cell? {
        return setCellValue(sheetId, rowNumber, columnNumber, Date.from(value.toInstant()), cellSource)
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
    open fun setCellValue(sheetId: String, rowNumber: Int, columnNumber: Int, value: Date, cellSource: CellSource): Cell? {
        val cell = findOrCreateCell(sheetId, rowNumber, columnNumber)
        if (cell != null) {
            cell.setCellValue(value)
            cell.cellStyle = dateTimeCellStyle
        }
        setCellSource(sheetId, rowNumber, columnNumber, cellSource)
        return cell
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
    open fun setCellValue(sheetId: String, rowNumber: Int, columnNumber: Int, value: Boolean, cellSource: CellSource): Cell? {
        val cell = findOrCreateCell(sheetId, rowNumber, columnNumber)
        cell?.setCellValue(value)
        setCellSource(sheetId, rowNumber, columnNumber, cellSource)
        return cell
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
    open fun setCellValue(sheetId: String, rowNumber: Int, columnNumber: Int, value: Double, cellSource: CellSource): Cell? {
        val cell = findOrCreateCell(sheetId, rowNumber, columnNumber)
        cell?.setCellValue(value)
        setCellSource(sheetId, rowNumber, columnNumber, cellSource)
        return cell
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
    open fun setCellValue(sheetId: String, rowNumber: Int, columnNumber: Int, value: Int, cellSource: CellSource): Cell? {
        val cell = findOrCreateCell(sheetId, rowNumber, columnNumber)
        cell?.setCellValue(value.toDouble())
        setCellSource(sheetId, rowNumber, columnNumber, cellSource)
        return cell
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
    open fun setCellValue(sheetId: String, rowNumber: Int, columnNumber: Int, value: Long, cellSource: CellSource): Cell? {
        val cell = findOrCreateCell(sheetId, rowNumber, columnNumber)
        cell?.setCellValue(value.toDouble())
        setCellSource(sheetId, rowNumber, columnNumber, cellSource)
        return cell
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
    open fun setCellLink(sheetId: String, rowNumber: Int, columnNumber: Int, type: HyperlinkType, address: String, cellText: String, cellSource: CellSource): Cell? {
        val cell = findOrCreateCell(sheetId, rowNumber, columnNumber)
        if (cell != null) {
            val link: Hyperlink = workbook.creationHelper.createHyperlink(type)
            link.address = address
            cell.setCellValue(cellText)
            cell.hyperlink = link
        }
        setCellSource(sheetId, rowNumber, columnNumber, cellSource)
        return cell
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
    open fun setCellFormula(sheetId: String, rowNumber: Int, columnNumber: Int, formula: String, cellSource: CellSource): Cell? {
        val cell = findOrCreateCell(sheetId, rowNumber, columnNumber)
        if (cell != null) {
            cell.cellFormula = formula
        }
        setCellSource(sheetId, rowNumber, columnNumber, cellSource)
        return cell
    }

    /**
     * Writes sheet into stream
     *
     * @param stream stream
     * @throws IOException thrown when writing fails
     */
    @Throws(IOException::class)
    open fun write(stream: OutputStream?) {
        workbook.creationHelper.createFormulaEvaluator().evaluateAll()
        workbook.write(stream)
    }

    @Throws(Exception::class)
    override fun close() {
        workbook.close()
    }

    /**
     * Finds or creates cell by sheet id, row number and column number
     *
     * @param sheetId sheet id
     * @param rowNumber row number
     * @param columnNumber column number
     * @return cell
     */
    private fun findOrCreateCell(sheetId: String, rowNumber: Int, columnNumber: Int): Cell? {
        val row = findOrCreateRow(sheetId, rowNumber) ?: return null
        val key = getCellKey(sheetId, rowNumber, columnNumber)
        if (!cells.containsKey(key)) {
            val cell = row.createCell(columnNumber)
            cells[key] = cell
        }
        return cells[key]
    }

    /**
     * Finds or creates row sheet id and row number
     *
     * @param sheetId sheet id
     * @param rowNumber row number
     * @return row
     */
    private fun findOrCreateRow(sheetId: String, rowNumber: Int): Row? {
        val sheet: S? = getSheet(sheetId)
        if (sheet == null) {
            logger.error("Could not find sheet {}", sheetId)
            return null
        }
        val key = String.format("%s-%s", sheetId, rowNumber)
        if (!rows.containsKey(key)) {
            rows[key] = sheet.createRow(rowNumber)
        }
        return rows[key]
    }

    /**
     * Sets cell source
     *
     * @param sheetId sheet id
     * @param rowNumber row number
     * @param columnNumber column number
     * @param cellSource cell source
     */
    private fun setCellSource(sheetId: String, rowNumber: Int, columnNumber: Int, cellSource: CellSource) {
        val key = getCellKey(sheetId, rowNumber, columnNumber)
        cellSources[key] = cellSource
    }

    /**
     * Creates a cell map key
     *
     * @param sheetId sheet id
     * @param rowNumber row number
     * @param columnNumber column number
     * @return a cell map key
     */
    private fun getCellKey(sheetId: String, rowNumber: Int, columnNumber: Int): String {
        return String.format("%s-%d-%d", sheetId, rowNumber, columnNumber)
    }
}