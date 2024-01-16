package fi.metatavu.metaform.server.test.functional.common

import fi.metatavu.metaform.server.test.functional.common.providers.MaxCharsInvalidValueProvider
import fi.metatavu.metaform.server.test.functional.common.providers.SimpleInvalidValueProvider

/**
 * Class containing commonly used invalid values
 */
class InvalidValues {

    companion object {
        // todo add more invalid values
        val NULL: Collection<InvalidValueProvider> = listOf(null).map { SimpleInvalidValueProvider(it) }
        val STRING: Collection<InvalidValueProvider> = NULL.union(listOf("", "Правда", "झूठ", "🤮").map { SimpleInvalidValueProvider(it) })
        val ENCRYPTED_ID: Collection<InvalidValueProvider> = STRING.union(listOf("notvalid", "ySMpFx4whPczThUmBlSnv-XXvOt1Ntjc0rB6Mb-c230").map { SimpleInvalidValueProvider(it) }.plus(MaxCharsInvalidValueProvider(255)))
        val DATE: Collection<InvalidValueProvider> = listOf(null, "invalid_date", "0021-83-97T08:24:30.695066Z", "2021-03-17T25:75:30.695066Z", "").map { SimpleInvalidValueProvider(it) }.plus(MaxCharsInvalidValueProvider(255))
        val TIME: Collection<InvalidValueProvider> = listOf(null, "invalid_time", "25:00", "22:90", "").map { SimpleInvalidValueProvider(it) }.plus(MaxCharsInvalidValueProvider(255))
        val DATE_TIME: Collection<InvalidValueProvider> = listOf(null, "invalid_date", "0021-83-97T08:24:30.695066Z", "2021-03-17T25:75:30.695066Z", "").map { SimpleInvalidValueProvider(it) }.plus(MaxCharsInvalidValueProvider(255))
        val STRING_NOT_NULL: Collection<InvalidValueProvider> = listOf("Правда", "झूठ", "🤮").map { SimpleInvalidValueProvider(it) }
        val ENCRYPTED_ID_NOT_NULL: Collection<InvalidValueProvider> = STRING_NOT_NULL.union(listOf("notvalid", "ySMpFx4whPczThUmBlSnv-XXvOt1Ntjc0rB6Mb-c230").map { SimpleInvalidValueProvider(it) }.plus(MaxCharsInvalidValueProvider(255)))
    }

}