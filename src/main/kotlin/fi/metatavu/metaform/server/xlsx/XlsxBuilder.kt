package fi.metatavu.metaform.server.xlsx

import org.apache.poi.xssf.streaming.SXSSFSheet
import org.apache.poi.xssf.streaming.SXSSFWorkbook

/**
 * XLSX implementation of builder
 *
 * @author Antti Leppä
 */
class XlsxBuilder: AbstractXlsxBuilder<SXSSFWorkbook, SXSSFSheet>(SXSSFWorkbook())