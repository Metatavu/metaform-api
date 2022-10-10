package fi.metatavu.metaform.server.controllers

import fi.metatavu.metaform.api.spec.model.User
import fi.metatavu.metaform.api.spec.model.UserFederatedIdentity
import fi.metatavu.metaform.api.spec.model.UserFederationSource
import fi.metatavu.metaform.keycloak.client.models.UserRepresentation
import java.util.*
import javax.enterprise.context.ApplicationScoped
import javax.inject.Inject

/**
 * Controller for User objects
 */
@ApplicationScoped
class UsersController {

    @Inject
    lateinit var cardAuthKeycloakController: CardAuthKeycloakController

    @Inject
    lateinit var metaformKeycloakController: MetaformKeycloakController

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

        val splittedName = user.displayName.split(" ")
        if (splittedName.size == 3) {

            val upnNumber = splittedName[2]
            val federatedUser =
                cardAuthKeycloakController.findUsersBySearchParam(
                    search = "",
                    firstResult = null,
                    maxResults = null
                ).find { it.username!!.contains(upnNumber) }

            return metaformKeycloakController.updateUser(
                userId = userId,
                user = user.copy(
                    federatedIdentities = listOf(
                        createUserFederatedIdentity(
                            userId = federatedUser?.id!!,
                            username = federatedUser.username!!
                        )
                    )
                )
            )
        }

        return metaformKeycloakController.updateUser(
            userId = userId,
            user = user
        )
    }

    /**
     * Creates UserFederatedIdentity object by parameters
     *
     * @param source source
     * @param userId userId
     * @param username username
     * @return UserFederatedIdentity
     */
    private fun createUserFederatedIdentity(
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