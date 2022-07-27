package fi.metatavu.metaform.server.rest

import fi.metatavu.metaform.api.spec.model.MetaformMemberGroup
import fi.metatavu.metaform.server.controllers.MetaformController
import fi.metatavu.metaform.server.rest.translate.MetaformMemberGroupTranslator
import org.keycloak.representations.idm.GroupRepresentation
import java.util.UUID
import javax.enterprise.context.RequestScoped
import javax.inject.Inject
import javax.transaction.Transactional
import javax.ws.rs.core.Response

@RequestScoped
@Transactional
class MetaformMemberGroupsApi: fi.metatavu.metaform.api.spec.MetaformMemberGroupsApi, AbstractApi() {

  @Inject
  lateinit var metaformMemberGroupTranslator: MetaformMemberGroupTranslator

  @Inject
  lateinit var metaformController: MetaformController
  override suspend fun createMetaformMemberGroup(metaformId: UUID, metaformMemberGroup: MetaformMemberGroup): Response {
    loggedUserId ?: return createForbidden(UNAUTHORIZED)

    if (!isMetaformAdmin(metaformId)) {
      return createForbidden(createNotAllowedMessage(CREATE, METAFORM_MEMBER))
    }

    metaformController.findMetaformById(metaformId) ?: return createNotFound(createNotFoundMessage(METAFORM, metaformId))

    val createdGroup = try {
      keycloakController.createMetaformMemberGroup(metaformId, GroupRepresentation().apply {
        name = metaformMemberGroup.displayName
      })
    } catch (e: Exception) {
      return createInternalServerError(e.message)
    }

    try {
      metaformMemberGroup.memberIds.forEach {
          memberId -> keycloakController.userJoinGroup(createdGroup.id, memberId.toString())
      }
    } catch (e: Exception) {
      return createInternalServerError(e.message)
    }

    return createOk(metaformMemberGroupTranslator.translate(createdGroup, metaformMemberGroup.memberIds))
  }

  override suspend fun deleteMetaformMemberGroup(metaformId: UUID, metaformMemberGroupId: UUID): Response {
    loggedUserId ?: return createForbidden(UNAUTHORIZED)

    if (!isMetaformAdmin(metaformId)) {
      return createForbidden(createNotAllowedMessage(CREATE, METAFORM_MEMBER))
    }

    metaformController.findMetaformById(metaformId) ?: return createNotFound(createNotFoundMessage(METAFORM, metaformId))

    keycloakController.findMetaformMemberGroup(metaformId, metaformMemberGroupId)
      ?: return createNotFound(createNotFoundMessage(METAFORM_MEMBER_GROUP, metaformMemberGroupId))

    keycloakController.deleteMetaformMemberGroupMembers(metaformMemberGroupId)

    return createNoContent()
  }

  override suspend fun findMetaformMemberGroup(metaformId: UUID, metaformMemberGroupId: UUID): Response {
    loggedUserId ?: return createForbidden(UNAUTHORIZED)

    if (!isMetaformAdmin(metaformId)) {
      return createForbidden(createNotAllowedMessage(CREATE, METAFORM_MEMBER))
    }

    metaformController.findMetaformById(metaformId) ?: return createNotFound(createNotFoundMessage(METAFORM, metaformId))

    val foundMetaformMemberGroup = keycloakController.findMetaformMemberGroup(metaformId, metaformMemberGroupId)
      ?: return createNotFound(createNotFoundMessage(METAFORM_MEMBER_GROUP, metaformMemberGroupId))

    val foundGroupMembers = keycloakController.findMetaformMemberGroupMembers(metaformMemberGroupId)

    return createOk(metaformMemberGroupTranslator.translate(foundMetaformMemberGroup, foundGroupMembers))
  }

  override suspend fun listMetaformMemberGroups(metaformId: UUID): Response {
    loggedUserId ?: return createForbidden(UNAUTHORIZED)

    if (!isMetaformAdmin(metaformId)) {
      return createForbidden(createNotAllowedMessage(CREATE, METAFORM_MEMBER))
    }

    metaformController.findMetaformById(metaformId) ?: return createNotFound(createNotFoundMessage(METAFORM, metaformId))

    val metaformMemberGroups = keycloakController.listMetaformMemberGroup(metaformId).map {
      val metaformGroupMemberIds = keycloakController.findMetaformMemberGroupMembers(UUID.fromString(it.id))
      metaformMemberGroupTranslator.translate(it, metaformGroupMemberIds)
    }

    return createOk(metaformMemberGroups)
  }

  override suspend fun updateMetaformMemberGroup(metaformId: UUID, metaformMemberGroupId: UUID, metaformMemberGroup: MetaformMemberGroup): Response {
    loggedUserId ?: return createForbidden(UNAUTHORIZED)

    if (!isMetaformAdmin(metaformId)) {
      return createForbidden(createNotAllowedMessage(CREATE, METAFORM_MEMBER))
    }

    metaformController.findMetaformById(metaformId) ?: return createNotFound(createNotFoundMessage(METAFORM, metaformId))

    val foundMetaformMemberGroup = keycloakController.findMetaformMemberGroup(metaformId, metaformMemberGroupId)
      ?: return createNotFound(createNotFoundMessage(METAFORM_MEMBER_GROUP, metaformMemberGroupId))

    val foundGroupMembers = keycloakController.findMetaformMemberGroupMembers(metaformMemberGroupId).toSet()
    val updatedGroupMembers = metaformMemberGroup.memberIds.toSet()

    val addedMembers = updatedGroupMembers - foundGroupMembers
    val removedMembers = foundGroupMembers - updatedGroupMembers

    addedMembers.forEach { keycloakController.userJoinGroup(metaformMemberGroupId.toString(), it.toString()) }
    removedMembers.forEach { keycloakController.userLeaveGroup(metaformMemberGroupId.toString(), it.toString()) }

    keycloakController.updateMetaformMemberGroup(
      foundMetaformMemberGroup.apply {
        name = metaformMemberGroup.displayName
      }
    )

    return createOk(metaformMemberGroup)
  }
}