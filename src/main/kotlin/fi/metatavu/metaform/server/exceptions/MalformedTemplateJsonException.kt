package fi.metatavu.metaform.server.exceptions

/**
 * Exception throw when malformed template data json
 *
 * @author Harri Häkkinen
 */
class MalformedTemplateJsonException : Exception {
    /**
     * Constructor
     *
     * @param message message
     * @param original original exception
     */
    constructor(message: String?, original: Throwable?) : super(message, original)

    /**
     * Constructor
     *
     * @param message message
     */
    constructor(message: String?) : super(message)

    companion object {
        private const val serialVersionUID: Long = 3261411570812429886L
    }
}