package fi.metatavu.metaform.server.test.functional.common.providers

import fi.metatavu.metaform.server.test.functional.common.InvalidValueProvider

/**
 * Simple invalid value provider
 *
 * @param value value
 */
class SimpleInvalidValueProvider(override val value: Any?): InvalidValueProvider {

}