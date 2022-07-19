package fi.metatavu.metaform.server.test.functional.tests

import fi.metatavu.metaform.api.client.models.*
import fi.metatavu.metaform.server.test.functional.AbstractTest
import fi.metatavu.metaform.server.test.functional.builder.TestBuilder
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
    fun createMetaformMember() {
        TestBuilder().use { testBuilder ->
            val metaform = testBuilder.metaformAdmin.metaforms.createFromJsonFile("simple")
            val metaformMember = testBuilder.metaformAdmin.metaformMembers.create(
                metaform.id!!,
                MetaformMember(
                    email = "tommi@example.com",
                    firstName = "tommi",
                    lastName = "tommi",
                    role = MetaformMemberRole.mANAGER
                ))

            assertNotNull(metaformMember)
            assertNotNull(metaformMember.id)
            assertEquals(MetaformMemberRole.mANAGER, metaformMember.role)
            assertEquals("tommi@example.com", metaformMember.email)
            assertEquals("tommi", metaformMember.firstName)
            assertEquals("tommi", metaformMember.lastName)

            val foundMember = testBuilder.metaformAdmin.metaformMembers.findMember(metaform.id, metaformMember.id!!)

            assertNotNull(foundMember)
            assertEquals(metaformMember.id, foundMember.id)
            assertEquals(metaformMember.firstName, foundMember.firstName)
            assertEquals(metaformMember.lastName, foundMember.lastName)
            assertEquals(metaformMember.email, foundMember.email)
            assertEquals(metaformMember.role, foundMember.role)
        }
    }

    @Test
    fun createMetaformMemberMetaformAdmin() {
        TestBuilder().use { testBuilder ->
            val metaform = testBuilder.metaformAdmin.metaforms.createFromJsonFile("simple")

            val metaformAdminAuthentication = testBuilder.createMetaformAdminAuthentication(metaform.id!!)

            val adminCreatedMetaformMember = metaformAdminAuthentication.metaformMembers.create(
                metaform.id,
                MetaformMember(
                    email = "tommi2@example.com",
                    firstName = "tommi2",
                    lastName = "tommi2",
                    role = MetaformMemberRole.mANAGER
                )
            )

            assertNotNull(adminCreatedMetaformMember)
        }
    }

    @Test
    fun createMetaformMemberUnauthorized() {
        TestBuilder().use { testBuilder ->
            val metaform = testBuilder.metaformAdmin.metaforms.createFromJsonFile("simple")
            testBuilder.test1.metaformMembers.assertCreateFailStatus(
                403,
                metaform.id!!,
                MetaformMember(
                    email = "tommi@example.com",
                    firstName = "tommi",
                    lastName = "tommi",
                    role = MetaformMemberRole.aDMINISTRATOR
                )
            )

            val metaformManagerAuthentication = testBuilder.createMetaformManagerAuthentication(metaform.id)
            metaformManagerAuthentication.metaformMembers.assertCreateFailStatus(
                403,
                metaform.id,
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
    fun createMetaformMemberNotFound() {
        TestBuilder().use { testBuilder ->
            testBuilder.metaformAdmin.metaformMembers.assertCreateFailStatus(
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
    fun findMetaformMemberMetaformAdmin() {
        TestBuilder().use { testBuilder ->
            val metaform = testBuilder.metaformAdmin.metaforms.createFromJsonFile("simple")
            val metaformMember = testBuilder.metaformAdmin.metaformMembers.create(
                metaform.id!!,
                MetaformMember(
                    email = "tommi2@example.com",
                    firstName = "tommi2",
                    lastName = "tommi2",
                    role = MetaformMemberRole.mANAGER
                ))

            val metaformAdminAuthentication = testBuilder.createMetaformAdminAuthentication(metaform.id)
            val foundMetaformMember = metaformAdminAuthentication.metaformMembers.findMember(metaform.id, metaformMember.id!!)

            assertNotNull(foundMetaformMember)
        }
    }

    @Test
    @Throws(Exception::class)
    fun findMetaformMemberUnauthorized() {
        TestBuilder().use { testBuilder ->
            val metaform = testBuilder.metaformAdmin.metaforms.createFromJsonFile("simple")
            val metaformMember = testBuilder.metaformAdmin.metaformMembers.create(
                metaform.id!!,
                MetaformMember(
                    email = "tommi@example.com",
                    firstName = "tommi",
                    lastName = "tommi",
                    role = MetaformMemberRole.mANAGER
                )
            )

            testBuilder.test1.metaformMembers.assertFindFailStatus(403, metaform.id, metaformMember.id!!)

            val metaformManagerAuthentication = testBuilder.createMetaformManagerAuthentication(metaform.id)
            metaformManagerAuthentication.metaformMembers.assertFindFailStatus(403, metaform.id, metaformMember.id!!)
        }
    }

    @Test
    @Throws(Exception::class)
    fun findMetaformMemberNotFound() {
        TestBuilder().use { testBuilder ->
            val metaform = testBuilder.metaformAdmin.metaforms.createFromJsonFile("simple")
            val metaformMember = testBuilder.metaformAdmin.metaformMembers.create(
                metaform.id!!,
                MetaformMember(
                    email = "tommi@example.com",
                    firstName = "tommi",
                    lastName = "tommi",
                    role = MetaformMemberRole.mANAGER
                )
            )

//            testBuilder.metaformAdmin.metaformMembers.assertFindFailStatus(404, UUID.randomUUID(), metaformMember.id!!)
            testBuilder.metaformAdmin.metaformMembers.assertFindFailStatus(404, metaform.id, UUID.randomUUID())
        }
    }

    @Test
    @Throws(Exception::class)
    fun deleteMetaformMember() {
        TestBuilder().use { testBuilder ->
            val metaform = testBuilder.metaformAdmin.metaforms.createFromJsonFile("simple")
            val metaformMember = testBuilder.metaformAdmin.metaformMembers.create(
                metaform.id!!,
                MetaformMember(
                    email = "tommi@example.com",
                    firstName = "tommi",
                    lastName = "tommi",
                    role = MetaformMemberRole.mANAGER
                )
            )

            val foundMember = testBuilder.metaformAdmin.metaformMembers.findMember(metaform.id, metaformMember.id!!)

            assertNotNull(foundMember)
            testBuilder.metaformAdmin.metaformMembers.delete(metaform.id, foundMember.id!!)

            testBuilder.metaformAdmin.metaformMembers.assertFindFailStatus(404, metaform.id, metaformMember.id)
        }
    }

    @Test
    @Throws(Exception::class)
    fun deleteMetaformMemberNotFound() {
        TestBuilder().use { testBuilder ->
            val metaform = testBuilder.metaformAdmin.metaforms.createFromJsonFile("simple")
            val metaformMember = testBuilder.metaformAdmin.metaformMembers.create(
                metaform.id!!,
                MetaformMember(
                    email = "tommi@example.com",
                    firstName = "tommi",
                    lastName = "tommi",
                    role = MetaformMemberRole.mANAGER
                )
            )

            val foundMember = testBuilder.metaformAdmin.metaformMembers.findMember(metaform.id, metaformMember.id!!)

            testBuilder.metaformAdmin.metaformMembers.assertDeleteFailStatus(404, UUID.randomUUID(), foundMember.id!!)
            testBuilder.metaformAdmin.metaformMembers.assertDeleteFailStatus(404, metaform.id, UUID.randomUUID())
        }
    }

    @Test
    @Throws(Exception::class)
    fun deleteMetaformMemberUnauthorized() {
        TestBuilder().use { testBuilder ->
            val metaform = testBuilder.metaformAdmin.metaforms.createFromJsonFile("simple")
            val metaformMember = testBuilder.metaformAdmin.metaformMembers.create(
                metaform.id!!,
                MetaformMember(
                    email = "tommi@example.com",
                    firstName = "tommi",
                    lastName = "tommi",
                    role = MetaformMemberRole.mANAGER
                )
            )

            val foundMember = testBuilder.metaformAdmin.metaformMembers.findMember(metaform.id, metaformMember.id!!)

            testBuilder.test1.metaformMembers.assertDeleteFailStatus(403, metaform.id, foundMember.id!!)

            val metaformManagerAuthentication = testBuilder.createMetaformManagerAuthentication(metaform.id)
            metaformManagerAuthentication.metaformMembers.assertDeleteFailStatus(403, metaform.id, foundMember.id)
        }
    }

    @Test
    @Throws(Exception::class)
    fun deleteMetaformMemberMetaformAdmin() {
        TestBuilder().use { testBuilder ->
            val metaform = testBuilder.metaformAdmin.metaforms.createFromJsonFile("simple")
            val metaformMember = testBuilder.metaformAdmin.metaformMembers.create(
                metaform.id!!,
                MetaformMember(
                    email = "tommi2@example.com",
                    firstName = "tommi2",
                    lastName = "tommi2",
                    role = MetaformMemberRole.mANAGER
                )
            )

            val metaformAdminAuthentication = testBuilder.createMetaformAdminAuthentication(metaform.id)

            val foundMember = metaformAdminAuthentication.metaformMembers.findMember(metaform.id, metaformMember.id!!)
            assertNotNull(foundMember)
        }
    }

    // TODO find a way to integrate all the authentication related tests
    @Test
    @Throws(Exception::class)
    fun updateMetaformMember() {
        TestBuilder().use { testBuilder ->
            val metaform = testBuilder.metaformAdmin.metaforms.createFromJsonFile("simple")
            val metaformMember = testBuilder.metaformAdmin.metaformMembers.create(
                metaform.id!!,
                MetaformMember(
                    email = "tommi@example.com",
                    firstName = "tommi",
                    lastName = "tommi",
                    role = MetaformMemberRole.mANAGER
                )
            )

            val foundMember = testBuilder.metaformAdmin.metaformMembers.findMember(metaform.id, metaformMember.id!!)

            assertEquals("tommi@example.com", foundMember.email)
            assertEquals("tommi", foundMember.firstName)
            assertEquals("tommi", foundMember.lastName)

            testBuilder.metaformAdmin.metaformMembers.updateMember(
                metaform.id,
                foundMember.id!!,
                MetaformMember(
                    email = "tommi1@example.com",
                    firstName = "tommi1",
                    lastName = "tommi1",
                    role = MetaformMemberRole.mANAGER
                )
            )

            val foundUpdatedMember = testBuilder.metaformAdmin.metaformMembers.findMember(metaform.id, metaformMember.id!!)

            assertEquals("tommi1@example.com", foundUpdatedMember.email)
            assertEquals("tommi1", foundUpdatedMember.firstName)
            assertEquals("tommi1", foundUpdatedMember.lastName)
        }
    }

    @Test
    @Throws(Exception::class)
    fun updateMetaformMemberNotFound() {
        TestBuilder().use { testBuilder ->
            val metaform = testBuilder.metaformAdmin.metaforms.createFromJsonFile("simple")
            val metaformMember = testBuilder.metaformAdmin.metaformMembers.create(
                metaform.id!!,
                MetaformMember(
                    email = "tommi@example.com",
                    firstName = "tommi",
                    lastName = "tommi",
                    role = MetaformMemberRole.mANAGER
                )
            )

            val foundMember = testBuilder.metaformAdmin.metaformMembers.findMember(metaform.id, metaformMember.id!!)

            testBuilder.metaformAdmin.metaformMembers.assertUpdateFailStatus(404, UUID.randomUUID(), foundMember.id!!, foundMember)
            testBuilder.metaformAdmin.metaformMembers.assertUpdateFailStatus(404, metaform.id, UUID.randomUUID(), foundMember)
        }
    }

    @Test
    @Throws(Exception::class)
    fun updateMetaformMemberUnauthorized() {
        TestBuilder().use { testBuilder ->
            val metaform = testBuilder.metaformAdmin.metaforms.createFromJsonFile("simple")
            val metaformMember = testBuilder.metaformAdmin.metaformMembers.create(
                metaform.id!!,
                MetaformMember(
                    email = "tommi@example.com",
                    firstName = "tommi",
                    lastName = "tommi",
                    role = MetaformMemberRole.mANAGER
                )
            )

            val foundMember = testBuilder.metaformAdmin.metaformMembers.findMember(metaform.id, metaformMember.id!!)

            testBuilder.test1.metaformMembers.assertUpdateFailStatus(403, metaform.id, foundMember.id!!, foundMember)

            val metaformManagerAuthentication = testBuilder.createMetaformManagerAuthentication(metaform.id)
            metaformManagerAuthentication.metaformMembers.assertUpdateFailStatus(403, metaform.id, foundMember.id, foundMember)
        }
    }

    @Test
    @Throws(Exception::class)
    fun updateMetaformMemberMetaformAdmin() {
        TestBuilder().use { testBuilder ->
            val metaform = testBuilder.metaformAdmin.metaforms.createFromJsonFile("simple")
            val metaformMember = testBuilder.metaformAdmin.metaformMembers.create(
                metaform.id!!,
                MetaformMember(
                    email = "tommi2@example.com",
                    firstName = "tommi2",
                    lastName = "tommi2",
                    role = MetaformMemberRole.mANAGER
                )
            )

            val metaformAdminAuthentication = testBuilder.createMetaformAdminAuthentication(metaform.id)

            val foundMember = metaformAdminAuthentication.metaformMembers.updateMember(
                metaform.id,
                metaformMember.id!!,
                MetaformMember(
                    email = "tommi3@example.com",
                    firstName = "tommi3",
                    lastName = "tommi3",
                    role = MetaformMemberRole.mANAGER
                )
            )
            assertNotNull(foundMember)
        }
    }

    @Test
    @Throws(Exception::class)
    fun updateMetaformMemberRole1() {
        // TODO manager -> admin
    }

    @Test
    @Throws(Exception::class)
    fun updateMetaformMemberRole2() {
        // TODO admin -> manager
    }
}