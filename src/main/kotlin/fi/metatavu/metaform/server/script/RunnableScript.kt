package fi.metatavu.metaform.server.script

/**
 * Class that defines single runnable script
 *
 * @author Antti Leppä
 */
data class RunnableScript (
    val language: String,
    val content: String,
    val name: String
)