package fi.metatavu.metaform.server.rest

import fi.metatavu.metaform.api.spec.model.Draft
import fi.metatavu.metaform.server.controllers.DraftController
import fi.metatavu.metaform.server.controllers.MetaformController
import fi.metatavu.metaform.server.exceptions.MalformedDraftDataException
import fi.metatavu.metaform.server.persistence.model.Metaform
import fi.metatavu.metaform.server.rest.translate.DraftTranslator
import fi.metatavu.metaform.server.rest.translate.MetaformTranslator
import org.apache.commons.lang3.BooleanUtils
import java.util.*
import javax.enterprise.context.RequestScoped
import javax.inject.Inject
import javax.transaction.Transactional
import javax.ws.rs.core.Response

@RequestScoped
@Transactional
class DraftsApi: fi.metatavu.metaform.api.spec.DraftsApi, AbstractApi() {

  @Inject
  lateinit var metaformController: MetaformController

  @Inject
  lateinit var metaformTranslator: MetaformTranslator

  @Inject
  lateinit var draftController: DraftController

  @Inject
  lateinit var draftTranslator: DraftTranslator

  override suspend fun createDraft(metaformId: UUID, draft: Draft): Response {
    val userId = loggedUserId ?: return createForbidden(UNAUTHORIZED)

    val metaform: Metaform = metaformController.findMetaformById(metaformId)
            ?: return createNotFound(createNotFoundMessage(METAFORM, metaformId))

    val foundMetaform = metaformTranslator.translate(metaform)!!
    if (BooleanUtils.isNotTrue(foundMetaform.allowDrafts)) {
      return createForbidden(createNotAllowedMessage(CREATE, DRAFT))
    }

    if (!metaform.allowAnonymous!! && isAnonymous) {
      return createForbidden(createAnonNotAllowedMessage(CREATE, DRAFT))
    }

    val serializedDraftData = try {
      draftController.serializeData(draft.data)
    } catch (e: MalformedDraftDataException) {
      return createBadRequest(createInvalidMessage(DRAFT))
    }

    val createdDraft = draftController.createDraft(metaform, userId, serializedDraftData)
    val draftEntity: Draft = draftTranslator.translateDraft(createdDraft)

    return createOk(draftEntity)
  }

  override suspend fun deleteDraft(metaformId: UUID, draftId: UUID): Response {
    loggedUserId ?: return createForbidden(UNAUTHORIZED)

    val metaform = metaformController.findMetaformById(metaformId)
            ?: return createNotFound(createNotFoundMessage(METAFORM, metaformId))

    val draft = draftController.findDraftById(draftId)
            ?: return createNotFound(createNotFoundMessage(DRAFT, metaformId))

    if (!isRealmMetaformAdmin) {
      return createForbidden(createNotAllowedMessage(DELETE, DRAFT))
    }

    if (draft.metaform?.id != metaform.id) {
      return createNotFound(createNotBelongMessage(DRAFT))
    }

    draftController.deleteDraft(draft)

    return createNoContent()
  }

  override suspend fun findDraft(metaformId: UUID, draftId: UUID): Response {
    loggedUserId ?: return createForbidden(UNAUTHORIZED)

    val metaform = metaformController.findMetaformById(metaformId)
            ?: return createNotFound(createNotFoundMessage(METAFORM, metaformId))

    val draft = draftController.findDraftById(draftId)
            ?: return createNotFound(createNotFoundMessage(DRAFT, draftId))

    return if (draft.metaform?.id != metaform.id) {
      createNotFound(createNotBelongMessage(DRAFT))
    } else createOk(draftTranslator.translateDraft(draft))

  }

  override suspend fun updateDraft(metaformId: UUID, draftId: UUID, draft: Draft): Response {
    loggedUserId ?: return createForbidden(UNAUTHORIZED)

    val metaform = metaformController.findMetaformById(metaformId)
            ?: return createNotFound(createNotFoundMessage(METAFORM, metaformId))

    val metaformEntity = metaformTranslator.translate(metaform)
    if (BooleanUtils.isNotTrue(metaformEntity?.allowDrafts)) {
      return createForbidden(createNotAllowedMessage(UPDATE, DRAFT))
    }

    if (metaform.allowAnonymous != true && isAnonymous) {
      return createForbidden(createAnonNotAllowedMessage(UPDATE, DRAFT))
    }

    val foundDraft = draftController.findDraftById(draftId)
            ?: return createNotFound(createNotFoundMessage(DRAFT, draftId))

    if (foundDraft.metaform?.id != metaform.id) {
      return createNotFound(createNotBelongMessage(DRAFT))
    }

    val serializedDraftData = try {
      draftController.serializeData(draft.data)
    } catch (e: MalformedDraftDataException) {
      return createBadRequest(createInvalidMessage(DRAFT))
    }

    val updatedDraft = draftController.updateDraft(foundDraft, serializedDraftData)

    return createOk(draftTranslator.translateDraft(updatedDraft))
  }


}