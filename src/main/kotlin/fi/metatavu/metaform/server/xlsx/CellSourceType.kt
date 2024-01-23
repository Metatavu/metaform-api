package fi.metatavu.metaform.server.xlsx

/**
 * Enumeration for XLSX cell source type
 */
enum class CellSourceType {
    /**
     * Table header cell
     */
    HEADER,

    /**
     * Table value cell
     */
    VALUE,

    /**
     * Table field header cell
     */
    TABLE_HEADER,

    /**
     * Table field value cell
     */
    TABLE_VALUE,

    /**
     * Table field sum cell
     */
    TABLE_SUM,

    /**
     * Script added cell
     */
    SCRIPT
}