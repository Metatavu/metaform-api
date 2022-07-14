package fi.metatavu.metaform.server.persistence.dao

import fi.metatavu.metaform.server.persistence.model.AdminTheme
import java.util.UUID
import javax.enterprise.context.ApplicationScoped

@ApplicationScoped
class AdminThemeDAO : AbstractDAO<AdminTheme>() {
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
    fun create(id: UUID,
               data: String,
               name: String,
               slug: String,
               creatorId: UUID,
               lastModifierId: UUID
    ): AdminTheme {
        val adminTheme = AdminTheme()
        adminTheme.id = id
        adminTheme.data = data
        adminTheme.name = name
        adminTheme.slug = slug
        adminTheme.creatorId = creatorId
        adminTheme.lastModifierId = lastModifierId

        return persist(adminTheme)
    }

    fun update(adminTheme: AdminTheme, data: String, name: String, slug: String): AdminTheme {
        adminTheme.data = data
        adminTheme.name = name
        adminTheme.slug = slug
        return persist(adminTheme)
    }
}