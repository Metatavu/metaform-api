package fi.metatavu.metaform.server.rest.translate

import fi.metatavu.metaform.api.spec.model.MetaformMember
import fi.metatavu.metaform.api.spec.model.MetaformMemberRole
import org.keycloak.representations.idm.UserRepresentation
import java.util.*
import javax.enterprise.context.ApplicationScoped

/**
 * Translator for Metaform members
 *
 * @author Tianxing Wu
 */
@ApplicationScoped
class MetaformMemberTranslator {
  /**
   * Translates user representation into REST Metaform member
   *
   * @param entity User representation
   * @param role User role
   * @return REST Metaform member
   */
  fun translate(entity: UserRepresentation, role: MetaformMemberRole): MetaformMember {
      return MetaformMember(
        id = UUID.fromString(entity.id),
        firstName = entity.firstName,
        lastName = entity.lastName,
        email = entity.email,
        role = role
      )
  }
}