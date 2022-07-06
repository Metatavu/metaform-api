package fi.metatavu.metaform.server.controllers

import fi.metatavu.metaform.api.spec.model.Metaform
import fi.metatavu.metaform.api.spec.model.MetaformField
import fi.metatavu.metaform.api.spec.model.MetaformFieldType
import fi.metatavu.metaform.server.metaform.*
import fi.metatavu.metaform.server.utils.MetaformUtils
import fi.metatavu.metaform.server.persistence.dao.*
import fi.metatavu.metaform.server.persistence.model.*
import org.apache.commons.lang3.BooleanUtils
import org.apache.commons.lang3.StringUtils
import org.apache.commons.lang3.math.NumberUtils
import org.slf4j.Logger
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import javax.enterprise.context.ApplicationScoped
import javax.inject.Inject

/**
 * Controller for Fields
 */
@ApplicationScoped
class FieldController {

    @Inject
    lateinit var logger: Logger

    @Inject
    lateinit var fieldTypeMapper: FieldTypeMapper

    @Inject
    lateinit var anyReplyFieldDAO: AnyReplyFieldDAO

    @Inject
    lateinit var listReplyFieldItemDAO: ListReplyFieldItemDAO

    @Inject
    lateinit var attachmentReplyFieldItemDAO: AttachmentReplyFieldItemDAO

    @Inject
    lateinit var tableReplyFieldRowDAO: TableReplyFieldRowDAO

    @Inject
    lateinit var anyTableReplyFieldRowCellDAO: AnyTableReplyFieldRowCellDAO

    /**
     * Parses field filters
     *
     * @param metaform metaform
     * @param filterList list of filters
     * @return parsed filters
     */
    fun parseFilters(metaform: Metaform, filterList: List<String?>?): FieldFilters? {
        filterList ?: return null
        
        val filterListCombined = filterList
                .filter(StringUtils::isNotEmpty)
                .map { filter: String? -> listOf(*StringUtils.split(filter, ',')) }
                .flatMap { it.toList() }

        val filters = filterListCombined
                .mapNotNull { filter: String -> parseFilter(metaform, filter) }

        return if (filters.isEmpty()) {
            null
        } else FieldFilters(filters)
    }

    /**
     * Resolves field type for a field name
     *
     * @param metaformEntity metaform
     * @param name field name
     * @return field type for a field name
     */
    fun getFieldType(metaformEntity: Metaform, name: String): MetaformFieldType? {
        return getField(metaformEntity, name)?.type
    }

    /**
     * Returns field value for a reply
     *
     * @param metaformEntity metaform model
     * @param reply reply
     * @param fieldName field name
     * @param replyFieldMap map containing reply fields
     * @return field value or null if not found
     */
    fun getFieldValue(metaformEntity: Metaform, reply: Reply, fieldName: String, replyFieldMap: Map<String, ReplyField?>): Any? {
        if (isMetaField(metaformEntity, fieldName)) {
            return resolveMetaField(fieldName, reply)
        }
        val field = replyFieldMap[fieldName] ?: return null

        return when (field) {
            is NumberReplyField -> field.value
            is BooleanReplyField -> field.value
            is StringReplyField -> field.value
            is ListReplyField -> listReplyFieldItemDAO.listItemValuesByField(field)
            is AttachmentReplyField -> attachmentReplyFieldItemDAO.listAttachmentIdsByField(field)
            is TableReplyField -> tableReplyFieldRowDAO.listByField(field).map{ getTableRowValue(it) }
            else -> {
                logger.error("Could not resolve {}", fieldName)
                null
            }
        }
    }

    /**
     * Returns table field row as map
     *
     * @param row row
     * @return table field row as map
     */
    private fun getTableRowValue(row: TableReplyFieldRow): Map<String?, Any?> {
        return anyTableReplyFieldRowCellDAO.listByRow(row).associate { cell ->
            cell.name to when (cell) {
                is TableReplyFieldNumberRowCell -> cell.value
                is TableReplyFieldStringRowCell -> cell.value
                else -> null
            }
        }
    }

    /**
     * Returns whether form field is a meta field
     *
     * @param metaformEntity form
     * @param name name
     * @return whether form field is a meta field
     */
    private fun isMetaField(metaformEntity: Metaform, name: String): Boolean {
        val field = getField(metaformEntity, name)
        return field?.contexts != null && field.contexts.contains("META")
    }

