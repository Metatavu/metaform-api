package fi.metatavu.metaform.server.test.functional.tests

import fi.metatavu.metaform.api.client.models.Metaform
import fi.metatavu.metaform.api.client.models.MetaformVisibility
import fi.metatavu.metaform.api.client.models.Script
import fi.metatavu.metaform.api.client.models.ScriptType
import fi.metatavu.metaform.server.test.functional.builder.TestBuilder
import fi.metatavu.metaform.server.test.functional.builder.resources.MetaformKeycloakResource
import fi.metatavu.metaform.server.test.functional.builder.resources.MysqlResource
import io.quarkus.test.common.QuarkusTestResource
import io.quarkus.test.junit.QuarkusTest
import io.quarkus.test.junit.TestProfile
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

/**
 * Tests for Scripts functionality
 */
@QuarkusTest
@QuarkusTestResource.List(
  QuarkusTestResource(MysqlResource::class),
  QuarkusTestResource(MetaformKeycloakResource::class)
)
@TestProfile(GeneralTestProfile::class)
class ScriptsTestsIT {
  @Test
  fun testCreateScript() = TestBuilder().use { builder ->
    val script = Script(
      name = "Script",
      language = "Haskell",
      content = "Script content",
      type = ScriptType.EXPORT_XLSX
    )

    val createdScript = builder.systemAdmin.scripts.create(script)
    assertEquals("Script", createdScript.name)
    assertEquals("Haskell", createdScript.language)
    assertEquals("Script content", createdScript.content)
    assertEquals(ScriptType.EXPORT_XLSX, createdScript.type)
  }

  @Test
  fun testUpdateScript() = TestBuilder().use { builder ->
    val script = Script(
      name = "Script",
      language = "Haskell",
      content = "Script content",
      type = ScriptType.EXPORT_XLSX
    )

    val createdScript = builder.systemAdmin.scripts.create(script)

    val updatedScript = builder.systemAdmin.scripts.update(createdScript.copy(
      name = "Updated script",
      language = "Java",
      content = "New content",
      type = ScriptType.AFTER_UPDATE_REPLY
    ))

    assertEquals("Updated script", updatedScript.name)
    assertEquals("Java", updatedScript.language)
    assertEquals("New content", updatedScript.content)
    assertEquals(ScriptType.AFTER_UPDATE_REPLY, updatedScript.type)
  }

  @Test
  fun testListScripts() = TestBuilder().use { builder ->
    val script = Script(
      name = "Script",
      language = "Haskell",
      content = "Script content",
      type = ScriptType.EXPORT_XLSX
    )

    builder.systemAdmin.scripts.create(script)
    assertEquals(1, builder.systemAdmin.scripts.list().size)
    builder.systemAdmin.scripts.create(script)
    builder.systemAdmin.scripts.create(script)
    assertEquals(3, builder.systemAdmin.scripts.list().size)
  }

  @Test
  fun testFindScript() = TestBuilder().use { builder ->
    val script = Script(
      name = "Script",
      language = "Haskell",
      content = "Script content",
      type = ScriptType.EXPORT_XLSX
    )

    val createdScript = builder.systemAdmin.scripts.create(script)
    val foundScript = builder.systemAdmin.scripts.find(createdScript.id!!)

    assertEquals("Script", foundScript.name)
    assertEquals("Haskell", foundScript.language)
    assertEquals("Script content", foundScript.content)
    assertEquals(ScriptType.EXPORT_XLSX, foundScript.type)
  }

  @Test
  fun testDeleteScript() = TestBuilder().use { builder ->
    val script = Script(
      name = "Script",
      language = "Haskell",
      content = "Script content",
      type = ScriptType.EXPORT_XLSX
    )

    val createdScript = builder.systemAdmin.scripts.create(script)
    assertEquals(1, builder.systemAdmin.scripts.list().size)
    builder.systemAdmin.scripts.delete(createdScript.id!!)
    assertEquals(0, builder.systemAdmin.scripts.list().size)
  }

  @Test
  fun testDeleteScriptPermission() = TestBuilder().use { builder ->
    val testMetaformId1 = builder.systemAdmin.metaforms.createFromJsonFile("simple").id!!
    val testMetaformId2 = builder.systemAdmin.metaforms.createFromJsonFile("simple").id!!
    val managerAuthentication = builder.createMetaformManagerAuthentication(testMetaformId1, false)
    val adminAuthentication = builder.createMetaformAdminAuthentication(testMetaformId2, false)

    val script = Script(
      name = "Script",
      language = "Haskell",
      content = "Script content",
      type = ScriptType.EXPORT_XLSX
    )

    val createdScript = builder.systemAdmin.scripts.create(script)

    builder.assertApiCallFailStatus(403) { builder.anon.scripts.delete(createdScript.id!!) }
    builder.assertApiCallFailStatus(403) { builder.test1.scripts.delete(createdScript.id!!) }
    builder.assertApiCallFailStatus(403) { managerAuthentication.scripts.delete(createdScript.id!!) }
    builder.assertApiCallFailStatus(403) { adminAuthentication.scripts.delete(createdScript.id!!) }
  }

  @Test
  fun testCreateScriptPermission() = TestBuilder().use { builder ->
    val testMetaformId1 = builder.systemAdmin.metaforms.createFromJsonFile("simple").id!!
    val testMetaformId2 = builder.systemAdmin.metaforms.createFromJsonFile("simple").id!!
    val managerAuthentication = builder.createMetaformManagerAuthentication(testMetaformId1, false)
    val adminAuthentication = builder.createMetaformAdminAuthentication(testMetaformId2, false)

    val script = Script(
      name = "Script",
      language = "Haskell",
      content = "Script content",
      type = ScriptType.EXPORT_XLSX
    )

    builder.assertApiCallFailStatus(403) { builder.anon.scripts.create(script) }
    builder.assertApiCallFailStatus(403) { builder.test1.scripts.create(script) }
    builder.assertApiCallFailStatus(403) { managerAuthentication.scripts.create(script) }
    builder.assertApiCallFailStatus(403) { adminAuthentication.scripts.create(script) }
  }

