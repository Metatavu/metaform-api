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
                    return createConflict("User with same UPN number already exists!")
                }
            }

            val createdUser = metaformKeycloakController.createUser(user)

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
            ?: return createNotFound("User not found")

        metaformKeycloakController.deleteUser(userId)

        return createNoContent()
    }

    override fun findUser(userId: UUID): Response {
        loggedUserId ?: return createForbidden(UNAUTHORIZED)

        if (!isMetaformAdminAny) {
            return createForbidden(UNAUTHORIZED)
        }

        val foundUser = metaformKeycloakController.findUserById(userId)
            ?: return createNotFound("User with $userId not found")

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
        ).map { metaformKeycloakController.findUserById(UUID.fromString(it.id!!)) }.map { userTranslator.translate(it!!) }.toMutableList()

        foundUsers.addAll(
            cardAuthKeycloakController.findUsersBySearchParam(
                search = search,
                firstResult = firstResult,
                maxResults = maxResults
            ).map { userTranslator.translateCardAuthUserRepresentation(it) }
        )

        var totalUsers = metaformKeycloakController.findUsersBySearchParam(
            search = search,
            maxResults = 1000
        ).size

        totalUsers += cardAuthKeycloakController.findUsersBySearchParam(
            search,
            maxResults = 1000
        ).size

        if (maxResults == null && foundUsers.size <= 9) {
            return createOk(
                entity = foundUsers.subList(firstResult ?: 0, foundUsers.size),
                count = totalUsers
            )
        }

        if (maxResults == null) {
            return createOk(
                entity = foundUsers.subList(firstResult ?: 0, firstResult?.plus(10) ?: 10),
                count = totalUsers
            )
        }

        return createOk(
            entity = foundUsers.subList(firstResult ?: 0, maxResults),
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
                ?: return createNotFound("User not found")

            val updatedUser = usersController.updateUser(
                userId = userId,
                user = user
            ) ?: return createBadRequest("Invalid User")

            return createOk(userTranslator.translate(updatedUser))
        } catch (e: Exception) {
            return createInternalServerError(e.localizedMessage)
        }
    }
}