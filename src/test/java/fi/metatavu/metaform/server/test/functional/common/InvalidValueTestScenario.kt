package fi.metatavu.metaform.server.test.functional.common

import io.restassured.http.Method

/**
 * Single test scenario for invalid value test scenario builder
 *
 * @param path request path
 * @param method request method
 * @param queryParams queryParams
 * @param pathParams pathParams
 * @param expectedStatus expectedStatus
 */
class InvalidValueTestScenario(
    val path: String,
    val method: Method,
    val queryParams: Map<String, Any?>,
    val pathParams: Map<String, Any?>,
    val expectedStatus: Int
) {

}