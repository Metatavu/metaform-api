package fi.metatavu.metaform.server.keycloak

/**
 * Enumeration for authorization scopes
 *
 * @author Antti Leppä
 */
enum class AuthorizationScope(val scopeName: String) {
    /**
     * Authorization scope for creating a reply
     */
    REPLY_CREATE("reply:create"),

    /**
     * Authorization scope for viewing a reply
     */
    REPLY_VIEW("reply:view"),

    /**
     * Authorization scope for editing a reply
     */
    REPLY_EDIT("reply:edit"),

    /**
     * Authorization scope for receiving a notification about reply
     */
    REPLY_NOTIFY("reply:notify");

}