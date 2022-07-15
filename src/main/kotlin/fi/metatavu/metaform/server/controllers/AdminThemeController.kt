package fi.metatavu.metaform.server.controllers

import com.fasterxml.jackson.databind.ObjectMapper
import fi.metatavu.metaform.server.persistence.dao.AdminThemeDAO
import fi.metatavu.metaform.server.persistence.model.AdminTheme
import java.util.UUID
import javax.enterprise.context.ApplicationScoped
import javax.inject.Inject

/**
 * Controller for admin themes
 */
@ApplicationScoped
class AdminThemeController {
    @Inject
    lateinit var adminThemeDAO: AdminThemeDAO

    @Inject
    lateinit var mapper: ObjectMapper

    /**
     * Creates a new admin theme
     *
     * @param id the id of the theme
     * @param data the data in the theme
     * @param name the name of the theme
     * @param slug the slug of the theme
     * @param creatorId who created this theme
     * @param lastModifierId who last modified this theme
     *
     * @return new [AdminTheme] object
     */
    fun create(
        id: UUID,
        data: Any?,
        name: String,
        slug: String,
        creatorId: UUID,
        lastModifierId: UUID
    ): AdminTheme {
        return adminThemeDAO.create(
            id, 
            mapper.writeValueAsString(data), 
            name, 
            slug, 
            creatorId, 
            lastModifierId
        )
    }


    /**
     * Finds an admin theme by id
     *
     * @param id the id of the admin theme
     * 
     * @return the located admin theme or null
     */
    fun findById(id: UUID): AdminTheme? {
        return adminThemeDAO.findById(id)
    }

    fun updateAdminTheme(
        theme: AdminTheme,
        data: Any?,
        name: String,
        slug: String,
    ): AdminTheme { 
        return adminThemeDAO.update(
            theme,
            mapper.writeValueAsString(data),
            name,
            slug
        )
    }
}