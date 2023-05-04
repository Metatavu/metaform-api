package fi.metatavu.metaform.server.persistence.dao

import fi.metatavu.metaform.api.spec.model.ScriptType
import fi.metatavu.metaform.server.persistence.model.Script
import java.util.*
import javax.enterprise.context.ApplicationScoped

/**
 * DAO class for Script-entity
 *
 * @param id id
 * @param name name
 * @param type type
 * @param content content
 * @param creatorId creator id
 */
@ApplicationScoped
class ScriptDAO: AbstractDAO<Script>() {
  /**
   * Creates a script
   *
   * @param id id
   * @param name name
   * @param type type
   * @param content content
   * @param creatorId creator id
   */
  fun createScript(
    id: UUID,
    name: String,
    language: String,
    type: ScriptType,
    content: String,
    creatorId: UUID
  ): Script {
    val script = Script()
    script.id = id
    script.name = name
    script.language = language
    script.content = content
    script.type = type
    script.creatorId = creatorId
    script.lastModifierId = creatorId
    return persist(script)
  }

  /**
   * Updates the name
   *
   * @param script script to update
   * @param name new name
   * @param lastModifierId last modifier id
   */
  fun updateName(script: Script, name: String, lastModifierId: UUID): Script {
    script.name = name
    script.lastModifierId = lastModifierId
    return persist(script)
  }

  /**
   * Updates the language
   *
   * @param script script to update
   * @param language new language
   * @param lastModifierId last modifier id
   */
  fun updateLanguage(script: Script, language: String, lastModifierId: UUID): Script {
    script.language = language
    script.lastModifierId = lastModifierId
    return persist(script)
  }

  /**
   * Updates the type
   *
   * @param script script to update
   * @param type new type
   * @param lastModifierId last modifier id
   */
  fun updateType(script: Script, type: ScriptType, lastModifierId: UUID): Script {
    script.type = type
    script.lastModifierId = lastModifierId
    return persist(script)
  }


  /**
   * Updates the type
   *
   * @param script script to update
   * @param content new content
   * @param lastModifierId last modifier id
   */
  fun updateContent(script: Script, content: String, lastModifierId: UUID): Script {
    script.content = content
    script.lastModifierId = lastModifierId
    return persist(script)
  }
}