package fi.metatavu.metaform.server.controllers

import fi.metatavu.metaform.api.spec.model.*
import fi.metatavu.metaform.server.exceptions.PdfRenderException
import fi.metatavu.metaform.server.exceptions.XlsxException
import fi.metatavu.metaform.server.exportTheme.ExportThemeFreemarkerRenderer
import fi.metatavu.metaform.server.exportTheme.ReplyExportDataModel
import fi.metatavu.metaform.server.utils.MetaformUtils
import fi.metatavu.metaform.server.metaform.FieldFilters
import fi.metatavu.metaform.server.pdf.PdfPrinter
import fi.metatavu.metaform.server.persistence.dao.*
import fi.metatavu.metaform.server.persistence.model.*
import fi.metatavu.metaform.server.persistence.model.Attachment
import fi.metatavu.metaform.server.persistence.model.Metaform
import fi.metatavu.metaform.server.persistence.model.Reply
import fi.metatavu.metaform.server.rest.ReplyMode
import fi.metatavu.metaform.server.script.FormRuntimeContext
import fi.metatavu.metaform.server.xlsx.CellSource
import fi.metatavu.metaform.server.xlsx.CellSourceType
import fi.metatavu.metaform.server.xlsx.XlsxBuilder
import org.apache.commons.io.IOUtils
import org.apache.commons.lang3.ArrayUtils
import org.apache.commons.lang3.StringUtils
import org.apache.commons.lang3.math.NumberUtils
import org.apache.poi.common.usermodel.HyperlinkType
import org.apache.poi.ss.util.CellReference
import org.slf4j.Logger
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.security.PrivateKey
import java.security.PublicKey
import java.time.OffsetDateTime
import java.util.*
import java.util.function.Consumer
import jakarta.enterprise.context.ApplicationScoped
import jakarta.enterprise.event.Event
import jakarta.inject.Inject

/**
 * Controller for Replies
 */
@ApplicationScoped
class ReplyController {

    @Inject
    lateinit var logger: Logger

    @Inject
    lateinit var replyDAO: ReplyDAO

    @Inject
    lateinit var anyReplyFieldDAO: AnyReplyFieldDAO

    @Inject
    lateinit var stringReplyFieldDAO: StringReplyFieldDAO

    @Inject
    lateinit var booleanReplyFieldDAO: BooleanReplyFieldDAO

    @Inject
    lateinit var numberReplyFieldDAO: NumberReplyFieldDAO

    @Inject
    lateinit var listReplyFieldDAO: ListReplyFieldDAO

    @Inject
    lateinit var listReplyFieldItemDAO: ListReplyFieldItemDAO

    @Inject
    lateinit var attachmentReplyFieldDAO: AttachmentReplyFieldDAO

    @Inject
    lateinit var attachmentReplyFieldItemDAO: AttachmentReplyFieldItemDAO

    @Inject
    lateinit var tableReplyFieldDAO: TableReplyFieldDAO

    @Inject
    lateinit var tableReplyFieldNumberRowCellDAO: TableReplyFieldNumberRowCellDAO

    @Inject
    lateinit var anyTableReplyFieldRowCellDAO: AnyTableReplyFieldRowCellDAO

    @Inject
    lateinit var tableReplyFieldRowDAO: TableReplyFieldRowDAO

    @Inject
    lateinit var tableReplyFieldStringRowCellDAO: TableReplyFieldStringRowCellDAO

    @Inject
    lateinit var attachmentController: AttachmentController

    @Inject
    lateinit var fileController: FileController

    @Inject
    lateinit var exportThemeFreemarkerRenderer: ExportThemeFreemarkerRenderer

    @Inject
    lateinit var pdfPrinter: PdfPrinter

    @Inject
    lateinit var cryptoController: CryptoController

    @Inject
    lateinit var formRuntimeContext: FormRuntimeContext

    @Inject
    lateinit var replyCreatedEvent: Event<ReplyCreatedEvent>

    @Inject
    lateinit var replyDeletedEvent: Event<ReplyDeletedEvent>

    @Inject
    lateinit var replyUpdatedEvent: Event<ReplyUpdatedEvent>

    @Inject
    lateinit var replyFoundEvent: Event<ReplyFoundEvent>

    /**
     * Creates new reply
     *
     * @param userId user id
     * @param metaform Metaform
     * @param privateKey private key
     * @return Created reply
     */
    fun createReply(
        userId: UUID,
        metaform: Metaform,
        privateKey: PrivateKey?,
        loggedUserId: UUID
    ): Reply {
        val id = UUID.randomUUID()
        return replyDAO.create(
                id = id,
                userId = userId,
                metaform = metaform,
                resourceId = null,
                privateKey = privateKey?.encoded,
                revision = null,
                lastModifierId = loggedUserId
        )
    }

