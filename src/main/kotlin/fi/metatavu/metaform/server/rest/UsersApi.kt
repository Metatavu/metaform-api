package fi.metatavu.metaform.server.rest

import fi.metatavu.metaform.api.spec.model.User
import fi.metatavu.metaform.server.rest.translate.UserTranslator
import fi.metatavu.metaform.api.spec.UsersApi
import fi.metatavu.metaform.server.controllers.UsersController
import java.util.UUID
import javax.enterprise.context.RequestScoped
import javax.inject.Inject
import javax.transaction.Transactional
import javax.ws.rs.core.Response

@RequestScoped
@Transactional
class UsersApi: UsersApi, AbstractApi() {

    @Inject
    lateinit var userTranslator: UserTranslator

    @Inject
    lateinit var usersController: UsersController

    override fun createUser(user: User): Response {
        try {
            loggedUserId ?: return createForbidden(UNAUTHORIZED)

            if (!isMetaformAdminAny) {
                return createForbidden(UNAUTHORIZED)
            }

            val existingUsers = metaformKeycloakController.searchUsers(
                search = null,
                maxResults = 1000
            )

            existingUsers.forEach { existingUser ->
                val conflictingFederatedIdentity = metaformKeycloakController.findUserById(UUID.fromString(existingUser.id))?.federatedIdentities
                    ?.any { federatedIdentity ->
                        user.federatedIdentities?.any { it.userId == federatedIdentity.userId } ?: false
                    } ?: false

                if (conflictingFederatedIdentity) {
                    return createConflict(createDuplicatedMessage(USER))
                }
            }

            val createdUser = usersController.createUser(user)

            return createOk(userTranslator.translate(createdUser))
        } catch (e: Exception) {
            return createInternalServerError(e.localizedMessage)
        }
    }

    override fun deleteUser(userId: UUID): Response {
        loggedUserId ?: return createForbidden(UNAUTHORIZED)

        if (!isMetatavuAdmin && !isRealmSystemAdmin) {
            return createForbidden(UNAUTHORIZED)
        }

        metaformKeycloakController.findUserById(userId)
            ?: return createNotFound(createNotFoundMessage(USER, userId))

        metaformKeycloakController.deleteUser(userId)

        return createNoContent()
    }

    override fun findUser(userId: UUID): Response {
        loggedUserId ?: return createForbidden(UNAUTHORIZED)

        if (!isMetaformAdminAny) {
            return createForbidden(UNAUTHORIZED)
        }

        val foundUser = metaformKeycloakController.findUserById(userId)
            ?: return createNotFound(createNotFoundMessage(USER, userId))

        return createOk(userTranslator.translate(foundUser))
    }

    override fun listUsers(search: String?): Response {
        loggedUserId ?: return createForbidden(UNAUTHORIZED)

        if (!isMetaformAdminAny) {
            return createForbidden(UNAUTHORIZED)
        }

        val foundUsers = metaformKeycloakController.searchUsers(search)
            .map { metaformKeycloakController.findUserById(UUID.fromString(it.id!!)) }
            .map { userTranslator.translate(it!!) }
            .toMutableList()

        val cardAuthKeycloakUsers = cardAuthKeycloakController.searchUsers(search)
            .toMutableList()
            .filter { cardAuthKeycloakUser ->
                foundUsers.find { foundUser -> foundUser.federatedIdentities?.any { it.userId == cardAuthKeycloakUser.id } ?: false } == null
            }
            .map { userTranslator.translateCardAuthUserRepresentation(it) }

        foundUsers.addAll(cardAuthKeycloakUsers)

        return createOk(foundUsers)
    }

    override fun updateUser(userId: UUID, user: User): Response {
        try {
            loggedUserId ?: return createForbidden(UNAUTHORIZED)

            if (!isMetaformAdminAny) {
                return createForbidden(UNAUTHORIZED)
            }

            metaformKeycloakController.findUserById(userId)
                ?: return createNotFound(createNotFoundMessage(USER, userId))

            val updatedUser = usersController.updateUser(
                userId = userId,
                user = user
            ) ?: return createBadRequest(createNotFoundMessage(USER, userId))

            return createOk(userTranslator.translate(updatedUser))
        } catch (e: Exception) {
            return createInternalServerError(e.localizedMessage)
        }
    }
}