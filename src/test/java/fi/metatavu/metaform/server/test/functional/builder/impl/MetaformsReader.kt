package fi.metatavu.metaform.server.test.functional.builder.impl

import fi.metatavu.metaform.api.client.models.Metaform
import java.io.IOException
import java.util.*
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue

/**
 * Class for reading and deserializing metaforms from json files
 */
class MetaformsReader {

    companion object {
        /**
         * Reads a Metaform from JSON file
         *
         * @param form file name
         * @return Metaform object
         * @throws IOException throws IOException when JSON reading fails
         */
        @Throws(IOException::class)
        fun readMetaform(form: String?): Metaform? {
            val path = String.format("fi/metatavu/metaform/testforms/%s.json", form)
            val formStream = this::class.java.classLoader.getResourceAsStream(path) ?: return null
            return jacksonObjectMapper().readValue(formStream)
        }
    }
}