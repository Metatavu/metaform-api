package fi.metatavu.metaform.server.test.functional.builder.impl

import fi.metatavu.jaxrs.test.functional.builder.auth.AccessTokenProvider
import fi.metatavu.metaform.api.client.apis.MetaformMemberGroupsApi
import fi.metatavu.metaform.api.client.infrastructure.ApiClient
import fi.metatavu.metaform.api.client.infrastructure.ApiClient.Companion.accessToken
import fi.metatavu.metaform.api.client.infrastructure.ClientException
import fi.metatavu.metaform.api.client.models.MetaformMember
import fi.metatavu.metaform.api.client.models.MetaformMemberGroup
import fi.metatavu.metaform.server.test.functional.ApiTestSettings
import fi.metatavu.metaform.server.test.functional.builder.TestBuilder
import org.junit.Assert
import java.io.IOException
import java.util.*

/**
 * Test builder resource for metaform member groups API
 *
 * @author Tianxing Wu
 */
class MetaformMemberGroupsTestBuilderResource(
    testBuilder: TestBuilder,
    private val accessTokenProvider: AccessTokenProvider?,
    apiClient: ApiClient
): ApiTestBuilderResource<MetaformMemberGroup, ApiClient?>(testBuilder, apiClient) {
    private val memberGroupsMetaforms: MutableMap<UUID?, UUID> = HashMap()

    override fun getApi(): MetaformMemberGroupsApi {
        accessToken = accessTokenProvider?.accessToken
        return MetaformMemberGroupsApi(ApiTestSettings.apiBasePath)
    }

    @Throws(IOException::class)
    override fun clean(metaformMemberGroup: MetaformMemberGroup) {
        val metaformId = memberGroupsMetaforms[metaformMemberGroup.id]
        api.deleteMetaformMemberGroup(metaformId!!, metaformMemberGroup.id!!)
    }

    /**
     * Creates new metaform member group
     *
     * @param metaformId metaform id
     * @param payload    payload
     * @return created metaform member
     */
    @Throws(IOException::class)
    fun create(metaformId: UUID, payload: MetaformMemberGroup): MetaformMemberGroup {
        val result = api.createMetaformMemberGroup(metaformId, payload)
        memberGroupsMetaforms[result.id] = metaformId
        return addClosable(result)
    }

    /**
     * Finds a metaform member group
     *
     * @param metaformId metaform id
     * @param memberGroupId member group id
     * @return found metaform member group
     */
    @Throws(IOException::class)
    fun findMemberGroup(metaformId: UUID, memberGroupId: UUID): MetaformMemberGroup {
        return api.findMetaformMemberGroup(metaformId, memberGroupId)
    }

    /**
     * Deletes a metaform member group from the API
     *
     * @param metaformId metaform id
     * @param metaformMemberGroupId metaform member group id
     */
    @Throws(IOException::class)
    fun delete(metaformId: UUID, metaformMemberGroupId: UUID) {
        Assert.assertNotNull(metaformMemberGroupId)
        api.deleteMetaformMemberGroup(metaformId, metaformMemberGroupId)
        removeCloseable { closable ->
            if (closable is MetaformMember) {
                return@removeCloseable metaformMemberGroupId == closable.id
            }
            false
        }
    }

    /**
     * Asserts find status fails with given status code
     *
     * @param expectedStatus expected status code
     * @param metaformId     metaform id
     * @param memberGroupId member group id
     */
    @Throws(IOException::class)
    fun assertFindFailStatus(expectedStatus: Int, metaformId: UUID, memberGroupId: UUID) {
        try {
            api.findMetaformMemberGroup(metaformId, memberGroupId)
            Assert.fail(String.format("Expected find to fail with status %d", expectedStatus))
        } catch (e: ClientException) {
            Assert.assertEquals(expectedStatus.toLong(), e.statusCode.toLong())
        }
    }

    /**
     * Asserts create status fails with given status code
     *
     * @param expectedStatus expected status code
     * @param metaformId     metaform id
     * @param payload        payload
     */
    @Throws(IOException::class)
    fun assertCreateFailStatus(expectedStatus: Int, metaformId: UUID, payload: MetaformMemberGroup) {
        try {
            api.createMetaformMemberGroup(metaformId, payload)
            Assert.fail(String.format("Expected create to fail with status %d", expectedStatus))
        } catch (e: ClientException) {
            Assert.assertEquals(expectedStatus.toLong(), e.statusCode.toLong())
        }
    }

    /**
     * Asserts delete status fails with given status code
     *
     * @param expectedStatus expected status code
     * @param metaformId     metaform
     * @param memberGroupId member group id
     */
    @Throws(IOException::class)
    fun assertDeleteFailStatus(expectedStatus: Int, metaformId: UUID, memberGroupId: UUID) {
        try {
            api.deleteMetaformMemberGroup(metaformId, memberGroupId)
            Assert.fail(String.format("Expected delete to fail with status %d", expectedStatus))
        } catch (e: ClientException) {
            Assert.assertEquals(expectedStatus.toLong(), e.statusCode.toLong())
        }
    }
}