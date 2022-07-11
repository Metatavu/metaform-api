package fi.metatavu.metaform.server.exceptions

/**
 * Exception throw when draft data deserialization failed
 *
 * @author Tianxing Wu
 */
class DeserializationFailedException : Exception {
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
        private const val serialVersionUID: Long = -1552801513250013947L
    }
}