package fi.metatavu.metaform.server.test.functional.tests

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import fi.metatavu.metaform.api.client.models.MetaformVersion
import fi.metatavu.metaform.api.client.models.MetaformVersionType
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
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.util.*

/**
 * Tests to test the Metaform version system
 */
@QuarkusTest
@QuarkusTestResource.List(
    value = [QuarkusTestResource(MysqlResource::class), QuarkusTestResource(
        MetaformKeycloakResource::class
    )]
)
@TestProfile(GeneralTestProfile::class)
class VersionTestsIT : AbstractTest() {
    @Test
    @Throws(Exception::class)
    fun createVersion() {
        TestBuilder().use { testBuilder ->
            val metaform = testBuilder.systemAdmin.metaforms.createFromJsonFile("simple")
            val versionData = testBuilder.systemAdmin.metaformVersions.exampleVersionData
            val version = MetaformVersion(
                    type = MetaformVersionType.ARCHIVED,
                    data = versionData
            )

            val createdVersion = testBuilder.systemAdmin.metaformVersions.create(metaform.id!!, version)
            Assertions.assertNotNull(createdVersion)
            Assertions.assertNotNull(createdVersion.id)
            Assertions.assertEquals(MetaformVersionType.ARCHIVED, createdVersion.type)
            Assertions.assertEquals(versionData, createdVersion.data)

            val foundVersion = testBuilder.systemAdmin.metaformVersions.findVersion(metaform.id, createdVersion.id!!)
            Assertions.assertNotNull(foundVersion)
            Assertions.assertEquals(createdVersion.id, foundVersion.id)
            Assertions.assertEquals(createdVersion.type, foundVersion.type)
            Assertions.assertEquals(createdVersion.data, foundVersion.data)
        }
    }

    @Test
    @Throws(Exception::class)
    fun createVersionNotFound() {
        TestBuilder().use { testBuilder ->
            val versionData = testBuilder.systemAdmin.metaformVersions.exampleVersionData
            val version = MetaformVersion(
                    type = MetaformVersionType.ARCHIVED,
                    data = versionData
            )

            testBuilder.systemAdmin.metaformVersions.assertCreateFailStatus(404, UUID.randomUUID(), version)
        }
    }

    @Test
    @Throws(Exception::class)
    fun createVersionUnAuthorized() {
        TestBuilder().use { testBuilder ->
            val metaform = testBuilder.systemAdmin.metaforms.createFromJsonFile("simple")
            val versionData = testBuilder.systemAdmin.metaformVersions.exampleVersionData
            val version = MetaformVersion(
                    type = MetaformVersionType.ARCHIVED,
                    data = versionData
            )

            testBuilder.test1.metaformVersions.assertCreateFailStatus(403, metaform.id!!, version)
        }
    }

    @Test
    @Throws(Exception::class)
    fun createVersionPermission() {
        TestBuilder().use { testBuilder ->
            val metaform = testBuilder.systemAdmin.metaforms.createFromJsonFile("simple")

            testBuilder.permissionTestByScopes(
                scope = PermissionScope.METAFORM_ADMIN,
                apiCaller = { authentication: TestBuilderAuthentication, _: Int ->
                    authentication.metaformVersions.create(
                        metaform.id!!,
                        MetaformVersion(
                            type = MetaformVersionType.ARCHIVED,
                            data = testBuilder.systemAdmin.metaformVersions.exampleVersionData
                        )
                    )
                },
                metaformId = metaform.id
            )
        }
    }

    @Test
    @Throws(Exception::class)
    fun listVersions() {
        TestBuilder().use { testBuilder ->
            val metaform = testBuilder.systemAdmin.metaforms.createFromJsonFile("simple")
            val versionData = testBuilder.systemAdmin.metaformVersions.exampleVersionData

            testBuilder.systemAdmin.metaformVersions.create(
                metaform.id!!,
                MetaformVersion(
                        type = MetaformVersionType.ARCHIVED,
                        data = versionData
                )
            )

            testBuilder.systemAdmin.metaformVersions.assertCount(metaform.id, 1)

            testBuilder.systemAdmin.metaformVersions.create(
                    metaform.id,
                    MetaformVersion(
                            type = MetaformVersionType.DRAFT,
                            data = versionData
                    )
            )
            testBuilder.systemAdmin.metaformVersions.assertCount(metaform.id, 2)
        }
    }

    @Test
    @Throws(Exception::class)
    fun listVersionsNotFound() {
        TestBuilder().use { testBuilder ->
            val metaform = testBuilder.systemAdmin.metaforms.createFromJsonFile("simple")
            val versionData = testBuilder.systemAdmin.metaformVersions.exampleVersionData

            testBuilder.systemAdmin.metaformVersions.create(
                    metaform.id!!,
                    MetaformVersion(
                            type = MetaformVersionType.ARCHIVED,
                            data = versionData
                    )
            )
            testBuilder.systemAdmin.metaformVersions.assertListFailStatus(404, UUID.randomUUID())
        }
    }

