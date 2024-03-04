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
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.time.Duration
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

    // this to test the scheduler
    @Test
    fun testBillingReportScheduled() {
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

                Thread.sleep(10000)
                println("Checking messages")
                mailgunMocker.verifyMessageSent(
                    "Metaform Test",
                    "metaform-test@example.com",
                    "test@example.com",
                    "Metaform Billing Report"
                )


            } finally {
                stopMailgunMocker(mailgunMocker)
            }
        }
    }

    /*
    myMethod.cron.expr=disabled
     */
    // this to test the scheduler
    @Test
    fun testBillingReportManual() {
        TestBuilder().use { testBuilder ->
            val metaform1 = testBuilder.systemAdmin.metaforms.createFromJsonFile("simple")
            val metaform2 = testBuilder.systemAdmin.metaforms.createFromJsonFile("simple")
            val metaform3 = testBuilder.systemAdmin.metaforms.createFromJsonFile("simple")
            val metaform4 = testBuilder.systemAdmin.metaforms.createFromJsonFile("simple")
            val metaform5 = testBuilder.systemAdmin.metaforms.createFromJsonFile("simple")
            val metaform1Members = mutableListOf<MetaformMember>()

            for (i in 1..3) {
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

            val mailgunMocker: MailgunMocker = startMailgunMocker()
            try {

                val requstBody = HashMap<String, Any>()
                requstBody["recipientEmail"] = "text@example.com"
                requstBody["start"] = OffsetDateTime.now().minusMonths(1)
                requstBody["end"] = OffsetDateTime.now()
                given()
                    .contentType("application/json")
                    .header("X-CRON-KEY", "8EDCE3DF-0BC2-48AF-942E-25A9E83FA19D")
                    .`when`().post("http://localhost:8081/v1/system/billingReport")
                    .then()
                    .statusCode(204)

                println("Checking messages")
                mailgunMocker.verifyMessageSent(
                    "Metaform Test",
                    "metaform-test@example.com",
                    "test@example.com",
                    "Metaform Billing Report"
                )


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