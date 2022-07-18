package fi.metatavu.metaform.server.rest.translate

import com.fasterxml.jackson.databind.ObjectMapper
import fi.metatavu.metaform.server.persistence.model.Metaform
import org.slf4j.Logger
import java.io.IOException
import javax.enterprise.context.ApplicationScoped
import javax.inject.Inject

/**
 * Translator for Metaforms
 *
 * @author Antti Leppä
 */
@ApplicationScoped
class MetaformTranslator {

  @Inject
  lateinit var logger: Logger

  /**
   * Translates JPA Metaform into REST Metaform
   *
   * @param entity JPA Metaform
   * @return REST Metaform
   */
  fun translate(entity: Metaform): fi.metatavu.metaform.api.spec.model.Metaform? {
    var result: fi.metatavu.metaform.api.spec.model.Metaform?
    val objectMapper = ObjectMapper()
    try {
      result = objectMapper.readValue(entity.data, fi.metatavu.metaform.api.spec.model.Metaform::class.java)

      if (result != null) {
        result = result.copy(
          id = entity.id,
          slug = entity.slug,
          exportThemeId = entity.exportTheme?.id,
          visibility = entity.visibility
        )

      }

    } catch (e: IOException) {
      logger.error(String.format("Failed to translate metaform %s", entity.id.toString()), e)
      return null
    }

    return result
  }
}