    /**
     * Lists field names used in reply
     *
     * @param reply reply
     * @return field names used in reply
     */
    fun listFieldNames(reply: Reply): List<String> {
        return anyReplyFieldDAO.listNamesByReply(reply)
    }

    /**
     * Finds reply by id
     *
     * @param replyId replyId
     * @return reply
     */
    fun findReplyById(replyId: UUID): Reply? {
        return replyDAO.findById(replyId)
    }

    /**
     * Fields active (non revisioned) reply by metaform and user id
     *
     * @param metaform metaform
     * @param userId user id
     * @return found reply
     */
    fun findActiveReplyByMetaformAndUserId(metaform: Metaform, userId: UUID): Reply? {
        return replyDAO.findByMetaformAndUserIdAndRevisionNull(metaform, userId)
    }

    /**
     * Updates reply authorization resource id
     *
     * @param reply reply
     * @param resourceId authorization resource id
     * @return reply
     */
    fun updateResourceId(reply: Reply, resourceId: UUID?): Reply {
        replyDAO.updateResourceId(reply, resourceId)
        return reply
    }

    /**
     * Updates reply last modifier id
     *
     * @param reply reply
     * @param lastModifierId lastModifierId
     * @return Updated Reply
     */
    fun updateReplyLastModifierId(reply: Reply, lastModifierId: UUID): Reply {
        return replyDAO.updateLastModifierId(reply, lastModifierId)
    }

    /**
     * Validates field value
     *
     * @param metaformField field
     * @param value value
     * @return whether value is valid or not
     */
    fun isValidFieldValue(metaformField: MetaformField, value: Any?): Boolean {
        return if (metaformField.type === MetaformFieldType.TABLE) {
            val columnMap = getTableColumnMap(metaformField)
            getTableValue(columnMap, value) != null
        } else true
    }


    /**
     * Sets reply field value
     *
     * @param reply reply
     * @param name name
     * @param value value
     * @return updated field
     */
    fun setReplyField(field: MetaformField, reply: Reply, name: String, value: Any): ReplyField? {
        return when (field.type) {
            MetaformFieldType.FILES -> setFileReplyField(reply, name, value)
            MetaformFieldType.TABLE -> setTableReplyField(field, reply, name, value)
            else -> when (value) {
                is Boolean -> setReplyField(reply, name, value)
                is Number -> setReplyField(reply, name, value)
                is String -> setReplyField(reply, name, value)
                is List<*> -> setReplyField(reply, name, value)
                else -> {
                    if (logger.isErrorEnabled) {
                        logger.error(String.format(
                            "Unsupported to type (%s) for field %s in reply %s",
                            value.javaClass.name,
                            name,
                            reply.id.toString()
                        ))
                    }
                    null
                }
            }
        }
    }

    /**
     * Sets reply field value
     *
     * @param reply reply
     * @param name name
     * @param value value
     * @return updated field
     */
    fun setReplyField(reply: Reply, name: String, value: String): ReplyField {
        var replyField = anyReplyFieldDAO.findByReplyAndName(reply, name)
        if (replyField != null && replyField !is StringReplyField) {
            deleteField(replyField)
            replyField = null
        }
        return if (replyField == null) {
            stringReplyFieldDAO.create(UUID.randomUUID(), reply, name, value)
        } else {
            stringReplyFieldDAO.updateValue((replyField as StringReplyField), value)
        }
    }

    /**
     * Sets reply field value
     *
     * @param reply reply
     * @param name name
     * @param value value
     * @return updated field
     */
    fun setReplyField(reply: Reply, name: String, value: Boolean): ReplyField {
        var replyField = anyReplyFieldDAO.findByReplyAndName(reply, name)
        if (replyField != null && replyField !is BooleanReplyField) {
            deleteField(replyField)
            replyField = null
        }
        return if (replyField == null) {
            booleanReplyFieldDAO.create(UUID.randomUUID(), reply, name, value)
        } else {
            booleanReplyFieldDAO.updateValue((replyField as BooleanReplyField), value)
        }
    }

    /**
     * Sets reply field value
     *
     * @param reply reply
     * @param name name
     * @param value value
     * @return updated field
     */
    fun setReplyField(reply: Reply, name: String, value: Number): ReplyField {
        var replyField = anyReplyFieldDAO.findByReplyAndName(reply, name)
        if (replyField != null && replyField !is NumberReplyField) {
            deleteField(replyField)
            replyField = null
        }
        return if (replyField == null) {
            numberReplyFieldDAO.create(UUID.randomUUID(), reply, name, value.toDouble())
        } else {
            numberReplyFieldDAO.updateValue((replyField as NumberReplyField), value.toDouble())
        }
    }

