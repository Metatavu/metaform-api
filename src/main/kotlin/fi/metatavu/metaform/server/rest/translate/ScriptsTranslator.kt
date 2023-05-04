package fi.metatavu.metaform.server.rest.translate

import fi.metatavu.metaform.server.persistence.model.Script
import javax.enterprise.context.ApplicationScoped

/**
 * Translator for scripts
 */
@ApplicationScoped
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