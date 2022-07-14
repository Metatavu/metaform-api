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

    override suspend fun deleteAdminTheme(themeId: UUID): Response {
        val userId = loggedUserId ?: return createForbidden(UNAUTHORIZED)
        if (!isRealmMetaformAdmin) {
            return createForbidden(createNotAllowedMessage(CREATE, ADMIN_THEME))
        }

        val theme = adminThemeController.findById(themeId) ?: return createNotFound("No such admin theme: $themeId")
        adminThemeController.adminThemeDAO.delete(theme)
        return createNoContent()
    }

    override suspend fun findAdminTheme(themeId: UUID): Response {
        loggedUserId ?: return createForbidden(UNAUTHORIZED)
        if (!isRealmMetaformAdmin) {
            return createForbidden(createNotAllowedMessage(CREATE, ADMIN_THEME))
        }

        val theme = adminThemeController.findById(themeId) ?: return createNotFound("No such admin theme: $themeId")
        return createOk(theme)
    }

    override suspend fun listAdminTheme(): Response {
        loggedUserId ?: return createForbidden(UNAUTHORIZED)
        if (!isRealmMetaformAdmin) {
            return createForbidden(createNotAllowedMessage(CREATE, ADMIN_THEME))
        }

        return createOk(adminThemeController.adminThemeDAO.listAll())
    }

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