package fi.metatavu.metaform.server.liquibase.changes

import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.quarkus.runtime.annotations.RegisterForReflection
import liquibase.database.Database
import liquibase.database.jvm.JdbcConnection
import liquibase.exception.CustomChangeException
import java.util.*


/**
 * Custom change for migrating deprecated field properties
 *
 * @author Antti Leppä
 */
@RegisterForReflection
@Suppress ("UNUSED")
class MigrateDeprecatedFieldProperties : AbstractAuthzCustomChange() {

    @Throws(CustomChangeException::class)
    override fun execute(database: Database) {
        val connection = database.connection as JdbcConnection
        appendConfirmationMessage("Migrating deprecated field properties...")

        readMetaforms(connection = connection, ignoreUnknownProperties = true).forEach {
            val metaformId = it.first
            val metaform = it.second

            appendConfirmationMessage("Migrating deprecated field properties for metaform ${metaform["slug"]}")

            val sections = jacksonObjectMapper().nodeFactory.arrayNode()
            metaform.get("sections").forEach { section ->
                val objectMapper = jacksonObjectMapper()
                objectMapper.registerModule(JavaTimeModule())
                val fields = jacksonObjectMapper().nodeFactory.arrayNode()
                section["fields"].forEach { field ->
                    val newField = (field as ObjectNode)
                    if (newField["type"].asText() == "table") {

                        val table = objectMapper.createObjectNode()

                        table.put("addRows", newField["addRows"])
                        table.put("columns", newField["columns"])
                        newField.put("table", table)
                    }

                    newField.remove("class")
                    newField.remove("printable")
                    newField.remove("source-url")
                    newField.remove("upload-url")
                    newField.remove("single-file")
                    newField.remove("only-images")
                    newField.remove("max-file-size")
                    newField.remove("draggable")
                    newField.remove("src")
                    newField.remove("addRows")
                    newField.remove("columns")

                    fields.add(newField)
                }


                val newSection = (section as ObjectNode)
                newSection.put("fields", fields)
                sections.add(newSection)
            }


            val newMetaform = (metaform as ObjectNode)
            newMetaform.put("sections", sections)

            updateMetaform(
                connection = connection,
                metaform = newMetaform,
                metaformId = metaformId
            )
        }

        appendConfirmationMessage("Deprecated field properties migrated.")
    }

}