    /**
     * Sets reply field value
     *
     * @param reply reply
     * @param name name
     * @param value values
     * @return updated field
     */
    fun setFileReplyField(reply: Reply, name: String, value: Any): ReplyField? {
        val fileRefs = if (value is List<*>) {
            value as List<String>
        } else {
            listOf(value.toString())
        }

        var replyField = anyReplyFieldDAO.findByReplyAndName(reply, name)
        if (replyField != null && replyField !is AttachmentReplyField) {
            deleteField(replyField)
            replyField = null
        }
        var attachmentReplyField = replyField as AttachmentReplyField?
        val items: List<AttachmentReplyFieldItem>
        if (attachmentReplyField == null) {
            items = emptyList()
            attachmentReplyField = attachmentReplyFieldDAO.create(UUID.randomUUID(), reply, name)
        } else {
            items = attachmentReplyFieldItemDAO.listByField(attachmentReplyField)
            removeUnusedAttachmentReplyItems(items, fileRefs)
        }
        attachUsedReplyItems(reply.userId, attachmentReplyField, items, fileRefs)
        return attachmentReplyField
    }

    /**
     * Deletes specified fields from the reply
     *
     * @param reply reply
     * @param fieldNames field names to be deleted
     */
    fun deleteReplyFields(reply: Reply, fieldNames: List<String>) {
        fieldNames.forEach { fieldName -> anyReplyFieldDAO.findByReplyAndName(reply, fieldName)?.let { deleteField(it) } }
    }

    /**
     * Deletes a reply
     *
     * @param reply reply
     */
    fun deleteReply(reply: Reply) {
        anyReplyFieldDAO.listByReply(reply).forEach { deleteField(it) }
        replyDAO.delete(reply)
    }

    /**
     * Lists replies
     *
     * @param metaform Metaform
     * @param userId userId
     * @param createdBefore filter results by created before specified time.
     * @param createdAfter filter results by created after specified time.
     * @param modifiedBefore filter results by modified before specified time.
     * @param modifiedAfter filter results by modified after specified time.
     * @param includeRevisions
     * @param orderBy criteria to order by
     * @param latestFirst return the latest result first according to the criteria in orderBy
     * @return replies list of replies
     */
    fun listReplies(
            metaform: Metaform? = null,
            userId: UUID? = null,
            createdBefore: OffsetDateTime? = null,
            createdAfter: OffsetDateTime? = null,
            modifiedBefore: OffsetDateTime? = null,
            modifiedAfter: OffsetDateTime? = null,
            includeRevisions: Boolean,
            fieldFilters: FieldFilters? = null,
            firstResult: Int? = null,
            maxResults: Int? = null,
            orderBy: ReplyOrderCriteria? = null,
            latestFirst: Boolean? = null
    ): List<Reply> {
        // mimic the original behavior
        val orderByReal = orderBy ?: ReplyOrderCriteria.CREATED
        val latestFirstReal = latestFirst ?: false

        return replyDAO.list(
                metaform,
                userId,
                includeRevisions,
                createdBefore,
                createdAfter,
                modifiedBefore,
                modifiedAfter,
                fieldFilters,
                firstResult,
                maxResults,
                orderByReal,
                latestFirstReal
        )
    }

    /**
     * Returns count of replies affected by filters
     *
     * @param metaform Metaform
     * @param userId userId
     * @param createdBefore filter results by created before specified time.
     * @param createdAfter filter results by created after specified time.
     * @param modifiedBefore filter results by modified before specified time.
     * @param modifiedAfter filter results by modified after specified time.
     * @param includeRevisions
     * @param fieldFilters field filters
     * @return Long Count of replies
     */
    fun countReplies(
            metaform: Metaform? = null,
            userId: UUID? = null,
            createdBefore: OffsetDateTime? = null,
            createdAfter: OffsetDateTime? = null,
            modifiedBefore: OffsetDateTime? = null,
            modifiedAfter: OffsetDateTime? = null,
            includeRevisions: Boolean,
            fieldFilters: FieldFilters? = null
    ): Long {
        return replyDAO.count(
                metaform = metaform,
                userId = userId,
                includeRevisions = includeRevisions,
                createdBefore = createdBefore,
                createdAfter = createdAfter,
                modifiedBefore = modifiedBefore,
                modifiedAfter = modifiedAfter,
                fieldFilters = fieldFilters
        )
    }

    /**
     * Lists reply fields by reply
     *
     * @param reply reply
     * @return reply fields
     */
    fun listReplyFields(reply: Reply): List<ReplyField> {
        return anyReplyFieldDAO.listByReply(reply)
    }

