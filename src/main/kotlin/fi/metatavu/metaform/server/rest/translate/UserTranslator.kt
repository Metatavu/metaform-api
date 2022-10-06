package fi.metatavu.metaform.server.rest.translate

import fi.metatavu.metaform.api.spec.model.User
import fi.metatavu.metaform.api.spec.model.UserFederatedIdentity
import fi.metatavu.metaform.api.spec.model.UserFederationSource
import fi.metatavu.metaform.keycloak.client.models.UserRepresentation
import java.util.UUID
import javax.enterprise.context.ApplicationScoped

/**
 * Translator for Users
 */
@ApplicationScoped
class UserTranslator {

    /**
     * Translates user representation into REST User
     *
     * @param entity User Representation
     * @return REST User
     */
    fun translate(entity: UserRepresentation): User {
        val isUserFederated = !entity.federatedIdentities.isNullOrEmpty()
        val federatedIdentities =
            if (isUserFederated) {
                listOf(UserFederatedIdentity(
                    source = UserFederationSource.CARD,
                    userId = entity.federatedIdentities?.get(0)?.userId!!,
                    userName = entity.federatedIdentities[0].userName!!
                ))
            } else {
                null
            }

        return User(
            email = entity.email ?: "",
            firstName = entity.firstName ?: "",
            lastName = entity.lastName ?: "",
            id = UUID.fromString(entity.id),
            displayName = entity.username,
            federatedIdentities = federatedIdentities
        )
    }

    /**
     * Translates Card Auth KC user representation into REST User
     *
     * @param entity User Representation
     * @return REST User
     */
    fun translateCardAuthUserRepresentation(entity: UserRepresentation): User {
        return User(
            email = entity.email ?: "",
            firstName = entity.firstName ?: "",
            lastName = entity.lastName ?: "",
            id = null,
            displayName = entity.username,
            federatedIdentities = null
        )
    }
}