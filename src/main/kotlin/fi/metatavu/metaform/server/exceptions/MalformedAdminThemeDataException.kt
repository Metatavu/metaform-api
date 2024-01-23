package fi.metatavu.metaform.server.exceptions

/**
 * Exception throw when malformed admin theme data json
 *
 * @author Tianxing Wu
 */
class MalformedAdminThemeDataException : Exception {
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
    constructor(message: String?) : super(message) {}

    companion object {
        private const val serialVersionUID: Long = -3156756706830402914L
    }
}