  @Test
  fun testUpdateScriptPermission() = TestBuilder().use { builder ->
    val testMetaformId1 = builder.systemAdmin.metaforms.createFromJsonFile("simple").id!!
    val testMetaformId2 = builder.systemAdmin.metaforms.createFromJsonFile("simple").id!!
    val managerAuthentication = builder.createMetaformManagerAuthentication(testMetaformId1, false)
    val adminAuthentication = builder.createMetaformAdminAuthentication(testMetaformId2, false)

    val script = Script(
      name = "Script",
      language = "Haskell",
      content = "Script content",
      type = ScriptType.EXPORT_XLSX
    )

    val createdScript = builder.systemAdmin.scripts.create(script)

    builder.assertApiCallFailStatus(403) { builder.anon.scripts.update(createdScript) }
    builder.assertApiCallFailStatus(403) { builder.test1.scripts.update(createdScript) }
    builder.assertApiCallFailStatus(403) { managerAuthentication.scripts.update(createdScript) }
    builder.assertApiCallFailStatus(403) { adminAuthentication.scripts.update(createdScript) }
  }

  @Test
  fun testFindScriptPermission() = TestBuilder().use { builder ->
    val testMetaformId1 = builder.systemAdmin.metaforms.createFromJsonFile("simple").id!!
    val testMetaformId2 = builder.systemAdmin.metaforms.createFromJsonFile("simple").id!!
    val managerAuthentication = builder.createMetaformManagerAuthentication(testMetaformId1, false)
    val adminAuthentication = builder.createMetaformAdminAuthentication(testMetaformId2, false)

    val script = Script(
      name = "Script",
      language = "Haskell",
      content = "Script content",
      type = ScriptType.EXPORT_XLSX
    )

    val createdScript = builder.systemAdmin.scripts.create(script)

    builder.assertApiCallFailStatus(403) { builder.anon.scripts.find(createdScript.id!!) }
    builder.assertApiCallFailStatus(403) { builder.test1.scripts.find(createdScript.id!!) }
    builder.assertApiCallFailStatus(403) { managerAuthentication.scripts.find(createdScript.id!!) }
    builder.assertApiCallFailStatus(200) { adminAuthentication.scripts.find(createdScript.id!!) }
  }

  @Test
  fun testListScriptsPermission() = TestBuilder().use { builder ->
    val testMetaformId1 = builder.systemAdmin.metaforms.createFromJsonFile("simple").id!!
    val testMetaformId2 = builder.systemAdmin.metaforms.createFromJsonFile("simple").id!!
    val managerAuthentication = builder.createMetaformManagerAuthentication(testMetaformId1, false)
    val adminAuthentication = builder.createMetaformAdminAuthentication(testMetaformId2, false)

    val script = Script(
      name = "Script",
      language = "Haskell",
      content = "Script content",
      type = ScriptType.EXPORT_XLSX
    )

    builder.systemAdmin.scripts.create(script)

    builder.assertApiCallFailStatus(403) { builder.anon.scripts.list() }
    builder.assertApiCallFailStatus(403) { builder.test1.scripts.list() }
    builder.assertApiCallFailStatus(403) { managerAuthentication.scripts.list() }
    builder.assertApiCallFailStatus(200) { adminAuthentication.scripts.list() }
  }
  @Test
  fun testMetaformScripts() = TestBuilder().use { builder ->
    val script = Script(
      name = "Script",
      language = "Haskell",
      content = "Script content",
      type = ScriptType.EXPORT_XLSX
    )

    val createdScript = builder.systemAdmin.scripts.create(script)
    val createdMetaform = builder.systemAdmin.metaforms.create(Metaform(
      title = "Test",
      allowDrafts = true,
      visibility = MetaformVisibility.PUBLIC,
      scripts = arrayOf(createdScript.id!!)
    ))

    assertEquals(createdScript.id, createdMetaform.scripts!![0])

    val createdScript2 = builder.systemAdmin.scripts.create(script)
    val updatedMetaform1 = builder.systemAdmin.metaforms.updateMetaform(createdMetaform.id!!, createdMetaform.copy(
      scripts = arrayOf(createdScript.id, createdScript2.id!!)
    ))
    assertEquals(2, updatedMetaform1.scripts!!.size)

    val createdScript3 = builder.systemAdmin.scripts.create(script)
    val updatedMetaform2 = builder.systemAdmin.metaforms.updateMetaform(createdMetaform.id, createdMetaform.copy(
      scripts = arrayOf(createdScript2.id, createdScript3.id!!)
    ))
    assertEquals(false, updatedMetaform2.scripts!!.contains(createdScript.id))
    assertEquals(true, updatedMetaform2.scripts.contains(createdScript2.id))
    assertEquals(true, updatedMetaform2.scripts.contains(createdScript3.id))

    builder.systemAdmin.scripts.delete(createdScript2.id)
    val foundMetaform = builder.systemAdmin.metaforms.findMetaform(metaformId = createdMetaform.id, metaformSlug = null, replyId = null, ownerKey = null)
    assertEquals(false, foundMetaform.scripts!!.contains(createdScript2.id))
    assertEquals(true, foundMetaform.scripts.contains(createdScript3.id))
  }
}