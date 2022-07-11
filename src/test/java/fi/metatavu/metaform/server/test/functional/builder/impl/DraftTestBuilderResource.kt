package fi.metatavu.metaform.server.test.functional.builder.impl

import fi.metatavu.jaxrs.test.functional.builder.auth.AccessTokenProvider
import fi.metatavu.metaform.api.client.apis.DraftsApi
import fi.metatavu.metaform.api.client.infrastructure.ApiClient
import fi.metatavu.metaform.api.client.infrastructure.ApiClient.Companion.accessToken
import fi.metatavu.metaform.api.client.models.Draft
import fi.metatavu.metaform.api.client.models.Metaform
import fi.metatavu.metaform.server.test.functional.ApiTestSettings
import fi.metatavu.metaform.server.test.functional.builder.TestBuilder
import java.io.IOException
import java.util.*

/**
 * Test builder resource for Drafts API
 */
class DraftTestBuilderResource(
        testBuilder: TestBuilder,
        private val accessTokenProvider: AccessTokenProvider?,
        apiClient: ApiClient
): ApiTestBuilderResource<Draft, ApiClient?>(testBuilder, apiClient) {
    private val metaformDraftMap: MutableMap<Metaform, Draft> = HashMap()

    override fun getApi(): DraftsApi {
        accessToken = accessTokenProvider?.accessToken
        return DraftsApi(ApiTestSettings.apiBasePath)
    }

    @Throws(IOException::class)
    override fun clean(draft: Draft) {
        val testBuilder = TestBuilder()
        val draftsAdminApi = testBuilder.metaformAdmin.drafts.api

        metaformDraftMap.entries.stream()
                .filter { it.value == draft }
                .forEach { (metaform, draft) ->
                    try {
                        draftsAdminApi.deleteDraft(metaform.id!!, draft.id!!)
                    } catch (e: IOException) {
                        throw RuntimeException(e)
                    }
                }
    }

    /**
     * Creates draft
     *
     * @param metaform  metaform
     * @param draftData draft data map
     * @return created draft
     */
    @Throws(IOException::class)
    fun createDraft(metaform: Metaform, draftData: Map<String, Any>): Draft {
        val draft = Draft(draftData, null, null, null)
        val createdDraft = api.createDraft(metaform.id!!, draft)
        metaformDraftMap[metaform] = createdDraft
        return addClosable(createdDraft)
    }

    /**
     * Updates draft
     *
     * @param metaformId metaform id
     * @param draftId    draft id
     * @param draft      new payload
     * @return updated draft
     */
    @Throws(IOException::class)
    fun updateDraft(metaformId: UUID, draftId: UUID, draft: Draft): Draft {
        return api.updateDraft(metaformId, draftId, draft)
    }

    /**
     * Finds draft
     *
     * @param metaformId metaform id
     * @param draftId    draft id
     * @return found draft
     */
    @Throws(IOException::class)
    fun findDraft(metaformId: UUID, draftId: UUID): Draft {
        return api.findDraft(metaformId, draftId)
    }
}