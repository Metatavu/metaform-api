package fi.metatavu.metaform.server.utils

import fi.metatavu.metaform.api.spec.model.*

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

    /**
     * Returns all defined permission groups from given metaform
     *
     * @param metaform metaform
     * @return permission groups from metaform
     */
    fun getPermissionGroups(metaform: Metaform): List<PermissionGroups> {
        val result = getMetaformFields(metaform = metaform)
            .mapNotNull(MetaformField::options)
            .flatMap { it.toList() }
            .mapNotNull(MetaformFieldOption::permissionGroups)
            .toMutableList()

        metaform.defaultPermissionGroups?.let {
            result.add(it)
        }

        return result
    }

}