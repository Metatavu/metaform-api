package fi.metatavu.metaform.server.test.functional.builder.impl

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import fi.metatavu.metaform.api.client.models.Script
import java.io.IOException

/**
 * Class for reading and deserializing scripts from json files
 */
class ScriptsReader {
    companion object {
        /**
         * Reads a Script from JSON file
         *
         * @param script file name
         * @return Script object
         * @throws IOException throws IOException when JSON reading fails
         */
        @Throws(IOException::class)
        fun readScript(script: String?): Script? {
            val path = String.format("fi/metatavu/metaform/testscripts/%s.json", script)
            val formStream = this::class.java.classLoader.getResourceAsStream(path) ?: return null
            return jacksonObjectMapper().readValue(formStream)
        }
    }
}