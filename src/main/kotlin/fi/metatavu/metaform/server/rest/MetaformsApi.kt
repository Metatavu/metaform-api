package fi.metatavu.metaform.server.rest

import fi.metatavu.metaform.api.spec.model.Metaform
import fi.metatavu.metaform.api.spec.model.MetaformVisibility
import fi.metatavu.metaform.server.controllers.ExportThemeController
import fi.metatavu.metaform.server.keycloak.AuthorizationScope
import fi.metatavu.metaform.server.controllers.MetaformController
import fi.metatavu.metaform.server.exceptions.KeycloakClientNotFoundException
import fi.metatavu.metaform.server.exceptions.MalformedMetaformJsonException
import fi.metatavu.metaform.server.metaform.SlugValidation
import fi.metatavu.metaform.server.persistence.model.Reply
import fi.metatavu.metaform.server.rest.translate.MetaformTranslator
import org.slf4j.Logger
import java.util.*
import javax.enterprise.context.RequestScoped
import javax.inject.Inject
import javax.transaction.Transactional
import javax.ws.rs.core.Response

@RequestScoped
@Transactional
class MetaformsApi: fi.metatavu.metaform.api.spec.MetaformsApi, AbstractApi() {

  @Inject
  lateinit var logger: Logger

  @Inject
  lateinit var metaformController: MetaformController

  @Inject
  lateinit var metaformTranslator: MetaformTranslator

  @Inject
  lateinit var exportThemeController: ExportThemeController

  override suspend fun createMetaform(metaform: Metaform): Response {
    loggedUserId ?: return createForbidden(UNAUTHORIZED)

    // TODO remove deprecated roles from kc.json
    // TODO check doc
    if (!isMetaformAdminAny) {
      return createForbidden(createNotAllowedMessage(CREATE, METAFORM))
    }

    if (!metaformController.validateMetaform(metaform)) {
      createBadRequest("Duplicate field names")
    }

    val exportTheme = if (metaform.exportThemeId != null) {
      exportThemeController.findExportTheme(metaform.exportThemeId)
        ?: return createBadRequest(createNotFoundMessage(EXPORT_THEME, metaform.exportThemeId))
    } else null

    metaform.slug?.let{ slug ->
      when (metaformController.validateSlug(null, slug)) {
        SlugValidation.INVALID -> return createConflict(createInvalidMessage(SLUG))
        SlugValidation.DUPLICATED -> return createConflict(createDuplicatedMessage(SLUG))
        else -> return@let
      }
    }

    val metaformData =  try {
      metaformController.serializeMetaform(metaform)
    } catch (e: MalformedMetaformJsonException) {
      createInvalidMessage(createInvalidMessage(METAFORM))
    }

    val createdMetaform =
      metaformController.createMetaform(
        exportTheme = exportTheme,
        allowAnonymous = metaform.allowAnonymous ?: false,
        visibility = metaform.visibility ?: MetaformVisibility.PRIVATE,
        title = metaform.title,
        slug = metaform.slug,
        data = metaformData
      )
    try {
      metaformController.updateMetaformPermissionGroups(createdMetaform.slug, metaform)
    } catch (e: KeycloakClientNotFoundException) {
      createInternalServerError(e.message)
    }

    return try {
      createOk(metaformTranslator.translate(createdMetaform))
    } catch (e: MalformedMetaformJsonException) {
      createInternalServerError(e.message)
    }
  }

  override suspend fun deleteMetaform(metaformId: UUID): Response {
    loggedUserId ?: return createForbidden(UNAUTHORIZED)

    if (!isMetaformAdmin(metaformId)) {
      return createForbidden(createNotAllowedMessage(DELETE, METAFORM))
    }

    val metaform = metaformController.findMetaformById(metaformId)
      ?: return createNotFound(createNotFoundMessage(METAFORM, metaformId))

    metaformController.deleteMetaform(metaform)

    return createNoContent()
  }

