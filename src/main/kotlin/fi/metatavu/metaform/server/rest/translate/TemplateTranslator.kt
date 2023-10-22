package fi.metatavu.metaform.server.rest.translate

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import fi.metatavu.metaform.api.spec.model.Template
import fi.metatavu.metaform.api.spec.model.TemplateData
import fi.metatavu.metaform.server.exceptions.DeserializationFailedException
import fi.metatavu.metaform.server.exceptions.MalformedMetaformJsonException
import org.slf4j.Logger
import java.io.IOException
import javax.enterprise.context.ApplicationScoped
import javax.inject.Inject

/**
 * Translator for templates
 *
 * @author Harri Häkkinen
 */
@ApplicationScoped
class TemplateTranslator {

    @Inject
    lateinit var logger: Logger

    @Throws(DeserializationFailedException::class)
    fun translateTemplate(template: fi.metatavu.metaform.server.persistence.model.Template): Template {
        /*
        val deserializedData = template.data?.let { templateData -> deserializeData(templateData) }
          ?: throw DeserializationFailedException("Template data deserialization failed")
        */

        val objectMapper = ObjectMapper()
        objectMapper.registerModule(JavaTimeModule())

        val result = try {
            objectMapper.readValue(template.data, TemplateData::class.java)
        } catch (e: IOException) {
            throw MalformedMetaformJsonException(String.format("Failed to translate template %s", template.id.toString()), e)
        }

        //return result.copy(
        return Template(
            id = template.id,
            data = result,
            visibility = template.visibility,
            creatorId = template.creatorId,
            lastModifierId = template.lastModifierId,
            createdAt = template.createdAt,
            modifiedAt = template.modifiedAt
        )
    }

    /*
    private fun deserializeData(data: String): TemplateData? {

        try {

            println("Deserialized data:$data")

          return jacksonObjectMapper().readValue(data)
        } catch (e: Exception) {
          logger.error("Failed to read template data", e)
        }
        return null
    }

    */
}