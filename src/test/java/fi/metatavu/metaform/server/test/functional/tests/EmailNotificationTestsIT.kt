package fi.metatavu.metaform.server.test.functional.tests

import com.github.tomakehurst.wiremock.client.WireMock
import fi.metatavu.metaform.api.client.models.EmailNotification
import fi.metatavu.metaform.api.client.models.FieldRule
import fi.metatavu.metaform.api.client.models.Metaform
import fi.metatavu.metaform.server.rest.ReplyMode
import fi.metatavu.metaform.server.test.functional.AbstractTest
import fi.metatavu.metaform.server.test.functional.MailgunMocker
import fi.metatavu.metaform.server.test.functional.builder.TestBuilder
import fi.metatavu.metaform.server.test.functional.builder.resources.KeycloakResource
import fi.metatavu.metaform.server.test.functional.builder.resources.MailgunResource
import fi.metatavu.metaform.server.test.functional.builder.resources.MysqlResource
import io.quarkus.test.common.QuarkusTestResource
import io.quarkus.test.junit.QuarkusTest
import io.quarkus.test.junit.TestProfile
import org.eclipse.microprofile.config.ConfigProvider
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.util.*

/**
 * Quarkus tests for intents API
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@QuarkusTest
@QuarkusTestResource.List(
        QuarkusTestResource(MysqlResource::class),
        QuarkusTestResource(KeycloakResource::class),
        QuarkusTestResource(MailgunResource::class)
)
@TestProfile(GeneralTestProfile::class)
class EmailNotificationTestsIT : AbstractTest() {
    @Test
    @Throws(Exception::class)
    fun singleEmailNotificationTest() {
        TestBuilder().use { testBuilder ->
            val metaform: Metaform = testBuilder.metaformAdmin.metaforms.createFromJsonFile("simple")
            val mailgunMocker: MailgunMocker = startMailgunMocker()
            try {
                testBuilder.metaformAdmin.emailNotifications.createEmailNotification(metaform.id!!, "Simple subject", "Simple content", listOf("user@example.com"))
                testBuilder.test1.replies.createSimpleReply(metaform.id, "val 1", ReplyMode.CUMULATIVE)
                mailgunMocker.verifyHtmlMessageSent("Metaform Test", "metaform-test@example.com", "user@example.com", "Simple subject", "Simple content")
            } finally {
                stopMailgunMocker(mailgunMocker)
            }
        }
    }

    @Test
    @Throws(Exception::class)
    fun notifyIfEqualsTest() {
        TestBuilder().use { testBuilder ->
            val metaform: Metaform = testBuilder.metaformAdmin.metaforms.createFromJsonFile("simple")
            val mailgunMocker: MailgunMocker = startMailgunMocker()
            try {
                testBuilder.metaformAdmin.emailNotifications.createEmailNotification(metaform.id!!, "subject", "content", listOf("val1@example.com"), createFieldRule("text", "val 1", null))
                testBuilder.metaformAdmin.emailNotifications.createEmailNotification(metaform.id, "subject", "content", listOf("val2@example.com"), createFieldRule("text", "val 2", null))
                testBuilder.test1.replies.createSimpleReply(metaform.id, "val 1", ReplyMode.CUMULATIVE)
                testBuilder.test1.replies.createSimpleReply(metaform.id, "val 3", ReplyMode.CUMULATIVE)
                mailgunMocker.verifyHtmlMessageSent(1, "Metaform Test", "metaform-test@example.com", "val1@example.com", "subject", "content")
                mailgunMocker.verifyHtmlMessageSent(0, "Metaform Test", "metaform-test@example.com", "val2@example.com", "subject", "content")
            } finally {
                stopMailgunMocker(mailgunMocker)
            }
        }
    }

    @Test
    @Throws(Exception::class)
    fun notifyIfNotEqualsTest() {
        TestBuilder().use { testBuilder ->
            val metaform: Metaform = testBuilder.metaformAdmin.metaforms.createFromJsonFile("simple")
            val mailgunMocker: MailgunMocker = startMailgunMocker()
            try {
                testBuilder.metaformAdmin.emailNotifications.createEmailNotification(metaform.id!!, "subject", "content", listOf("val1@example.com"), createFieldRule("text", null, "val 1"))
                testBuilder.metaformAdmin.emailNotifications.createEmailNotification(metaform.id, "subject", "content", listOf("val2@example.com"), createFieldRule("text", null, "val 2"))
                testBuilder.test1.replies.createSimpleReply(metaform.id, "val 1", ReplyMode.CUMULATIVE)
                testBuilder.test1.replies.createSimpleReply(metaform.id, "val 3", ReplyMode.CUMULATIVE)
                mailgunMocker.verifyHtmlMessageSent(1, "Metaform Test", "metaform-test@example.com", "val1@example.com", "subject", "content")
                mailgunMocker.verifyHtmlMessageSent(2, "Metaform Test", "metaform-test@example.com", "val2@example.com", "subject", "content")
            } finally {
                stopMailgunMocker(mailgunMocker)
            }
        }
    }

    @Test
    @Throws(Exception::class)
    fun multipleEmailNotificationTest() {
        TestBuilder().use { testBuilder ->
            val metaform: Metaform = testBuilder.metaformAdmin.metaforms.createFromJsonFile("simple")
            val mailgunMocker: MailgunMocker = startMailgunMocker()
            try {
                testBuilder.metaformAdmin.emailNotifications.createEmailNotification(metaform.id!!, "Simple subject", "Simple content", listOf("user-1@example.com", "user-2@example.com"))
                testBuilder.test1.replies.createSimpleReply(metaform.id, "val 1", ReplyMode.CUMULATIVE)
                mailgunMocker.verifyHtmlMessageSent("Metaform Test", "metaform-test@example.com", "user-1@example.com", "Simple subject", "Simple content")
                mailgunMocker.verifyHtmlMessageSent("Metaform Test", "metaform-test@example.com", "user-2@example.com", "Simple subject", "Simple content")
            } finally {
                stopMailgunMocker(mailgunMocker)
            }
        }
    }

    @Test
    @Throws(Exception::class)
    fun replacedEmailNotificationTest() {
        TestBuilder().use { testBuilder ->
            val metaform: Metaform = testBuilder.metaformAdmin.metaforms.createFromJsonFile("simple")
            val mailgunMocker: MailgunMocker = startMailgunMocker()
            try {
                testBuilder.metaformAdmin.emailNotifications.createEmailNotification(metaform.id!!, "Replaced \${data.text} subject", "Replaced \${data.text} content", listOf("user@example.com"))
                testBuilder.test1.replies.createSimpleReply(metaform.id, "val 1", ReplyMode.CUMULATIVE)
                mailgunMocker.verifyHtmlMessageSent("Metaform Test", "metaform-test@example.com", "user@example.com", "Replaced val 1 subject", "Replaced val 1 content")
            } finally {
                stopMailgunMocker(mailgunMocker)
            }
        }
    }

    @Test
    @Throws(Exception::class)
    fun multipleRepliesEmailNotificationTest() {
        TestBuilder().use { testBuilder ->
            val metaform: Metaform = testBuilder.metaformAdmin.metaforms.createFromJsonFile("simple")
            val mailgunMocker: MailgunMocker = startMailgunMocker()
            try {
                testBuilder.metaformAdmin.emailNotifications.createEmailNotification(metaform.id!!, "Simple subject", "Simple content \${data.text}", listOf("user@example.com"))
                testBuilder.test1.replies.createSimpleReply(metaform.id, "val 1", ReplyMode.CUMULATIVE)
                testBuilder.test1.replies.createSimpleReply(metaform.id, "val 2", ReplyMode.CUMULATIVE)
                mailgunMocker.verifyHtmlMessageSent("Metaform Test", "metaform-test@example.com", "user@example.com", "Simple subject", "Simple content val 1")
                mailgunMocker.verifyHtmlMessageSent("Metaform Test", "metaform-test@example.com", "user@example.com", "Simple subject", "Simple content val 2")
            } finally {
                stopMailgunMocker(mailgunMocker)
            }
        }
    }

    @Test
    @Throws(Exception::class)
    fun unicodeEmailNotificationTest() {
        TestBuilder().use { testBuilder ->
            val metaform: Metaform = testBuilder.metaformAdmin.metaforms.createFromJsonFile("simple")
            val mailgunMocker: MailgunMocker = startMailgunMocker()
            try {
                testBuilder.metaformAdmin.emailNotifications.createEmailNotification(metaform.id!!, "Simple subject \${data.text}", "Simple content \${data.text}", Arrays.asList("user@example.com"))
                testBuilder.test1.replies.createSimpleReply(metaform.id, "ääkköset", ReplyMode.CUMULATIVE)
                testBuilder.test1.replies.createSimpleReply(metaform.id, "Правда.Ру", ReplyMode.CUMULATIVE)
                mailgunMocker.verifyHtmlMessageSent("Metaform Test", "metaform-test@example.com", "user@example.com", "Simple subject ääkköset", "Simple content ääkköset")
                mailgunMocker.verifyHtmlMessageSent("Metaform Test", "metaform-test@example.com", "user@example.com", "Simple subject Правда.Ру", "Simple content Правда.Ру")
            } finally {
                stopMailgunMocker(mailgunMocker)
            }
        }
    }

    @Test
    @Throws(Exception::class)
    fun testCreateEmailNotification() {
        TestBuilder().use { testBuilder ->
            val metaform: Metaform = testBuilder.metaformAdmin.metaforms.createFromJsonFile("simple")
            val mailgunMocker: MailgunMocker = startMailgunMocker()
            try {
                val createdEmailNotification: EmailNotification = testBuilder.metaformAdmin.emailNotifications.createEmailNotification(metaform.id!!, "Simple subject \${data.text}", "Simple content \${data.text}", Arrays.asList("user@example.com"), createFieldRule("field", "eq", "neq"))
                val foundEmailNotification: EmailNotification = testBuilder.metaformAdmin.emailNotifications.findEmailNotification(metaform.id, createdEmailNotification.id!!)
                Assertions.assertEquals(createdEmailNotification.toString(), foundEmailNotification.toString())
            } finally {
                stopMailgunMocker(mailgunMocker)
            }
        }
    }

    @Test
    @Throws(Exception::class)
    fun testListEmailNotifications() {
        TestBuilder().use { testBuilder ->
            val metaform: Metaform = testBuilder.metaformAdmin.metaforms.createFromJsonFile("simple")
            val mailgunMocker: MailgunMocker = startMailgunMocker()
            try {
                val notification1: EmailNotification = testBuilder.metaformAdmin.emailNotifications.createEmailNotification(metaform.id!!, "Subject 1", "Content 2", Arrays.asList("user@example.com"))
                val notification2: EmailNotification = testBuilder.metaformAdmin.emailNotifications.createEmailNotification(metaform.id, "Subject 2", "Content 2", Arrays.asList("user@example.com"))
                val list: List<EmailNotification> = testBuilder.metaformAdmin.emailNotifications.listEmailNotifications(metaform.id)
                Assertions.assertEquals(2, list.size)
                val listNotification1 = list.firstOrNull { it.id == notification1.id }
                val listNotification2 = list.firstOrNull { it.id == notification2.id }
                Assertions.assertEquals(notification1.toString(), listNotification1.toString())
                Assertions.assertEquals(notification2.toString(), listNotification2.toString())
            } finally {
                stopMailgunMocker(mailgunMocker)
            }
        }
    }

   /**
    * Creates a field rule
    *
    * @param field field name
    * @param equals equals value
    * @param notEquals not equals value
    * @param ands list of ands or null if none defined
    * @param ors list of ors or null if none defined
    * @return created rule
    */
    private fun createFieldRule(field: String, equals: String?, notEquals: String?, ands: Array<FieldRule>? = null, ors: Array<FieldRule>? = null): FieldRule {
        return FieldRule(field, equals, notEquals, ands, ors)
    }

    @BeforeAll
    fun setMocker() {
        val host = ConfigProvider.getConfig().getValue("wiremock.host", String::class.java)
        val port = ConfigProvider.getConfig().getValue("wiremock.port", String::class.java).toInt()
        WireMock.configureFor(host, port)
    }
}