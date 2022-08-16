package fi.metatavu.metaform.server.utils

import fi.metatavu.metaform.api.spec.model.Metaform
import fi.metatavu.metaform.api.spec.model.MetaformField
import fi.metatavu.metaform.api.spec.model.MetaformSection

/**
 * Metaform utilities
 */
object MetaformUtils {

    /**
     * Returns flatted list of metaform fields
     *
     * @param metaform metaform
     * @return list of metaform fields
     */
    fun getMetaformFields(metaform: Metaform): List<MetaformField> {
        return metaform.sections
                ?.mapNotNull(MetaformSection::fields)
                ?.flatMap { it.toList() } ?: emptyList()
    }
}