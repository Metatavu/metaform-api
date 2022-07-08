package fi.metatavu.metaform.server.test.functional.builder.impl

import fi.metatavu.jaxrs.test.functional.builder.auth.AccessTokenProvider
import fi.metatavu.metaform.api.client.apis.AuditLogEntriesApi
import fi.metatavu.metaform.api.client.infrastructure.ApiClient
import fi.metatavu.metaform.api.client.infrastructure.ApiClient.Companion.accessToken
import fi.metatavu.metaform.api.client.infrastructure.ClientException
import fi.metatavu.metaform.api.client.models.AuditLogEntry
import fi.metatavu.metaform.server.test.functional.ApiTestSettings
import fi.metatavu.metaform.server.test.functional.builder.TestBuilder
import fi.metatavu.pickway.cloud.api.test.functional.impl.ApiTestBuilderResource
import org.junit.Assert
import java.io.IOException
import java.util.*

/**
 * Test builder resource for Audit Log Entries API
 */
class AuditLogEntriesTestBuilderResource(
        testBuilder: TestBuilder,
        private val accessTokenProvider: AccessTokenProvider?,
        apiClient: ApiClient
): ApiTestBuilderResource<AuditLogEntry, ApiClient?>(testBuilder, apiClient) {

    private val auditLogEntriesMetaforms: Map<UUID?, UUID> = HashMap()

    override fun getApi(): AuditLogEntriesApi {
        accessToken = accessTokenProvider?.accessToken
        return AuditLogEntriesApi(ApiTestSettings.apiBasePath)
    }

    @Throws(Exception::class)
    override fun clean(auditLogEntry: AuditLogEntry) {
        val metaformId = auditLogEntriesMetaforms[auditLogEntry.id]
        api.deleteAuditLogEntry(metaformId!!, auditLogEntry.id!!)
    }

    /**
     * Lists audit log entries
     *
     * @param metaformId    metaform id
     * @param userId        user id
     * @param replyId       reply id
     * @param createdBefore created before
     * @param createdAfter  created after
     * @return audit entries
     */
    @Throws(IOException::class)
    fun listAuditLogEntries(metaformId: UUID, userId: UUID?, replyId: UUID?, createdBefore: String?, createdAfter: String?): Array<AuditLogEntry> {
        return api.listAuditLogEntries(metaformId, userId, replyId, createdBefore, createdAfter)
    }

    /**
     * Asserts that find returns fail with given status
     *
     * @param status        expected status
     * @param metaformId    metaform id
     * @param userId        user id
     * @param replyId       reply id
     * @param createdBefore created before
     * @param createdAfter  created after
     */
    @Throws(IOException::class)
    fun assertListFailStatus(status: Int, metaformId: UUID, userId: UUID?, replyId: UUID?, createdBefore: String?, createdAfter: String?) {
        try {
            api.listAuditLogEntries(metaformId, userId, replyId, createdBefore, createdAfter)
            Assert.fail(String.format("Only users with metaform-view-all-audit-logs can access this view"))
        } catch (e: ClientException) {
            Assert.assertEquals(status.toLong(), e.statusCode.toLong())
        }
    }
}