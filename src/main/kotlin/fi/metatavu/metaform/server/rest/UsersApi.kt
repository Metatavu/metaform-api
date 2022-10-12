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

            val existingUsers = metaformKeycloakController.findUsersBySearchParam(
                search = null,
                firstResult = null,
                maxResults = 1000
            )
            val upnNumber = user.displayName!!.split(" ")

            if (upnNumber.size == 3) {
                if (existingUsers.any { it.username!!.contains(upnNumber[2]) }) {
                    return createConflict(createDuplicatedMessage(UPN_NUMBER))
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

        if (!isRealmSystemAdmin) {
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

    override fun listUsers(search: String?, firstResult: Int?, maxResults: Int?): Response {
        loggedUserId ?: return createForbidden(UNAUTHORIZED)

        if (!isMetaformAdminAny) {
            return createForbidden(UNAUTHORIZED)
        }

        val foundUsers = metaformKeycloakController.findUsersBySearchParam(
            search = search,
            firstResult = firstResult,
            maxResults = maxResults
        )
            .map { metaformKeycloakController.findUserById(UUID.fromString(it.id!!)) }
            .map { userTranslator.translate(it!!) }
            .toMutableList()

        val cardAuthKeycloakUsers = cardAuthKeycloakController.findUsersBySearchParam(
            search = search,
            firstResult = firstResult,
            maxResults = maxResults
        )
            .toMutableList()
            .filter { cardAuthKeycloakUser ->
                foundUsers.find { it.federatedIdentities?.get(0)?.userId == cardAuthKeycloakUser.id } == null
            }
            .map { userTranslator.translateCardAuthUserRepresentation(it) }

        foundUsers.addAll(cardAuthKeycloakUsers)

        var totalUsers = metaformKeycloakController.findUsersBySearchParam(
            search = search,
            maxResults = 1000
        ).size

        totalUsers += cardAuthKeycloakController.findUsersBySearchParam(
            search,
            maxResults = 1000
        ).size

        return createOk(
            entity = foundUsers,
            count = totalUsers
        )
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