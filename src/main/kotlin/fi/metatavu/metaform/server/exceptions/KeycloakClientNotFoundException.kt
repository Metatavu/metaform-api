package fi.metatavu.metaform.server.exceptions

/**
 * Exception throw when Keycloak client not found
 *
 * @author Tianxing Wu
 */
class KeycloakClientNotFoundException: Exception {
    constructor(message: String?, original: Throwable?) : super(message, original)

    constructor(message: String?) : super(message)

    companion object {
        private const val serialVersionUID: Long = 7618561225948636260L
    }
}