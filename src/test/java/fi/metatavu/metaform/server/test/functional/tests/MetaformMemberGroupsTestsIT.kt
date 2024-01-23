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
import fi.metatavu.metaform.server.test.functional.common.InvalidValues
import io.quarkus.test.common.QuarkusTestResource
import io.quarkus.test.junit.QuarkusTest
import io.quarkus.test.junit.TestProfile
import io.restassured.http.Method
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.util.*

/**
 * Tests to test the Metaform member group system
 */
@QuarkusTest
@QuarkusTestResource.List(
    value = [QuarkusTestResource(MysqlResource::class), QuarkusTestResource(
        MetaformKeycloakResource::class
    )]
)
@TestProfile(GeneralTestProfile::class)
class MetaformMemberGroupsTestsIT : AbstractTest() {
    @Test
    @Throws(Exception::class)
    fun createMetaformMemberGroup() {
        TestBuilder().use { testBuilder ->
            val metaform = testBuilder.systemAdmin.metaforms.createFromJsonFile("simple")

            val metaformMember = testBuilder.systemAdmin.metaformMembers.createSimpleMember(metaform.id!!, "tommi")
            val metaformMemberGroup = testBuilder.systemAdmin.metaformMemberGroups.create(
                metaform.id,
                MetaformMemberGroup(
                    displayName = "Mikkeli",
                    memberIds = arrayOf(metaformMember.id!!)
                )
            )

            assertNotNull(metaformMemberGroup)
            assertNotNull(metaformMemberGroup.id)
            assertEquals("Mikkeli", metaformMemberGroup.displayName)
            assertEquals(metaformMember.id, metaformMemberGroup.memberIds[0])

            val foundMemberGroup = testBuilder.systemAdmin.metaformMemberGroups.find(metaform.id, metaformMemberGroup.id!!)

            assertNotNull(foundMemberGroup)
            assertEquals(metaformMemberGroup.id, foundMemberGroup.id)
            assertEquals(metaformMemberGroup.displayName, foundMemberGroup.displayName)
            assertEquals(metaformMemberGroup.memberIds[0], foundMemberGroup.memberIds[0])
        }
    }

    @Test
    @Throws(Exception::class)
    fun createMetaformMemberGroupPermission() {
        TestBuilder().use { testBuilder ->
            val metaform = testBuilder.systemAdmin.metaforms.createFromJsonFile("simple")

            testBuilder.permissionTestByScopes(
                scope = PermissionScope.METAFORM_ADMIN,
                apiCaller = { authentication: TestBuilderAuthentication, index: Int ->
                    authentication.metaformMemberGroups.create(
                        metaform.id!!,
                        MetaformMemberGroup(
                            displayName = String.format("Mikkeli%d", index),
                            memberIds = emptyArray()
                        )
                    )
                },
                metaformId = metaform.id
            )
        }
    }

    @Test
    @Throws(Exception::class)
    fun findMetaformMemberGroup() {
        TestBuilder().use { testBuilder ->
            val metaform = testBuilder.systemAdmin.metaforms.createFromJsonFile("simple")
            val metaformMemberGroup = testBuilder.systemAdmin.metaformMemberGroups.create(
                metaform.id!!,
                MetaformMemberGroup(
                    displayName = "Mikkeli",
                    memberIds = emptyArray()
                )
            )

            val foundMemberGroup = testBuilder.systemAdmin.metaformMemberGroups.find(metaform.id, metaformMemberGroup.id!!)

            assertNotNull(foundMemberGroup)
        }
    }

    @Test
    @Throws(Exception::class)
    fun findMetaformMemberGroupPermission() {
        TestBuilder().use { testBuilder ->
            val metaform = testBuilder.systemAdmin.metaforms.createFromJsonFile("simple")
            val metaformMemberGroup = testBuilder.systemAdmin.metaformMemberGroups.create(
                metaform.id!!,
                MetaformMemberGroup(
                    displayName = "Mikkeli",
                    memberIds = emptyArray()
                )
            )

            testBuilder.permissionTestByScopes(
                scope = PermissionScope.METAFORM_ADMIN,
                apiCaller = { authentication: TestBuilderAuthentication, index: Int ->
                    authentication.metaformMemberGroups.find(
                        metaform.id,
                        metaformMemberGroup.id!!
                    )
                },
                metaformId = metaform.id
            )
        }
    }

    @Test
    @Throws(Exception::class)
    fun findMetaformMemberGroupNotFound() {
        TestBuilder().use { testBuilder ->
            val metaform = testBuilder.systemAdmin.metaforms.createFromJsonFile("simple")
            val metaformMemberGroup = testBuilder.systemAdmin.metaformMemberGroups.create(
                metaform.id!!,
                MetaformMemberGroup(
                    displayName = "Mikkeli",
                    memberIds = emptyArray()
                )
            )

            testBuilder.systemAdmin.metaformMembers.assertFindFailStatus(404, UUID.randomUUID(), metaformMemberGroup.id!!)
            testBuilder.systemAdmin.metaformMembers.assertFindFailStatus(404, metaform.id, UUID.randomUUID())

            InvalidValueTestScenarioBuilder(path = "v1/metaforms/{metaformId}/memberGroups/{metaformMemberGroupId}", method = Method.GET, token = testBuilder.systemAdmin.token)
                .path(InvalidValueTestScenarioPath(name = "metaformId", values = InvalidValues.STRING_NOT_NULL, default = "", expectedStatus = 404))
                .path(InvalidValueTestScenarioPath(name = "metaformMemberGroupId", values = InvalidValues.STRING_NOT_NULL, default = "", expectedStatus = 404))
                .build()
                .test()
        }
    }

