package fi.metatavu.metaform.server.keycloak.translate

import fi.metatavu.metaform.api.spec.model.User
import fi.metatavu.metaform.keycloak.client.models.UserRepresentation
import javax.enterprise.context.ApplicationScoped

/**
 * Translator for Keycloak related objects
 */
@ApplicationScoped
class KeycloakTranslator {

    /**
     * Translates REST Metaform User into Keycloak UserRepresentation
     *
     * @param entity User
     * @return UserRepresentation
     */
    fun translateUser(entity: User): UserRepresentation {
        return UserRepresentation(
            firstName = entity.firstName,
            lastName = entity.lastName,
            email = entity.email,
            emailVerified = true,
            username = entity.displayName,
            id = entity.id.toString()
        )
    }
}