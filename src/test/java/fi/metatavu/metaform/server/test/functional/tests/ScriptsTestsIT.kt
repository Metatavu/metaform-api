package fi.metatavu.metaform.server.test.functional.tests

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
  fun testCreateScript() {
    TestBuilder().use { builder ->
      val script = Script(
        name = "Script",
        language = "Haskell",
        content = "Script content",
        type = ScriptType.EXPORT_XSLX
      )

      val createdScript = builder.systemAdmin.scripts.create(script)
      assertEquals("Script", createdScript.name)
      assertEquals("Haskell", createdScript.language)
      assertEquals("Script content", createdScript.content)
      assertEquals(ScriptType.EXPORT_XSLX, createdScript.type)
    }
  }

  @Test
  fun testUpdateScript() {
    TestBuilder().use { builder ->
      val script = Script(
        name = "Script",
        language = "Haskell",
        content = "Script content",
        type = ScriptType.EXPORT_XSLX
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
  }

  @Test
  fun testListScripts() {
    TestBuilder().use { builder ->
      val script = Script(
        name = "Script",
        language = "Haskell",
        content = "Script content",
        type = ScriptType.EXPORT_XSLX
      )

      builder.systemAdmin.scripts.create(script)
      assertEquals(1, builder.systemAdmin.scripts.list().size)
      builder.systemAdmin.scripts.create(script)
      builder.systemAdmin.scripts.create(script)
      assertEquals(3, builder.systemAdmin.scripts.list().size)
    }
  }

  @Test
  fun testFindScripts() {
    TestBuilder().use { builder ->
      val script = Script(
        name = "Script",
        language = "Haskell",
        content = "Script content",
        type = ScriptType.EXPORT_XSLX
      )

      val createdScript = builder.systemAdmin.scripts.create(script)
      val foundScript = builder.systemAdmin.scripts.find(createdScript.id!!)

      assertEquals("Script", foundScript.name)
      assertEquals("Haskell", foundScript.language)
      assertEquals("Script content", foundScript.content)
      assertEquals(ScriptType.EXPORT_XSLX, foundScript.type)
    }
  }
}