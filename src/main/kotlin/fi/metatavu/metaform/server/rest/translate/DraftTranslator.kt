package fi.metatavu.metaform.server.rest.translate

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import fi.metatavu.metaform.api.spec.model.Draft
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
  fun translateDraft(draft: fi.metatavu.metaform.server.persistence.model.Draft?): Draft? {
    if (draft == null) {
      return null
    }

    return Draft(
      id = draft.id,
      data = deserializeData(draft.data!!)!! //todo add exception for deserialization fail to avoid npe
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
      logger.error("Failed to read version data", e)
    }
    return null
  }
}