  override suspend fun findMetaform(metaformId: UUID, replyId: UUID?, ownerKey: String?): Response {
    loggedUserId ?: return createForbidden(UNAUTHORIZED)

    val reply: Reply? = if (replyId != null) replyController.findReplyById(replyId) else null

    val metaform = metaformController.findMetaformById(metaformId)
      ?: return createNotFound(createNotFoundMessage(METAFORM, metaformId))

    val translatedMetaform = try {
      metaformTranslator.translate(metaform)
    } catch (e: MalformedMetaformJsonException) {
      createInternalServerError(e.message)
    }

    if (isPermittedReply(reply, ownerKey, AuthorizationScope.REPLY_VIEW) && metaform.id == reply?.metaform?.id) {
      return createOk(translatedMetaform)
    }

    return when (metaform.visibility!!) {
      MetaformVisibility.PUBLIC -> {
        try {
          createOk(translatedMetaform)
        } catch (e: MalformedMetaformJsonException) {
          createInternalServerError(e.message)
        }
      }
      MetaformVisibility.PRIVATE -> {
        if (isMetaformManager(metaformId)) {
          createOk(translatedMetaform)
        }
        else {
          createForbidden(createNotAllowedMessage(FIND, METAFORM))
        }
      }
    }
  }

  override suspend fun listMetaforms(visibility: MetaformVisibility?): Response {
    loggedUserId ?: return createForbidden(UNAUTHORIZED)

    // TODO check permission the doc says metaform admin and manager but not specified which metaform
    return try {
      when(visibility) {
        MetaformVisibility.PUBLIC ->
          createOk(metaformController.listMetaforms(visibility).map(metaformTranslator::translate))
        MetaformVisibility.PRIVATE -> {
          if (!isMetaformManagerAny) {
            createForbidden(createNotAllowedMessage(LIST, METAFORM))
          }
          else {
            createOk(metaformController.listMetaforms(visibility).map(metaformTranslator::translate))
          }
        }
        null -> {
          if (isMetaformManagerAny) {
            createOk(metaformController.listMetaforms().map(metaformTranslator::translate))
          }
          else {
            createOk(metaformController.listMetaforms(MetaformVisibility.PUBLIC).map(metaformTranslator::translate))
          }
        }
      }
    } catch (e: MalformedMetaformJsonException) {
      createInternalServerError(e.message)
    }
  }

  override suspend fun updateMetaform(metaformId: UUID, metaform: Metaform): Response {
    loggedUserId ?: return createForbidden(UNAUTHORIZED)

    val foundMetaform = metaformController.findMetaformById(metaformId)
            ?: return createNotFound(createNotFoundMessage(METAFORM, metaformId))

    if (!isMetaformAdmin(metaformId)) {
      return createForbidden(createNotAllowedMessage(UPDATE, METAFORM))
    }

    val data =  try {
      metaformController.serializeMetaform(metaform)
    } catch (e: MalformedMetaformJsonException) {
      createInvalidMessage(createInvalidMessage(METAFORM))
    }

    if (!metaformController.validateMetaform(metaform)) {
      createBadRequest("Duplicate field names")
    }

    val formSlug = metaform.slug?.let{ slug ->
      when (metaformController.validateSlug(metaformId, slug)) {
        SlugValidation.INVALID -> return createConflict(createInvalidMessage(SLUG))
        SlugValidation.DUPLICATED -> return createConflict(createDuplicatedMessage(SLUG))
        else -> metaform.slug
      }
    } ?: foundMetaform.slug

    val exportTheme = if (metaform.exportThemeId != null) {
      exportThemeController.findExportTheme(metaform.exportThemeId)
        ?: return createBadRequest(createNotFoundMessage(EXPORT_THEME, metaform.exportThemeId))
    } else null

    try {
      metaformController.updateMetaformPermissionGroups(formSlug, metaform)
    } catch (e: KeycloakClientNotFoundException) {
      return createInternalServerError(e.message)
    }

    val updatedMetaform = metaformController.updateMetaform(
      metaform = foundMetaform,
      exportTheme = exportTheme,
      visibility = metaform.visibility ?: foundMetaform.visibility!!,
      data = data,
      allowAnonymous = metaform.allowAnonymous ?: false,
      slug = formSlug
    )

    return try {
      createOk(metaformTranslator.translate(updatedMetaform))
    } catch (e: MalformedMetaformJsonException) {
      createInternalServerError(e.message)
    }
  }
}