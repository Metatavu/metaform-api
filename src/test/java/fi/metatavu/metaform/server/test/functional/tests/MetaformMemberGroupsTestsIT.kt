package fi.metatavu.metaform.server.test.functional.tests

import fi.metatavu.metaform.api.client.models.*
import fi.metatavu.metaform.server.test.functional.AbstractTest
import fi.metatavu.metaform.server.test.functional.builder.TestBuilder
import fi.metatavu.metaform.server.test.functional.builder.resources.KeycloakResource
import fi.metatavu.metaform.server.test.functional.builder.resources.MysqlResource
import io.quarkus.test.common.QuarkusTestResource
import io.quarkus.test.junit.QuarkusTest
import io.quarkus.test.junit.TestProfile
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.util.*

/**
 * Tests to test the Metaform member group system
 */
@QuarkusTest
@QuarkusTestResource.List(
    value = [QuarkusTestResource(MysqlResource::class), QuarkusTestResource(
        KeycloakResource::class
    )]
)
@TestProfile(GeneralTestProfile::class)
class MetaformMemberGroupsTestsIT : AbstractTest() {
    @Test
    fun createMetaformMemberGroup() {
        TestBuilder().use { testBuilder ->
            val metaform = testBuilder.metaformAdmin.metaforms.createFromJsonFile("simple")

            val metaformMember = testBuilder.metaformAdmin.metaformMembers.createSimpleMember(metaform.id!!, "tommi")
            val metaformMemberGroup = testBuilder.metaformAdmin.metaformMemberGroups.create(
                metaform.id,
                MetaformMemberGroup(
                    displayName = "Mikkeli",
                    memberIds = arrayOf(metaformMember.id!!)
                )
            )

            Assertions.assertNotNull(metaformMemberGroup)
            Assertions.assertNotNull(metaformMemberGroup.id)
            Assertions.assertEquals("Mikkeli", metaformMemberGroup.displayName)
            Assertions.assertEquals(metaformMember.id, metaformMemberGroup.memberIds[0])

            val foundMemberGroup = testBuilder.metaformAdmin.metaformMemberGroups.findMemberGroup(metaform.id, metaformMemberGroup.id!!)

            Assertions.assertNotNull(foundMemberGroup)
            Assertions.assertEquals(metaformMemberGroup.id, foundMemberGroup.id)
            Assertions.assertEquals(metaformMemberGroup.displayName, foundMemberGroup.displayName)
            Assertions.assertEquals(metaformMemberGroup.memberIds[0], foundMemberGroup.memberIds[0])
        }
    }

    @Test
    fun createMetaformMemberGroupUnauthorized() {
        TestBuilder().use { testBuilder ->
            val metaform = testBuilder.metaformAdmin.metaforms.createFromJsonFile("simple")
            testBuilder.test1.metaformMemberGroups.assertCreateFailStatus(
                403,
                metaform.id!!,
                MetaformMemberGroup(
                    displayName = "Mikkeli",
                    memberIds = emptyArray()
                )
            )
        }
    }

    @Test
    @Throws(Exception::class)
    fun createMetaformMemberGroupNotFound() {
        TestBuilder().use { testBuilder ->
            testBuilder.metaformAdmin.metaformMemberGroups.assertCreateFailStatus(
                404,
                UUID.randomUUID(),
                MetaformMemberGroup(
                    displayName = "Mikkeli",
                    memberIds = emptyArray()
                )
            )
        }
    }

    @Test
    @Throws(Exception::class)
    fun findMetaformMemberGroup() {
        TestBuilder().use { testBuilder ->
            val metaform = testBuilder.metaformAdmin.metaforms.createFromJsonFile("simple")
            val metaformMemberGroup = testBuilder.metaformAdmin.metaformMemberGroups.create(
                metaform.id!!,
                MetaformMemberGroup(
                    displayName = "Mikkeli",
                    memberIds = emptyArray()
                )
            )

            val foundMemberGroup = testBuilder.metaformAdmin.metaformMemberGroups.findMemberGroup(metaform.id, metaformMemberGroup.id!!)

            Assertions.assertNotNull(foundMemberGroup)
        }
    }

