package fi.metatavu.metaform.server.exceptions

/**
 * Exception throw on authorization errors
 *
 * @author Antti Leppä
 */
class AuthzException(message: String? = null, cause: Exception? = null): Exception(message, cause) {

}