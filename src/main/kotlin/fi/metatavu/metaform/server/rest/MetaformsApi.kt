package fi.metatavu.metaform.server.rest

import fi.metatavu.metaform.api.spec.model.Metaform
import fi.metatavu.metaform.api.spec.model.MetaformMemberRole
import fi.metatavu.metaform.api.spec.model.MetaformVisibility
import fi.metatavu.metaform.server.controllers.ExportThemeController
import fi.metatavu.metaform.server.keycloak.AuthorizationScope
import fi.metatavu.metaform.server.controllers.MetaformController
import fi.metatavu.metaform.server.exceptions.MalformedMetaformJsonException
import fi.metatavu.metaform.server.metaform.SlugValidation
import fi.metatavu.metaform.server.persistence.model.Reply
import fi.metatavu.metaform.server.rest.translate.MetaformTranslator
import fi.metatavu.metaform.server.utils.MetaformUtils
import org.slf4j.Logger
import java.util.*
import javax.enterprise.context.RequestScoped
import javax.inject.Inject
import javax.transaction.Transactional
import javax.ws.rs.core.Response

@RequestScoped
@Transactional
@Suppress("unused")
class MetaformsApi: fi.metatavu.metaform.api.spec.MetaformsApi, AbstractApi() {

  @Inject
  lateinit var logger: Logger

  @Inject
  lateinit var metaformController: MetaformController

  @Inject
  lateinit var metaformTranslator: MetaformTranslator

  @Inject
  lateinit var exportThemeController: ExportThemeController

  override fun createMetaform(metaform: Metaform): Response {
    val userId = loggedUserId ?: return createForbidden(UNAUTHORIZED)

    if (!isRealmSystemAdmin) {
      return createForbidden(createNotAllowedMessage(CREATE, METAFORM))
    }

    if (!metaformController.validateMetaform(metaform)) {
      return createBadRequest("Duplicate field names")
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

    if (!metaformController.validatePermissionGroups(MetaformUtils.getPermissionGroups(metaform = metaform))) {
      return createBadRequest("Invalid permission groups")
    }

    val metaformData = try {
      metaformController.serializeMetaform(metaform)
    } catch (e: MalformedMetaformJsonException) {
      createInvalidMessage(createInvalidMessage(METAFORM))
    }

    val createdMetaform = metaformController.createMetaform(
      exportTheme = exportTheme,
      allowAnonymous = metaform.allowAnonymous ?: false,
      visibility = metaform.visibility ?: MetaformVisibility.PRIVATE,
      publishedAt = metaform.publishedAt,
      nonBillable = metaform.nonBillable,
      title = metaform.title,
      slug = metaform.slug,
      data = metaformData,
      creatorId = userId
    )

    return try {
      createOk(metaformTranslator.translate(createdMetaform))
    } catch (e: MalformedMetaformJsonException) {
      createInternalServerError(e.message)
    }
  }

  override fun deleteMetaform(metaformId: UUID): Response {
    loggedUserId ?: return createForbidden(UNAUTHORIZED)

    if (!isMetaformAdmin(metaformId)) {
      return createForbidden(createNotAllowedMessage(DELETE, METAFORM))
    }

    val metaform = metaformController.findMetaformById(metaformId)
      ?: return createNotFound(createNotFoundMessage(METAFORM, metaformId))

    metaformController.deleteMetaform(metaform)

    return createNoContent()
  }

  override fun findMetaform(
    metaformSlug: String?,
    metaformId: UUID?,
    replyId: UUID?,
    ownerKey: String?
  ): Response {
    loggedUserId ?: return createForbidden(UNAUTHORIZED)

    val metaform = if (metaformSlug != null) {
      metaformController.findMetaformBySlug(metaformSlug)
        ?: return createNotFound(createSlugNotFoundMessage(METAFORM, metaformSlug))
    } else if (metaformId != null) {
      metaformController.findMetaformById(metaformId)
        ?: return createNotFound(createNotFoundMessage(METAFORM, metaformId))
    } else {
      return createBadRequest("Invalid request")
    }

    val translatedMetaform = try {
      metaformTranslator.translate(metaform)
    } catch (e: MalformedMetaformJsonException) {
      createInternalServerError(e.message)
    }

    val reply: Reply? = if (replyId != null) replyController.findReplyById(replyId) else null

    if (isPermittedReply(reply, ownerKey, AuthorizationScope.REPLY_VIEW) && metaform.id == reply?.metaform?.id) {
      return createOk(translatedMetaform)
    }

    return when (metaform.visibility!!) {
      MetaformVisibility.PUBLIC -> {
          createOk(translatedMetaform)
      }
      MetaformVisibility.PRIVATE -> {
        if (!isAnonymous) {
          createOk(translatedMetaform)
        } else {
          createForbidden(createNotAllowedMessage(FIND, METAFORM))
        }
      }
    }
  }

  override fun listMetaforms(visibility: MetaformVisibility?, memberRole: MetaformMemberRole?): Response {
    loggedUserId ?: return createForbidden(UNAUTHORIZED)

    if (!isMetaformManagerAny && visibility == MetaformVisibility.PRIVATE) {
      return createForbidden(createNotAllowedMessage(LIST, METAFORM))
    }

    val metaforms = metaformController.listMetaforms(visibility = visibility)
      .filter { metaform ->
        val metaformId = metaform.id!!
        metaform.visibility == MetaformVisibility.PUBLIC || isMetaformManager(metaformId)
      }
      .filter { metaform ->
        val metaformId = metaform.id!!
        when (memberRole) {
          MetaformMemberRole.ADMINISTRATOR -> isMetaformAdmin(metaformId)
          MetaformMemberRole.MANAGER -> isMetaformManager(metaformId)
          else -> true
        }
      }

    return createOk(metaforms.map(metaformTranslator::translate))
  }

  override fun updateMetaform(metaformId: UUID, metaform: Metaform): Response {
    val userId = loggedUserId ?: return createForbidden(UNAUTHORIZED)

    val foundMetaform = metaformController.findMetaformById(metaformId)
            ?: return createNotFound(createNotFoundMessage(METAFORM, metaformId))

    if (!isMetaformAdmin(metaformId)) {
      return createForbidden(createNotAllowedMessage(UPDATE, METAFORM))
    }

    val data = try {
      metaformController.serializeMetaform(metaform)
    } catch (e: MalformedMetaformJsonException) {
      createInvalidMessage(createInvalidMessage(METAFORM))
    }

    if (!metaformController.validateMetaform(metaform)) {
      return createBadRequest("Duplicate field names")
    }

    val permissionGroups = MetaformUtils.getPermissionGroups(metaform = metaform)

    if (!metaformController.validatePermissionGroups(permissionGroups = permissionGroups)) {
      return createBadRequest("Invalid permission groups")
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

    val updatedMetaform = metaformController.updateMetaform(
      metaform = foundMetaform,
      exportTheme = exportTheme,
      visibility = metaform.visibility ?: foundMetaform.visibility!!,
      data = data,
      allowAnonymous = metaform.allowAnonymous ?: false,
      slug = formSlug,
      lastModifierId = userId
    )

    return try {
      createOk(metaformTranslator.translate(updatedMetaform))
    } catch (e: MalformedMetaformJsonException) {
      createInternalServerError(e.message)
    }
  }
}