    @Test
    @Throws(Exception::class)
    fun findMetaformMemberGroupUnauthorized() {
        TestBuilder().use { testBuilder ->
            val metaform = testBuilder.metaformAdmin.metaforms.createFromJsonFile("simple")
            val metaformMemberGroup = testBuilder.metaformAdmin.metaformMemberGroups.create(
                metaform.id!!,
                MetaformMemberGroup(
                    displayName = "Mikkeli",
                    memberIds = emptyArray()
                )
            )

            testBuilder.test1.metaformMemberGroups.assertFindFailStatus(403, metaform.id, metaformMemberGroup.id!!)
        }
    }

    @Test
    @Throws(Exception::class)
    fun findMetaformMemberGroupNotFound() {
        TestBuilder().use { testBuilder ->
            val metaform = testBuilder.metaformAdmin.metaforms.createFromJsonFile("simple")
            val metaformMemberGroup = testBuilder.metaformAdmin.metaformMemberGroups.create(
                metaform.id!!,
                MetaformMemberGroup(
                    displayName = "Mikkeli",
                    memberIds = emptyArray()
                )
            )

            testBuilder.metaformAdmin.metaformMembers.assertFindFailStatus(404, UUID.randomUUID(), metaformMemberGroup.id!!)
            testBuilder.metaformAdmin.metaformMembers.assertFindFailStatus(404, metaform.id, UUID.randomUUID())
        }
    }

    @Test
    @Throws(Exception::class)
    fun deleteMetaformMemberGroup() {
        TestBuilder().use { testBuilder ->
            val metaform = testBuilder.metaformAdmin.metaforms.createFromJsonFile("simple")
            val metaformMemberGroup = testBuilder.metaformAdmin.metaformMemberGroups.create(
                metaform.id!!,
                MetaformMemberGroup(
                    displayName = "Mikkeli",
                    memberIds = emptyArray()
                )
            )

            val foundMemberGroup = testBuilder.metaformAdmin.metaformMemberGroups.findMemberGroup(metaform.id, metaformMemberGroup.id!!)

            Assertions.assertNotNull(foundMemberGroup)
            testBuilder.metaformAdmin.metaformMemberGroups.delete(metaform.id, foundMemberGroup.id!!)

            testBuilder.metaformAdmin.metaformMemberGroups.assertFindFailStatus(404, metaform.id, foundMemberGroup.id)
        }
    }

    @Test
    @Throws(Exception::class)
    fun deleteMetaformMemberNotFound() {
        TestBuilder().use { testBuilder ->
            val metaform = testBuilder.metaformAdmin.metaforms.createFromJsonFile("simple")
            val metaformMemberGroup = testBuilder.metaformAdmin.metaformMemberGroups.create(
                metaform.id!!,
                MetaformMemberGroup(
                    displayName = "Mikkeli",
                    memberIds = emptyArray()
                )
            )

            val foundMemberGroup = testBuilder.metaformAdmin.metaformMemberGroups.findMemberGroup(metaform.id, metaformMemberGroup.id!!)

            testBuilder.metaformAdmin.metaformMemberGroups.assertDeleteFailStatus(404, UUID.randomUUID(), foundMemberGroup.id!!)
            testBuilder.metaformAdmin.metaformMemberGroups.assertDeleteFailStatus(404, metaform.id, UUID.randomUUID())
        }
    }

    @Test
    @Throws(Exception::class)
    fun deleteMetaformMemberGroupUnauthorized() {
        TestBuilder().use { testBuilder ->
            val metaform = testBuilder.metaformAdmin.metaforms.createFromJsonFile("simple")
            val metaformMemberGroup = testBuilder.metaformAdmin.metaformMemberGroups.create(
                metaform.id!!,
                MetaformMemberGroup(
                    displayName = "Mikkeli",
                    memberIds = emptyArray()
                )
            )

            val foundMemberGroup = testBuilder.metaformAdmin.metaformMemberGroups.findMemberGroup(metaform.id, metaformMemberGroup.id!!)

            testBuilder.test1.metaformMembers.assertDeleteFailStatus(403, metaform.id, foundMemberGroup.id!!)
        }
    }

