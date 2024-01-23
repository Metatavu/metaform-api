package fi.metatavu.metaform.server.exceptions

/**
 * Exception throw when PDF generation fails
 *
 * @author Antti Leppä
 */
class PdfRenderException: Exception {
    constructor(message: String?, original: Throwable?) : super(message, original)

    constructor(message: String?) : super(message)

    companion object {
        private const val serialVersionUID = -835234745158422224L
    }
}