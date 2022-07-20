package fi.metatavu.metaform.server.rest

import fi.metatavu.metaform.api.spec.model.AdminTheme
import fi.metatavu.metaform.server.controllers.AdminThemeController
import fi.metatavu.metaform.server.exceptions.MalformedAdminThemeDataException
import fi.metatavu.metaform.server.rest.translate.AdminThemeTranslator
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

    @Inject
    lateinit var adminThemeTranslator: AdminThemeTranslator

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

        adminTheme.slug?.let{ slug ->
            if (!adminThemeController.validateSlug(slug)) {
                return createConflict(createInvalidMessage(SLUG))
              } else if (!adminThemeController.isSlugUnique(null, slug)) {
                return createConflict(createDuplicatedMessage(SLUG))
            }
        }

        val serializedDraftData = try {
            adminThemeController.serializeData(adminTheme.data)
        } catch (e: MalformedAdminThemeDataException) {
            return createBadRequest(createInvalidMessage(DRAFT))
        }

        val createdTheme = adminThemeController.create(
            UUID.randomUUID(),
            serializedDraftData,
            adminTheme.name,
            adminTheme.slug,
            userId
        )

        val translatedTheme = try {
            adminThemeTranslator.translate(createdTheme)
        } catch (e: Exception) {
            return createInternalServerError(e.message)
        }

        return createOk(translatedTheme)
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

        val theme = adminThemeController.findById(themeId) ?: return createNotFound(createNotFoundMessage(ADMIN_THEME, themeId))
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

        val theme = adminThemeController.findById(themeId) ?: return createNotFound(createNotFoundMessage(ADMIN_THEME, themeId))

        val translatedTheme = try {
            adminThemeTranslator.translate(theme)
        } catch (e: Exception) {
            return createInternalServerError(e.message)
        }
        return createOk(translatedTheme)
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

        val translatedThemes = try {
            adminThemeController.adminThemeDAO.listAll().map { adminThemeTranslator.translate(it) }
        } catch (e: Exception) {
            return createInternalServerError(e.message)
        }

        return createOk(translatedThemes)
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

        val foundAdminTheme = adminThemeController.findById(themeId) ?: return createNotFound(createNotFoundMessage(ADMIN_THEME, themeId))

        adminTheme.slug?.let{ slug ->
            if (!adminThemeController.validateSlug(slug)) {
                return createConflict(createInvalidMessage(SLUG))
            } else if (!adminThemeController.isSlugUnique(themeId, slug)) {
                return createConflict(createDuplicatedMessage(SLUG))
            }
        }

        val serializedDraftData = try {
            adminThemeController.serializeData(adminTheme.data)
        } catch (e: MalformedAdminThemeDataException) {
            return createBadRequest(createInvalidMessage(DRAFT))
        }

        val updatedAdminTheme = adminThemeController.updateAdminTheme(
            foundAdminTheme,
            serializedDraftData,
            adminTheme.name,
            adminTheme.slug
        )

        val translatedTheme = try {
            adminThemeTranslator.translate(updatedAdminTheme)
        } catch (e: Exception) {
            return createInternalServerError(e.message)
        }

        return createOk(translatedTheme)
    }
}