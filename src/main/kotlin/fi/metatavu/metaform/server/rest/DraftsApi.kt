package fi.metatavu.metaform.server.rest

import fi.metatavu.metaform.api.spec.model.Draft
import fi.metatavu.metaform.server.controllers.DraftController
import fi.metatavu.metaform.server.controllers.MetaformController
import fi.metatavu.metaform.server.exceptions.DeserializationFailedException
import fi.metatavu.metaform.server.exceptions.MalformedDraftDataException
import fi.metatavu.metaform.server.exceptions.MalformedMetaformJsonException
import fi.metatavu.metaform.server.persistence.model.Metaform
import fi.metatavu.metaform.server.rest.translate.DraftTranslator
import fi.metatavu.metaform.server.rest.translate.MetaformTranslator
import org.apache.commons.lang3.BooleanUtils
import java.util.*
import jakarta.enterprise.context.RequestScoped
import jakarta.inject.Inject
import jakarta.transaction.Transactional
import jakarta.ws.rs.core.Response

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

  /**
   * Creates a new draft
   * 
   * @param metaformId Metaform id
   * @param draft Draft to create
   * @return Created draft
   */
  override fun createDraft(metaformId: UUID, draft: Draft): Response {
    val userId = loggedUserId ?: return createForbidden(UNAUTHORIZED)

    val metaform: Metaform = metaformController.findMetaformById(metaformId)
            ?: return createNotFound(createNotFoundMessage(METAFORM, metaformId))

    val translatedMetaform = metaformTranslator.translate(metaform)
    if (BooleanUtils.isNotTrue(translatedMetaform.allowDrafts)) {
      return createForbidden(createNotAllowedMessage(CREATE, DRAFT))
    }

    if (metaform.allowAnonymous != true && isAnonymous) {
      return createForbidden(ANONYMOUS_USERS_METAFORM_MESSAGE)
    }

    val serializedDraftData = try {
      draftController.serializeData(draft.data)
    } catch (e: MalformedDraftDataException) {
      return createBadRequest(createInvalidMessage(DRAFT))
    }

    val createdDraft = draftController.createDraft(metaform, userId, serializedDraftData)
    val draftEntity: Draft = try {
       draftTranslator.translateDraft(createdDraft)
    } catch (e: DeserializationFailedException) {
      return createInternalServerError(e.message)
    }

    return createOk(draftEntity)
  }

  /**
   * Deletes a draft
   * 
   * @param metaformId Metaform id
   * @param draftId Draft id
   */
  override fun deleteDraft(metaformId: UUID, draftId: UUID): Response {
    loggedUserId ?: return createForbidden(UNAUTHORIZED)

    val metaform = metaformController.findMetaformById(metaformId)
            ?: return createNotFound(createNotFoundMessage(METAFORM, metaformId))

    val metaformEntity = try {
      metaformTranslator.translate(metaform)
    } catch (e: MalformedMetaformJsonException) {
      return createInternalServerError(e.message)
    }

    if (metaform.allowAnonymous != true && isAnonymous) {
      return createForbidden(ANONYMOUS_USERS_METAFORM_MESSAGE)
    }

    if (BooleanUtils.isNotTrue(metaformEntity.allowDrafts)) {
      return createForbidden(createNotAllowedMessage(UPDATE, DRAFT))
    }


    val draft = draftController.findDraftById(draftId)
      ?: return createNotFound(createNotFoundMessage(DRAFT, metaformId))


    if (draft.metaform?.id != metaform.id) {
      return createNotFound(createNotBelongMessage(DRAFT))
    }

    draftController.deleteDraft(draft)

    return createNoContent()
  }

  /**
   * Finds a draft by id
   * 
   * @param metaformId Metaform id
   * @param draftId Draft id
   */
  override fun findDraft(metaformId: UUID, draftId: UUID): Response {
    loggedUserId ?: return createForbidden(UNAUTHORIZED)

    val metaform = metaformController.findMetaformById(metaformId)
            ?: return createNotFound(createNotFoundMessage(METAFORM, metaformId))

    val translatedMetaform = metaformTranslator.translate(metaform)

    if (metaform.allowAnonymous != true && isAnonymous) {
      return createForbidden(ANONYMOUS_USERS_METAFORM_MESSAGE)
    }

    if (BooleanUtils.isNotTrue(translatedMetaform.allowDrafts)) {
      return createForbidden(createNotAllowedMessage(CREATE, DRAFT))
    }

    val draft = draftController.findDraftById(draftId)
            ?: return createNotFound(createNotFoundMessage(DRAFT, draftId))


    if (draft.metaform?.id != metaform.id) {
      createNotFound(createNotBelongMessage(DRAFT))
    }

    if (draft.userId != loggedUserId) {
      return createForbidden(createNotOwnedMessage(DRAFT))
    }

    return try {
      createOk(draftTranslator.translateDraft(draft))
    } catch (e: DeserializationFailedException) {
      createInternalServerError(e.message)
    }
  }

  override fun listDrafts(metaformId: UUID): Response {
    loggedUserId ?: return createForbidden(UNAUTHORIZED)

    val metaform = metaformController.findMetaformById(metaformId)
      ?: return createNotFound(createNotFoundMessage(METAFORM, metaformId))

    val translatedMetaform = metaformTranslator.translate(metaform)

    if (metaform.allowAnonymous != true && isAnonymous) {
      return createForbidden(ANONYMOUS_USERS_METAFORM_MESSAGE)
    }

    if (BooleanUtils.isNotTrue(translatedMetaform.allowDrafts)) {
      return createForbidden(createNotAllowedMessage(CREATE, DRAFT))
    }

    return try {
      createOk(draftController.listByMetaform(metaform, null, null).map(draftTranslator::translateDraft))
    } catch (e: DeserializationFailedException) {
      createInternalServerError(e.message)
    }
  }

  /**
   * Update a draft
   * 
   * @param metaformId Metaform id
   * @param draftId Draft id
   * @param draft Draft data
   */
  override fun updateDraft(metaformId: UUID, draftId: UUID, draft: Draft): Response {
    loggedUserId ?: return createForbidden(UNAUTHORIZED)

    val metaform = metaformController.findMetaformById(metaformId)
            ?: return createNotFound(createNotFoundMessage(METAFORM, metaformId))

    val metaformEntity = try {
      metaformTranslator.translate(metaform)
    } catch (e: MalformedMetaformJsonException) {
      return createInternalServerError(e.message)
    }

    if (metaform.allowAnonymous != true && isAnonymous) {
      return createForbidden(ANONYMOUS_USERS_METAFORM_MESSAGE)
    }

    if (BooleanUtils.isNotTrue(metaformEntity.allowDrafts)) {
      return createForbidden(createNotAllowedMessage(UPDATE, DRAFT))
    }

    val foundDraft = draftController.findDraftById(draftId)
            ?: return createNotFound(createNotFoundMessage(DRAFT, draftId))

    if (foundDraft.metaform?.id != metaform.id) {
      return createNotFound(createNotBelongMessage(DRAFT))
    }

    if (foundDraft.userId != loggedUserId) {
      return createForbidden(createNotOwnedMessage(DRAFT))
    }

    val serializedDraftData = try {
      draftController.serializeData(draft.data)
    } catch (e: MalformedDraftDataException) {
      return createBadRequest(createInvalidMessage(DRAFT))
    }

    val updatedDraft = draftController.updateDraft(foundDraft, serializedDraftData)

    return try {
      createOk(draftTranslator.translateDraft(updatedDraft))
    } catch (e: DeserializationFailedException) {
      createInternalServerError(e.message)
    }
  }


}