package fi.metatavu.metaform.test.functional.tests

import fi.metatavu.metaform.api.client.models.Draft
import fi.metatavu.metaform.api.client.models.MetaformVersion
import fi.metatavu.metaform.api.client.models.MetaformVersionType
import fi.metatavu.metaform.test.functional.AbstractIntegrationTest
import fi.metatavu.metaform.test.functional.builder.TestBuilder
import fi.metatavu.metaform.test.functional.builder.resources.KeycloakResource
import fi.metatavu.metaform.test.functional.builder.resources.MysqlResource
import io.quarkus.test.common.QuarkusTestResource
import io.quarkus.test.junit.QuarkusTest
import io.quarkus.test.junit.TestProfile
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.util.*

@QuarkusTest
@QuarkusTestResource.List(value = [QuarkusTestResource(MysqlResource::class), QuarkusTestResource(KeycloakResource::class)])
@TestProfile(GeneralTestProfile::class)
class VersionTestsIT : AbstractIntegrationTest() {
    @Test
    @Throws(Exception::class)
    fun createVersion() {
        TestBuilder().use { testBuilder ->
            val parsedMetaform = testBuilder.metaformAdmin.metaforms().readMetaform("simple")
            val metaform = testBuilder.metaformAdmin.metaforms().create(parsedMetaform)
            val versionData = testBuilder.metaformAdmin.metaformVersions().getExampleVersionData()
            val version = MetaformVersion(
                    type = MetaformVersionType.ARCHIVED,
                    data = versionData
            )

            testBuilder.test1.metaformVersions().assertCreateFailStatus(403, metaform.id!!, version);
            val createdVersion = testBuilder.metaformAdmin.metaformVersions().create(metaform.id, version)
            Assertions.assertNotNull(createdVersion)
            Assertions.assertNotNull(createdVersion.id)
            Assertions.assertEquals(MetaformVersionType.ARCHIVED, createdVersion.type)
            Assertions.assertEquals(versionData, createdVersion.data)

            val foundVersion = testBuilder.metaformAdmin.metaformVersions().findVersion(metaform.id, createdVersion.id!!)
            Assertions.assertNotNull(foundVersion)
            Assertions.assertEquals(createdVersion.id, foundVersion.id)
            Assertions.assertEquals(createdVersion.type, foundVersion.type)
            Assertions.assertEquals(createdVersion.data, foundVersion.data)
        }
    }

    @Test
    @Throws(Exception::class)
    fun listVersions() {
        TestBuilder().use { testBuilder ->
            val parsedMetaform = testBuilder.metaformAdmin.metaforms().readMetaform("simple")
            val metaform = testBuilder.metaformAdmin.metaforms().create(parsedMetaform)
            val versionData = testBuilder.metaformAdmin.metaformVersions().getExampleVersionData()

            testBuilder.metaformAdmin.metaformVersions().create(
                metaform.id!!,
                MetaformVersion(
                        type = MetaformVersionType.ARCHIVED,
                        data = versionData
                )
            )

            testBuilder.metaformAdmin.metaformVersions().assertCount(metaform.id, 1)

            testBuilder.metaformAdmin.metaformVersions().create(
                    metaform.id,
                    MetaformVersion(
                            type = MetaformVersionType.DRAFT,
                            data = versionData
                    )
            )
            testBuilder.metaformAdmin.metaformVersions().assertCount(metaform.id, 2)
            testBuilder.metaformAdmin.metaformVersions().assertListFailStatus(404, UUID.randomUUID())
            testBuilder.test1.metaformVersions().assertListFailStatus(403, metaform.id)
        }
    }

    @Test
    @Throws(Exception::class)
    fun findVersion() {
        TestBuilder().use { testBuilder ->
            val parsedMetaform = testBuilder.metaformAdmin.metaforms().readMetaform("simple")
            val metaform = testBuilder.metaformAdmin.metaforms().create(parsedMetaform)
            val versionData = testBuilder.metaformAdmin.metaformVersions().getExampleVersionData()

            val version = MetaformVersion(
                    type = MetaformVersionType.ARCHIVED,
                    data = versionData
            )

            val createdVersion = testBuilder.metaformAdmin.metaformVersions().create(metaform.id!!, version)
            testBuilder.test1.metaformVersions().assertFindFailStatus(403, metaform.id, createdVersion.id!!)
            testBuilder.metaformAdmin.metaformVersions().assertFindFailStatus(404, UUID.randomUUID(), createdVersion.id)
            val foundVersion = testBuilder.metaformAdmin.metaformVersions().findVersion(metaform.id, createdVersion.id)
            testBuilder.metaformAdmin.metaformVersions().assertCount(metaform.id, 1)

            Assertions.assertNotNull(foundVersion)
            Assertions.assertEquals(MetaformVersionType.ARCHIVED, foundVersion.type)
            Assertions.assertEquals(versionData, foundVersion.data)
        }
    }

    @Test
    @Throws(Exception::class)
    fun deleteVersion() {
        TestBuilder().use { testBuilder ->
            val parsedMetaform = testBuilder.metaformAdmin.metaforms().readMetaform("simple")
            val metaform = testBuilder.metaformAdmin.metaforms().create(parsedMetaform)
            val versionData = testBuilder.metaformAdmin.metaformVersions().getExampleVersionData()

            val version = MetaformVersion(
                    type = MetaformVersionType.ARCHIVED,
                    data = versionData
            )

            val createdVersion = testBuilder.metaformAdmin.metaformVersions().create(metaform.id!!, version)
            val foundVersion = testBuilder.metaformAdmin.metaformVersions().findVersion(metaform.id, createdVersion.id!!)
            testBuilder.metaformAdmin.metaformVersions().assertCount(metaform.id, 1)

            Assertions.assertNotNull(foundVersion)
            Assertions.assertEquals(MetaformVersionType.ARCHIVED, foundVersion.type)
            Assertions.assertEquals(versionData, foundVersion.data)
            testBuilder.test1.metaformVersions().assertDeleteFailStatus(403, metaform.id, foundVersion.id!!)
            testBuilder.metaformAdmin.metaformVersions().assertDeleteFailStatus(404, UUID.randomUUID(), foundVersion.id)
            testBuilder.metaformAdmin.metaformVersions().delete(metaform.id, foundVersion)

            testBuilder.metaformAdmin.metaformVersions().assertCount(metaform.id, 0)
        }
    }
}