package fi.metatavu.metaform.server.rest

import fi.metatavu.metaform.api.spec.model.MetaformMember
import fi.metatavu.metaform.api.spec.model.MetaformMemberGroup
import fi.metatavu.metaform.server.controllers.KeycloakController
import java.util.UUID
import javax.enterprise.context.RequestScoped
import javax.inject.Inject
import javax.transaction.Transactional
import javax.ws.rs.*
import javax.ws.rs.core.Response

@RequestScoped
@Transactional
class MetaformMemberGroupsApi: fi.metatavu.metaform.api.spec.MetaformMemberGroupsApi, AbstractApi() {

  @Inject
  lateinit var keycloakController: KeycloakController

  override suspend fun createMetaformMemberGroup(metaformId: UUID, metaformMemberGroup: MetaformMemberGroup): Response {
    return createNotImplemented("TODO")
  }

  override suspend fun deleteMetaformMemberGroup(metaformId: UUID, metaformMemberGroupId: UUID): Response {
    return createNotImplemented("TODO")
  }

  override suspend fun findMetaformMemberGroup(metaformId: UUID, metaformMemberGroupId: UUID): Response {
    return createNotImplemented("TODO")
  }

  override suspend fun updateMetaformMemberGroup(metaformId: UUID, metaformMemberGroupId: UUID, metaformMemberGroup: MetaformMemberGroup): Response {
    return createNotImplemented("TODO")
  }
}