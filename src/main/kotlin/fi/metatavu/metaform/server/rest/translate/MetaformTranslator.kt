package fi.metatavu.metaform.server.rest.translate

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import fi.metatavu.metaform.server.exceptions.MalformedMetaformJsonException
import fi.metatavu.metaform.server.persistence.dao.MetaformScriptDAO
import fi.metatavu.metaform.server.persistence.model.Metaform
import java.io.IOException
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject

/**
 * Translator for Metaforms
 *
 * @author Antti Leppä
 */
@ApplicationScoped
class MetaformTranslator {

  @Inject
  lateinit var metaformScriptDAO: MetaformScriptDAO

  /**
   * Translates JPA Metaform into REST Metaform
   *
   * @param entity JPA Metaform
   * @return REST Metaform
   */
  @Throws(MalformedMetaformJsonException::class)
  fun translate(entity: Metaform): fi.metatavu.metaform.api.spec.model.Metaform {
    val objectMapper = ObjectMapper()
    objectMapper.registerModule(JavaTimeModule())

    val result = try {
       objectMapper.readValue(entity.data, fi.metatavu.metaform.api.spec.model.Metaform::class.java)
    } catch (e: IOException) {
      throw MalformedMetaformJsonException(String.format("Failed to translate metaform %s", entity.id.toString()), e)
    }

    val scripts = metaformScriptDAO.listByMetaform(entity).mapNotNull { metaformScript -> metaformScript.script.id}

    return result.copy(
      id = entity.id,
      slug = entity.slug,
      scripts = scripts,
      exportThemeId = entity.exportTheme?.id,
      visibility = entity.visibility,
      createdAt = entity.createdAt,
      modifiedAt = entity.modifiedAt,
      creatorId = entity.creatorId,
      lastModifierId = entity.lastModifierId
    )
  }
}