    /**
     * Renders Reply as PDF document
     *
     * @param metaformEntity Metaform
     * @param replyEntity Reply
     * @param locale locale
     * @return Pdf bytes
     * @throws PdfRenderException throw when rendering fails
     */
    @Throws(PdfRenderException::class)
    fun getReplyPdf(
            exportThemeName: String,
            metaformEntity: fi.metatavu.metaform.api.spec.model.Metaform,
            replyEntity: fi.metatavu.metaform.api.spec.model.Reply,
            attachmentMap: Map<String, fi.metatavu.metaform.api.spec.model.Attachment>,
            locale: Locale
    ): ByteArray {
        val dataModel = ReplyExportDataModel(metaformEntity, replyEntity, attachmentMap, getDate(replyEntity.createdAt), getDate(replyEntity.modifiedAt))
        val html = exportThemeFreemarkerRenderer.render(String.format("%s/reply/pdf.ftl", exportThemeName), dataModel, locale)
            ?.replace("ä", "&auml;")
            ?.replace("ö", "&ouml;")
            ?.replace("ü", "&uuml;")
            ?.replace("Ä", "&Auml;")
            ?.replace("Ö", "&Ouml;")
            ?.replace("Ü", "&Uuml;")
            ?.replace("ß", "&szlig;")

        try {
            IOUtils.toInputStream(html, StandardCharsets.UTF_8).use { htmlStream ->
                ByteArrayOutputStream().use { pdfStream ->
                    pdfPrinter.printHtmlAsPdf(htmlStream, pdfStream)
                    return pdfStream.toByteArray()
                }
            }
        } catch (e: IOException) {
            throw PdfRenderException("Pdf rendering failed", e)
        }
    }

    /**
     * Returns metaform replies as XLSX binary
     *
     * @param metaform metaform
     * @param metaformEntity metaform REST entity
     * @param replyEntities metaform reply entites
     * @return replies as XLSX binary
     * @throws XlsxException thrown when export fails
     */
    @Throws(XlsxException::class)
    fun getRepliesAsXlsx(
            metaform: Metaform,
            metaformEntity: fi.metatavu.metaform.api.spec.model.Metaform,
            replyEntities: List<fi.metatavu.metaform.api.spec.model.Reply>
    ): ByteArray {
        var title = metaformEntity.title
        if (StringUtils.isBlank(title)) {
            title = metaform.slug
        }
        try {
            ByteArrayOutputStream().use { output ->
                XlsxBuilder().use { xlsxBuilder ->
                    val sheetId = xlsxBuilder.createSheet(title!!)
                    val fields = MetaformUtils.getMetaformFields(metaformEntity)
                            .filter { StringUtils.isNotEmpty(it.name) }
                            .filter { StringUtils.isNotEmpty(it.title) }

                    // Headers
                    fields.forEachIndexed{ i, field ->
                        xlsxBuilder.setCellValue(sheetId, 0, i, field.title, CellSource(field, CellSourceType.HEADER))
                    }

                    // Values
                    fields.forEachIndexed { columnIndex, field ->
                        replyEntities.forEachIndexed { replyIndex, reply ->
                            val rowIndex = replyIndex + 1
                            val cellSource = CellSource(field, CellSourceType.VALUE)
                            val value = reply.data?.get(field.name)
                            if (value != null) {
                                when (field.type) {
                                    MetaformFieldType.DATE, MetaformFieldType.DATE_MINUS_TIME -> xlsxBuilder.setCellValue(sheetId, rowIndex, columnIndex, OffsetDateTime.parse(value.toString()), cellSource)
                                    MetaformFieldType.SELECT, MetaformFieldType.RADIO -> {
                                        val selectedValue = field.options
                                                ?.filter { it.name == value.toString() }
                                                ?.map(MetaformFieldOption::text)
                                                ?.firstOrNull() ?: value.toString()
                                        xlsxBuilder.setCellValue(sheetId, rowIndex, columnIndex, selectedValue, cellSource)
                                    }
                                    MetaformFieldType.TABLE -> {
                                        val sheetName = createXlsxTableSheet(xlsxBuilder, replyIndex, field, value)
                                        if (StringUtils.isNotBlank(sheetName)) {
                                            xlsxBuilder.setCellLink(sheetId, rowIndex, columnIndex, HyperlinkType.DOCUMENT, sheetName!!, sheetName, cellSource)
                                        }
                                    }
                                    else -> xlsxBuilder.setCellValue(sheetId, rowIndex, columnIndex, value.toString(), cellSource)
                                }
                            }
                        }
                    }
                    formRuntimeContext.xlsxBuilder = xlsxBuilder

                    xlsxBuilder.write(output)
                    return output.toByteArray()
                }
            }
        } catch (e: Exception) {
            throw XlsxException("Failed to create XLSX export", e)
        }
    }

