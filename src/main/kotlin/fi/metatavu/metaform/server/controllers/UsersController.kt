package fi.metatavu.metaform.server.controllers

import fi.metatavu.metaform.api.spec.model.User
import fi.metatavu.metaform.api.spec.model.UserFederatedIdentity
import fi.metatavu.metaform.api.spec.model.UserFederationSource
import fi.metatavu.metaform.keycloak.client.models.UserRepresentation
import org.eclipse.microprofile.config.inject.ConfigProperty
import java.util.*
import javax.enterprise.context.ApplicationScoped
import javax.inject.Inject

/**
 * Controller for User objects
 */
@ApplicationScoped
class UsersController {

    @Inject
    lateinit var metaformKeycloakController: MetaformKeycloakController

    @Inject
    @ConfigProperty(name = "metaforms.keycloak.card.identity.provider")
    lateinit var cardIdentityProvider: String

    /**
     * Creates User in Metaform Keycloak
     *
     * @param user User
     * @return UserRepresentation
     */
    fun createUser(user: User): UserRepresentation {
        val createdUser = metaformKeycloakController.createUser(user)

        if (!user.federatedIdentities.isNullOrEmpty()) {
            user.federatedIdentities.forEach { userFederatedIdentity ->
                when (userFederatedIdentity.source) {
                    UserFederationSource.CARD -> metaformKeycloakController.createUserFederatedIdentity(
                        userId = UUID.fromString(createdUser.id),
                        userFederatedIdentity = userFederatedIdentity,
                        identityProvider = cardIdentityProvider
                    )
                }
            }

            return metaformKeycloakController.findUserById(UUID.fromString(createdUser.id)) ?: throw Exception("Couldn't find created user")
        }

        return createdUser
    }

    /**
     * Updates User in Metaform Keycloak
     *
     * @param userId userId
     * @param user User
     * @return UserRepresentation
     */
    fun updateUser(userId: UUID, user: User): UserRepresentation? {
        if (user.displayName.isNullOrEmpty()) {
            return null
        }

        if (!user.federatedIdentities.isNullOrEmpty()) {
            metaformKeycloakController.updateUser(
                userId = userId,
                user = user
            )
            user.federatedIdentities.forEach {  userFederatedIdentity ->
                when (userFederatedIdentity.source) {
                    UserFederationSource.CARD -> metaformKeycloakController.createUserFederatedIdentity(
                            userId = userId,
                            identityProvider = cardIdentityProvider,
                            userFederatedIdentity = constructUserFederatedIdentity(
                                userId = userFederatedIdentity.userId,
                                username = userFederatedIdentity.userName,
                            )
                        )
                }
            }

            return metaformKeycloakController.findUserById(userId)
        }

        metaformKeycloakController.updateUser(
            userId = userId,
            user = user
        )
        metaformKeycloakController.deleteUserFederatedIdentity(
            userId = userId,
            identityProvider = cardIdentityProvider
        )

        return metaformKeycloakController.findUserById(userId)
    }

    /**
     * Constructs UserFederatedIdentity object by parameters
     *
     * @param source source
     * @param userId userId
     * @param username username
     * @return UserFederatedIdentity
     */
    private fun constructUserFederatedIdentity(
        source: UserFederationSource = UserFederationSource.CARD,
        userId: String,
        username: String
    ): UserFederatedIdentity {
     return UserFederatedIdentity(
         source = source,
         userId = userId,
         userName = username
     )
    }
}