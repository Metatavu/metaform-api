package fi.metatavu.metaform.server.test.functional.tests

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import fi.metatavu.metaform.api.client.models.*
import fi.metatavu.metaform.server.test.functional.AbstractTest
import fi.metatavu.metaform.server.test.functional.builder.PermissionScope
import fi.metatavu.metaform.server.test.functional.builder.TestBuilder
import fi.metatavu.metaform.server.test.functional.builder.auth.TestBuilderAuthentication
import fi.metatavu.metaform.server.test.functional.builder.resources.MetaformKeycloakResource
import fi.metatavu.metaform.server.test.functional.builder.resources.MysqlResource
import fi.metatavu.metaform.server.test.functional.common.InvalidValueTestScenarioBuilder
import fi.metatavu.metaform.server.test.functional.common.InvalidValueTestScenarioPath
import fi.metatavu.metaform.server.test.functional.common.InvalidValueTestScenarioQuery
import fi.metatavu.metaform.server.test.functional.common.InvalidValues
import io.quarkus.test.common.QuarkusTestResource
import io.quarkus.test.junit.QuarkusTest
import io.quarkus.test.junit.TestProfile
import io.restassured.http.Method
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import java.util.*

/**
 * Tests to test the Metaform member system
 */
@QuarkusTest
@QuarkusTestResource.List(
    value = [QuarkusTestResource(MysqlResource::class), QuarkusTestResource(
        MetaformKeycloakResource::class
    )]
)
@TestProfile(GeneralTestProfile::class)
class MetaformMembersTestsIT : AbstractTest() {

    @Test
    @Throws(Exception::class)
    fun createMetaformMember() {
        TestBuilder().use { testBuilder ->
            val metaform = testBuilder.systemAdmin.metaforms.createFromJsonFile("simple")
            val metaformMember = testBuilder.systemAdmin.metaformMembers.createSimpleMember(metaform.id!!, "create-test")

            assertNotNull(metaformMember)
            assertNotNull(metaformMember.id)
            assertEquals(MetaformMemberRole.MANAGER, metaformMember.role)
            assertEquals("create-test@example.com", metaformMember.email)
            assertEquals("create-test", metaformMember.firstName)
            assertEquals("create-test", metaformMember.lastName)

            val foundMember = testBuilder.systemAdmin.metaformMembers.findMember(metaform.id, metaformMember.id!!)

            assertNotNull(foundMember)
            assertEquals(metaformMember.id, foundMember.id)
            assertEquals(metaformMember.firstName, foundMember.firstName)
            assertEquals(metaformMember.lastName, foundMember.lastName)
            assertEquals(metaformMember.email, foundMember.email)
            assertEquals(metaformMember.role, foundMember.role)
        }
    }

    @Test
    @Throws(Exception::class)
    fun createMetaformMemberPermission() {
        TestBuilder().use { testBuilder ->
            val metaform = testBuilder.systemAdmin.metaforms.createFromJsonFile("simple")

            testBuilder.permissionTestByScopes(
                scope = PermissionScope.METAFORM_ADMIN,
                apiCaller = { authentication: TestBuilderAuthentication, index: Int ->
                    authentication.metaformMembers.create(
                        metaform.id!!,
                        MetaformMember(
                            email = String.format("create-permission-test-%d@example.com", index) ,
                            firstName = String.format("create-permission-test-%d", index),
                            lastName = String.format("create-permission-test-%d", index),
                            role = MetaformMemberRole.ADMINISTRATOR
                        )
                    )
                },
                metaformId = metaform.id
            )
        }
    }

    @Test
    @Throws(Exception::class)
    fun createMetaformMemberNotFound() {
        TestBuilder().use { testBuilder ->
            InvalidValueTestScenarioBuilder(
                path = "v1/metaforms/{metaformId}/members",
                method = Method.POST,
                token = testBuilder.systemAdmin.token,
                body = jacksonObjectMapper().writeValueAsString(
                    MetaformMember(
                        email = "tommi@example.com",
                        firstName = "tommi",
                        lastName = "tommi",
                        role = MetaformMemberRole.ADMINISTRATOR
                    )
                )
            )
                .path(InvalidValueTestScenarioPath(name = "metaformId", values = InvalidValues.STRING_NOT_NULL, expectedStatus = 404))
                .build()
                .test()
        }
    }

