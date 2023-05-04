package fi.metatavu.metaform.server.rest.translate

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import fi.metatavu.metaform.server.exceptions.MalformedMetaformJsonException
import fi.metatavu.metaform.server.persistence.model.Script
import java.io.IOException

/**
 * Translator for scripts
 */
class ScriptsTranslator {
  fun translate(entity: Script): fi.metatavu.metaform.api.spec.model.Script {
    return fi.metatavu.metaform.api.spec.model.Script(
      id = entity.id.toString(),
      name = entity.name,
      language = entity.language,
      content = entity.content,
      type = entity.type
    )
  }
}