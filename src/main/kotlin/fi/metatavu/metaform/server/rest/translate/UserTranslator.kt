package fi.metatavu.metaform.server.rest.translate

import fi.metatavu.metaform.api.spec.model.User
import fi.metatavu.metaform.api.spec.model.UserFederatedIdentity
import fi.metatavu.metaform.api.spec.model.UserFederationSource
import fi.metatavu.metaform.keycloak.client.models.UserRepresentation
import org.eclipse.microprofile.config.inject.ConfigProperty
import java.util.UUID
import javax.enterprise.context.ApplicationScoped
import javax.inject.Inject

/**
 * Translator for Users
 */
@ApplicationScoped
class UserTranslator {

    @Inject
    @ConfigProperty(name = "metaforms.keycloak.card.identity.provider")
    lateinit var cardIdentityProvider: String

    /**
     * Translates Metaform KC user representation into REST User
     *
     * @param entity User Representation
     * @return REST User
     */
    fun translate(entity: UserRepresentation): User {
        val federatedIdentities =
            entity.federatedIdentities?.map { federatedIdentity ->
                val federatedIdentitySource =
                    when (federatedIdentity.identityProvider) {
                        cardIdentityProvider -> UserFederationSource.CARD
                        else -> null
                    } ?: return@map null

                UserFederatedIdentity(
                    source = federatedIdentitySource,
                    userId = federatedIdentity.userId!!,
                    username = federatedIdentity.userName!!
                )
            }?.filterNotNull()

        return User(
            email = entity.email ?: "",
            firstName = entity.firstName ?: "",
            lastName = entity.lastName ?: "",
            username = entity.username ?: "",
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
            username = entity.username ?: "",
            id = null,
            displayName = entity.username,
            federatedIdentities = listOf(UserFederatedIdentity(
                source = UserFederationSource.CARD,
                userId = entity.id!!,
                username = entity.username!!
                ))
        )
    }
}