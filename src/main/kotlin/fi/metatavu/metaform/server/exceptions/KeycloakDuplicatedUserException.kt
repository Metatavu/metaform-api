package fi.metatavu.metaform.server.exceptions

/**
 * Exception throw when Keycloak throw duplicated user exception
 *
 * @author Tianxing Wu
 */
class KeycloakDuplicatedUserException: Exception {
    constructor(message: String?, original: Throwable?) : super(message, original)

    constructor(message: String?) : super(message)

    companion object {
        private const val serialVersionUID: Long = 5688223862861835919L
    }
}