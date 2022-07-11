package fi.metatavu.metaform.server.keycloak

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.ObjectMapper
import org.jboss.resteasy.plugins.providers.jackson.ResteasyJackson2Provider
import javax.ws.rs.core.MediaType

/**
 * Resteasy client provider that skips nulls when serializing JSON
 */
class NotNullResteasyJackson2Provider: ResteasyJackson2Provider() {

    override fun locateMapper(type: Class<*>?, mediaType: MediaType): ObjectMapper {
        val result: ObjectMapper = super.locateMapper(type, mediaType)
        result.setSerializationInclusion(JsonInclude.Include.NON_NULL)
        return result
    }
}