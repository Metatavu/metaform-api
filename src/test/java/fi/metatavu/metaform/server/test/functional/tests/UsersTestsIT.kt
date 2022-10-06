package fi.metatavu.metaform.server.test.functional.tests

import fi.metatavu.metaform.api.client.models.UserFederationSource
import fi.metatavu.metaform.server.test.functional.AbstractTest
import fi.metatavu.metaform.server.test.functional.builder.PermissionScope
import fi.metatavu.metaform.server.test.functional.builder.TestBuilder
import fi.metatavu.metaform.server.test.functional.builder.auth.TestBuilderAuthentication
import fi.metatavu.metaform.server.test.functional.builder.resources.CardAuthKeycloakResource
import fi.metatavu.metaform.server.test.functional.builder.resources.MetaformKeycloakResource
import fi.metatavu.metaform.server.test.functional.builder.resources.MysqlResource
import io.quarkus.test.common.QuarkusTestResource
import io.quarkus.test.junit.QuarkusTest
import io.quarkus.test.junit.TestProfile
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.util.*

/**
 * Tests to test the Users API
 */
@QuarkusTest
@QuarkusTestResource.List(
    QuarkusTestResource(MysqlResource::class),
    QuarkusTestResource(MetaformKeycloakResource::class),
    QuarkusTestResource(CardAuthKeycloakResource::class)
)
@TestProfile(GeneralTestProfile::class)
class UsersTestsIT: AbstractTest() {

    @Test
    @Throws(Exception::class)
    fun listUsers() {
        TestBuilder().use { testBuilder ->
            val foundUsers = testBuilder.systemAdmin.users.listUsers(
                search = null,
                firstResult = null,
                maxResults = null
            )
            val metaformKeycloakUsers = foundUsers.filter { it.id != null}
            val metaformKeycloakFederatedUsers = metaformKeycloakUsers.filter { !it.federatedIdentities.isNullOrEmpty() }
            val cardAuthKeycloakUsers = foundUsers.filter { it.id == null }

            Assertions.assertTrue(metaformKeycloakUsers.size != metaformKeycloakFederatedUsers.size)
            Assertions.assertTrue(cardAuthKeycloakUsers.size == 8)
        }
    }

    @Test
    @Throws(Exception::class)
    fun listUsersPermission() {
        TestBuilder().use { testBuilder ->
            testBuilder.permissionTestByScopes(
                scope = PermissionScope.METAFORM_ADMIN,
                apiCaller = { authentication, _: Int ->
                    authentication.users.listUsers(
                        search = null,
                        firstResult = null,
                        maxResults = null
                    )
                }
            )
        }
    }

    @Test
    @Throws(Exception::class)
    fun findUser() {
        TestBuilder().use { testBuilder ->
            val foundUserWithoutIDPLink = testBuilder.systemAdmin.users.findUser(USER_WITHOUT_IDP_ID)
            val foundUserWithIDPLink = testBuilder.systemAdmin.users.findUser(USER_WITH_IDP_ID)

            Assertions.assertTrue(foundUserWithoutIDPLink.federatedIdentities.isNullOrEmpty())
            Assertions.assertTrue(!foundUserWithIDPLink.federatedIdentities.isNullOrEmpty())
            Assertions.assertTrue(foundUserWithoutIDPLink.displayName!!.all { !it.isDigit() })
            Assertions.assertEquals(foundUserWithoutIDPLink.id, USER_WITHOUT_IDP_ID)
            Assertions.assertEquals(foundUserWithoutIDPLink.displayName, "turmiola tommi")
            Assertions.assertEquals(foundUserWithIDPLink.federatedIdentities?.get(0)?.source , UserFederationSource.CARD)
            Assertions.assertEquals(foundUserWithIDPLink.federatedIdentities?.get(0)?.userId, "915e2fae-f702-4c49-ab84-4bf3802ab18e")
            Assertions.assertEquals(foundUserWithIDPLink.federatedIdentities?.get(0)?.userName, foundUserWithIDPLink.displayName)
        }
    }

