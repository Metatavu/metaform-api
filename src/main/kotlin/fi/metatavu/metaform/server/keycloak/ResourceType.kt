package fi.metatavu.metaform.server.keycloak

/**
 * Enumeration for authorization resource types
 *
 * @author Antti Leppä
 */
enum class ResourceType(val urn: String) {
    /**
     * Authorization resource for a reply
     */
    REPLY("urn:metaform:resources:reply");

}