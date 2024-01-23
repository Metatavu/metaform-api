package fi.metatavu.metaform.server.exceptions

/**
 * Exception throw when Keycloak throws exception
 *
 * @author Tianxing Wu
 */
class KeycloakException: Exception {
    constructor(message: String?, original: Throwable?) : super(message, original)

    constructor(message: String?) : super(message)

    companion object {
        private const val serialVersionUID: Long = 2132829034683922687L
    }
}