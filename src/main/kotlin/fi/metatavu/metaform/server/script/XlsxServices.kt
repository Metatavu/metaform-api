package fi.metatavu.metaform.server.script

import fi.metatavu.metaform.server.xlsx.CellSource
import fi.metatavu.metaform.server.xlsx.CellSourceType
import org.apache.poi.common.usermodel.HyperlinkType
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.util.CellReference
import javax.enterprise.context.ApplicationScoped
import javax.inject.Inject

@ApplicationScoped
class XlsxServices {

    @Inject
    lateinit var formRuntimeContext: FormRuntimeContext

    /**
     * Returns sheet ids
     *
     * @return sheet ids
     */
    fun getSheetIds(): Array<String> {
        val xlsxBuilder = formRuntimeContext.xlsxBuilder
        return xlsxBuilder.getSheetIds().toTypedArray()
    }

    /**
     * Returns sheet by sheetId
     *
     * @param sheetId sheet id
     * @return sheet or null if not found
     */
    fun getSheet(sheetId: String): Sheet? {
        val xlsxBuilder = formRuntimeContext.xlsxBuilder
        return xlsxBuilder.getSheet(sheetId)
    }

    /**
     * Returns sheet by sheetId
     *
     * @param sheetName sheet name
     * @return sheet or null if not found
     */
    fun getSheetByName(sheetName: String): Sheet? {
        val xlsxBuilder = formRuntimeContext.xlsxBuilder
        return xlsxBuilder.getSheetByName(sheetName)
    }

    /**
     * Returns sheet id by name
     *
     * @param sheetName sheet name
     * @return sheet id or null if not found
     */
    fun getSheetIdByName(sheetName: String): String? {
        val xlsxBuilder = formRuntimeContext.xlsxBuilder
        return xlsxBuilder.getSheetIdByName(sheetName)
    }

    /**
     * Finds cell by sheet id, row number and column number
     *
     * @param sheetId sheet id
     * @param rowNumber row number
     * @param columnNumber column number
     * @return cell or null if not found
     */
    fun getCell(sheetId: String, rowNumber: Int, columnNumber: Int): Cell? {
        val xlsxBuilder = formRuntimeContext.xlsxBuilder
        return xlsxBuilder.getCell(sheetId, rowNumber, columnNumber)
    }

    /**
     * Insert a column before given column
     *
     * @param sheetId sheet id
     * @param referenceColumnIndex reference column index
     */
    fun insertColumnBefore(sheetId: String, referenceColumnIndex: Int) {
        val xlsxBuilder = formRuntimeContext.xlsxBuilder
        xlsxBuilder.insertColumnBefore(sheetId, referenceColumnIndex)
    }

    /**
     * Insert a column after given column
     *
     * @param sheetId sheet id
     * @param referenceColumnIndex reference column index
     */
    fun insertColumnAfter(sheetId: String, referenceColumnIndex: Int) {
        val xlsxBuilder = formRuntimeContext.xlsxBuilder
        xlsxBuilder.insertColumnAfter(sheetId, referenceColumnIndex)
    }

    /**
     * Returns cell source
     *
     * @param sheetId sheet id
     * @param rowNumber row number
     * @param columnNumber column number
     * @return cell source
     */
    fun getCellSource(sheetId: String, rowNumber: Int, columnNumber: Int): CellSource? {
        val xlsxBuilder = formRuntimeContext.xlsxBuilder
        return xlsxBuilder.getCellSource(sheetId, rowNumber, columnNumber)
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
    fun setCellValueString(sheetId: String, rowNumber: Int, columnNumber: Int, value: String?): Cell? {
        val xlsxBuilder = formRuntimeContext.xlsxBuilder
        return xlsxBuilder.setCellValue(sheetId, rowNumber, columnNumber, value, createCellSource())
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
    fun setCellLink(
        sheetId: String,
        rowNumber: Int,
        columnNumber: Int,
        type: HyperlinkType,
        address: String,
        cellText: String
    ): Cell? {
        val xlsxBuilder = formRuntimeContext.xlsxBuilder
        return xlsxBuilder.setCellLink(sheetId, rowNumber, columnNumber, type, address, cellText, createCellSource())
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
    fun setCellFormula(sheetId: String, rowNumber: Int, columnNumber: Int, formula: String): Cell? {
        val xlsxBuilder = formRuntimeContext.xlsxBuilder
        return xlsxBuilder.setCellFormula(sheetId, rowNumber, columnNumber, formula, createCellSource())
    }

    /**
     * Returns sheet row count
     *
     * @param sheetId sheetId
     * @return sheet row count
     */
    fun getRowCount(sheetId: String): Int {
        val xlsxBuilder = formRuntimeContext.xlsxBuilder
        return xlsxBuilder.getRowCount(sheetId)
    }

    /**
     * Returns cell count in a row
     *
     * @param sheetId sheetId
     * @param rowNumber row number
     * @return cell count in a row
     */
    fun getColumnCount(sheetId: String, rowNumber: Int): Int {
        val xlsxBuilder = formRuntimeContext.xlsxBuilder
        return xlsxBuilder.getColumnCount(sheetId, rowNumber)
    }

    /**
     * Returns cell reference with sheet id
     *
     * @param sheetName sheet name
     * @param rowNumber row number
     * @param columnNumber column number
     * @return cell reference with sheet id
     */
    fun getCellReferenceWithSheet(sheetName: String, rowNumber: Int, columnNumber: Int): String {
        return CellReference(
                sheetName,
                rowNumber,
                columnNumber,
                false,
                false
        ).formatAsString(true)
    }

    /**
     * Creates a cell source for script generated cell
     *
     * @return a cell source for script generated cell
     */
    private fun createCellSource(): CellSource {
        return CellSource(null, CellSourceType.SCRIPT)
    }

}