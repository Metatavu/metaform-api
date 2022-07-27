package fi.metatavu.metaform.server.rest.translate

import fi.metatavu.metaform.api.spec.model.MetaformMemberGroup
import org.keycloak.representations.idm.GroupRepresentation
import java.util.*
import javax.enterprise.context.ApplicationScoped

/**
 * Translator for Metaform member group
 *
 * @author Tianxing Wu
 */
@ApplicationScoped
class MetaformMemberGroupTranslator {
  /**
   * Translates group representation into REST Metaform member group
   *
   * @param entity User representation
   * @param memberIds member ids
   * @return REST Metaform member group
   */
  fun translate(entity: GroupRepresentation, memberIds: List<UUID>): MetaformMemberGroup {
      return MetaformMemberGroup(
        id = UUID.fromString(entity.id),
        displayName = entity.name,
        memberIds = memberIds
      )
  }
}