    @Test
    @Throws(Exception::class)
    fun findMetaformMemberPermission() {
        TestBuilder().use { testBuilder ->
            val metaform = testBuilder.systemAdmin.metaforms.createFromJsonFile("simple")
            val metaformMember = testBuilder.systemAdmin.metaformMembers.createSimpleMember(metaform.id!!, "tommi")

            testBuilder.permissionTestByScopes(
                scope = PermissionScope.METAFORM_ADMIN,
                apiCaller = { authentication: TestBuilderAuthentication, _: Int ->
                    authentication.metaformMembers.findMember(
                        metaform.id,
                        metaformMember.id!!
                    )
                },
                metaformId = metaform.id
            )
        }
    }

    @Test
    @Throws(Exception::class)
    fun findMetaformMemberNotFound() {
        TestBuilder().use { testBuilder ->
            val metaform = testBuilder.systemAdmin.metaforms.createFromJsonFile("simple")
            val metaformMember = testBuilder.systemAdmin.metaformMembers.createSimpleMember(metaform.id!!, "tommi")

            InvalidValueTestScenarioBuilder(
                path = "v1/metaforms/{metaformId}/members/{memberId}",
                method = Method.GET,
                token = testBuilder.systemAdmin.token
            )
                .path(InvalidValueTestScenarioPath(name = "metaformId", values = InvalidValues.STRING_NOT_NULL, default = metaform.id, expectedStatus = 404))
                .path(InvalidValueTestScenarioPath(name = "memberId", values = InvalidValues.STRING_NOT_NULL, default = metaformMember.id, expectedStatus = 404))
                .build()
                .test()
        }
    }

    @Test
    @Throws(Exception::class)
    fun listMetaformMember() {
        TestBuilder().use { testBuilder ->
            val metaform = testBuilder.systemAdmin.metaforms.createFromJsonFile("simple")
            testBuilder.systemAdmin.metaformMembers.createSimpleMember(metaform.id!!, "manager")
            testBuilder.systemAdmin.metaformMembers.create(metaform.id, MetaformMember(
                role = MetaformMemberRole.ADMINISTRATOR,
                email = "admin@examplel.fi",
                lastName = "admin",
                firstName = "admin"
            ))

            val listedAdminMember = testBuilder.systemAdmin.metaformMembers.list(metaform.id, MetaformMemberRole.ADMINISTRATOR)
            val listedManagerMember = testBuilder.systemAdmin.metaformMembers.list(metaform.id, MetaformMemberRole.MANAGER)
            val listedAllMember = testBuilder.systemAdmin.metaformMembers.list(metaform.id, null)

            assertEquals(listedAdminMember.size, 1)
            assertEquals(listedManagerMember.size, 1)
            assertEquals(listedAllMember.size, 2)
        }
    }

    @Test
    @Throws(Exception::class)
    fun listMetaformMemberNotFound() {
        TestBuilder().use { testBuilder ->
            val metaform = testBuilder.systemAdmin.metaforms.createFromJsonFile("simple")
            testBuilder.systemAdmin.metaformMembers.createSimpleMember(metaform.id!!, "tommi")

            InvalidValueTestScenarioBuilder(
                path = "v1/metaforms/{metaformId}/members",
                method = Method.GET,
                token = testBuilder.systemAdmin.token
            )
                .path(InvalidValueTestScenarioPath(name = "metaformId", values = InvalidValues.STRING_NOT_NULL, default = metaform.id, expectedStatus = 404))
                .query(InvalidValueTestScenarioQuery(name = "role", values = InvalidValues.STRING_NOT_NULL, default = MetaformMemberRole.MANAGER, expectedStatus = 404))
                .build()
                .test()
        }
    }

    @Test
    @Throws(Exception::class)
    fun listMetaformMemberPermission() {
        TestBuilder().use { testBuilder ->
            val metaform = testBuilder.systemAdmin.metaforms.createFromJsonFile("simple")
            testBuilder.systemAdmin.metaformMembers.createSimpleMember(
                metaformId =  metaform.id!!,
                name = "tommi"
            )

            testBuilder.permissionTestByScopes(
                scope = PermissionScope.METAFORM_ADMIN,
                apiCaller = { authentication: TestBuilderAuthentication, _: Int ->
                    authentication.metaformMembers.list(metaform.id,null)
                },
                metaformId = metaform.id
            )
        }
    }