    /**
     * Returns whether owner key is valid for given reply
     *
     * @param reply reply
     * @param ownerKey owner key
     * @return whether owner key is valid for given reply
     */
    fun isValidOwnerKey(reply: Reply, ownerKey: String?): Boolean {
        val publicKey = cryptoController.loadPublicKeyBase64(ownerKey) ?: return false
        return isValidOwnerKey(reply, publicKey)
    }

    /**
     * Returns whether owner key is valid for given reply
     *
     * @param reply reply
     * @param ownerKey owner key
     * @return whether owner key is valid for given reply
     */
    private fun isValidOwnerKey(reply: Reply, ownerKey: PublicKey): Boolean {
        val privateKey = cryptoController.loadPrivateKey(reply.privateKey) ?: return false
        val signature = cryptoController.signUUID(privateKey, reply.id!!) ?: return false
        return cryptoController.verifyUUID(ownerKey, signature, reply.id!!)
    }

    /**
     * Creates new sheet for table values
     *
     * @param xlsxBuilder
     * @param replyIndex
     * @param field
     * @param value
     * @return Xlsx table sheet
     */
    private fun createXlsxTableSheet(xlsxBuilder: XlsxBuilder, replyIndex: Int, field: MetaformField, value: Any): String? {
        val table = field.table ?: return null

        val columns = table.columns
        if (columns?.isEmpty() != false) {
            return null
        }

        val columnMap = getTableColumnMap(field)
        val tableValue = getTableValue(columnMap, value)
        if (tableValue?.isEmpty() == false) {
            val fieldTitle = if (StringUtils.isBlank(field.title)) {
                field.name
            } else field.title
            val sheetName = String.format("%s - %s", StringUtils.truncate(fieldTitle, 20), replyIndex + 1)
            val sheetId = xlsxBuilder.createSheet(sheetName)

            // Headers
            for (columnIndex in columns.indices) {
                val column = columns[columnIndex]
                var columnTitle = column.title
                if (StringUtils.isBlank(columnTitle)) {
                    columnTitle = column.name
                }
                xlsxBuilder.setCellValue(sheetId, 0, columnIndex, columnTitle, CellSource(field, CellSourceType.TABLE_HEADER))
            }

            // Values
            tableValue.forEachIndexed { rowIndex, row ->
                val cellSource = CellSource(field, CellSourceType.TABLE_VALUE)
                val rowNumber = rowIndex + 1
                for (columnIndex in columns.indices) {
                    val (type, name) = columns[columnIndex]
                    val cellValue = row[name]
                    if (cellValue != null) {
                        when (type) {
                            MetaformTableColumnType.DATE, MetaformTableColumnType.TIME ->
                                xlsxBuilder.setCellValue(sheetId, rowNumber, columnIndex, OffsetDateTime.parse(cellValue.toString()), cellSource)
                            MetaformTableColumnType.NUMBER ->
                                xlsxBuilder.setCellValue(sheetId, rowNumber, columnIndex, NumberUtils.createDouble(cellValue.toString()), cellSource)
                            else -> xlsxBuilder.setCellValue(sheetId, rowNumber, columnIndex, cellValue.toString(), cellSource)
                        }
                    }
                }
            }

            // Sums
            columns.forEachIndexed { columnIndex, column ->
                if (java.lang.Boolean.TRUE == column.calculateSum) {
                    val cellSource = CellSource(field, CellSourceType.TABLE_SUM)
                    val columnString = CellReference.convertNumToColString(columnIndex)
                    val postfix = column.sumPostfix ?: ""
                    xlsxBuilder.setCellFormula(sheetId, tableValue.size + 1, columnIndex, String.format("SUM(%s%d:%s%d)&\"%s\"", columnString, 2, columnString, tableValue.size + 1, postfix), cellSource)
                }
            }
            return sheetName
        }
        return null
    }

    /**
     * Returns OffsetDateTime as java.util.Date
     *
     * @param offsetDateTime offset date time
     * @return java.util.Date
     */
    private fun getDate(offsetDateTime: OffsetDateTime?): Date? {
        return offsetDateTime?.let { Date.from(it.toInstant()) }
    }

    /**
     * Returns table field as valid table data.
     *
     * If input object is not valid null is returned instead
     *
     * @param columnMap column map
     * @param value input object
     * @return table data
     */
    private fun getTableValue(columnMap: Map<String, MetaformTableColumn>, value: Any?): List<Map<String, Any?>>? {
        if (value == null) {
            return emptyList()
        }
        if (value !is List<*>) {
            return null
        }

        if (value.isEmpty()) {
            return emptyList()
        }
        for (listItem in value) {
            if (listItem !is Map<*, *>) {
                return null
            }

            for (key in listItem.keys) {
                if (key !is String) {
                    return null
                }
                val column = columnMap[key]
                if (column == null || !isSupportedTableColumnType(column.type)) {
                    return null
                }
            }
        }
        return value as List<Map<String, Any?>>?
    }

