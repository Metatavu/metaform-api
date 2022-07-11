package fi.metatavu.metaform.server.metaform

import java.util.stream.Collectors

class FieldFilters(val filters: List<FieldFilter>) {


    /**
     * Returns field filters for single data type
     *
     * @param storeDataType data type
     * @return field filters for single data type
     */
    fun getFilters(storeDataType: StoreDataType): List<FieldFilter> {
        return filters
                .filter { fieldFilter -> fieldFilter.dataType === storeDataType }
    }

}