package fi.metatavu.metaform.server.exceptions

/**
 * Exception throw when file store failed
 *
 * @author Tianxing Wu
 */
class FailStoreFailedException : Exception {
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
        private const val serialVersionUID: Long = 7119462942436105672L
    }
}