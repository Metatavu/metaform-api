package fi.metatavu.metaform.server.metaform

import fi.metatavu.metaform.api.spec.model.MetaformFieldType
import org.slf4j.Logger
import jakarta.inject.Inject

/**
 * Mapper for mapping field types into store data types
 *
 * @author Antti Leppa
 */
object FieldTypeMapper {

    @Inject
    lateinit var logger: Logger

    /**
     * Maps field type into store data types
     *
     * @param fieldType field type
     * @return store type
     */
    fun getStoreDataType(fieldType: MetaformFieldType?): StoreDataType? {
        if (fieldType == null) {
            logger.error("Failed to resolve field type from null")
            return null
        }
        return when (fieldType) {
            MetaformFieldType.AUTOCOMPLETE, MetaformFieldType.DATE, MetaformFieldType.DATE_MINUS_TIME, MetaformFieldType.FILES, MetaformFieldType.EMAIL, MetaformFieldType.AUTOCOMPLETE_MINUS_MULTIPLE -> StoreDataType.STRING
            MetaformFieldType.BOOLEAN -> StoreDataType.BOOLEAN
            MetaformFieldType.CHECKLIST -> StoreDataType.LIST
            MetaformFieldType.HIDDEN -> StoreDataType.STRING
            MetaformFieldType.HTML -> StoreDataType.NONE
            MetaformFieldType.LOGO -> StoreDataType.NONE
            MetaformFieldType.MEMO -> StoreDataType.STRING
            MetaformFieldType.NUMBER -> StoreDataType.NUMBER
            MetaformFieldType.RADIO -> StoreDataType.STRING
            MetaformFieldType.SELECT -> StoreDataType.STRING
            MetaformFieldType.SMALL_MINUS_TEXT -> StoreDataType.NONE
            MetaformFieldType.SUBMIT -> StoreDataType.NONE
            MetaformFieldType.TABLE -> StoreDataType.STRING
            MetaformFieldType.TEXT -> StoreDataType.STRING
            MetaformFieldType.TIME -> StoreDataType.STRING
            else -> {
                logger.error("Failed to resolve field type {}", fieldType)
                StoreDataType.NONE
            }
        }
    }
}