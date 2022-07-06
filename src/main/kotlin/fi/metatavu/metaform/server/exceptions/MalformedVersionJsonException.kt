package fi.metatavu.metaform.server.exceptions

/**
 * Exception throw when malformed version data json
 *
 * @author Tianxing Wu
 */
class MalformedVersionJsonException : Exception {
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
        private const val serialVersionUID = 6760138346241227829L
    }
}