    @Test
    @Throws(Exception::class)
    fun listMetaformMemberGroup() {
        TestBuilder().use { testBuilder ->
            val metaform = testBuilder.systemAdmin.metaforms.createFromJsonFile("simple")
            val metaformMember = testBuilder.systemAdmin.metaformMembers.createSimpleMember(metaform.id!!, "tommi")

            testBuilder.systemAdmin.metaformMemberGroups.create(metaform.id,
                MetaformMemberGroup(
                displayName = "Mikkeli",
                memberIds = arrayOf(metaformMember.id!!)
            ))

            val metaformMemberGroups = testBuilder.systemAdmin.metaformMemberGroups.list(metaform.id)

            assertEquals(metaformMemberGroups.size, 1)
            assertEquals(metaformMemberGroups[0].memberIds[0], metaformMember.id)
        }
    }

    @Test
    @Throws(Exception::class)
    fun listMetaformMemberNotFound() {
        TestBuilder().use { testBuilder ->
            InvalidValueTestScenarioBuilder(path = "v1/metaforms/{metaformId}/memberGroups", method = Method.GET, token = testBuilder.systemAdmin.token)
                .path(InvalidValueTestScenarioPath(name = "metaformId", values = InvalidValues.STRING_NOT_NULL,  expectedStatus = 404))
                .build()
                .test()
        }
    }

    @Test
    @Throws(Exception::class)
    fun listMetaformMemberPermission() {
        TestBuilder().use { testBuilder ->
            val metaform = testBuilder.systemAdmin.metaforms.createFromJsonFile("simple")
            testBuilder.systemAdmin.metaformMemberGroups.create(
                metaform.id!!,
                MetaformMemberGroup(
                    displayName = "Mikkeli",
                    memberIds = emptyArray()
                )
            )

            testBuilder.permissionTestByScopes(
                scope = PermissionScope.METAFORM_ADMIN,
                apiCaller = { authentication: TestBuilderAuthentication, _: Int ->
                    authentication.metaformMemberGroups.list(metaform.id)
                },
                metaformId = metaform.id
            )
        }
    }

    @Test
    @Throws(Exception::class)
    fun deleteMetaformMemberGroup() {
        TestBuilder().use { testBuilder ->
            val metaform = testBuilder.systemAdmin.metaforms.createFromJsonFile("simple")
            val metaformMemberGroup = testBuilder.systemAdmin.metaformMemberGroups.create(
                metaform.id!!,
                MetaformMemberGroup(
                    displayName = "Mikkeli",
                    memberIds = emptyArray()
                )
            )

            val foundMemberGroup = testBuilder.systemAdmin.metaformMemberGroups.find(metaform.id, metaformMemberGroup.id!!)

            assertNotNull(foundMemberGroup)
            testBuilder.systemAdmin.metaformMemberGroups.delete(metaform.id, foundMemberGroup.id!!)

            testBuilder.systemAdmin.metaformMemberGroups.assertFindFailStatus(404, metaform.id, foundMemberGroup.id)
        }
    }

    @Test
    @Throws(Exception::class)
    fun deleteMetaformMemberGroupPermission() {
        TestBuilder().use { testBuilder ->
            val metaform = testBuilder.systemAdmin.metaforms.createFromJsonFile("simple")

            testBuilder.permissionTestByScopes(
                scope = PermissionScope.METAFORM_ADMIN,
                apiCaller = { authentication: TestBuilderAuthentication, index: Int ->
                    val metaformMemberGroup = testBuilder.systemAdmin.metaformMemberGroups.create(
                        metaform.id!!,
                        MetaformMemberGroup(
                            displayName = String.format("Mikkeli%d", index),
                            memberIds = emptyArray()
                        )
                    )

                    authentication.metaformMemberGroups.find(
                        metaform.id,
                        metaformMemberGroup.id!!
                    )
                },
                metaformId = metaform.id
            )
        }
    }

