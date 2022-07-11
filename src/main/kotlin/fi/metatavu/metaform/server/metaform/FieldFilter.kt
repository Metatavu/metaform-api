package fi.metatavu.metaform.server.metaform

/**
 * Field filter
 *
 * @author Antti Leppä
 */
data class FieldFilter (
    val field: String,
    val value: Any?,
    val dataType: StoreDataType,
    val  operator: FieldFilterOperator
)