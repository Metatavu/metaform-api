package fi.metatavu.metaform.server.test.functional.common

import fi.metatavu.metaform.server.test.functional.common.InvalidValueProvider

/**
 * Base class for test scenario builder scenario parameter
 *
 * @param name parameter name
 * @param values tested values
 * @param except values excluded from the values
 * @param expectedStatus expected status
 * @param default default value
 */
open class InvalidValueTestScenarioBase(
    val name: String,
    val values: Collection<InvalidValueProvider>,
    val except: Collection<Any?> = emptyList(),
    val default: Any?,
    val expectedStatus: Int
) {

}