    /**
     * Sets reply field value
     *
     * @param field field
     * @param reply reply
     * @param name name
     * @param value values
     * @return updated field
     */
    private fun setTableReplyField(field: MetaformField, reply: Reply, name: String, value: Any?): ReplyField? {
        val columnMap = getTableColumnMap(field)
        val tableValue = getTableValue(columnMap, value)
        if (tableValue == null) {
            if (logger.isErrorEnabled) {
                logger.error(String.format("Invalid value (%s) passes to table field %s in reply %s", value.toString(), name, reply.id.toString()))
            }
            return null
        }
        var replyField = anyReplyFieldDAO.findByReplyAndName(reply, name)
        if (replyField != null && replyField !is TableReplyField) {
            deleteField(replyField)
            replyField = null
        }
        var tableReplyField = replyField as TableReplyField?
        if (tableReplyField == null) {
            tableReplyField = tableReplyFieldDAO.create(UUID.randomUUID(), reply, name)
        } else {
            deleteTableReplyFieldRows(tableReplyField)
        }
        for (rowValue in tableValue) {
            createTableReplyFieldRowValue(tableReplyField, columnMap, rowValue)
        }
        return tableReplyField
    }

    /**
     * Returns column map for a table field
     *
     * @param field field
     * @return column map for a table field
     */
    private fun getTableColumnMap(field: MetaformField): Map<String, MetaformTableColumn> {
        return field.table?.columns?.associateBy(MetaformTableColumn::name) ?: emptyMap()
    }

    /**
     * Creates new row for table field reply
     *
     * @param tableReplyField table field
     * @param columnMap column map
     * @param rowValue row value
     * @return created row
     */
    private fun createTableReplyFieldRowValue(tableReplyField: TableReplyField, columnMap: Map<String, MetaformTableColumn>, rowValue: Map<String, Any?>): TableReplyFieldRow? {
        val row = createTableReplyFieldRow(tableReplyField)
        rowValue.entries
                .filter { !isBlankString(it.value) && columnMap.containsKey(it.key) }
                .forEach { (key, value) ->
                    val column = columnMap[key]
                    createTableReplyFieldRowCell(row, column, value)
                }
        return row
    }

    /**
     * Returns whether object is a blank string
     *
     * @param object object
     * @return whether object is a blank string
     */
    private fun isBlankString(`object`: Any?): Boolean {
        if (`object` is String) {
            return StringUtils.isBlank(`object`)
        }
        return false
    }

    /**
     * Creates new table reply row
     *
     * @param field field
     * @return created row
     */
    private fun createTableReplyFieldRow(field: TableReplyField): TableReplyFieldRow {
        return tableReplyFieldRowDAO.create(UUID.randomUUID(), field)
    }

    /**
     * Returns whether table column type is supported or not
     *
     * @param type type
     * @return whether table column type is supported or not
     */
    private fun isSupportedTableColumnType(type: MetaformTableColumnType): Boolean {
        return ArrayUtils.contains(SUPPORTED_TABLE_COLUMN_TYPES, type)
    }

    /**
     * Creates table reply field cell
     *
     * @param row row
     * @param column column
     * @param value value
     * @return created cell
     */
    private fun createTableReplyFieldRowCell(row: TableReplyFieldRow, column: MetaformTableColumn?, value: Any?): TableReplyFieldRowCell? {
        when (column!!.type) {
            MetaformTableColumnType.TEXT -> return tableReplyFieldStringRowCellDAO.create(UUID.randomUUID(), row, column.name, value.toString())
            MetaformTableColumnType.NUMBER -> {
                when (value) {
                    is Number -> value.toDouble()
                    is String -> NumberUtils.createDouble(value)
                    else -> {
                        if (logger.isErrorEnabled) {
                            logger.error("Could not save value {} for tableReplyFieldNumberRowCell", value)
                        }
                        null
                    }
                }?.let { tableReplyFieldNumberRowCellDAO.create(UUID.randomUUID(), row, column.name, it) }
            }
            else -> if (logger.isErrorEnabled) {
                logger.error("Unsupported table column type {}", column.type)
            }
        }
        return null
    }

