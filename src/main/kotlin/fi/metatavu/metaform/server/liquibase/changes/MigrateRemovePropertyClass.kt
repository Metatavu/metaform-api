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
 */
@RegisterForReflection
@Suppress ("UNUSED")
class MigrateRemovePropertyClass : AbstractAuthzCustomChange() {

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
                    newField.remove("propertyClass")
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
