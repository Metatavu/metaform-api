package fi.metatavu.metaform.server.rest.translate

import fi.metatavu.metaform.server.persistence.model.Script
import jakarta.enterprise.context.ApplicationScoped

/**
 * Translator for scripts
 */
@ApplicationScoped
class ScriptsTranslator {
  fun translate(entity: Script): fi.metatavu.metaform.api.spec.model.Script {
    return fi.metatavu.metaform.api.spec.model.Script(
      id = entity.id,
      name = entity.name,
      language = entity.language,
      content = entity.content,
      type = entity.scriptType
    )
  }
}