    /**
     * Sets reply field value
     *
     * @param reply reply
     * @param name name
     * @param values values
     * @return updated field
     */
    private fun setReplyField(reply: Reply, name: String, values: List<*>?): ReplyField? {
        var replyField = anyReplyFieldDAO.findByReplyAndName(reply, name)
        if (replyField != null && replyField !is ListReplyField) {
            deleteField(replyField)
            replyField = null
        }
        var listReplyField = replyField as ListReplyField?
        if (values == null || values.isEmpty()) {
            if (listReplyField != null) {
                deleteListReplyFieldItems(listReplyField)
                listReplyFieldDAO.delete(listReplyField)
            }
            return null
        }
        if (listReplyField == null) {
            listReplyField = listReplyFieldDAO.create(UUID.randomUUID(), reply, name)
        } else {
            deleteListReplyFieldItems(listReplyField)
        }
        for (value in values) {
            value?.let{ listReplyFieldItemDAO.create(UUID.randomUUID(), listReplyField, value.toString()) }
        }
        return listReplyField
    }

    /**
     * Removes unused attachment reply items
     *
     * @param items items
     * @param savedFileRefs saved file refs
     */
    private fun removeUnusedAttachmentReplyItems(items: List<AttachmentReplyFieldItem>, savedFileRefs: List<String?>) {
        items
            .filter { attachmentReplyFieldItem: AttachmentReplyFieldItem ->
                val fileRef = attachmentReplyFieldItem.attachment?.id.toString()
                !savedFileRefs.contains(fileRef)
            }
            .forEach { deleteAttachmentReplyFieldItem(it) }
    }

    /**
     * Deletes attachment reply field item
     *
     * @param item item
     */
    private fun deleteAttachmentReplyFieldItem(item: AttachmentReplyFieldItem) {
        attachmentReplyFieldItemDAO.delete(item)
        item.attachment?.let { attachmentController.deleteAttachment(it) }
    }

    /**
     * Attaches attachments to reply
     *
     * @param userId user
     * @param field field
     * @param items items
     * @param savedFileRefs saved file refs
     */
    private fun attachUsedReplyItems(userId: UUID, field: AttachmentReplyField, items: List<AttachmentReplyFieldItem>, savedFileRefs: List<String>) {
        val usedFileRefs = items
            .mapNotNull(AttachmentReplyFieldItem::attachment)
            .map(Attachment::id)
            .map { it.toString() }

        savedFileRefs
            .filterNot(usedFileRefs::contains)
            .forEach { id ->
                val attachment = retrieveOrPersistAttachment(UUID.fromString(id), userId)
                attachmentReplyFieldItemDAO.create(UUID.randomUUID(), field, attachment)
            }
    }

    /**
     * Converts reply into a revision by updating the modifiedAt field into the revision field.
     *
     * @param reply reply to be converted into a revision
     */
    fun convertToRevision(reply: Reply) {
        replyDAO.updateRevision(reply, reply.modifiedAt)
    }

    /**
     * Deletes a reply field
     *
     * @param replyField reply field
     */
    private fun deleteField(replyField: ReplyField) {
        when (replyField) {
            is ListReplyField -> deleteListReplyFieldItems(replyField)
            is AttachmentReplyField -> deleteAttachmentReplyFieldItems(replyField)
            is TableReplyField -> deleteTableReplyFieldRows(replyField)
        }
        anyReplyFieldDAO.delete(replyField)
    }

    /**
     * Deletes all rows from table reply field
     *
     * @param tableReplyField table reply field
     */
    private fun deleteTableReplyFieldRows(tableReplyField: TableReplyField) {
        tableReplyFieldRowDAO.listByField(tableReplyField).forEach(Consumer { tableReplyFieldRow: TableReplyFieldRow -> deleteTableReplyFieldRow(tableReplyFieldRow) })
    }

    /**
     * Deletes table reply field row
     *
     * @param tableReplyFieldRow row
     */
    private fun deleteTableReplyFieldRow(tableReplyFieldRow: TableReplyFieldRow) {
        anyTableReplyFieldRowCellDAO.listByRow(tableReplyFieldRow).forEach(anyTableReplyFieldRowCellDAO::delete)
        tableReplyFieldRowDAO.delete(tableReplyFieldRow)
    }

    /**
     * Removes all items from list reply field
     *
     * @param listReplyField field
     */
    private fun deleteListReplyFieldItems(listReplyField: ListReplyField) {
        listReplyFieldItemDAO.listByField(listReplyField).forEach(listReplyFieldItemDAO::delete)
    }

    /**
     * Removes all items from attachment reply field
     *
     * @param attachmentReplyField field
     */
    private fun deleteAttachmentReplyFieldItems(attachmentReplyField: AttachmentReplyField) {
        attachmentReplyFieldItemDAO.listByField(attachmentReplyField).forEach { deleteAttachmentReplyFieldItem(it) }
    }

