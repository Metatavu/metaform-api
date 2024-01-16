package fi.metatavu.metaform.server.test.functional.common.providers

import fi.metatavu.metaform.server.test.functional.common.InvalidValueProvider
import org.apache.commons.lang3.RandomStringUtils

/**
 * Invalid value provider for random strings
 *
 * @param count character count
 */
class MaxCharsInvalidValueProvider(private val count: Int): InvalidValueProvider {

    override val value: Any?
        get() = RandomStringUtils.random(count)

}