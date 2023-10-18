package fi.metatavu.metaform.server.test.functional.builder.impl

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import fi.metatavu.metaform.api.client.models.Template
import java.io.IOException

/**
 * Class for reading and deserializing template from json file
 */
class TemplateReader {

    companion object {

        /**
         * Reads a Template from JSON file
         *
         * @param form file name
         * @return Template object
         * @throws IOException throws IOException when JSON reading fails
         */
        @Throws(IOException::class)
        fun readTemplate(form: String?): Template? {
            val path = String.format("fi/metatavu/metaform/testforms/%s.json", form)
            val formStream = this::class.java.classLoader.getResourceAsStream(path) ?: return null
            return jacksonObjectMapper().readValue(formStream)
        }
    }
}