    /**
     * Retrieves existing attachment or persists one from previously uploaded one.
     *
     * @param id attachment id
     * @param userId user id
     * @return attachment
     */
    private fun retrieveOrPersistAttachment(id: UUID, userId: UUID): Attachment {
        return attachmentController.findAttachmentById(id) ?: return persistAttachment(id, userId)
    }

    /**
     * Persists previously uploaded file as attachment
     *
     * @param id attachment id / fileRef
     * @param userId user id
     * @return
     */
    private fun persistAttachment(id: UUID, userId: UUID): Attachment {
        val file = fileController.popFileData(id.toString())!!
        return attachmentController.create(
            id = id,
            name = file.meta.fileName,
            content = file.data,
            contentType= file.meta.contentType,
            userId = userId
        )
    }

    /**
     * Returns resource name for a reply
     *
     * @param reply reply
     * @return resource name
     */
    fun getReplyResourceName(reply: Reply): String {
        return String.format(REPLY_RESOURCE_NAME_TEMPLATE, reply.id)
    }

    /**
     * Returns resource URI for reply
     *
     * @param reply reply
     * @return resource URI
     */
    fun getReplyResourceUri(reply: Reply): String {
        return getReplyResourceUri(reply.metaform.id!!, reply.id)
    }

    /**
     * Returns resource URI for reply
     *
     * @param metaformId Metaform id
     * @param replyId reply id
     * @return resource URI
     */
    private fun getReplyResourceUri(metaformId: UUID, replyId: UUID?): String {
        return String.format(REPLY_RESOURCE_URI_TEMPLATE, metaformId, replyId)
    }

    /**
     * Resolves reply object when creating new reply
     *
     * @param replyMode reply mode
     * @param metaform metaform
     * @param anonymous is user anonymous
     * @param userId user id
     * @return reply object
     */
    fun createReplyResolveReply(
        replyMode: ReplyMode,
        metaform: Metaform,
        anonymous: Boolean,
        userId: UUID,
        privateKey: PrivateKey?,
        loggedUserId: UUID
    ): Reply {
        return if (anonymous || replyMode == ReplyMode.CUMULATIVE) {
            createReply(userId, metaform, privateKey, loggedUserId)
        } else {
            val foundReply = findActiveReplyByMetaformAndUserId(metaform, userId)
            if (foundReply == null) {
                createReply(userId, metaform, privateKey, loggedUserId)
            } else if (replyMode == ReplyMode.REVISION) {
                // If there is already an existing reply, but we are not updating it
                // We need to change the existing reply into a revision and create new reply
                convertToRevision(foundReply)
                createReply(userId, metaform, privateKey, loggedUserId)
            } else foundReply
        }
    }

    /**
     * Triggers Reply created event
     *
     * @param reply Reply
     */
    fun triggerReplyCreatedEvent(reply: Reply) {
        replyCreatedEvent.fire(
                ReplyCreatedEvent(
                        replyId = reply.id!!,
                        metaformId = reply.metaform.id!!
                )
        )
    }

    /**
     * Triggers Reply updated event
     *
     * @param reply Reply
     */
    fun triggerReplyUpdatedEvent(reply: Reply) {
        replyUpdatedEvent.fire(
                ReplyUpdatedEvent(
                        replyId = reply.id!!,
                        metaformId = reply.metaform.id!!
                )
        )
    }

    /**
     * Triggers Reply deleted event
     *
     * @param reply Reply
     */
    fun triggerReplyDeletedEvent(reply: Reply) {
        replyDeletedEvent.fire(
            ReplyDeletedEvent(
                replyId = reply.id!!,
                metaformId = reply.metaform.id!!
            )
        )
    }

    /**
     * Triggers Reply found event
     *
     * @param reply Reply
     */
    fun triggerReplyFoundEvent(reply: Reply) {
        replyFoundEvent.fire(
            ReplyFoundEvent(
                replyId = reply.id!!,
                metaformId = reply.metaform.id!!
            )
        )
    }

    /**
     * Sets reply firstViewedAt and lastViewedAt
     *
     * @param reply reply
     * @param viewedAtDateTime OffsetDateTime
     */
    fun setReplyViewedAt(reply: Reply, viewedAtDateTime: OffsetDateTime) {
        if (reply.firstViewedAt == null) {
            reply.firstViewedAt = viewedAtDateTime
        }
        reply.lastViewedAt = viewedAtDateTime
    }

    companion object {
        val SUPPORTED_TABLE_COLUMN_TYPES = arrayOf(
                MetaformTableColumnType.TEXT,
                MetaformTableColumnType.NUMBER
        )

        const val  REPLY_RESOURCE_URI_TEMPLATE = "/v1/metaforms/%s/replies/%s"
        const val  REPLY_RESOURCE_NAME_TEMPLATE = "reply-%s"
    }

}