    @Test
    @Throws(Exception::class)
    fun listVersionPermission() {
        TestBuilder().use { testBuilder ->
            val metaform = testBuilder.systemAdmin.metaforms.createFromJsonFile("simple")
            testBuilder.systemAdmin.metaformVersions.create(
                metaform.id!!,
                MetaformVersion(
                    type = MetaformVersionType.ARCHIVED,
                    data = testBuilder.systemAdmin.metaformVersions.exampleVersionData
                )
            )

            testBuilder.permissionTestByScopes(
                scope = PermissionScope.METAFORM_ADMIN,
                apiCaller = { authentication: TestBuilderAuthentication, _: Int ->
                    authentication.metaformVersions.list(metaform.id)
                },
                metaformId = metaform.id
            )
        }
    }

    @Test
    @Throws(Exception::class)
    fun findVersion() {
        TestBuilder().use { testBuilder ->
            val metaform = testBuilder.systemAdmin.metaforms.createFromJsonFile("simple")
            val versionData = testBuilder.systemAdmin.metaformVersions.exampleVersionData

            val version = MetaformVersion(
                    type = MetaformVersionType.ARCHIVED,
                    data = versionData
            )

            val createdVersion = testBuilder.systemAdmin.metaformVersions.create(metaform.id!!, version)
            val foundVersion = testBuilder.systemAdmin.metaformVersions.findVersion(metaform.id, createdVersion.id!!)
            testBuilder.systemAdmin.metaformVersions.assertCount(metaform.id, 1)

            Assertions.assertNotNull(foundVersion)
            Assertions.assertEquals(MetaformVersionType.ARCHIVED, foundVersion.type)
            Assertions.assertEquals(versionData, foundVersion.data)
        }
    }

    @Test
    @Throws(Exception::class)
    fun findVersionPermission() {
        TestBuilder().use { testBuilder ->
            val metaform = testBuilder.systemAdmin.metaforms.createFromJsonFile("simple")
            val metaformVersion = testBuilder.systemAdmin.metaformVersions.create(
                metaform.id!!,
                MetaformVersion(
                    type = MetaformVersionType.ARCHIVED,
                    data = testBuilder.systemAdmin.metaformVersions.exampleVersionData
                )
            )

            testBuilder.permissionTestByScopes(
                scope = PermissionScope.METAFORM_ADMIN,
                apiCaller = { authentication: TestBuilderAuthentication, _: Int ->
                    authentication.metaformVersions.findVersion(metaform.id, metaformVersion.id!!)
                },
                metaformId = metaform.id
            )
        }
    }

    @Test
    @Throws(Exception::class)
    fun findVersionNotFound() {
        TestBuilder().use { testBuilder ->
            val metaform = testBuilder.systemAdmin.metaforms.createFromJsonFile("simple")
            val versionData = testBuilder.systemAdmin.metaformVersions.exampleVersionData

            val version = MetaformVersion(
                    type = MetaformVersionType.ARCHIVED,
                    data = versionData
            )

            val createdVersion = testBuilder.systemAdmin.metaformVersions.create(metaform.id!!, version)
            testBuilder.systemAdmin.metaformVersions.assertFindFailStatus(404, UUID.randomUUID(), createdVersion.id!!)
        }
    }

    @Test
    @Throws(Exception::class)
    fun deleteVersion() {
        TestBuilder().use { testBuilder ->
            val metaform = testBuilder.systemAdmin.metaforms.createFromJsonFile("simple")
            val versionData = testBuilder.systemAdmin.metaformVersions.exampleVersionData

            val version = MetaformVersion(
                    type = MetaformVersionType.ARCHIVED,
                    data = versionData
            )

            val createdVersion = testBuilder.systemAdmin.metaformVersions.create(metaform.id!!, version)
            val foundVersion = testBuilder.systemAdmin.metaformVersions.findVersion(metaform.id, createdVersion.id!!)
            testBuilder.systemAdmin.metaformVersions.assertCount(metaform.id, 1)

            Assertions.assertNotNull(foundVersion)
            Assertions.assertEquals(MetaformVersionType.ARCHIVED, foundVersion.type)
            Assertions.assertEquals(versionData, foundVersion.data)
            testBuilder.systemAdmin.metaformVersions.delete(metaform.id, foundVersion.id!!)

            testBuilder.systemAdmin.metaformVersions.assertCount(metaform.id, 0)
        }
    }

