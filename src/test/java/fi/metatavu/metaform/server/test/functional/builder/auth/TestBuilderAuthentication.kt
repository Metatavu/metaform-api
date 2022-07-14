package fi.metatavu.metaform.server.test.functional.builder.auth

import fi.metatavu.jaxrs.test.functional.builder.auth.AccessTokenProvider
import fi.metatavu.jaxrs.test.functional.builder.auth.AuthorizedTestBuilderAuthentication
import fi.metatavu.metaform.api.client.infrastructure.ApiClient
import fi.metatavu.metaform.api.client.infrastructure.ApiClient.Companion.accessToken
import fi.metatavu.metaform.server.test.functional.builder.TestBuilder
import fi.metatavu.metaform.server.test.functional.builder.impl.*
import fi.metatavu.metaform.server.test.functional.ApiTestSettings

/**
 * Default implementation of test builder authentication provider
 *
 * @author Antti Leppä
 */
class TestBuilderAuthentication(
    private val testBuilder: TestBuilder,
    private val accessTokenProvider: AccessTokenProvider
) : AuthorizedTestBuilderAuthentication<ApiClient, AccessTokenProvider>(testBuilder, accessTokenProvider) {
    val adminThemes: AdminThemeTestBuilderResource = AdminThemeTestBuilderResource(testBuilder, accessTokenProvider, createClient())
    val metaforms: MetaformTestBuilderResource = MetaformTestBuilderResource(testBuilder, accessTokenProvider, createClient())
    val metaformVersions: MetaformVersionTestBuilderResource = MetaformVersionTestBuilderResource(testBuilder, accessTokenProvider, createClient())
    val replies: ReplyTestBuilderResource = ReplyTestBuilderResource(testBuilder, accessTokenProvider, createClient())
    val exportThemes: ExportThemeTestBuilderResource = ExportThemeTestBuilderResource(testBuilder, accessTokenProvider, createClient())
    val exportFiles: ExportThemeFilesTestBuilderResource = ExportThemeFilesTestBuilderResource(testBuilder, accessTokenProvider, createClient())
    val drafts: DraftTestBuilderResource = DraftTestBuilderResource(testBuilder, accessTokenProvider, createClient())
    val attachments: AttachmentTestBuilderResource = AttachmentTestBuilderResource(testBuilder, accessTokenProvider, createClient())
    val auditLogs: AuditLogEntriesTestBuilderResource = AuditLogEntriesTestBuilderResource(testBuilder, accessTokenProvider, createClient())
    val emailNotifications: EmailNotificationsTestBuilderResource = EmailNotificationsTestBuilderResource(testBuilder, accessTokenProvider, createClient())

    val token: String
        get() {
            return accessTokenProvider.accessToken
        }

    /**
     * Creates an API client
     *
     * @param authProvider auth provider
     * @return API client
     */
    override fun createClient(authProvider: AccessTokenProvider): ApiClient {
        val result = ApiClient(ApiTestSettings.apiBasePath)
        accessToken = authProvider.accessToken
        return result
    }
}