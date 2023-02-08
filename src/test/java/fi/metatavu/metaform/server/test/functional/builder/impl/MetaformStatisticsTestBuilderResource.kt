package fi.metatavu.metaform.server.test.functional.builder.impl

import fi.metatavu.jaxrs.test.functional.builder.auth.AccessTokenProvider
import fi.metatavu.metaform.api.client.apis.MetaformStatisticsApi
import fi.metatavu.metaform.api.client.infrastructure.ApiClient
import fi.metatavu.metaform.api.client.infrastructure.ApiClient.Companion.accessToken
import fi.metatavu.metaform.api.client.models.MetaformStatistics
import fi.metatavu.metaform.api.client.models.Reply
import fi.metatavu.metaform.server.rest.ReplyMode
import fi.metatavu.metaform.server.test.functional.ApiTestSettings
import fi.metatavu.metaform.server.test.functional.builder.TestBuilder
import java.io.IOException
import java.util.*

/**
 * Test builder resource for metaform statistics
 */
class MetaformStatisticsTestBuilderResource(
    testBuilder: TestBuilder,
    private val accessTokenProvider: AccessTokenProvider?,
    apiClient: ApiClient
): ApiTestBuilderResource<MetaformStatistics, ApiClient?>(testBuilder, apiClient) {

    override fun getApi(): MetaformStatisticsApi {
        accessToken = accessTokenProvider?.accessToken
        return MetaformStatisticsApi(ApiTestSettings.apiBasePath)
    }

    @Throws(IOException::class)
    override fun clean(t: MetaformStatistics?) {
        // This test builder doesn't create anything and therefore doesn't clean anything
    }

    /**
     * Gets Metaform Statistics
     * 
     * @param metaformId metaform id
     * @returns Metaform Statistics
     */
    @Throws(IOException::class)
    fun getMetaformStatistics(metaformId: UUID): MetaformStatistics {
        return api.getStatistics(metaformId)
    }

    /**
     * Creates Reply for given Metaform
     *
     * @param metaformId metaform id
     * @returns Created Reply
     */
    fun createReplyForMetaform(metaformId: UUID): Reply {
        val replyData: MutableMap<String, Any> = HashMap()
        replyData["text"] = "Test text value"
        val reply: Reply = testBuilder.test1.replies.createReplyWithData(replyData)

        return testBuilder.systemAdmin.replies.create(metaformId, null, ReplyMode.REVISION.toString(), reply)
    }

    /**
     * Creates n-amount of Replies for given Metaform with given status
     *
     * @param metaformId metaform id
     * @param amount amount
     * @param status status
     */
    fun createNReplies(metaformId: UUID, amount: Int, status: String = "waiting") {
        var amountCreated = 0

        do {
            val replyData: MutableMap<String, Any> = HashMap()
            replyData["text"] = "Test text value"
            replyData["status"] = status
            val reply: Reply = testBuilder.test1.replies.createReplyWithData(replyData)
            testBuilder.systemAdmin.replies.create(metaformId, null, ReplyMode.REVISION.toString(), reply)

            amountCreated++
        } while (amountCreated < amount)
    }
}