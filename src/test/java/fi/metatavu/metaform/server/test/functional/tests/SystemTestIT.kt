package fi.metatavu.metaform.server.test.functional.tests

import fi.metatavu.metaform.api.client.models.MetaformMember
import fi.metatavu.metaform.api.client.models.MetaformMemberRole
import fi.metatavu.metaform.server.test.functional.AbstractTest
import fi.metatavu.metaform.server.test.functional.builder.TestBuilder
import fi.metatavu.metaform.server.test.functional.builder.resources.MetaformKeycloakResource
import fi.metatavu.metaform.server.test.functional.builder.resources.MysqlResource
import io.quarkus.test.common.QuarkusTestResource
import io.quarkus.test.junit.QuarkusTest
import io.quarkus.test.junit.TestProfile
import io.restassured.RestAssured.given
import org.hamcrest.CoreMatchers.`is`
import org.junit.jupiter.api.Test

/**
 * Tests for System API
 */
@QuarkusTest
@QuarkusTestResource.List(
    QuarkusTestResource(MetaformKeycloakResource::class),
    QuarkusTestResource(MysqlResource::class)
)
@TestProfile(GeneralTestProfile::class)
class SystemTestIT: AbstractTest() {

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
    fun testBillingReport() {
        TestBuilder().use { testBuilder ->
            val metaform1 = testBuilder.systemAdmin.metaforms.createFromJsonFile("simple")
            val metaform2 = testBuilder.systemAdmin.metaforms.createFromJsonFile("simple")
            val metaform3 = testBuilder.systemAdmin.metaforms.createFromJsonFile("simple")
            val metaform4 = testBuilder.systemAdmin.metaforms.createFromJsonFile("simple")
            val metaform5 = testBuilder.systemAdmin.metaforms.createFromJsonFile("simple")

            val metaform1Members = mutableListOf<MetaformMember>()

            for (i in 1..10) {
                metaform1Members.add(testBuilder.systemAdmin.metaformMembers.create(
                    metaformId = metaform1.id!!,
                    payload = MetaformMember(
                        email = "test$i@example.com",
                        firstName = "test",
                        lastName = "test",
                        role = MetaformMemberRole.MANAGER,
                    )
                ))
            }
            val requstBody = HashMap<String, Any>()
            requstBody.put("recipientEmail", "text@example.com")
            requstBody.put("period", "1")
            given()
                .contentType("application/json")
                .header("X-CRON-KEY", "8EDCE3DF-0BC2-48AF-942E-25A9E83FA19D")
                .`when`().post("http://localhost:8081/v1/system/billingReport")
                .then()
                .statusCode(204)
        }
    }
}