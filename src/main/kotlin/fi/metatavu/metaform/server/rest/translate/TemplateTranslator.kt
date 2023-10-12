package fi.metatavu.metaform.server.rest.translate

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import fi.metatavu.metaform.api.spec.model.Template
import fi.metatavu.metaform.api.spec.model.TemplateData
import fi.metatavu.metaform.server.exceptions.DeserializationFailedException
import org.slf4j.Logger
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
        val deserializedData = template.data?.let { templateData -> deserializeData(templateData) }
          ?: throw DeserializationFailedException("Template data deserialization failed")

        return Template(
          id = template.id,
          data = deserializedData
        )
    }

    /**
     * Deserializes draft data from string
     *
     * @param data data
     * @return draft data
     */
    private fun deserializeData(data: String): TemplateData? {
        try {
          return jacksonObjectMapper().readValue(data)
        } catch (e: Exception) {
          logger.error("Failed to read template data", e)
        }
        return null
    }
}