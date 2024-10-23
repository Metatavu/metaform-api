package fi.metatavu.metaform.server.test.functional.tests

import com.github.tomakehurst.wiremock.client.WireMock
import fi.metatavu.metaform.api.client.models.MetaformMember
import fi.metatavu.metaform.api.client.models.MetaformMemberRole
import fi.metatavu.metaform.server.test.functional.AbstractTest
import fi.metatavu.metaform.server.test.functional.MailgunMocker
import fi.metatavu.metaform.server.test.functional.builder.TestBuilder
import fi.metatavu.metaform.server.test.functional.builder.resources.MailgunResource
import fi.metatavu.metaform.server.test.functional.builder.resources.MetaformKeycloakResource
import fi.metatavu.metaform.server.test.functional.builder.resources.MysqlResource
import io.quarkus.test.common.QuarkusTestResource
import io.quarkus.test.junit.QuarkusTest
import io.quarkus.test.junit.TestProfile
import io.restassured.RestAssured.given
import org.awaitility.Awaitility
import org.eclipse.microprofile.config.ConfigProvider
import org.hamcrest.CoreMatchers.`is`
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.time.OffsetDateTime

/**
 * Tests for System API
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@QuarkusTest
@QuarkusTestResource.List(
    QuarkusTestResource(MetaformKeycloakResource::class),
    QuarkusTestResource(MysqlResource::class),
    QuarkusTestResource(MailgunResource::class)
)
@TestProfile(GeneralTestProfile::class)
class SystemTestIT : AbstractTest() {

    @Test
    fun testPingEndpoint() {
        given()
            .contentType("application/json")
            .`when`().get("http://localhost:8081/v1/system/ping")
            .then()
            .statusCode(200)
            .body(`is`("pong"))
    }

    @Test
    fun testBillingReportScheduledManual() {
        TestBuilder().use { testBuilder ->
            val metaform1 = testBuilder.systemAdmin.metaforms.createFromJsonFile("simple")
            val metaform2 = testBuilder.systemAdmin.metaforms.createFromJsonFile("simple")
            val metaform1Members = mutableListOf<MetaformMember>()

            for (i in 1..10) {
                metaform1Members.add(
                    testBuilder.systemAdmin.metaformMembers.create(
                        metaformId = metaform1.id!!,
                        payload = MetaformMember(
                            email = "test$i@example.com",
                            firstName = "test",
                            lastName = "test",
                            role = MetaformMemberRole.MANAGER,
                        )
                    )
                )
            }

            for (i in 1..5) {
                metaform1Members.add(
                    testBuilder.systemAdmin.metaformMembers.create(
                        metaformId = metaform2.id!!,
                        payload = MetaformMember(
                            email = "test$i@example.com",
                            firstName = "test",
                            lastName = "test",
                            role = MetaformMemberRole.MANAGER,
                        )
                    )
                )
            }

            val mailgunMocker: MailgunMocker = startMailgunMocker()
            try {
                Awaitility.waitAtMost(60, java.util.concurrent.TimeUnit.MINUTES).until {
                    val messages = mailgunMocker.countMessagesSentPartialMatch(
                        "Metaform Test",
                        "metaform-test@example.com",
                        "Metaform Billing Report",
                    )
                    val filteredMessages = messages.filter {
                        val requestBody = it.request.bodyAsString
                        requestBody.contains("test1%40example.com") || requestBody.contains("test%40example.com")
                    }
                    filteredMessages.size == 2
                }


                val body = mapOf(
                    "recipientEmail" to "special_email@example.com",
                    "startDate" to OffsetDateTime.now().minusMonths(1).toLocalDate(),
                    "endDate" to OffsetDateTime.now().toLocalDate()
                )

                val statusCode = given()
                    .contentType("application/json")
                    .header("X-API-KEY", "testKey")
                    .`when`()
                    .body(body)
                    .post("http://localhost:8081/v1/system/billingReport")
                    .then()
                    .extract()
                    .statusCode()
                assertEquals(204, statusCode)

                Awaitility.waitAtMost(60, java.util.concurrent.TimeUnit.SECONDS).until {
                    val messages = mailgunMocker.countMessagesSentPartialMatch(
                        "Metaform Test",
                        "metaform-test@example.com",
                        "Metaform Billing Report",
                    )
                    val filteredMessages =
                        messages.filter { it.request.bodyAsString.contains("special_email%40example.com") }
                    filteredMessages.size == 1
                }
            } finally {
                stopMailgunMocker(mailgunMocker)
            }
        }
    }

    @BeforeAll
    fun setMocker() {
        val host = ConfigProvider.getConfig().getValue("wiremock.host", String::class.java)
        val port = ConfigProvider.getConfig().getValue("wiremock.port", String::class.java).toInt()
        WireMock.configureFor(host, port)
    }
}