    @Test
    @Throws(Exception::class)
    fun deleteVersionNotFound() {
        TestBuilder().use { testBuilder ->
            val metaform = testBuilder.systemAdmin.metaforms.createFromJsonFile("simple")
            val versionData = testBuilder.systemAdmin.metaformVersions.exampleVersionData

            val version = MetaformVersion(
                    type = MetaformVersionType.ARCHIVED,
                    data = versionData
            )

            val createdVersion = testBuilder.systemAdmin.metaformVersions.create(metaform.id!!, version)
            val foundVersion = testBuilder.systemAdmin.metaformVersions.findVersion(metaform.id, createdVersion.id!!)

            testBuilder.systemAdmin.metaformVersions.assertDeleteFailStatus(404, UUID.randomUUID(), foundVersion.id!!)
            testBuilder.systemAdmin.metaformVersions.assertDeleteFailStatus(404, metaform.id, UUID.randomUUID())
        }
    }

    @Test
    @Throws(Exception::class)
    fun deleteVersionPermission() {
        TestBuilder().use { testBuilder ->
            val metaform = testBuilder.systemAdmin.metaforms.createFromJsonFile("simple")

            testBuilder.permissionTestByScopes(
                scope = PermissionScope.METAFORM_ADMIN,
                apiCaller = { authentication: TestBuilderAuthentication, _: Int ->
                    val metaformVersion = testBuilder.systemAdmin.metaformVersions.create(
                        metaform.id!!,
                        MetaformVersion(
                            type = MetaformVersionType.ARCHIVED,
                            data = testBuilder.systemAdmin.metaformVersions.exampleVersionData
                        )
                    )
                    authentication.metaformVersions.delete(metaform.id, metaformVersion.id!!)
                },
                metaformId = metaform.id
            )
        }
    }

    @Test
    @Throws(Exception::class)
    fun updateVersion() {
        TestBuilder().use { testBuilder ->
            val metaform = testBuilder.systemAdmin.metaforms.createFromJsonFile("simple")
            val version = MetaformVersion(
                type = MetaformVersionType.DRAFT,
                data = readJsonMap(objectMapper.writeValueAsString(metaform))
            )
            val createdVersion = testBuilder.systemAdmin.metaformVersions.create(
                metaformId = metaform.id!!,
                payload = version
            )
            val versionModified = createdVersion.copy(
                type = MetaformVersionType.ARCHIVED
            )

            testBuilder.systemAdmin.metaformVersions.updateMetaformVersion(
                metaformId = metaform.id,
                versionId = createdVersion.id!!,
                body = versionModified
            )
            val updatedVersion = testBuilder.systemAdmin.metaformVersions.findVersion(
                metaformId = metaform.id,
                versionId = createdVersion.id
            )

            Assertions.assertEquals(createdVersion.createdAt, updatedVersion.createdAt)
            Assertions.assertEquals(createdVersion.creatorId, updatedVersion.creatorId)
            Assertions.assertEquals(createdVersion.lastModifierId, updatedVersion.lastModifierId)
            Assertions.assertEquals(createdVersion.data, updatedVersion.data)
            Assertions.assertNotEquals(createdVersion.type, updatedVersion.type)
            Assertions.assertNotEquals(createdVersion.modifiedAt, updatedVersion.modifiedAt)
        }
    }

    @Test
    @Throws(Exception::class)
    fun updateVersionUnAuthorized() {
        TestBuilder().use { testBuilder ->
            val metaform = testBuilder.systemAdmin.metaforms.createFromJsonFile("simple")
            val versionData = testBuilder.systemAdmin.metaformVersions.exampleVersionData
            val version = MetaformVersion(
                type = MetaformVersionType.ARCHIVED,
                data = versionData
            )
            val createdVersion = testBuilder.systemAdmin.metaformVersions.create(metaform.id!!, version)

            testBuilder.permissionTestByScopes(
                scope = PermissionScope.METAFORM_ADMIN,
                apiCaller = { authentication: TestBuilderAuthentication, _: Int ->
                    authentication.metaformVersions.updateMetaformVersion(metaform.id, createdVersion.id!!, createdVersion)
                },
                metaformId = metaform.id
            )
        }
    }

    @Test
    @Throws(Exception::class)
    fun updateVersionNotFound() {
        TestBuilder().use { testBuilder ->
            val metaform = testBuilder.systemAdmin.metaforms.createFromJsonFile("simple")
            val versionData = testBuilder.systemAdmin.metaformVersions.exampleVersionData
            val version = MetaformVersion(
                type = MetaformVersionType.ARCHIVED,
                data = versionData
            )
            val createdVersion = testBuilder.systemAdmin.metaformVersions.create(metaform.id!!, version)

            InvalidValueTestScenarioBuilder(
                path = "v1/metaforms/{metaformId}/versions/{versionId}",
                method = Method.PUT,
                token = testBuilder.systemAdmin.token,
                body = jacksonObjectMapper().writeValueAsString(createdVersion)
            )
                .path(InvalidValueTestScenarioPath(name = "metaformId", values = InvalidValues.STRING, default = metaform.id, expectedStatus = 404))
                .path(InvalidValueTestScenarioPath(name = "versionId", values = InvalidValues.STRING, default = createdVersion.id, expectedStatus = 404))
                .build()
                .test()
        }
    }
}