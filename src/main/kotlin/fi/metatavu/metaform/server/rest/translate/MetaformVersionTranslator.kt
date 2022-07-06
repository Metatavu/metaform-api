package fi.metatavu.metaform.server.rest.translate

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import fi.metatavu.metaform.server.persistence.model.MetaformVersion
import org.slf4j.Logger
import javax.enterprise.context.ApplicationScoped
import javax.inject.Inject

/**
 * Translatro for Metaform versions
 *
 * @author Tianxing Wu
 */
@ApplicationScoped
class MetaformVersionTranslator {

  @Inject
  lateinit var logger: Logger

  @Inject
  lateinit var objectMapper: ObjectMapper

  /**
   * Translates JPA Metaform version into REST Metaform version
   *
   * @param entity JPA Metaform version
   * @return REST Metaform version
   */
  fun translate(entity: MetaformVersion): fi.metatavu.metaform.api.spec.model.MetaformVersion {
    return fi.metatavu.metaform.api.spec.model.MetaformVersion(
      id = entity.id,
      type = entity.type!!,
      data = deserializeData(entity.data!!)!!, //todo add exception if translation fails
      creatorId = entity.creatorId,
      lastModifierId = entity.lastModifierId,
      createdAt = entity.createdAt,
      modifiedAt = entity.modifiedAt

    )
  }

  /**
   * Deserializes version data from string
   *
   * @param data data
   * @return version data
   */
  private fun deserializeData(data: String): Any? {
    try {
      val typeRef: TypeReference<Map<String, Any>> = object : TypeReference<Map<String, Any>>() {}
      return objectMapper.readValue(data, typeRef)
    } catch (e: Exception) {
      logger.error("Failed to read version data", e)
    }
    return null
  }
}