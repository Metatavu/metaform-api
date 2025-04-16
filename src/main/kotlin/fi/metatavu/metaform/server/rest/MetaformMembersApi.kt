package fi.metatavu.metaform.server.rest

import fi.metatavu.metaform.api.spec.model.MetaformMember
import fi.metatavu.metaform.api.spec.model.MetaformMemberRole
import fi.metatavu.metaform.server.controllers.MetaformController
import fi.metatavu.metaform.server.exceptions.KeycloakDuplicatedUserException
import fi.metatavu.metaform.server.exceptions.MetaformMemberRoleNotFoundException
import fi.metatavu.metaform.server.rest.translate.MetaformMemberTranslator
import org.keycloak.representations.idm.UserRepresentation
import org.slf4j.Logger
import java.util.UUID
import jakarta.enterprise.context.RequestScoped
import jakarta.inject.Inject
import jakarta.transaction.Transactional
import jakarta.ws.rs.core.Response

@RequestScoped
@Transactional
@Suppress ("unused")
class MetaformMembersApi: fi.metatavu.metaform.api.spec.MetaformMembersApi, AbstractApi() {

  @Inject
  lateinit var logger: Logger

  @Inject
  lateinit var metaformController: MetaformController

  @Inject
  lateinit var metaformMemberTranslator: MetaformMemberTranslator

  override fun createMetaformMember(metaformId: UUID, metaformMember: MetaformMember): Response {
    loggedUserId ?: return createForbidden(UNAUTHORIZED)

    if (!isMetaformAdmin(metaformId)) {
      return createForbidden(createNotAllowedMessage(CREATE, METAFORM_MEMBER))
    }

    metaformController.findMetaformById(metaformId) ?: return createNotFound(createNotFoundMessage(METAFORM, metaformId))

    val createdMetaformMember = try {
      metaformKeycloakController.createMetaformMember(
        metaformId,
        metaformMember.role,
        UserRepresentation().apply {
          this.firstName = metaformMember.firstName
          this.lastName = metaformMember.lastName
          this.username = metaformMember.email
          this.email = metaformMember.email
        }
      )
    } catch (e: KeycloakDuplicatedUserException) {
      logger.warn("failed to create user ${metaformMember.email}: ${e.message}")
      return createConflict(createDuplicatedMessage(METAFORM_MEMBER))
    } catch (e: Exception) {
      return createInternalServerError(e.message)
    }

    return createOk(metaformMemberTranslator.translate(createdMetaformMember, metaformMember.role))
  }

  override fun deleteMetaformMember(metaformId: UUID, metaformMemberId: UUID): Response {
    loggedUserId ?: return createForbidden(UNAUTHORIZED)

    if (!isMetaformAdmin(metaformId)) {
      return createForbidden(createNotAllowedMessage(DELETE, METAFORM_MEMBER))
    }

    metaformController.findMetaformById(metaformId) ?: return createNotFound(createNotFoundMessage(METAFORM, metaformId))

    val foundMetaformMember = metaformKeycloakController.findMetaformMember(metaformMemberId)
      ?: return createNotFound(createNotFoundMessage(METAFORM_MEMBER, metaformMemberId))

    metaformKeycloakController.deleteMetaformMember(UUID.fromString(foundMetaformMember.id!!), metaformId)

    return createNoContent()
  }

  override fun findMetaformMember(metaformId: UUID, metaformMemberId: UUID): Response {
    loggedUserId ?: return createForbidden(UNAUTHORIZED)

    if (!isMetaformAdmin(metaformId)) {
      return createForbidden(createNotAllowedMessage(FIND, METAFORM_MEMBER))
    }

    metaformController.findMetaformById(metaformId) ?: return createNotFound(createNotFoundMessage(METAFORM, metaformId))

    val foundMetaformMember = metaformKeycloakController.findMetaformMember(metaformMemberId)
      ?: return createNotFound(createNotFoundMessage(METAFORM_MEMBER, metaformMemberId))

    val metaformMemberRole = try {
      metaformKeycloakController.getMetaformMemberRole(foundMetaformMember.id!!, metaformId)
    } catch (e: MetaformMemberRoleNotFoundException) {
      return createNotFound(createNotBelongMessage(METAFORM_MEMBER))
    }

    return createOk(metaformMemberTranslator.translate(foundMetaformMember, metaformMemberRole))
  }

  override fun listMetaformMembers(metaformId: UUID, role: MetaformMemberRole?): Response {
    loggedUserId ?: return createForbidden(UNAUTHORIZED)

    if (!isMetaformAdmin(metaformId)) {
      return createForbidden(createNotAllowedMessage(LIST, METAFORM_MEMBER))
    }

    metaformController.findMetaformById(metaformId) ?: return createNotFound(createNotFoundMessage(METAFORM, metaformId))

    val allUsers = metaformKeycloakController.listMetaformMemberManager(metaformId, null, null)
    val adminUsers = metaformKeycloakController.listMetaformMemberAdmin(metaformId, null, null)
    val managerUsers = allUsers.toMutableList()
    managerUsers.removeAll { adminUsers.any { adminUser -> adminUser.id == it.id } }

    val metaformMembers = when (role) {
      MetaformMemberRole.ADMINISTRATOR -> adminUsers.map { metaformMemberTranslator.translate(it, MetaformMemberRole.ADMINISTRATOR) }
      MetaformMemberRole.MANAGER -> managerUsers.map { metaformMemberTranslator.translate(it, MetaformMemberRole.MANAGER) }
      null -> adminUsers.map { metaformMemberTranslator.translate(it, MetaformMemberRole.ADMINISTRATOR) } +
              managerUsers.map { metaformMemberTranslator.translate(it, MetaformMemberRole.MANAGER) }
    }

    return createOk(metaformMembers)
  }

  override fun updateMetaformMember(metaformId: UUID, metaformMemberId: UUID, metaformMember: MetaformMember): Response {
    loggedUserId ?: return createForbidden(UNAUTHORIZED)

    if (!isMetaformAdmin(metaformId)) {
      return createForbidden(createNotAllowedMessage(UPDATE, METAFORM_MEMBER))
    }

    metaformController.findMetaformById(metaformId) ?: return createNotFound(createNotFoundMessage(METAFORM, metaformId))

    val foundMetaformMember = metaformKeycloakController.findMetaformMember(metaformMemberId)
      ?: return createNotFound(createNotFoundMessage(METAFORM_MEMBER, metaformMemberId))

    try {
      metaformKeycloakController.getMetaformMemberRole(foundMetaformMember.id!!, metaformId)
    } catch (e: MetaformMemberRoleNotFoundException) {
      return createNotFound(createNotBelongMessage(METAFORM_MEMBER))
    }

    val updateMember = foundMetaformMember.copy(
      firstName = metaformMember.firstName,
      lastName = metaformMember.lastName,
      email = metaformMember.email
    )

    metaformKeycloakController.updateMemberManagementGroup(metaformId, updateMember, metaformMember.role)
    val updatedMember = metaformKeycloakController.updateMetaformMember(updateMember)

    return createOk(metaformMemberTranslator.translate(updatedMember, metaformMember.role))
  }
}