package fi.metatavu.metaform.server.rest

import fi.metatavu.metaform.api.spec.model.MetaformMemberGroup
import fi.metatavu.metaform.server.controllers.MetaformController
import fi.metatavu.metaform.server.permissions.PermissionController
import fi.metatavu.metaform.server.rest.translate.MetaformMemberGroupTranslator
import org.keycloak.representations.idm.GroupRepresentation
import java.util.UUID
import jakarta.enterprise.context.RequestScoped
import jakarta.inject.Inject
import jakarta.transaction.Transactional
import jakarta.ws.rs.core.Response

@RequestScoped
@Transactional
@Suppress ("unused")
class MetaformMemberGroupsApi: fi.metatavu.metaform.api.spec.MetaformMemberGroupsApi, AbstractApi() {

  @Inject
  lateinit var metaformMemberGroupTranslator: MetaformMemberGroupTranslator

  @Inject
  lateinit var metaformController: MetaformController

  @Inject
  lateinit var permissionController: PermissionController

  override fun createMetaformMemberGroup(metaformId: UUID, metaformMemberGroup: MetaformMemberGroup): Response {
    loggedUserId ?: return createForbidden(UNAUTHORIZED)

    if (!isMetaformAdmin(metaformId)) {
      return createForbidden(createNotAllowedMessage(CREATE, METAFORM_MEMBER))
    }

    metaformController.findMetaformById(metaformId) ?: return createNotFound(createNotFoundMessage(METAFORM, metaformId))

    val createdGroup = try {
      val group = metaformKeycloakController.createMetaformMemberGroup(metaformId, GroupRepresentation().apply {
        name = metaformMemberGroup.displayName
      })

      permissionController.createMemberGroupPolicy(
        group = group
      )

      group
    } catch (e: Exception) {
      return createInternalServerError(e.message)
    }

    try {
      metaformMemberGroup.memberIds.forEach {
          memberId -> metaformKeycloakController.userJoinGroup(createdGroup.id, memberId.toString())
      }
    } catch (e: Exception) {
      return createInternalServerError(e.message)
    }

    return createOk(metaformMemberGroupTranslator.translate(createdGroup, metaformMemberGroup.memberIds))
  }

  override fun deleteMetaformMemberGroup(metaformId: UUID, metaformMemberGroupId: UUID): Response {
    loggedUserId ?: return createForbidden(UNAUTHORIZED)

    if (!isMetaformAdmin(metaformId)) {
      return createForbidden(createNotAllowedMessage(CREATE, METAFORM_MEMBER))
    }

    metaformController.findMetaformById(metaformId)
      ?: return createNotFound(createNotFoundMessage(METAFORM, metaformId))

    val memberGroup = metaformKeycloakController.findMetaformMemberGroup(metaformId, metaformMemberGroupId)
      ?: return createNotFound(createNotFoundMessage(METAFORM_MEMBER_GROUP, metaformMemberGroupId))

    metaformKeycloakController.deleteGroup(id = UUID.fromString(memberGroup.id))

    return createNoContent()
  }

  override fun findMetaformMemberGroup(metaformId: UUID, metaformMemberGroupId: UUID): Response {
    loggedUserId ?: return createForbidden(UNAUTHORIZED)

    if (!isMetaformAdmin(metaformId)) {
      return createForbidden(createNotAllowedMessage(CREATE, METAFORM_MEMBER))
    }

    metaformController.findMetaformById(metaformId) ?: return createNotFound(createNotFoundMessage(METAFORM, metaformId))

    val foundMetaformMemberGroup = metaformKeycloakController.findMetaformMemberGroup(metaformId, metaformMemberGroupId)
      ?: return createNotFound(createNotFoundMessage(METAFORM_MEMBER_GROUP, metaformMemberGroupId))

    val foundGroupMembers = metaformKeycloakController.findMetaformMemberGroupMembers(metaformMemberGroupId)

    return createOk(metaformMemberGroupTranslator.translate(foundMetaformMemberGroup, foundGroupMembers))
  }

  override fun listMetaformMemberGroups(metaformId: UUID): Response {
    loggedUserId ?: return createForbidden(UNAUTHORIZED)

    if (!isMetaformAdmin(metaformId)) {
      return createForbidden(createNotAllowedMessage(CREATE, METAFORM_MEMBER))
    }

    metaformController.findMetaformById(metaformId) ?: return createNotFound(createNotFoundMessage(METAFORM, metaformId))

    val metaformMemberGroups = metaformKeycloakController.listMetaformMemberGroups(metaformId).map {
      val metaformGroupMemberIds = metaformKeycloakController.findMetaformMemberGroupMembers(UUID.fromString(it.id))
      metaformMemberGroupTranslator.translate(it, metaformGroupMemberIds)
    }

    return createOk(metaformMemberGroups)
  }

  override fun updateMetaformMemberGroup(metaformId: UUID, metaformMemberGroupId: UUID, metaformMemberGroup: MetaformMemberGroup): Response {
    loggedUserId ?: return createForbidden(UNAUTHORIZED)

    if (!isMetaformAdmin(metaformId)) {
      return createForbidden(createNotAllowedMessage(CREATE, METAFORM_MEMBER))
    }

    metaformController.findMetaformById(metaformId) ?: return createNotFound(createNotFoundMessage(METAFORM, metaformId))

    val foundMetaformMemberGroup = metaformKeycloakController.findMetaformMemberGroup(metaformId, metaformMemberGroupId)
      ?: return createNotFound(createNotFoundMessage(METAFORM_MEMBER_GROUP, metaformMemberGroupId))

    val foundGroupMembers = metaformKeycloakController.findMetaformMemberGroupMembers(metaformMemberGroupId).toSet()
    val updatedGroupMembers = metaformMemberGroup.memberIds.toSet()

    val addedMembers = updatedGroupMembers - foundGroupMembers
    val removedMembers = foundGroupMembers - updatedGroupMembers

    addedMembers.forEach { metaformKeycloakController.userJoinGroup(metaformMemberGroupId.toString(), it.toString()) }
    removedMembers.forEach { metaformKeycloakController.userLeaveGroup(metaformMemberGroupId.toString(), it.toString()) }

    val updatedGroup = metaformKeycloakController.updateUserGroup(
      foundMetaformMemberGroup.apply {
        name = metaformMemberGroup.displayName
      }
    )

    return createOk(metaformMemberGroupTranslator.translate(updatedGroup, updatedGroupMembers.toList()))
  }

}