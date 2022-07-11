package fi.metatavu.metaform.server.exceptions

/**
 * Exception throw when Resource not found
 *
 * @author Tianxing Wu
 */
class ResourceNotFoundException: Exception {
    constructor(message: String?, original: Throwable?) : super(message, original)

    constructor(message: String?) : super(message)

    companion object {
        private const val serialVersionUID: Long = 162982387185510739L
    }
}