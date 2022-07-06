package fi.metatavu.metaform.server.exceptions

/**
 * Exception throw when XLSX generation fails
 *
 * @author Antti Leppä
 */
class XlsxException : Exception {
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
        private const val serialVersionUID = 8611634313163534478L
    }
}