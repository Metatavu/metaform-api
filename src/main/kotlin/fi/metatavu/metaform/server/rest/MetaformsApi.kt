package fi.metatavu.metaform.server.rest

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import fi.metatavu.metaform.api.spec.model.Metaform
import fi.metatavu.metaform.api.spec.model.MetaformField
import fi.metatavu.metaform.server.controllers.ExportThemeController
import fi.metatavu.metaform.server.utils.MetaformUtils
import fi.metatavu.metaform.server.keycloak.AuthorizationScope
import fi.metatavu.metaform.server.controllers.MetaformController
import fi.metatavu.metaform.server.exceptions.KeycloakClientNotFoundException
import fi.metatavu.metaform.server.persistence.model.Reply
import fi.metatavu.metaform.server.rest.translate.MetaformTranslator
import org.apache.commons.lang3.StringUtils
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

    if (!isRealmMetaformAdmin) {
      return createForbidden(createNotAllowedMessage(CREATE, METAFORM))
    }

    validateMetaform(metaform)?.let { return it }

    val exportTheme = if (metaform.exportThemeId != null) {
      exportThemeController.findExportTheme(metaform.exportThemeId) ?: return createBadRequest(createNotFoundMessage(EXPORT_THEME, metaform.exportThemeId))
    } else null

    metaform.slug?.let{ slug ->
      if (!metaformController.validateSlug(slug)) {
        return createConflict(createInvalidMessage(SLUG))
      } else if (!metaformController.isSlugUnique(null, slug)) {
        return createConflict(createDuplicatedMessage(SLUG))
      }
    }

    val metaformData = serializeMetaform(metaform) ?: return createBadRequest(createInvalidMessage(METAFORM))

    val createdMetaform: fi.metatavu.metaform.server.persistence.model.Metaform =
            metaformController.createMetaform(
                    exportTheme,
                    metaform.allowAnonymous ?: false,
                    metaform.title,
                    metaform.slug,
                    metaformData
            )
    try {
      updateMetaformPermissionGroups(createdMetaform.slug, metaform)
    } catch (e: KeycloakClientNotFoundException) {
      createInternalServerError(e.message!!)
    }
    return createOk(metaformTranslator.translate(createdMetaform))
  }

  /**
   * Validates incoming Metaform
   *
   * @param payload metaform data
   * @return validation error or null if metaform is valid
   */
  private fun validateMetaform(payload: Metaform): Response? {
    val keys = MetaformUtils.getMetaformFields(payload).map(MetaformField::name)

    val duplicates = keys
            .filter { key: String? -> Collections.frequency(keys, key) > 1 }
            .distinct()

    return if (duplicates.isNotEmpty()) {
      createBadRequest(String.format("Duplicate field names: %s", StringUtils.join(duplicates, ',')))
    } else null
  }

  /**
   * Serializes Metaform into JSON
   *
   * @param metaform Metaform
   * @return serialized Metaform
   */
  protected fun serializeMetaform(metaform: Metaform?): String? {
    val objectMapper = ObjectMapper()
    try {
      return objectMapper.writeValueAsString(metaform)
    } catch (e: JsonProcessingException) {
      logger.error("Failed to serialize metaform", e)
    }
    return null
  }

  /**
   * Updates permission groups to match metaform
   *
   * @param formSlug form slug
   * @param metaformEntity Metaform REST entity
   */
  private fun updateMetaformPermissionGroups(formSlug: String, metaformEntity: Metaform) {
    val adminClient = keycloakController.getAdminClient()
    val keycloakClient = try {
      keycloakController.getKeycloakClient(adminClient)
    } catch (e: KeycloakClientNotFoundException) {
      throw e
    }

    val groupNames = metaformController.getPermissionContextFields(metaformEntity)
            .map { field -> field.options
                    ?.map { option -> metaformController.getReplySecurityContextGroup(formSlug, field.name, option.name) } }
            .flatMap { it?.toList() ?: emptyList() }
    keycloakController.updatePermissionGroups(adminClient, keycloakClient, groupNames)
  }

  override suspend fun deleteMetaform(metaformId: UUID): Response {
    loggedUserId ?: return createForbidden(UNAUTHORIZED)

    if (!isRealmMetaformAdmin) {
      return createForbidden(createNotAllowedMessage(DELETE, METAFORM))
    }

    val metaform = metaformController.findMetaformById(metaformId)
            ?: return createNotFound(createNotFoundMessage(METAFORM, metaformId))

    metaformController.deleteMetaform(metaform)

    return createNoContent()
  }

  override suspend fun findMetaform(metaformId: UUID, replyId: UUID?, ownerKey: String?): Response {
    loggedUserId ?: return createForbidden(UNAUTHORIZED)

    val metaform = metaformController.findMetaformById(metaformId) ?: return createNotFound(createNotFoundMessage(METAFORM, metaformId))

    if (metaform.allowAnonymous == false && isAnonymous) {
      val reply: Reply? = if (replyId != null) replyController.findReplyById(replyId) else null
      if (reply == null || metaform.id != reply.metaform.id || ownerKey == null) {
        return createForbidden(createAnonNotAllowedMessage(FIND, METAFORM))
      }
      if (!isPermittedReply(reply, ownerKey, AuthorizationScope.REPLY_VIEW)) {
        return createForbidden(createAnonNotAllowedMessage(FIND, METAFORM))
      }
    }

    return createOk(metaformTranslator.translate(metaform))
  }

  override suspend fun listMetaforms(): Response {
    loggedUserId ?: return createForbidden(UNAUTHORIZED)

    return if (!isRealmUser) {
      createForbidden(createAnonNotAllowedMessage(LIST, METAFORM))
    } else createOk(metaformController.listMetaforms().map(metaformTranslator::translate))

  }

  override suspend fun updateMetaform(metaformId: UUID, metaform: Metaform): Response {
    loggedUserId ?: return createForbidden(UNAUTHORIZED)

    if (!isRealmMetaformAdmin) {
      return createForbidden(createNotAllowedMessage(UPDATE, METAFORM))
    }

    val foundMetaform = metaformController.findMetaformById(metaformId)
            ?: return createNotFound(createNotFoundMessage(METAFORM, metaformId))

    val data = serializeMetaform(metaform) ?: return createBadRequest(createInvalidMessage(METAFORM))

    validateMetaform(metaform)?.let{ return it }

    metaform.slug?.let{ slug ->
      if (!metaformController.validateSlug(slug)) {
        return createConflict(createInvalidMessage(SLUG))
      } else if (!metaformController.isSlugUnique(metaformId, slug)) {
        return createConflict(createDuplicatedMessage(SLUG))
      }
    }
    val formSlug = metaform.slug ?: foundMetaform.slug

    val exportTheme = if (metaform.exportThemeId != null) {
      exportThemeController.findExportTheme(metaform.exportThemeId) ?: return createBadRequest(createNotFoundMessage(EXPORT_THEME, metaform.exportThemeId))
    } else null

    try {
      updateMetaformPermissionGroups(formSlug, metaform)
    } catch (e: KeycloakClientNotFoundException) {
      return createInternalServerError(e.message!!)
    }

    return createOk(metaformTranslator.translate(metaformController.updateMetaform(
            foundMetaform,
            exportTheme,
            data,
            metaform.allowAnonymous ?: false,
            formSlug
    )))

  }
}