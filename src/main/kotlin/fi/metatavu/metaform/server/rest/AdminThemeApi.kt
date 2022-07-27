package fi.metatavu.metaform.server.rest

import fi.metatavu.metaform.api.spec.model.AdminTheme
import fi.metatavu.metaform.server.controllers.AdminThemeController
import fi.metatavu.metaform.server.exceptions.MalformedAdminThemeDataException
import fi.metatavu.metaform.server.metaform.SlugValidation
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

    override suspend fun createAdminTheme(adminTheme: AdminTheme): Response {
        val userId = loggedUserId ?: return createForbidden(UNAUTHORIZED)
        if (!isRealmSystemAdmin) {
            return createForbidden(createNotAllowedMessage(CREATE, ADMIN_THEME))
        }

        adminTheme.slug?.let{ slug ->
            when (adminThemeController.validateSlug(null, slug)) {
                SlugValidation.INVALID -> return createConflict(createInvalidMessage(SLUG))
                SlugValidation.DUPLICATED -> return createConflict(createDuplicatedMessage(SLUG))
                else -> return@let
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

    override suspend fun deleteAdminTheme(themeId: UUID): Response {
        loggedUserId ?: return createForbidden(UNAUTHORIZED)
        if (!isRealmSystemAdmin) {
            return createForbidden(createNotAllowedMessage(CREATE, ADMIN_THEME))
        }

        val theme = adminThemeController.findById(themeId) ?: return createNotFound(createNotFoundMessage(ADMIN_THEME, themeId))
        adminThemeController.adminThemeDAO.delete(theme)
        return createNoContent()
    }

    override suspend fun findAdminTheme(themeId: UUID): Response {
        loggedUserId ?: return createForbidden(UNAUTHORIZED)
        if (!isMetaformManagerAny) {
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

    override suspend fun listAdminTheme(): Response {
        loggedUserId ?: return createForbidden(UNAUTHORIZED)
        if (!isRealmSystemAdmin) {
            return createForbidden(createNotAllowedMessage(CREATE, ADMIN_THEME))
        }

        val translatedThemes = try {
            adminThemeController.adminThemeDAO.listAll().map { adminThemeTranslator.translate(it) }
        } catch (e: Exception) {
            return createInternalServerError(e.message)
        }

        return createOk(translatedThemes)
    }

    override suspend fun updateAdminTheme(themeId: UUID, adminTheme: AdminTheme): Response {
        loggedUserId ?: return createForbidden(UNAUTHORIZED)
        if (!isRealmSystemAdmin) {
            return createForbidden(createNotAllowedMessage(CREATE, ADMIN_THEME))
        }

        val foundAdminTheme = adminThemeController.findById(themeId) ?: return createNotFound(createNotFoundMessage(ADMIN_THEME, themeId))

        adminTheme.slug?.let{ slug ->
            when (adminThemeController.validateSlug(themeId, slug)) {
                SlugValidation.INVALID -> return createConflict(createInvalidMessage(SLUG))
                SlugValidation.DUPLICATED -> return createConflict(createDuplicatedMessage(SLUG))
                else -> adminTheme.slug
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
            adminTheme.slug ?: foundAdminTheme.slug
        )

        val translatedTheme = try {
            adminThemeTranslator.translate(updatedAdminTheme)
        } catch (e: Exception) {
            return createInternalServerError(e.message)
        }

        return createOk(translatedTheme)
    }
}