    // TODO find a way to integrate all the authentication related tests
    @Test
    @Throws(Exception::class)
    fun updateMetaformMemberGroup() {
        TestBuilder().use { testBuilder ->
            val metaform = testBuilder.metaformAdmin.metaforms.createFromJsonFile("simple")
            val metaformMemberGroup = testBuilder.metaformAdmin.metaformMemberGroups.create(
                metaform.id!!,
                MetaformMemberGroup(
                    displayName = "Mikkeli",
                    memberIds = emptyArray()
                )
            )

            val foundMemberGroup = testBuilder.metaformAdmin.metaformMemberGroups.findMemberGroup(metaform.id, metaformMemberGroup.id!!)

            Assertions.assertEquals(0, foundMemberGroup.memberIds.size)
            Assertions.assertEquals("Mikkeli", foundMemberGroup.displayName)

            val metaformMember1 = testBuilder.metaformAdmin.metaformMembers.createSimpleMember(metaform.id, "tommi")
            val metaformMember2 = testBuilder.metaformAdmin.metaformMembers.createSimpleMember(metaform.id, "tommi2")

            testBuilder.metaformAdmin.metaformMemberGroups.update(
                metaform.id,
                foundMemberGroup.id!!,
                MetaformMemberGroup(
                    displayName = "Mikkeli2",
                    memberIds = arrayOf(metaformMember1.id!!, metaformMember2.id!!)
                )
            )

            val foundUpdatedMember = testBuilder.metaformAdmin.metaformMemberGroups.findMemberGroup(metaform.id, metaformMemberGroup.id)

            Assertions.assertEquals(metaformMember1.id, foundUpdatedMember.memberIds[0])
            Assertions.assertEquals(metaformMember2.id, foundUpdatedMember.memberIds[1])
            Assertions.assertEquals("Mikkeli2", foundUpdatedMember.displayName)

            testBuilder.metaformAdmin.metaformMemberGroups.update(
                metaform.id,
                foundMemberGroup.id,
                MetaformMemberGroup(
                    displayName = "Mikkeli2",
                    memberIds = emptyArray()
                )
            )
            val foundUpdatedMember2 = testBuilder.metaformAdmin.metaformMemberGroups.findMemberGroup(metaform.id, metaformMemberGroup.id)
            Assertions.assertEquals(0, foundUpdatedMember2.memberIds.size)

        }
    }

    @Test
    @Throws(Exception::class)
    fun updateMetaformMemberGroupNotFound() {
        TestBuilder().use { testBuilder ->
            val metaform = testBuilder.metaformAdmin.metaforms.createFromJsonFile("simple")
            val metaformMemberGroup = testBuilder.metaformAdmin.metaformMemberGroups.create(
                metaform.id!!,
                MetaformMemberGroup(
                    displayName = "Mikkeli",
                    memberIds = emptyArray()
                )
            )

            val foundMemberGroup = testBuilder.metaformAdmin.metaformMemberGroups.findMemberGroup(metaform.id, metaformMemberGroup.id!!)

            testBuilder.metaformAdmin.metaformMemberGroups.assertUpdateFailStatus(404, UUID.randomUUID(), foundMemberGroup.id!!, foundMemberGroup)
            testBuilder.metaformAdmin.metaformMemberGroups.assertUpdateFailStatus(404, metaform.id, UUID.randomUUID(), foundMemberGroup)
        }
    }

    @Test
    @Throws(Exception::class)
    fun updateMetaformMemberGroupUnauthorized() {
        TestBuilder().use { testBuilder ->
            val metaform = testBuilder.metaformAdmin.metaforms.createFromJsonFile("simple")
            val metaformMemberGroup = testBuilder.metaformAdmin.metaformMemberGroups.create(
                metaform.id!!,
                MetaformMemberGroup(
                    displayName = "Mikkeli",
                    memberIds = emptyArray()
                )
            )

            val foundMemberGroup = testBuilder.metaformAdmin.metaformMemberGroups.findMemberGroup(metaform.id, metaformMemberGroup.id!!)

            testBuilder.test1.metaformMemberGroups.assertUpdateFailStatus(403, metaform.id, foundMemberGroup.id!!, foundMemberGroup)
        }
    }
}