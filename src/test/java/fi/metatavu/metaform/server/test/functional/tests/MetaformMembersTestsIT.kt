package fi.metatavu.metaform.server.test.functional.tests

import fi.metatavu.metaform.api.client.models.*
import fi.metatavu.metaform.server.test.functional.AbstractTest
import fi.metatavu.metaform.server.test.functional.builder.PermissionScope
import fi.metatavu.metaform.server.test.functional.builder.TestBuilder
import fi.metatavu.metaform.server.test.functional.builder.auth.TestBuilderAuthentication
import fi.metatavu.metaform.server.test.functional.builder.resources.KeycloakResource
import fi.metatavu.metaform.server.test.functional.builder.resources.MysqlResource
import io.quarkus.test.common.QuarkusTestResource
import io.quarkus.test.junit.QuarkusTest
import io.quarkus.test.junit.TestProfile
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
        KeycloakResource::class
    )]
)
@TestProfile(GeneralTestProfile::class)
class MetaformMembersTestsIT : AbstractTest() {
    @Test
    @Throws(Exception::class)
    fun createMetaformMember() {
        TestBuilder().use { testBuilder ->
            val metaform = testBuilder.systemAdmin.metaforms.createFromJsonFile("simple")
            val metaformMember = testBuilder.systemAdmin.metaformMembers.createSimpleMember(metaform.id!!, "tommi")

            assertNotNull(metaformMember)
            assertNotNull(metaformMember.id)
            assertEquals(MetaformMemberRole.mANAGER, metaformMember.role)
            assertEquals("tommi@example.com", metaformMember.email)
            assertEquals("tommi", metaformMember.firstName)
            assertEquals("tommi", metaformMember.lastName)

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
    fun createDuplicatedMetaformMember() {
        TestBuilder().use { testBuilder ->
            val metaform = testBuilder.systemAdmin.metaforms.createFromJsonFile("simple")
            val metaformMember = testBuilder.systemAdmin.metaformMembers.createSimpleMember(metaform.id!!, "tommi")

            testBuilder.systemAdmin.metaformMembers.assertCreateFailStatus(409, metaform.id, metaformMember)
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
                            email = String.format("tommi%d@example.com", index) ,
                            firstName = String.format("tommi%d", index),
                            lastName = String.format("tommi%d", index),
                            role = MetaformMemberRole.aDMINISTRATOR
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
            testBuilder.systemAdmin.metaformMembers.assertCreateFailStatus(
                404,
                UUID.randomUUID(),
                MetaformMember(
                    email = "tommi@example.com",
                    firstName = "tommi",
                    lastName = "tommi",
                    role = MetaformMemberRole.aDMINISTRATOR
                )
            )
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

            testBuilder.systemAdmin.metaformMembers.assertFindFailStatus(404, UUID.randomUUID(), metaformMember.id!!)
            testBuilder.systemAdmin.metaformMembers.assertFindFailStatus(404, metaform.id, UUID.randomUUID())
        }
    }

    @Test
    @Throws(Exception::class)
    fun listMetaformMember() {
        TestBuilder().use { testBuilder ->
            val metaform = testBuilder.systemAdmin.metaforms.createFromJsonFile("simple")
            testBuilder.systemAdmin.metaformMembers.createSimpleMember(metaform.id!!, "manager")
            testBuilder.systemAdmin.metaformMembers.create(metaform.id, MetaformMember(
                role = MetaformMemberRole.aDMINISTRATOR,
                email = "admin@examplel.fi",
                lastName = "admin",
                firstName = "admin"
            ))

            val listedAdminMember = testBuilder.systemAdmin.metaformMembers.list(metaform.id, MetaformMemberRole.aDMINISTRATOR)
            val listedManagerMember = testBuilder.systemAdmin.metaformMembers.list(metaform.id, MetaformMemberRole.mANAGER)
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

            testBuilder.systemAdmin.metaformMembers.assertListFailStatus(404, UUID.randomUUID(), MetaformMemberRole.mANAGER)
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
                        String.format("tommi%d", index)
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

            testBuilder.systemAdmin.metaformMembers.assertDeleteFailStatus(404, UUID.randomUUID(), foundMember.id!!)
            testBuilder.systemAdmin.metaformMembers.assertDeleteFailStatus(404, metaform.id, UUID.randomUUID())
        }
    }

    @Test
    @Throws(Exception::class)
    fun updateMetaformMember() {
        TestBuilder().use { testBuilder ->
            val metaform = testBuilder.systemAdmin.metaforms.createFromJsonFile("simple")
            val metaformMember = testBuilder.systemAdmin.metaformMembers.createSimpleMember(metaform.id!!, "tommi")

            val foundMember = testBuilder.systemAdmin.metaformMembers.findMember(metaform.id, metaformMember.id!!)

            assertEquals("tommi@example.com", foundMember.email)
            assertEquals("tommi", foundMember.firstName)
            assertEquals("tommi", foundMember.lastName)

            testBuilder.systemAdmin.metaformMembers.updateMember(
                metaform.id,
                foundMember.id!!,
                MetaformMember(
                    email = "tommi1@example.com",
                    firstName = "tommi1",
                    lastName = "tommi1",
                    role = MetaformMemberRole.mANAGER
                )
            )

            val foundUpdatedMember = testBuilder.systemAdmin.metaformMembers.findMember(metaform.id, metaformMember.id!!)

            assertEquals("tommi1@example.com", foundUpdatedMember.email)
            assertEquals("tommi1", foundUpdatedMember.firstName)
            assertEquals("tommi1", foundUpdatedMember.lastName)
        }
    }

    @Test
    @Throws(Exception::class)
    fun updateMetaformMemberNotFound() {
        TestBuilder().use { testBuilder ->
            val metaform = testBuilder.systemAdmin.metaforms.createFromJsonFile("simple")
            val metaformMember = testBuilder.systemAdmin.metaformMembers.createSimpleMember(metaform.id!!, "tommi")

            val foundMember = testBuilder.systemAdmin.metaformMembers.findMember(metaform.id, metaformMember.id!!)

            testBuilder.systemAdmin.metaformMembers.assertUpdateFailStatus(404, UUID.randomUUID(), foundMember.id!!, foundMember)
            testBuilder.systemAdmin.metaformMembers.assertUpdateFailStatus(404, metaform.id, UUID.randomUUID(), foundMember)
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
                            role = MetaformMemberRole.aDMINISTRATOR
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
            val managerAuthentication = testBuilder.createTestBuilderAuthentication(metaformMember.firstName, memberPassword)

            managerAuthentication.metaformMembers.assertFindFailStatus(403, metaform.id, metaformMember.id)

            testBuilder.systemAdmin.metaformMembers.updateMember(
                metaform.id,
                metaformMember.id,
                MetaformMember(
                    email = "tommi@example.com",
                    firstName = "tommi",
                    lastName = "tommi",
                    role = MetaformMemberRole.aDMINISTRATOR
                )
            )

            val adminAuthentication = testBuilder.createTestBuilderAuthentication(metaformMember.firstName, memberPassword)
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
                    role = MetaformMemberRole.aDMINISTRATOR
                )
            )

            val memberPassword = UUID.randomUUID().toString()
            testBuilder.resetMetaformMemberPassword(metaformMember.id!!, memberPassword)
            val adminAuthentication = testBuilder.createTestBuilderAuthentication(metaformMember.firstName, memberPassword)

            val foundMember = adminAuthentication.metaformMembers.findMember(metaform.id, metaformMember.id)
            assertNotNull(foundMember)

            testBuilder.systemAdmin.metaformMembers.updateMember(
                metaform.id,
                metaformMember.id,
                MetaformMember(
                    email = "tommi@example.com",
                    firstName = "tommi",
                    lastName = "tommi",
                    role = MetaformMemberRole.mANAGER
                )
            )

            val managerAuthentication = testBuilder.createTestBuilderAuthentication(metaformMember.firstName, memberPassword)
            managerAuthentication.metaformMembers.assertFindFailStatus(403, metaform.id, metaformMember.id)
        }
    }
}