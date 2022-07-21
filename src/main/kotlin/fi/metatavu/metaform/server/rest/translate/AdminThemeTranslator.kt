package fi.metatavu.metaform.server.rest.translate

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import fi.metatavu.metaform.api.spec.model.AdminTheme
import fi.metatavu.metaform.server.exceptions.DeserializationFailedException
import org.slf4j.Logger
import javax.enterprise.context.ApplicationScoped
import javax.inject.Inject

/**
 * Translator for Admin themes
 *
 * @author Otto Hooper
 */
@ApplicationScoped
class AdminThemeTranslator {

    @Inject
    lateinit var objectMapper: ObjectMapper

    @Inject
    lateinit var logger: Logger
    /**
     * Translate JPA admin theme object to REST admin theme object
     * 
     * @param adminTheme JPA admin theme object
     * 
     * @return REST admin theme object
     */
    fun translate(adminTheme: fi.metatavu.metaform.server.persistence.model.AdminTheme): AdminTheme {
        val deserializedData = deserializeData(adminTheme.data)
            ?: throw DeserializationFailedException("Theme data deserialization failed")

        return AdminTheme(
            id = adminTheme.id,
            data = deserializedData,
            name = adminTheme.name,
            slug = adminTheme.slug,
            creatorId = adminTheme.creatorId,
            lastModifierId = adminTheme.lastModifierId,
        )
    }

    /**
     * Deserializes draft data from string
     *
     * @param data data
     * @return draft data
     */
    private fun deserializeData(data: String): Map<String, Any>? {
        try {
            val typeRef: TypeReference<Map<String, Any>> = object : TypeReference<Map<String, Any>>() {}
            return objectMapper.readValue(data, typeRef)
        } catch (e: Exception) {
            logger.error("Failed to read draft data", e)
        }
        return null
    }
}