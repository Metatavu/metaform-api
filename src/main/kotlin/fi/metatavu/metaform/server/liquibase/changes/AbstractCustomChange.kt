package fi.metatavu.metaform.server.liquibase.changes

import io.quarkus.runtime.annotations.RegisterForReflection
import liquibase.change.custom.CustomTaskChange
import liquibase.database.Database
import liquibase.exception.SetupException
import liquibase.exception.ValidationErrors
import liquibase.resource.ResourceAccessor

/**
 * Abstract base class for custom Liquibase changes
 *
 * @author Antti Leppä
 */
@RegisterForReflection
abstract class AbstractCustomChange : CustomTaskChange {
    private val confirmationMessage = StringBuilder()

    /**
     * Appends string to confirmation message
     *
     * @param message message
     */
    protected fun appendConfirmationMessage(message: String?) {
        confirmationMessage.append(message)
    }

    override fun getConfirmationMessage(): String {
        return confirmationMessage.toString()
    }

    @Throws(SetupException::class)
    override fun setUp() {
        // No need to set anything up
    }

    override fun setFileOpener(resourceAccessor: ResourceAccessor) {}
    override fun validate(database: Database): ValidationErrors? {
        return null
    }
}