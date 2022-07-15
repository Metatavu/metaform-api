package fi.metatavu.metaform.server.rest

import fi.metatavu.metaform.api.spec.model.AdminTheme
import fi.metatavu.metaform.server.controllers.AdminThemeController
import java.util.UUID
import javax.enterprise.context.RequestScoped
import javax.inject.Inject
import javax.transaction.Transactional
import javax.ws.rs.core.Response

@RequestScoped
@Transactional
class AdminThemeApi : fi.metatavu.metaform.api.spec.AdminThemeApi, AbstractApi() {
    @Inject
    lateinit var adminThemeController: AdminThemeController

    /***
     * Creates a new admin theme
     * 
     * @param adminTheme admin theme to create
     * 
     * @return created admin theme
     */
    override suspend fun createAdminTheme(adminTheme: AdminTheme): Response {
        val userId = loggedUserId ?: return createForbidden(UNAUTHORIZED)
        if (!isRealmMetaformAdmin) {
            return createForbidden(createNotAllowedMessage(CREATE, ADMIN_THEME))
        }

        return createOk(adminThemeController.create(
            UUID.randomUUID(),
            adminTheme.data,
            adminTheme.name,
            adminTheme.slug,
            userId,
            userId,
        ))
    }

    /**
     * Deletes an admin theme
     * 
     * @param themeId admin theme id
     * 
     * @return deleted admin theme
     */
    override suspend fun deleteAdminTheme(themeId: UUID): Response {
        loggedUserId ?: return createForbidden(UNAUTHORIZED)
        if (!isRealmMetaformAdmin) {
            return createForbidden(createNotAllowedMessage(CREATE, ADMIN_THEME))
        }

        val theme = adminThemeController.findById(themeId) ?: return createNotFound("No such admin theme: $themeId")
        adminThemeController.adminThemeDAO.delete(theme)
        return createNoContent()
    }

    /**
     * Finds admin theme by id
     * 
     * @param themeId admin theme id
     * 
     * @return admin theme
     */
    override suspend fun findAdminTheme(themeId: UUID): Response {
        loggedUserId ?: return createForbidden(UNAUTHORIZED)
        if (!isRealmMetaformAdmin) {
            return createForbidden(createNotAllowedMessage(CREATE, ADMIN_THEME))
        }

        val theme = adminThemeController.findById(themeId) ?: return createNotFound("No such admin theme: $themeId")
        return createOk(theme)
    }

    /**
     * Lists admin themes
     * 
     * @return admin themes
     */
    override suspend fun listAdminTheme(): Response {
        loggedUserId ?: return createForbidden(UNAUTHORIZED)
        if (!isRealmMetaformAdmin) {
            return createForbidden(createNotAllowedMessage(CREATE, ADMIN_THEME))
        }

        return createOk(adminThemeController.adminThemeDAO.listAll())
    }

    /**
     * Updates an admin theme
     * 
     * @param themeId admin theme id
     * @param adminTheme data to update with
     * 
     * @return updated admin theme
     */
    override suspend fun updateAdminTheme(themeId: UUID, adminTheme: AdminTheme): Response {
        loggedUserId ?: return createForbidden(UNAUTHORIZED)
        if (!isRealmMetaformAdmin) {
            return createForbidden(createNotAllowedMessage(CREATE, ADMIN_THEME))
        }

        val theme = adminThemeController.findById(themeId) ?: return createNotFound("No such admin theme: $themeId")
        return adminTheme.run {
            createOk(adminThemeController.updateAdminTheme(theme, data, name, slug))
        }
    }
}