    /**
     * Returns field by name
     *
     * @param metaformEntity form
     * @param name name
     * @return field
     */
    fun getField(metaformEntity: Metaform, name: String): MetaformField? {
        return MetaformUtils.getMetaformFields(metaformEntity).find{ field -> field.name == name }
    }

    /**
     * Resolves meta field
     *
     * @param fieldName field name
     * @param entity reply
     * @return meta field value
     */
    fun resolveMetaField(fieldName: String?, entity: Reply): Any? {
        return when (fieldName) {
            "lastEditor" -> entity.userId
            "created", "createdAt" -> formatDateTime(entity.createdAt)
            "modified" -> formatDateTime(entity.modifiedAt)
            else -> {
                logger.warn("Metafield {} not recognized", fieldName)
                null
            }
        }
    }

    /**
     * Returns field name <> type map from Metaform
     *
     * @param metaformEntity Metaform REST entity
     * @return field name <> type map
     */
    fun getFieldMap(metaformEntity: Metaform): Map<String, MetaformField> {
        return MetaformUtils.getMetaformFields(metaformEntity).associateBy { field -> field.name!! }
    }

    /**
     * Lists field name by type
     *
     * @param metaformEntity Metaform
     * @param type type
     * @return field names by type
     */
    fun getFieldNamesByType(metaformEntity: Metaform, type: MetaformFieldType): List<String> {
        return MetaformUtils.getMetaformFields(metaformEntity)
                .filter { type == it.type }
                .mapNotNull(MetaformField::name)
    }

    /**
     * Returns map of reply fields where reply field name is a key and the field value
     *
     * @param reply reply
     * @return map of reply fields where reply field name is a key and the field value
     */
    fun getReplyFieldMap(reply: Reply): Map<String, ReplyField?> {
        return anyReplyFieldDAO.listByReply(reply).associate { replyField -> replyField.name to replyField }
    }

    /**
     * Parses a field filter
     *
     * @param metaformEntity
     * @param filter
     * @return
     */
    private fun parseFilter(metaformEntity: Metaform, filter: String): FieldFilter? {
        val tokenizedFilter = tokenizeFilter(filter)
        if (tokenizedFilter.size == 3) {
            val fieldName = tokenizedFilter[0]
            val operatorString = tokenizedFilter[1]
            val valueString = tokenizedFilter[2]
            val fieldType = getFieldType(metaformEntity, fieldName)
            val storeDataType = fieldTypeMapper.getStoreDataType(fieldType)
            val operator = when (operatorString) {
                ":" -> FieldFilterOperator.EQUALS
                "^" -> FieldFilterOperator.NOT_EQUALS
                else -> {
                    logger.error("Could not parse operator string {}", operatorString)
                    return null
                }
            }
            return if (storeDataType == null || storeDataType == StoreDataType.NONE) {
                null
            } else FieldFilter(fieldName, getFieldFilterValue(storeDataType, valueString), storeDataType, operator)
        }
        return null
    }

    /**
     * Tokenizes a field filter
     *
     * @param filter filter string
     * @return tokenized filter
     */
    private fun tokenizeFilter(filter: String): List<String> {
        val stringTokenizer = StringTokenizer(filter, ":^", true)
        val result: MutableList<String> = ArrayList(3)
        while (stringTokenizer.hasMoreTokens()) {
            result.add(stringTokenizer.nextToken())
        }
        return result
    }

    /**
     * Resolves field filter value
     *
     * @param storeDataType store data type
     * @param valueString value as string
     * @return field filter value
     */
    private fun getFieldFilterValue(storeDataType: StoreDataType, valueString: String?): Any? {
        if (valueString == null) {
            return null
        }
        return when (storeDataType) {
            StoreDataType.BOOLEAN -> BooleanUtils.toBooleanObject(valueString)
            StoreDataType.LIST, StoreDataType.STRING -> valueString
            StoreDataType.NUMBER -> {
                try {
                    NumberUtils.createDouble(valueString)
                } catch (e: NumberFormatException) {
                    logger.error("Failed to parse valueString {}", valueString)
                    null
                }

            }
            else -> {
                logger.error("Failed to parse valueString {}", valueString)
                null
            }
        }
    }

    /**
     * Formats date time in ISO date-time format
     *
     * @param dateTime date time
     * @return date time in ISO date-time format
     */
    private fun formatDateTime(dateTime: OffsetDateTime?): String? {
        return dateTime?.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
    }

}