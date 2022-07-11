package fi.metatavu.metaform.server.rest.translate

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import fi.metatavu.metaform.api.spec.model.Draft
import fi.metatavu.metaform.server.exceptions.DeserializationFailedException
import org.slf4j.Logger
import javax.enterprise.context.ApplicationScoped
import javax.inject.Inject

/**
 * Translator for drafts
 *
 * @author Antti Leppä
 */
@ApplicationScoped
class DraftTranslator {

  @Inject
  lateinit var logger: Logger

  @Inject
  lateinit var objectMapper: ObjectMapper

  /**
   * Translates JPA draft object into REST draft object
   *
   * @param draft JPA drasft object
   * @return REST object
   */
  @Throws(DeserializationFailedException::class)
  fun translateDraft(draft: fi.metatavu.metaform.server.persistence.model.Draft): Draft {
    val deserializedData = draft.data?.let { draftData -> deserializeData(draftData) }
            ?: throw DeserializationFailedException("Draft data deserialization failed")

    return Draft(
      id = draft.id,
      data = deserializedData
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