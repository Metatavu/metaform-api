package fi.metatavu.metaform.server.test.functional.tests

import fi.metatavu.metaform.api.client.models.UserFederatedIdentity
import fi.metatavu.metaform.api.client.models.UserFederationSource
import fi.metatavu.metaform.server.test.functional.AbstractTest
import fi.metatavu.metaform.server.test.functional.builder.PermissionScope
import fi.metatavu.metaform.server.test.functional.builder.TestBuilder
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
            val foundUsers = testBuilder.systemAdmin.users.listUsers()
            val foundUserWithSearchParam = testBuilder.systemAdmin.users.listUsers(search = "test-Tommi")
            val metaformKeycloakFederatedUsers = testBuilder.systemAdmin.users.listUsers(
                search = "Käyttäjä1"
            )
            val cardAuthKeycloakUsers = testBuilder.systemAdmin.users.listUsers(
                search = "Käyttäjä2"
            )

            Assertions.assertTrue(foundUsers.size > 10)
            Assertions.assertEquals(1, metaformKeycloakFederatedUsers.size)
            Assertions.assertEquals(2, foundUserWithSearchParam.size)
            Assertions.assertEquals("test-Tommi", foundUserWithSearchParam[0].firstName)
            Assertions.assertEquals(2, cardAuthKeycloakUsers.size)
            metaformKeycloakFederatedUsers.forEach { Assertions.assertNotNull(it.federatedIdentities) }
            cardAuthKeycloakUsers.forEach { Assertions.assertNotNull(it.federatedIdentities) }
            foundUsers
                .filter { !it.federatedIdentities.isNullOrEmpty() }
                .forEach { Assertions.assertTrue(it.federatedIdentities?.get(0)?.source == UserFederationSource.CARD ) }
        }
    }

    @Test
    @Throws(Exception::class)
    fun listUsersPermission() {
        TestBuilder().use { testBuilder ->
            testBuilder.permissionTestByScopes(
                scope = PermissionScope.METAFORM_ADMIN,
                apiCaller = { authentication, _: Int ->
                    authentication.users.listUsers()
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
            Assertions.assertEquals(foundUserWithoutIDPLink.displayName, "turmiola test-tommi")
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
            val cardAuthUser = testBuilder.systemAdmin.users.listUsers(search = "Käyttäjä21").first()
            val createdUser2 = testBuilder.systemAdmin.users.create(cardAuthUser)

            Assertions.assertEquals(createdUser.displayName, userToCreate.displayName)
            Assertions.assertEquals(createdUser.firstName, userToCreate.firstName)
            Assertions.assertEquals(createdUser.lastName, userToCreate.lastName)
            Assertions.assertEquals(createdUser.email, userToCreate.email)
            Assertions.assertEquals(createdUser.federatedIdentities!![0].source, userToCreate.federatedIdentities!![0].source)
            Assertions.assertEquals(createdUser.federatedIdentities[0].userId, userToCreate.federatedIdentities[0].userId)
            Assertions.assertEquals(createdUser.federatedIdentities[0].userName, userToCreate.federatedIdentities[0].userName)
            Assertions.assertEquals(createdUser2.federatedIdentities!![0].userId , cardAuthUser.federatedIdentities!![0].userId)
            Assertions.assertEquals(createdUser2.federatedIdentities[0].userName , cardAuthUser.federatedIdentities[0].userName)
        }
    }

    @Test
    @Throws(Exception::class)
    fun createUserConflict() {
        TestBuilder().use { testBuilder ->
            val firstUserToCreate = testBuilder.systemAdmin.users.createUserWithIDP(
                firstName = "create-test-conflict1",
                federatedUserId = UUID.fromString("915e2fae-f702-4c49-ab84-4bf3802ab18e")
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
                apiCaller = { authentication, index: Int ->
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

            val users = testBuilder.systemAdmin.users.listUsers(search = null)

            Assertions.assertTrue(users.none { it.id == createdUser.id })
        }
    }

    @Test
    @Throws(Exception::class)
    fun deleteUserPermission() {
        TestBuilder().use { testBuilder ->
            testBuilder.permissionTestByScopes(
                scope = PermissionScope.SYSTEM_ADMIN,
                apiCaller = { authentication, index: Int ->
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

    @Test
    @Throws(Exception::class)
    fun updateUser() {
        TestBuilder().use { testBuilder ->
            val userToCreate = testBuilder.systemAdmin.users.createUserWithoutIDP(firstName = "update-test")
            val createdUser = testBuilder.systemAdmin.users.create(userToCreate)
            val userToUpdate = createdUser.copy(
                displayName = userToCreate.displayName.plus(" 78912345601"),
                federatedIdentities = arrayOf(
                    UserFederatedIdentity(
                        source = UserFederationSource.CARD,
                        userId = "7e0f2037-20aa-4115-990a-6355c46cf36e",
                        userName = "testi kayttaja2 78912345601"
                    )
                )
            )
            val updatedUser1 = testBuilder.systemAdmin.users.updateUser(
                userId = userToUpdate.id!!,
                user = userToUpdate
            )
            val updatedUser2 = testBuilder.systemAdmin.users.updateUser(
                userId = userToUpdate.id,
                user = userToUpdate.copy(
                    displayName = "testi update-test",
                    federatedIdentities = emptyArray()
                )
            )

            Assertions.assertNotEquals(createdUser.displayName, updatedUser1.displayName)
            Assertions.assertEquals(createdUser.id, updatedUser1.id)
            Assertions.assertFalse(updatedUser1.federatedIdentities!!.isEmpty())
            Assertions.assertTrue(updatedUser2.federatedIdentities!!.isEmpty())
            Assertions.assertEquals(updatedUser2.displayName, "testi update-test")

            testBuilder.systemAdmin.users.clean(createdUser)
        }
    }

    @Test
    @Throws(Exception::class)
    fun updateUserNotFound() {
        TestBuilder().use { testBuilder ->
            val userToCreate = testBuilder.systemAdmin.users.createUserWithoutIDP(firstName = "update-test")
            val createdUser = testBuilder.systemAdmin.users.create(userToCreate)
            testBuilder.systemAdmin.users.assertUpdateFailStatus(404, UUID.randomUUID(), createdUser)
        }
    }

    @Test
    @Throws(Exception::class)
    fun updateUserPermission() {
        TestBuilder().use { testBuilder ->
            testBuilder.permissionTestByScopes(
                scope = PermissionScope.METAFORM_ADMIN,
                apiCaller = { authentication, index: Int ->
                    val userToCreate = testBuilder.systemAdmin.users.createUserWithoutIDP(firstName = String.format("update-test-permission%d", index))
                    val createdUser = testBuilder.systemAdmin.users.create(userToCreate)
                    authentication.users.updateUser(
                        userId = createdUser.id!!,
                        user = createdUser.copy(displayName = userToCreate.displayName.plus(String.format( "7891234560%d", index)))
                    )
                }
            )
        }
    }
}