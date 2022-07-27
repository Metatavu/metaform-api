package fi.metatavu.metaform.server.test.functional.builder.impl

import fi.metatavu.jaxrs.test.functional.builder.auth.AccessTokenProvider
import fi.metatavu.metaform.api.client.apis.MetaformMembersApi
import fi.metatavu.metaform.api.client.infrastructure.ApiClient
import fi.metatavu.metaform.api.client.infrastructure.ApiClient.Companion.accessToken
import fi.metatavu.metaform.api.client.infrastructure.ClientException
import fi.metatavu.metaform.api.client.models.MetaformMember
import fi.metatavu.metaform.api.client.models.MetaformMemberRole
import fi.metatavu.metaform.server.test.functional.ApiTestSettings
import fi.metatavu.metaform.server.test.functional.builder.TestBuilder
import org.junit.Assert
import java.io.IOException
import java.util.*

/**
 * Test builder resource for metaform members API
 *
 * @author Tianxing Wu
 */
class MetaformMembersTestBuilderResource(
    testBuilder: TestBuilder,
    private val accessTokenProvider: AccessTokenProvider?,
    apiClient: ApiClient
): ApiTestBuilderResource<MetaformMember, ApiClient?>(testBuilder, apiClient) {
    private val membersMetaforms: MutableMap<UUID?, UUID> = HashMap()

    override fun getApi(): MetaformMembersApi {
        accessToken = accessTokenProvider?.accessToken
        return MetaformMembersApi(ApiTestSettings.apiBasePath)
    }

    @Throws(IOException::class)
    override fun clean(metaformMember: MetaformMember) {
        val metaformId = membersMetaforms[metaformMember.id]
        api.deleteMetaformMember(metaformId!!, metaformMember.id!!)
    }

    /**
     * Creates new metaform member
     *
     * @param metaformId metaform id
     * @param payload    payload
     * @return created metaform member
     */
    @Throws(IOException::class)
    fun create(metaformId: UUID, payload: MetaformMember, addClosable: Boolean = true): MetaformMember {
        val result = api.createMetaformMember(metaformId, payload)
        membersMetaforms[result.id] = metaformId
        if (addClosable) return addClosable(result)
        return result
    }

    /**
     * Creates simple metaform member
     *
     * @param metaformId metaform id
     * @param name    name
     * @return created simple metaform member
     */
    @Throws(IOException::class)
    fun createSimpleMember(metaformId: UUID, name: String): MetaformMember {
        val result = api.createMetaformMember(metaformId, MetaformMember(
            firstName = name,
            lastName  = name,
            email = String.format("%s@example.com", name),
            role = MetaformMemberRole.mANAGER
        ))
        membersMetaforms[result.id] = metaformId
        return addClosable(result)
    }

    /**
     * Finds a metaform member
     *
     * @param metaformId metaform id
     * @param memberId member id
     * @return found metaform member
     */
    @Throws(IOException::class)
    fun findMember(metaformId: UUID, memberId: UUID): MetaformMember {
        return api.findMetaformMember(metaformId, memberId)
    }


    /**
     * Lists metaform members
     *
     * @param metaformId metaform id
     * @param role    metaform member role
     * @return metaform members
     */
    @Throws(IOException::class)
    fun list(metaformId: UUID, role: MetaformMemberRole?): Array<MetaformMember> {
        return api.listMetaformMembers(metaformId, role)
    }
    /**
     * Updates a metaform member
     *
     * @param metaformId metaform id
     * @param memberId member id
     * @param metaformMember metaform member
     * @return updated metaform member
     */
    @Throws(IOException::class)
    fun updateMember(metaformId: UUID, memberId: UUID, metaformMember: MetaformMember): MetaformMember {
        return api.updateMetaformMember(metaformId, memberId, metaformMember)
    }

    /**
     * Deletes a metaform member from the API
     *
     * @param metaformId      metaform id
     * @param metaformMemberId metaform member id
     */
    @Throws(IOException::class)
    fun delete(metaformId: UUID, metaformMemberId: UUID) {
        api.deleteMetaformMember(metaformId, metaformMemberId)
        removeCloseable { closable ->
            if (closable is MetaformMember) {
                return@removeCloseable metaformMemberId == closable.id
            }
            false
        }
    }

    /**
     * Asserts find status fails with given status code
     *
     * @param expectedStatus expected status code
     * @param metaformId     metaform id
     * @param memberId      member id
     */
    @Throws(IOException::class)
    fun assertFindFailStatus(expectedStatus: Int, metaformId: UUID, memberId: UUID) {
        try {
            api.findMetaformMember(metaformId, memberId)
            Assert.fail(String.format("Expected find to fail with status %d", expectedStatus))
        } catch (e: ClientException) {
            Assert.assertEquals(expectedStatus.toLong(), e.statusCode.toLong())
        }
    }

    /**
     * Asserts delete status fails with given status code
     *
     * @param expectedStatus expected status code
     * @param metaformId     metaform
     * @param memberId      member id
     * @param metaformMember      metaform member
     */
    @Throws(IOException::class)
    fun assertUpdateFailStatus(expectedStatus: Int, metaformId: UUID, memberId: UUID, metaformMember: MetaformMember) {
        try {
            api.updateMetaformMember(metaformId, memberId, metaformMember)
            Assert.fail(String.format("Expected delete to fail with status %d", expectedStatus))
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
    fun assertCreateFailStatus(expectedStatus: Int, metaformId: UUID, payload: MetaformMember) {
        try {
            api.createMetaformMember(metaformId, payload)
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
     * @param memberId      member id
     */
    @Throws(IOException::class)
    fun assertDeleteFailStatus(expectedStatus: Int, metaformId: UUID, memberId: UUID) {
        try {
            api.deleteMetaformMember(metaformId, memberId)
            Assert.fail(String.format("Expected delete to fail with status %d", expectedStatus))
        } catch (e: ClientException) {
            Assert.assertEquals(expectedStatus.toLong(), e.statusCode.toLong())
        }
    }

    /**
     * Asserts delete status fails with given status code
     *
     * @param expectedStatus expected status code
     * @param metaformId     metaform
     * @param role      metaform member role
     */
    @Throws(IOException::class)
    fun assertListFailStatus(expectedStatus: Int, metaformId: UUID, role: MetaformMemberRole?) {
        try {
            api.listMetaformMembers(metaformId, role)
            Assert.fail(String.format("Expected delete to fail with status %d", expectedStatus))
        } catch (e: ClientException) {
            Assert.assertEquals(expectedStatus.toLong(), e.statusCode.toLong())
        }
    }
}