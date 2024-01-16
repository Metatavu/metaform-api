package fi.metatavu.metaform.server.test.functional.common

import fi.metatavu.metaform.server.test.functional.ApiTestSettings
import io.restassured.module.kotlin.extensions.Extract
import io.restassured.module.kotlin.extensions.Given
import io.restassured.module.kotlin.extensions.When
import org.junit.jupiter.api.Assertions.assertEquals
import org.slf4j.LoggerFactory


/**
 * Scenarios for invalid value test scenario builder
 */
class InvalidValueTestScenarios(private val scenarios: MutableList<InvalidValueTestScenario>) {

    private val logger = LoggerFactory.getLogger(InvalidValueTestScenarios::class.java)

    /**
     * Runs tests
     */
    fun test() {
        logger.debug("Executing test ${scenarios.size} scenarios")

        scenarios.forEach { scenario ->
            logger.debug("Executing test scenario ({}) {} with query: {}, path: {}", scenario.method, scenario.path, scenario.queryParams, scenario.pathParams)

            Given {
                baseUri(ApiTestSettings.apiBasePath)
            } When  {
                queryParams(scenario.queryParams)
                pathParams(scenario.pathParams)
                auth().preemptive().oauth2(scenario.token)
                request(scenario.method, scenario.path)
            } Extract {
                val statusCode = statusCode()
                assertEquals(scenario.expectedStatus, statusCode, "Test scenario (${scenario.method}) ${scenario.path} with query: ${scenario.queryParams}, path: ${scenario.pathParams} failed. Expected status ${scenario.expectedStatus} does not match ${statusCode}")
            }
        }
    }

}