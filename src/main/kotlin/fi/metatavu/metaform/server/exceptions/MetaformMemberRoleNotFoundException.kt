package fi.metatavu.metaform.server.exceptions

/**
 * Exception throw when Metaform member role not found
 *
 * @author Tianxing Wu
 */
class MetaformMemberRoleNotFoundException: Exception {
    constructor(message: String?, original: Throwable?) : super(message, original)

    constructor(message: String?) : super(message)

    companion object {
        private const val serialVersionUID: Long = -8555787746207782170L
    }
}