package fi.metatavu.metaform.server.utils

import com.github.slugify.Slugify
import fi.metatavu.metaform.api.spec.model.Metaform
import fi.metatavu.metaform.api.spec.model.MetaformField
import fi.metatavu.metaform.api.spec.model.MetaformSection
import org.apache.commons.lang3.StringUtils

object MetaformUtils {
    fun getMetaformFields(metaform: Metaform): List<MetaformField> {
        return metaform.sections
                ?.mapNotNull(MetaformSection::fields)
                ?.flatMap { it.toList() } ?: emptyList()
    }
}