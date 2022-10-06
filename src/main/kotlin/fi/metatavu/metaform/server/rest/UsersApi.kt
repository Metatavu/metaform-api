package fi.metatavu.metaform.server.rest

import fi.metatavu.metaform.api.spec.model.User
import fi.metatavu.metaform.server.rest.translate.UserTranslator
import fi.metatavu.metaform.api.spec.UsersApi
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

    override fun createUser(user: User): Response {
        try {
            loggedUserId ?: return createForbidden(UNAUTHORIZED)

            if (!isMetaformAdminAny) {
                return createForbidden(UNAUTHORIZED)
            }

            val existingUsers = metaformKeycloakController.findUsersBySearchParam(
                search = null,
                firstResult = null,
                maxResults = null
            )
            val upnNumber = user.displayName!!.split(" ")[2]
            println(upnNumber)
            existingUsers.map { println(it.toString()) }
            if (existingUsers.any { it.username!!.contains(upnNumber) }) {
                return createConflict("User with same UPN number already exists!")
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
        ).map { userTranslator.translate(it) }.toMutableList()

        foundUsers.addAll(
            cardAuthKeycloakController.findUsersBySearchParam(
                search = search,
                firstResult = firstResult,
                maxResults = maxResults
            ).map { userTranslator.translateCardAuthUserRepresentation(it) }
        )

        return createOk(foundUsers)
    }

    override fun updateUser(userId: UUID, user: User): Response {
        loggedUserId ?: return createForbidden(UNAUTHORIZED)

        if (!isMetaformAdminAny) {
            return createForbidden(UNAUTHORIZED)
        }

        metaformKeycloakController.findUserById(userId)
            ?: return createNotFound("User not found")

        val updatedUser = metaformKeycloakController.updateUser(
            userId = userId,
            user = user
        )

        return createOk(userTranslator.translate(updatedUser))
    }
}