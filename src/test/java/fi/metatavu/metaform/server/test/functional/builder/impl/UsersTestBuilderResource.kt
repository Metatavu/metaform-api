package fi.metatavu.metaform.server.test.functional.builder.impl

import fi.metatavu.jaxrs.test.functional.builder.auth.AccessTokenProvider
import fi.metatavu.metaform.api.client.apis.UsersApi
import fi.metatavu.metaform.api.client.infrastructure.ApiClient
import fi.metatavu.metaform.api.client.infrastructure.ApiClient.Companion.accessToken
import fi.metatavu.metaform.api.client.infrastructure.ClientException
import fi.metatavu.metaform.api.client.models.User
import fi.metatavu.metaform.api.client.models.UserFederatedIdentity
import fi.metatavu.metaform.api.client.models.UserFederationSource
import fi.metatavu.metaform.server.test.functional.ApiTestSettings
import fi.metatavu.metaform.server.test.functional.builder.TestBuilder
import org.junit.Assert
import java.io.IOException
import java.util.UUID
/**
 * Test builder resource for Users API
 */
class UsersTestBuilderResource(
    testBuilder: TestBuilder,
    private val accessTokenProvider: AccessTokenProvider?,
    apiClient: ApiClient
): ApiTestBuilderResource<User, ApiClient?>(testBuilder, apiClient) {

    override fun getApi(): UsersApi {
        accessToken = accessTokenProvider?.accessToken
        return UsersApi(ApiTestSettings.apiBasePath)
    }

    @Throws(IOException::class)
    override fun clean(user: User) {
        testBuilder.systemAdmin.users.deleteUser(user.id!!)
    }

    /**
     * Creates new User
     *
     * @param user User
     * @return created User
     */
    @Throws(IOException::class)
    fun create(user: User): User {
        val result = api.createUser(user)

        return addClosable(result)
    }

    /**
     * Finds a User by Id
     *
     * @param userId userId
     * @return User
     */
    @Throws(IOException::class)
    fun findUser(userId: UUID): User {
        return api.findUser(userId)
    }

    /**
     * Lists Users
     *
     * @param search search
     * @param firstResult firstResult
     * @param maxResults maxResults
     * @return List of Users
     */
    @Throws(IOException::class)
    fun listUsers(search: String? = null): Array<User> {
        return api.listUsers(
            search = search
        )
    }

    /**
     * Updates User
     *
     * @param userId userId
     * @param user User
     * @return updated User
     */
    @Throws(IOException::class)
    fun updateUser(userId: UUID, user: User): User {
        return api.updateUser(
            userId = userId,
            user = user
        )
    }

    /**
     * Deletes User
     *
     * @param userId userId
     */
    @Throws(IOException::class)
    fun deleteUser(userId: UUID) {
        api.deleteUser(userId)
        removeCloseable { closable ->
            if (closable is User) {
                return@removeCloseable userId == closable.id
            }
            false
        }
    }

    /**
     * Asserts create status fails with given status code
     *
     * @param expectedStatus expected status code
     * @param user User
     */
    @Throws(IOException::class)
    fun assertCreateFailStatus(expectedStatus: Int, user: User) {
        try {
            api.createUser(user)
            Assert.fail(String.format("Expected create to fail with status %d", expectedStatus))
        } catch (e: ClientException) {
            Assert.assertEquals(expectedStatus.toLong(), e.statusCode.toLong())
        }
    }

    /**
     * Asserts find status fails with given status code
     *
     * @param expectedStatus expected status code
     * @param userId userId
     */
    @Throws(IOException::class)
    fun assertFindFailStatus(expectedStatus: Int, userId: UUID) {
        try {
            api.findUser(userId)
            Assert.fail(String.format("Expected find to fail with status %d", expectedStatus))
        } catch (e: ClientException) {
            Assert.assertEquals(expectedStatus.toLong(), e.statusCode.toLong())
        }
    }

    /**
     * Asserts update status fails with given status code
     *
     * @param expectedStatus expected status code
     * @param userId userId
     * @param user User
     */
    @Throws(IOException::class)
    fun assertUpdateFailStatus(expectedStatus: Int, userId: UUID, user: User) {
        try {
            api.updateUser(
                userId = userId,
                user = user
            )
            Assert.fail(String.format("Expected update to fail with status %d", expectedStatus))
        } catch (e: ClientException) {
            Assert.assertEquals(expectedStatus.toLong(), e.statusCode.toLong())
        }
    }

    /**
     * Asserts delete status fails with given status code
     *
     * @param expectedStatus expected status code
     * @param userId userId
     */
    @Throws(IOException::class)
    fun assertDeleteFailStatus(expectedStatus: Int, userId: UUID) {
        try {
            api.deleteUser(userId)
            Assert.fail(String.format("Expected delete to fail with status %d", expectedStatus))
        } catch (e: ClientException) {
            Assert.assertEquals(expectedStatus.toLong(), e.statusCode.toLong())
        }
    }

    /**
     * Creates user from parameters without IDP link
     *
     * @param firstName
     * @return User
     */
    fun createUserWithoutIDP(firstName: String): User {
        return User(
            email = String.format("%s.testi@example.com", firstName),
            firstName = firstName,
            lastName = "Testi",
            displayName = String.format("testi %s", firstName)
        )
    }

    /**
     * Creates user from parameters with IDP link
     *
     * @param firstName firstName
     * @param federatedUserId federatedUserId
     * @return User
     */
    fun createUserWithIDP(
        firstName: String,
        federatedUserId: UUID = UUID.randomUUID()
    ): User {
        return User(
            email = String.format("%s.testi@example.com", firstName),
            firstName = firstName,
            lastName = "Testi",
            displayName = String.format("testi %s", firstName),
            federatedIdentities = arrayOf(
                UserFederatedIdentity(
                    source = UserFederationSource.CARD,
                    userId = federatedUserId.toString(),
                    userName = String.format("testi %s", firstName)
                )
            )
        )
    }
}

