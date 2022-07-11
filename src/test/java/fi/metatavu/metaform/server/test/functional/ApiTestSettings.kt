package fi.metatavu.metaform.server.test.functional

/**
 * Settings implementation for test builder
 *
 * @author Jari Nykänen
 */
class ApiTestSettings {

    companion object {

        /**
         * Returns API service base path
         */
        val apiBasePath: String
            get() = "http://localhost:8081/"

    }
}
