package fi.metatavu.metaform.server.rest.translate

import fi.metatavu.metaform.api.spec.model.AdminTheme
import javax.enterprise.context.ApplicationScoped

/**
 * Translator for Admin themes
 *
 * @author Otto Hooper
 */
@ApplicationScoped
class AdminThemeTranslator {

    /**
     * Translate JPA admin theme object to REST admin theme object
     * 
     * @param adminTheme JPA admin theme object
     * 
     * @return REST admin theme object
     */
    fun translate(adminTheme: fi.metatavu.metaform.server.persistence.model.AdminTheme): AdminTheme {
        return AdminTheme(
            id = adminTheme.id,
            data = adminTheme.data,
            name = adminTheme.name,
            slug = adminTheme.slug,
            creatorId = adminTheme.creatorId,
            lastModifierId = adminTheme.lastModifierId,
        )
    }
}