    @Test
    @Throws(Exception::class)
    fun findUserNotFound() {
        TestBuilder().use { testBuilder ->
            testBuilder.systemAdmin.users.assertFindFailStatus(404, UUID.randomUUID())
        }
    }

    @Test
    @Throws(Exception::class)
    fun findUserPermission() {
        TestBuilder().use { testBuilder ->
            testBuilder.permissionTestByScopes(
                scope = PermissionScope.METAFORM_ADMIN,
                apiCaller= { authentication, _: Int ->
                    authentication.users.findUser(USER_WITH_IDP_ID)
                }
            )
        }
    }

    @Test
    @Throws(Exception::class)
    fun createUser() {
        TestBuilder().use { testBuilder ->
            val userToCreate = testBuilder.systemAdmin.users.createUserWithIDP(
                firstName = "create-test"
            )
            val createdUser = testBuilder.systemAdmin.users.create(userToCreate)

            Assertions.assertEquals(createdUser.displayName, userToCreate.displayName)
            Assertions.assertEquals(createdUser.firstName, userToCreate.firstName)
            Assertions.assertEquals(createdUser.lastName, userToCreate.lastName)
            Assertions.assertEquals(createdUser.email, userToCreate.email)
            Assertions.assertEquals(createdUser.federatedIdentities!![0].source, userToCreate.federatedIdentities!![0].source)
            Assertions.assertEquals(createdUser.federatedIdentities[0].userId, userToCreate.federatedIdentities[0].userId)
            Assertions.assertEquals(createdUser.federatedIdentities[0].userName, userToCreate.federatedIdentities[0].userName)
        }
    }

    @Test
    @Throws(Exception::class)
    fun createUserConflict() {
        TestBuilder().use { testBuilder ->
            val firstUserToCreate = testBuilder.systemAdmin.users.createUserWithIDP(
                firstName = "create-test-conflict1",
                upnNumber = 45678912301
            )

            testBuilder.systemAdmin.users.assertCreateFailStatus(
                expectedStatus = 409,
                user = firstUserToCreate
            )
        }
    }

    @Test
    @Throws(Exception::class)
    fun createUserPermission() {
        TestBuilder().use { testBuilder ->
            testBuilder.permissionTestByScopes(
                scope = PermissionScope.METAFORM_ADMIN,
                apiCaller = { authentication: TestBuilderAuthentication, index: Int ->
                    val userToCreate = testBuilder.systemAdmin.users.createUserWithIDP(
                        firstName = String.format("create-permission-test%d", index)
                    )
                    authentication.users.create(userToCreate)
                }
            )
        }
    }

    @Test
    @Throws(Exception::class)
    fun deleteUser() {
        TestBuilder().use { testBuilder ->
            val createdUser = testBuilder.systemAdmin.users.create(testBuilder.systemAdmin.users.createUserWithIDP(
                firstName = "delete-test"
            ))

            testBuilder.systemAdmin.users.deleteUser(createdUser.id!!)

            val users = testBuilder.systemAdmin.users.listUsers(
                search = null,
                firstResult = null,
                maxResults = null
            )

            Assertions.assertTrue(users.none { it.id == createdUser.id })
        }
    }

    @Test
    @Throws(Exception::class)
    fun deleteUserPermission() {
        TestBuilder().use { testBuilder ->
            testBuilder.permissionTestByScopes(
                scope = PermissionScope.SYSTEM_ADMIN,
                apiCaller = { authentication: TestBuilderAuthentication, index: Int ->
                    val userToCreate = testBuilder.systemAdmin.users.createUserWithIDP(
                        firstName = String.format("delete-permission-test%d", index)
                    )
                    val createdUser = testBuilder.systemAdmin.users.create(userToCreate)
                    authentication.users.deleteUser(createdUser.id!!)
                },
                successStatus = 204
            )
        }
    }

    @Test
    @Throws(Exception::class)
    fun deleteUserNotFound() {
        TestBuilder().use { testBuilder ->
            testBuilder.systemAdmin.users.assertDeleteFailStatus(404, UUID.randomUUID())
        }
    }
}