    @Test
    @Throws(Exception::class)
    fun deleteMetaformMember() {
        TestBuilder().use { testBuilder ->
            val metaform = testBuilder.systemAdmin.metaforms.createFromJsonFile("simple")
            val metaformMember = testBuilder.systemAdmin.metaformMembers.createSimpleMember(metaform.id!!, "tommi")

            val foundMember = testBuilder.systemAdmin.metaformMembers.findMember(metaform.id, metaformMember.id!!)

            assertNotNull(foundMember)
            testBuilder.systemAdmin.metaformMembers.delete(metaform.id, foundMember.id!!)

            testBuilder.systemAdmin.metaformMembers.assertFindFailStatus(404, metaform.id, metaformMember.id)
        }
    }

    @Test
    @Throws(Exception::class)
    fun deleteMetaformMemberPermission() {
        TestBuilder().use { testBuilder ->
            val metaform = testBuilder.systemAdmin.metaforms.createFromJsonFile("simple")

            testBuilder.permissionTestByScopes(
                scope = PermissionScope.METAFORM_ADMIN,
                apiCaller = { authentication: TestBuilderAuthentication, index: Int ->
                    val metaformMember = testBuilder.systemAdmin.metaformMembers.createSimpleMember(
                        metaform.id!!,
                        String.format("delete-permission-test-%d", index)
                    )
                    authentication.metaformMembers.delete(
                        metaform.id,
                        metaformMember.id!!
                    )
                },
                successStatus = 204,
                metaformId = metaform.id
            )
        }
    }

    @Test
    @Throws(Exception::class)
    fun deleteMetaformMemberNotFound() {
        TestBuilder().use { testBuilder ->
            val metaform = testBuilder.systemAdmin.metaforms.createFromJsonFile("simple")
            val metaformMember = testBuilder.systemAdmin.metaformMembers.createSimpleMember(metaform.id!!, "tommi")

            val foundMember = testBuilder.systemAdmin.metaformMembers.findMember(metaform.id, metaformMember.id!!)

            InvalidValueTestScenarioBuilder(
                path = "v1/metaforms/{metaformId}/members/{memberId}",
                method = Method.DELETE,
                token = testBuilder.systemAdmin.token
            )
                .path(InvalidValueTestScenarioPath(name = "metaformId", values = InvalidValues.STRING_NOT_NULL, default = metaform.id, expectedStatus = 404))
                .path(InvalidValueTestScenarioPath(name = "memberId", values = InvalidValues.STRING_NOT_NULL, default = foundMember.id, expectedStatus = 404))
                .build()
                .test()
        }
    }

    @Test
    @Throws(Exception::class)
    fun updateMetaformMember() {
        TestBuilder().use { testBuilder ->
            val metaform = testBuilder.systemAdmin.metaforms.createFromJsonFile("simple")
            val metaformMember = testBuilder.systemAdmin.metaformMembers.createSimpleMember(metaform.id!!, "update-test-user")

            val foundMember = testBuilder.systemAdmin.metaformMembers.findMember(metaform.id, metaformMember.id!!)

            assertEquals("update-test-user@example.com", foundMember.email)
            assertEquals("update-test-user", foundMember.firstName)
            assertEquals("update-test-user", foundMember.lastName)

            testBuilder.systemAdmin.metaformMembers.updateMember(
                metaform.id,
                foundMember.id!!,
                MetaformMember(
                    email = "update-test-user-updated@example.com",
                    firstName = "updated first name",
                    lastName = "updated last name",
                    role = MetaformMemberRole.MANAGER
                )
            )

            val foundUpdatedMember = testBuilder.systemAdmin.metaformMembers.findMember(metaform.id, metaformMember.id)

            assertEquals("update-test-user-updated@example.com", foundUpdatedMember.email)
            assertEquals("updated first name", foundUpdatedMember.firstName)
            assertEquals("updated last name", foundUpdatedMember.lastName)
        }
    }

