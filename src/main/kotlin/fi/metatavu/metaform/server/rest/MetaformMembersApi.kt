package fi.metatavu.metaform.server.rest

import fi.metatavu.metaform.api.spec.model.MetaformMember
import fi.metatavu.metaform.server.controllers.KeycloakController
import fi.metatavu.metaform.server.controllers.MetaformController
import java.util.UUID
import javax.enterprise.context.RequestScoped
import javax.inject.Inject
import javax.transaction.Transactional
import javax.ws.rs.core.Response

@RequestScoped
@Transactional
class MetaformMembersApi: fi.metatavu.metaform.api.spec.MetaformMembersApi, AbstractApi() {

  @Inject
  lateinit var keycloakController: KeycloakController

  @Inject
  lateinit var metaformController: MetaformController

  override suspend fun createMetaformMember(metaformId: UUID, metaformMember: MetaformMember, metaformMemberGroupId: UUID?): Response {
    loggedUserId ?: return createForbidden(UNAUTHORIZED)

    if (!isRealmMetaformAdmin && !keycloakController.isMetaformAdmin(metaformId, loggedUserId!!)) {
      return createForbidden(createNotAllowedMessage())
    }
    val metaform = metaformController.findMetaformById(metaformId) ?: return createNotFound(createNotFoundMessage(METAFORM, metaformId))

    return createNotImplemented("TODO")
  }

  override suspend fun deleteMetaformMember(metaformId: UUID, metaformMemberId: UUID): Response {
    return createNotImplemented("TODO")
  }

  override suspend fun findMetaformMember(metaformId: UUID,  metaformMemberId: UUID): Response {
    return createNotImplemented("TODO")
  }

  override suspend fun updateMetaformMember(metaformId: UUID, metaformMemberId: UUID, metaformMember: MetaformMember, metaformMemberGroupId: UUID?): Response {
    return createNotImplemented("TODO")
  }
}