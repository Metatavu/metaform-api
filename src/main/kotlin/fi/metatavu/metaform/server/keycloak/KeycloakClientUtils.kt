package fi.metatavu.metaform.server.keycloak

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.commons.io.IOUtils
import org.apache.commons.lang3.StringUtils
import org.slf4j.Logger
import java.io.IOException
import java.io.InputStream
import java.util.*
import java.util.regex.Pattern
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import jakarta.ws.rs.core.Response

/**
 * Keycloak client utilities
 */
@ApplicationScoped
class KeycloakClientUtils {

    @Inject
    lateinit var logger: Logger

    /**
     * Finds a id from Keycloak create response
     *
     * @param response response object
     * @return id
     */
    fun getCreateResponseId(response: Response): UUID? {
        if (response.status != 201) {
            try {
                if (logger.isErrorEnabled) {
                    logger.error("Failed to execute create: {}", IOUtils.toString(response.entity as InputStream, "UTF-8"))
                }
            } catch (e: IOException) {
                logger.error("Failed to extract error message", e)
            }
            return null
        }
        val locationId = getCreateResponseLocationId(response)
        return locationId ?: getCreateResponseBodyId(response)
    }

    /**
     * Attempts to locate id from create location response
     *
     * @param response response
     * @return id or null if not found
     */
    private fun getCreateResponseLocationId(response: Response): UUID? {
        val location = response.getHeaderString("location")
        if (StringUtils.isNotBlank(location)) {
            val pattern = Pattern.compile(".*/(.*)$")
            val matcher = pattern.matcher(location)
            if (matcher.find()) {
                return UUID.fromString(matcher.group(1))
            }
        }
        return null
    }

    /**
     * Attempts to locate id from create response body
     *
     * @param response response object
     * @return id or null if not found
     */
    private fun getCreateResponseBodyId(response: Response): UUID? {
        if (response.entity is InputStream) {
            try {
                (response.entity as InputStream).use { inputStream ->
                    val result = readJsonMap(inputStream)
                    if (result["_id"] is String) {
                        return UUID.fromString(result["_id"] as String?)
                    }
                    if (result["id"] is String) {
                        return UUID.fromString(result["id"] as String?)
                    }
                }
            } catch (e: IOException) {
                if (logger.isDebugEnabled) {
                    logger.debug("Failed to locate id from response", e)
                }
            }
        }
        return null
    }

    /**
     * Reads JSON src into Map
     *
     * @param src input
     * @return map
     * @throws IOException throws IOException when there is error when reading the input
     */
    @Throws(IOException::class)
    private fun readJsonMap(src: InputStream): Map<String, Any?> {
        val objectMapper = ObjectMapper()
        return objectMapper.readValue(src, object : TypeReference<Map<String, Any?>>() {})
    }

}