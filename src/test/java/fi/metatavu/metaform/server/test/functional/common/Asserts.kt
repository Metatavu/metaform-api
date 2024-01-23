package fi.metatavu.metaform.server.test.functional.common

import org.junit.jupiter.api.Assertions
import java.time.OffsetDateTime

/**
 * Common assertions
 */
class Asserts {

    companion object {

        /**
         * Asserts that two ISO-formatted offset date times are equal
         *
         * @param expected expected value
         * @param actual actual value
         */
        fun assertEqualsOffsetDateTime(expected: String, actual: String) {
            Assertions.assertEquals(
                OffsetDateTime.parse(expected).toEpochSecond(),
                OffsetDateTime.parse(actual).toEpochSecond()
            )
        }
    }

}