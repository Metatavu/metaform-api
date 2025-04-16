package fi.metatavu.metaform.server.test.functional.builder.impl

import fi.metatavu.jaxrs.test.functional.builder.auth.AccessTokenProvider
import fi.metatavu.metaform.api.client.apis.EmailNotificationsApi
import fi.metatavu.metaform.api.client.infrastructure.ApiClient
import fi.metatavu.metaform.api.client.infrastructure.ApiClient.Companion.accessToken
import fi.metatavu.metaform.api.client.infrastructure.ClientException
import fi.metatavu.metaform.api.client.models.EmailNotification
import fi.metatavu.metaform.api.client.models.FieldRule
import fi.metatavu.metaform.server.test.functional.ApiTestSettings
import fi.metatavu.metaform.server.test.functional.builder.TestBuilder
import org.junit.Assert
import java.io.IOException
import java.util.*

/**
 * Test builder resource for EmailNotifications API
 */
class EmailNotificationsTestBuilderResource(
        testBuilder: TestBuilder,
        private val accessTokenProvider: AccessTokenProvider?,
        apiClient: ApiClient
): ApiTestBuilderResource<EmailNotification, ApiClient?>(testBuilder, apiClient) {

    private val emailNotificationMetaforms: MutableMap<UUID?, UUID?> = HashMap()

    override fun getApi(): EmailNotificationsApi {
        accessToken = accessTokenProvider?.accessToken
        return EmailNotificationsApi(ApiTestSettings.apiBasePath)
    }

    @Throws(IOException::class)
    override fun clean(emailNotification: EmailNotification) {
        val metaformId = emailNotificationMetaforms[emailNotification.id]
        api.deleteEmailNotification(metaformId!!, emailNotification.id!!)
    }

    /**
     * Creates new email notification for a Metaform
     *
     * @param metaformId      metaform id
     * @param subjectTemplate freemarker template for subject
     * @param contentTemplate freemarker template for content
     * @param emails          email addresses
     * @return email notification
     */
    @Throws(IOException::class)
    fun createEmailNotification(metaformId: UUID, subjectTemplate: String, contentTemplate: String, emails: List<String>): EmailNotification {
        return createEmailNotification(metaformId, subjectTemplate, contentTemplate, emails, null)
    }

    /**
     * Creates new email notification for a Metaform
     *
     * @param metaformId      metaform id
     * @param subjectTemplate freemarker template for subject
     * @param contentTemplate freemarker template for content
     * @param emails          email addresses
     * @param notifyIf        notify if rule
     * @return email notification
     */
    @Throws(IOException::class)
    fun createEmailNotification(metaformId: UUID, subjectTemplate: String, contentTemplate: String, emails: List<String>, notifyIf: FieldRule?, addClosable: Boolean = true): EmailNotification {
        val notification = EmailNotification(subjectTemplate, contentTemplate, emails.toTypedArray(), null, notifyIf)
        val createdNotification = api.createEmailNotification(metaformId, notification)
        emailNotificationMetaforms[createdNotification.id] = metaformId
        if (addClosable) addClosable(createdNotification)
        return createdNotification
    }

    /**
     * Finds email notification
     *
     * @param metaformId metaform id
     * @param emailNotId notification id
     * @return found email notification
     */
    @Throws(IOException::class)
    fun findEmailNotification(metaformId: UUID, emailNotId: UUID): EmailNotification {
        return api.findEmailNotification(metaformId, emailNotId)
    }

    /**
     * Updates email notification
     *
     * @param metaformId metaform id
     * @param emailNotId notification id
     * @param emailNotification email notification
     * @return updated email notification
     */
    @Throws(IOException::class)
    fun updateEmailNotification(metaformId: UUID, emailNotId: UUID, emailNotification: EmailNotification): EmailNotification {
        return api.updateEmailNotification(metaformId, emailNotId, emailNotification)
    }

    /**
     * Returns all email notifications for metaform
     *
     * @param metaformId metaform id
     * @return found notifications
     */
    @Throws(IOException::class)
    fun listEmailNotifications(metaformId: UUID): List<EmailNotification> {
        return listOf(*api.listEmailNotifications(metaformId))
    }

    /**
     * Asserts that finding EmailNotification fails with given status
     *
     * @param expectedStatus expected status
     * @param emailNotificationId email notification id
     * @param metaformId metaform id
     */
    @Throws(IOException::class)
    fun assertFindFailStatus(expectedStatus: Int, emailNotificationId: UUID, metaformId: UUID) {
        try {
            api.findEmailNotification(metaformId, emailNotificationId)
            Assert.fail(String.format("Expected find to fail with status %d", expectedStatus))
        } catch (e: ClientException) {
            Assert.assertEquals(expectedStatus, e.statusCode)
        }
    }

    /**
     * Asserts that finding EmailNotification fails with given status
     *
     * @param expectedStatus expected status
     * @param metaformId metaform id
     */
    @Throws(IOException::class)
    fun assertListFailStatus(expectedStatus: Int, metaformId: UUID) {
        try {
            api.listEmailNotifications(metaformId = metaformId)
            Assert.fail(String.format("Expected list to fail with status %d", expectedStatus))
        } catch (e: ClientException) {
            Assert.assertEquals(expectedStatus, e.statusCode)
        }
    }
}