    @Test
    @Throws(Exception::class)
    fun updateMetaformMemberNotFound() {
        TestBuilder().use { testBuilder ->
            val metaform = testBuilder.systemAdmin.metaforms.createFromJsonFile("simple")
            val metaformMember = testBuilder.systemAdmin.metaformMembers.createSimpleMember(metaform.id!!, "tommi")

            val foundMember = testBuilder.systemAdmin.metaformMembers.findMember(metaform.id, metaformMember.id!!)

            InvalidValueTestScenarioBuilder(
                path = "v1/metaforms/{metaformId}/members/{memberId}",
                method = Method.PUT,
                token = testBuilder.systemAdmin.token,
                body = jacksonObjectMapper().writeValueAsString(foundMember)
            )
                .path(InvalidValueTestScenarioPath(name = "metaformId", values = InvalidValues.STRING_NOT_NULL, default = metaform.id, expectedStatus = 404))
                .path(InvalidValueTestScenarioPath(name = "memberId", values = InvalidValues.STRING_NOT_NULL, default = foundMember.id, expectedStatus = 404))
                .build()
                .test()
        }
    }

    @Test
    @Throws(Exception::class)
    fun updateMetaformMemberPermission() {
        TestBuilder().use { testBuilder ->
            val metaform = testBuilder.systemAdmin.metaforms.createFromJsonFile("simple")
            val metaformMember = testBuilder.systemAdmin.metaformMembers.createSimpleMember(metaform.id!!, "tommi")

            testBuilder.permissionTestByScopes(
                scope = PermissionScope.METAFORM_ADMIN,
                apiCaller = { authentication: TestBuilderAuthentication, index: Int ->
                    authentication.metaformMembers.updateMember(
                        metaform.id,
                        metaformMember.id!!,
                        MetaformMember(
                            email = String.format("tommi%d@example.com", index) ,
                            firstName = String.format("tommi%d", index),
                            lastName = String.format("tommi%d", index),
                            role = MetaformMemberRole.ADMINISTRATOR
                        )
                    )
                },
                successStatus = 204,
                metaformId = metaform.id
            )
        }
    }

    @Test
    @Throws(Exception::class)
    fun updateMetaformMemberRole1() {
        TestBuilder().use { testBuilder ->
            val metaform = testBuilder.systemAdmin.metaforms.createFromJsonFile("simple")
            val metaformMember = testBuilder.systemAdmin.metaformMembers.createSimpleMember(metaform.id!!, "tommi")

            val memberPassword = UUID.randomUUID().toString()
            testBuilder.resetMetaformMemberPassword(metaformMember.id!!, memberPassword)
            val managerAuthentication = testBuilder.createTestBuilderAuthentication(metaformMember.email, memberPassword)

            managerAuthentication.metaformMembers.assertFindFailStatus(403, metaform.id, metaformMember.id)

            testBuilder.systemAdmin.metaformMembers.updateMember(
                metaform.id,
                metaformMember.id,
                MetaformMember(
                    email = "tommi@example.com",
                    firstName = "tommi",
                    lastName = "tommi",
                    role = MetaformMemberRole.ADMINISTRATOR
                )
            )

            val adminAuthentication = testBuilder.createTestBuilderAuthentication("tommi@example.com", memberPassword)
            val foundMember = adminAuthentication.metaformMembers.findMember(metaform.id, metaformMember.id)

            assertNotNull(foundMember)
        }
    }

    @Test
    @Throws(Exception::class)
    fun updateMetaformMemberRole2() {
        TestBuilder().use { testBuilder ->
            val metaform = testBuilder.systemAdmin.metaforms.createFromJsonFile("simple")
            val metaformMember = testBuilder.systemAdmin.metaformMembers.create(
                metaform.id!!,
                MetaformMember(
                    email = "tommi@example.com",
                    firstName = "tommi",
                    lastName = "tommi",
                    role = MetaformMemberRole.ADMINISTRATOR
                )
            )

            val memberPassword = UUID.randomUUID().toString()
            testBuilder.resetMetaformMemberPassword(metaformMember.id!!, memberPassword)
            val adminAuthentication = testBuilder.createTestBuilderAuthentication(metaformMember.email, memberPassword)

            val foundMember = adminAuthentication.metaformMembers.findMember(metaform.id, metaformMember.id)
            assertNotNull(foundMember)

            testBuilder.systemAdmin.metaformMembers.updateMember(
                metaform.id,
                metaformMember.id,
                MetaformMember(
                    email = "tommi@example.com",
                    firstName = "tommi",
                    lastName = "tommi",
                    role = MetaformMemberRole.MANAGER
                )
            )

            val managerAuthentication = testBuilder.createTestBuilderAuthentication(metaformMember.email, memberPassword)
            managerAuthentication.metaformMembers.assertFindFailStatus(403, metaform.id, metaformMember.id)
        }
    }
}