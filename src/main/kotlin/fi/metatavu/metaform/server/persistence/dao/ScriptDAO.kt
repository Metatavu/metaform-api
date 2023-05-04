package fi.metatavu.metaform.server.persistence.dao

import fi.metatavu.metaform.api.spec.model.ScriptType
import fi.metatavu.metaform.server.persistence.model.Script
import java.util.*

/**
 * DAO class for Script-entity
 */
class ScriptDAO: AbstractDAO<Script>() {
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

  fun updateName(script: Script, name: String, lastModifierId: UUID): Script {
    script.name = name
    script.lastModifierId = lastModifierId
    return persist(script)
  }

  fun updateLanguage(script: Script, language: String, lastModifierId: UUID): Script {
    script.language = language
    script.lastModifierId = lastModifierId
    return persist(script)
  }

  fun updateType(script: Script, type: ScriptType, lastModifierId: UUID): Script {
    script.type = type
    script.lastModifierId = lastModifierId
    return persist(script)
  }

  fun updateContent(script: Script, content: String, lastModifierId: UUID): Script {
    script.content = content
    script.lastModifierId = lastModifierId
    return persist(script)
  }
}