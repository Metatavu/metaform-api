package fi.metatavu.metaform.server.rest

import fi.metatavu.metaform.api.spec.model.MetaformVersion
import fi.metatavu.metaform.server.controllers.MetaformController
import fi.metatavu.metaform.server.controllers.MetaformVersionController
import fi.metatavu.metaform.server.exceptions.DeserializationFailedException
import fi.metatavu.metaform.server.exceptions.MalformedVersionJsonException
import fi.metatavu.metaform.server.persistence.model.Metaform
import fi.metatavu.metaform.server.rest.translate.MetaformVersionTranslator
import java.util.*
import javax.enterprise.context.RequestScoped
import javax.inject.Inject
import javax.transaction.Transactional
import javax.ws.rs.core.Response

@RequestScoped
@Transactional
class VersionsApi: fi.metatavu.metaform.api.spec.VersionsApi, AbstractApi() {

  @Inject
  lateinit var metaformController: MetaformController

  @Inject
  lateinit var metaformVersionController: MetaformVersionController

  @Inject
  lateinit var metaformVersionTranslator: MetaformVersionTranslator

  override suspend fun createMetaformVersion(
    metaformId: UUID,
    metaformVersion: MetaformVersion
  ): Response {
    val userId = loggedUserId ?: return createForbidden(UNAUTHORIZED)

    if (!isRealmMetaformAdmin) {
      return createForbidden(createNotAllowedMessage(CREATE, METAFORM))
    }

    val foundMetaform: Metaform = metaformController.findMetaformById(metaformId)
            ?: return createNotFound(createNotFoundMessage(METAFORM, metaformId))

    val createdMetaformVersion = try {
      metaformVersionController.create(
              foundMetaform,
              metaformVersion.type,
              metaformVersion.data,
              userId
      )
    } catch (e: MalformedVersionJsonException) {
      return createBadRequest(createInvalidMessage(METAFORM_VERSION))
    }

    return try {
      createOk(metaformVersionTranslator.translate(createdMetaformVersion))
    } catch (e: DeserializationFailedException) {
      createInternalServerError(e.message)
    }
  }

  override suspend fun deleteMetaformVersion(metaformId: UUID, versionId: UUID): Response {
    loggedUserId ?: return createForbidden(UNAUTHORIZED)

    if (!isRealmMetaformAdmin) {
      return createForbidden(createNotAllowedMessage(DELETE, METAFORM_VERSION))
    }

    val foundMetaform = metaformController.findMetaformById(metaformId)
            ?: return createNotFound(createNotFoundMessage(METAFORM, metaformId))

    val foundMetaformVersion = metaformVersionController.findMetaformVersionById(versionId)
            ?: return createNotFound(createNotFoundMessage(METAFORM_VERSION, versionId))

    if (foundMetaformVersion.metaform?.id != foundMetaform.id) {
      return createNotFound(createNotBelongMessage(METAFORM_VERSION))
    }

    metaformVersionController.deleteMetaformVersion(foundMetaformVersion)

    return createNoContent()
  }

  override suspend fun findMetaformVersion(metaformId: UUID, versionId: UUID): Response {
    loggedUserId ?: return createForbidden(UNAUTHORIZED)

    if (!isRealmMetaformAdmin) {
      return createForbidden(createNotAllowedMessage(FIND, METAFORM_VERSION))
    }

    val foundMetaform = metaformController.findMetaformById(metaformId)
            ?: return createNotFound(createNotFoundMessage(METAFORM, metaformId))

    val foundMetaformVersion = metaformVersionController.findMetaformVersionById(versionId)
            ?: return createNotFound(createNotFoundMessage(METAFORM_VERSION, versionId))

    if (foundMetaformVersion.metaform?.id != foundMetaform.id) {
      return createNotFound(createNotBelongMessage(METAFORM_VERSION))
    }

    return try {
      createOk(metaformVersionTranslator.translate(foundMetaformVersion))
    } catch (e: DeserializationFailedException) {
      createInternalServerError(e.message)
    }
  }

  override suspend fun listMetaformVersions(metaformId: UUID): Response {
    loggedUserId ?: return createForbidden(UNAUTHORIZED)

    if (!isRealmMetaformAdmin) {
      return createForbidden(createNotAllowedMessage(LIST, METAFORM_VERSION))
    }

    val foundMetaform = metaformController.findMetaformById(metaformId)
            ?: return createNotFound(createNotFoundMessage(METAFORM, metaformId))

    return createOk(metaformVersionController.listMetaformVersionsByMetaform(foundMetaform)
            .map(metaformVersionTranslator::translate))
  }
}