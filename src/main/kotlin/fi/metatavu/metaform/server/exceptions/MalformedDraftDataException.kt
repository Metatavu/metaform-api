package fi.metatavu.metaform.server.exceptions

/**
 * Exception throw when malformed draft data json
 *
 * @author Tianxing Wu
 */
class MalformedDraftDataException : Exception {
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
        private const val serialVersionUID: Long = 7366608503238267241L
    }
}