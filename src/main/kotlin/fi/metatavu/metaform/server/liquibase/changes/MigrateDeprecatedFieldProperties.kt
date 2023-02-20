package fi.metatavu.metaform.server.liquibase.changes

import fi.metatavu.metaform.api.spec.model.MetaformFieldType
import fi.metatavu.metaform.api.spec.model.MetaformSection
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

            appendConfirmationMessage("Migrating deprecated field properties for metaform ${metaform.slug}")

            val tableFields = metaform.sections
                ?.mapNotNull(MetaformSection::fields)
                ?.flatMap { fields -> fields.toList() }
                ?.filter { field -> field.type == MetaformFieldType.TABLE }

            if (!tableFields.isNullOrEmpty()) {
                throw CustomChangeException("Table field migration is not supported")
            }

            updateMetaform(
                connection = connection,
                metaform = metaform,
                metaformId = metaformId
            )
        }

        appendConfirmationMessage("Deprecated field properties migrated.")
    }

}