    @Test
    @Throws(Exception::class)
    fun deleteMetaformMemberNotFound() {
        TestBuilder().use { testBuilder ->
            val metaform = testBuilder.systemAdmin.metaforms.createFromJsonFile("simple")
            val metaformMemberGroup = testBuilder.systemAdmin.metaformMemberGroups.create(
                metaform.id!!,
                MetaformMemberGroup(
                    displayName = "Mikkeli",
                    memberIds = emptyArray()
                )
            )

            val foundMemberGroup = testBuilder.systemAdmin.metaformMemberGroups.find(metaform.id, metaformMemberGroup.id!!)

            InvalidValueTestScenarioBuilder(path = "v1/metaforms/{metaformId}/memberGroups/{metaformMemberGroupId}", method = Method.DELETE, token = testBuilder.systemAdmin.token)
                .path(InvalidValueTestScenarioPath(name = "metaformId", values = InvalidValues.STRING_NOT_NULL, default = metaform.id, expectedStatus = 404))
                .path(InvalidValueTestScenarioPath(name = "metaformMemberGroupId", values = InvalidValues.STRING_NOT_NULL,  default = foundMemberGroup.id, expectedStatus = 404))
                .build()
                .test()
        }
    }

    @Test
    @Throws(Exception::class)
    fun updateMetaformMemberGroup() {
        TestBuilder().use { testBuilder ->
            val metaform = testBuilder.systemAdmin.metaforms.createFromJsonFile("simple")
            val metaformMemberGroup = testBuilder.systemAdmin.metaformMemberGroups.create(
                metaform.id!!,
                MetaformMemberGroup(
                    displayName = "Mikkeli",
                    memberIds = emptyArray()
                )
            )

            val foundMemberGroup = testBuilder.systemAdmin.metaformMemberGroups.find(metaform.id, metaformMemberGroup.id!!)

            assertEquals(0, foundMemberGroup.memberIds.size)
            assertEquals("Mikkeli", foundMemberGroup.displayName)

            val metaformMember1 = testBuilder.systemAdmin.metaformMembers.createSimpleMember(metaform.id, "tommi")
            val metaformMember2 = testBuilder.systemAdmin.metaformMembers.createSimpleMember(metaform.id, "tommi2")

            testBuilder.systemAdmin.metaformMemberGroups.update(
                metaform.id,
                foundMemberGroup.id!!,
                MetaformMemberGroup(
                    displayName = "Mikkeli2",
                    memberIds = arrayOf(metaformMember1.id!!, metaformMember2.id!!)
                )
            )

            val foundUpdatedGroup = testBuilder.systemAdmin.metaformMemberGroups.find(metaform.id, metaformMemberGroup.id)
            assertTrue(foundUpdatedGroup.memberIds.contains(metaformMember1.id))
            assertTrue(foundUpdatedGroup.memberIds.contains(metaformMember2.id))
            assertEquals("Mikkeli2", foundUpdatedGroup.displayName)

            testBuilder.systemAdmin.metaformMemberGroups.update(
                metaform.id,
                foundMemberGroup.id,
                MetaformMemberGroup(
                    displayName = "Mikkeli2",
                    memberIds = emptyArray()
                )
            )
            val foundUpdatedMember2 = testBuilder.systemAdmin.metaformMemberGroups.find(metaform.id, metaformMemberGroup.id)
            assertEquals(0, foundUpdatedMember2.memberIds.size)

        }
    }

    @Test
    @Throws(Exception::class)
    fun updateMetaformMemberGroupPermission() {
        TestBuilder().use { testBuilder ->
            val metaform = testBuilder.systemAdmin.metaforms.createFromJsonFile("simple")
            val metaformMemberGroup = testBuilder.systemAdmin.metaformMemberGroups.create(
                metaform.id!!,
                MetaformMemberGroup(
                    displayName = "Mikkeli",
                    memberIds = emptyArray()
                )
            )

            testBuilder.permissionTestByScopes(
                scope = PermissionScope.METAFORM_ADMIN,
                apiCaller = { authentication: TestBuilderAuthentication, index: Int ->
                    authentication.metaformMemberGroups.update(
                        metaform.id,
                        metaformMemberGroup.id!!,
                        metaformMemberGroup
                    )
                },
                metaformId = metaform.id
            )
        }
    }

    @Test
    @Throws(Exception::class)
    fun updateMetaformMemberGroupNotFound() {
        TestBuilder().use { testBuilder ->
            val metaform = testBuilder.systemAdmin.metaforms.createFromJsonFile("simple")
            val metaformMemberGroup = testBuilder.systemAdmin.metaformMemberGroups.create(
                metaform.id!!,
                MetaformMemberGroup(
                    displayName = "Mikkeli",
                    memberIds = emptyArray()
                )
            )

            val foundMemberGroup = testBuilder.systemAdmin.metaformMemberGroups.find(metaform.id, metaformMemberGroup.id!!)

            InvalidValueTestScenarioBuilder(
                path = "v1/metaforms/{metaformId}/memberGroups/{metaformMemberGroupId}",
                method = Method.PUT,
                token = testBuilder.systemAdmin.token,
                body = jacksonObjectMapper().writeValueAsString(foundMemberGroup)
            )
                .path(InvalidValueTestScenarioPath(name = "metaformId", values = InvalidValues.STRING_NOT_NULL, default = metaform.id, expectedStatus = 404))
                .path(InvalidValueTestScenarioPath(name = "metaformMemberGroupId", values = InvalidValues.STRING_NOT_NULL, default = foundMemberGroup.id, expectedStatus = 404))
                .build()
                .test()
        }
    }
}