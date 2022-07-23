package fi.metatavu.metaform.server.rest.translate

import com.fasterxml.jackson.databind.ObjectMapper
import fi.metatavu.metaform.server.exceptions.MalformedMetaformJsonException
import fi.metatavu.metaform.server.persistence.model.Metaform
import java.io.IOException
import javax.enterprise.context.ApplicationScoped

/**
 * Translator for Metaforms
 *
 * @author Antti Leppä
 */
@ApplicationScoped
class MetaformTranslator {

  /**
   * Translates JPA Metaform into REST Metaform
   *
   * @param entity JPA Metaform
   * @return REST Metaform
   */
  @Throws(MalformedMetaformJsonException::class)
  fun translate(entity: Metaform): fi.metatavu.metaform.api.spec.model.Metaform {
    val objectMapper = ObjectMapper()

    val result = try {
       objectMapper.readValue(entity.data, fi.metatavu.metaform.api.spec.model.Metaform::class.java)
    } catch (e: IOException) {
      throw MalformedMetaformJsonException(String.format("Failed to translate metaform %s", entity.id.toString()), e)
    }

    return result.copy(
      id = entity.id,
      slug = entity.slug,
      exportThemeId = entity.exportTheme?.id,
      visibility = entity.visibility
    )
  }
}