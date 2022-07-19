package fi.metatavu.metaform.server.rest

import fi.metatavu.metaform.api.spec.model.MetaformMember
import fi.metatavu.metaform.api.spec.model.MetaformMemberRole
import fi.metatavu.metaform.server.controllers.MetaformController
import fi.metatavu.metaform.server.exceptions.MetaformMemberRoleNotFoundException
import fi.metatavu.metaform.server.rest.translate.MetaformMemberTranslator
import org.keycloak.representations.idm.UserRepresentation
import java.util.UUID
import javax.enterprise.context.RequestScoped
import javax.inject.Inject
import javax.transaction.Transactional
import javax.ws.rs.core.Response

@RequestScoped
@Transactional
class MetaformMembersApi: fi.metatavu.metaform.api.spec.MetaformMembersApi, AbstractApi() {

  @Inject
  lateinit var metaformController: MetaformController

  @Inject
  lateinit var metaformMemberTranslator: MetaformMemberTranslator

  override suspend fun createMetaformMember(metaformId: UUID, metaformMember: MetaformMember): Response {
    loggedUserId ?: return createForbidden(UNAUTHORIZED)

    if (!isRealmMetaformAdmin && !keycloakController.isMetaformAdmin(metaformId, loggedUserId!!)) {
      return createForbidden(createNotAllowedMessage(CREATE, METAFORM_MEMBER))
    }

    metaformController.findMetaformById(metaformId) ?: return createNotFound(createNotFoundMessage(METAFORM, metaformId))

    val managementGroupName = when (metaformMember.role) {
      MetaformMemberRole.ADMINISTRATOR -> keycloakController.getMetaformAdminGroupName(metaformId)
      MetaformMemberRole.MANAGER -> keycloakController.getMetaformManagerGroupName(metaformId)
    }

    // TODO error handle
    val createdMetaformMember = keycloakController.createMetaformMember(UserRepresentation().apply {
      this.firstName = metaformMember.firstName
      this.lastName = metaformMember.lastName
      this.username = metaformMember.firstName
      this.email = metaformMember.email
      this.isEnabled = true
      this.groups = listOf(String.format("/%s", managementGroupName))
    })

    // TODO user enabled?
    return createOk(metaformMemberTranslator.translate(createdMetaformMember, metaformMember.role))
  }

  override suspend fun deleteMetaformMember(metaformId: UUID, metaformMemberId: UUID): Response {
    loggedUserId ?: return createForbidden(UNAUTHORIZED)

    if (!isRealmMetaformAdmin && !keycloakController.isMetaformAdmin(metaformId, loggedUserId!!)) {
      return createForbidden(createNotAllowedMessage(DELETE, METAFORM_MEMBER))
    }

    metaformController.findMetaformById(metaformId) ?: return createNotFound(createNotFoundMessage(METAFORM, metaformId))
    // TODO can a user be in both parent group and subgroup?

    val foundMetaformMember = keycloakController.findMetaformMember(metaformMemberId) ?: return createNotFound(createNotFoundMessage(METAFORM_MEMBER, metaformMemberId))

    try {
      keycloakController.getMetaformMemberRole(foundMetaformMember.id, metaformId)
    } catch (e: MetaformMemberRoleNotFoundException) {
      return createNotFound(createNotBelongMessage(METAFORM_MEMBER))
    }

    keycloakController.deleteMetaformMember(metaformMemberId)

    return createNoContent()
  }

  override suspend fun findMetaformMember(metaformId: UUID, metaformMemberId: UUID): Response {
    loggedUserId ?: return createForbidden(UNAUTHORIZED)

    if (!isRealmMetaformAdmin && !keycloakController.isMetaformAdmin(metaformId, loggedUserId!!)) {
      return createForbidden(createNotAllowedMessage(FIND, METAFORM_MEMBER))
    }

    metaformController.findMetaformById(metaformId) ?: return createNotFound(createNotFoundMessage(METAFORM, metaformId))

    val foundMetaformMember = keycloakController.findMetaformMember(metaformMemberId) ?: return createNotFound(createNotFoundMessage(METAFORM_MEMBER, metaformMemberId))

    val metaformMemberRole = try {
      keycloakController.getMetaformMemberRole(foundMetaformMember.id, metaformId)
    } catch (e: MetaformMemberRoleNotFoundException) {
      return createNotFound(createNotBelongMessage(METAFORM_MEMBER))
    }

    return createOk(metaformMemberTranslator.translate(foundMetaformMember, metaformMemberRole))
  }

  override suspend fun updateMetaformMember(metaformId: UUID, metaformMemberId: UUID, metaformMember: MetaformMember): Response {
    loggedUserId ?: return createForbidden(UNAUTHORIZED)

    val loggedInUser = keycloakController.findMetaformMember(loggedUserId!!)

    if (!isRealmMetaformAdmin && !keycloakController.isMetaformAdmin(metaformId, loggedUserId!!)) {
      return createForbidden(createNotAllowedMessage(UPDATE, METAFORM_MEMBER))
    }

    metaformController.findMetaformById(metaformId) ?: return createNotFound(createNotFoundMessage(METAFORM, metaformId))

    val foundMetaformMember = keycloakController.findMetaformMember(metaformMemberId) ?: return createNotFound(createNotFoundMessage(METAFORM_MEMBER, metaformMemberId))

    try {
      keycloakController.getMetaformMemberRole(foundMetaformMember.id, metaformId)
    } catch (e: MetaformMemberRoleNotFoundException) {
      return createNotFound(createNotBelongMessage(METAFORM_MEMBER))
    }

    foundMetaformMember.apply {
      firstName = metaformMember.firstName
      lastName = metaformMember.lastName
      email = metaformMember.email
    }

    keycloakController.updateMemberManagementGroup(metaformId, foundMetaformMember, metaformMember.role)
    keycloakController.updateMetaformMember(foundMetaformMember)

    return createOk(metaformMember)
  }
}