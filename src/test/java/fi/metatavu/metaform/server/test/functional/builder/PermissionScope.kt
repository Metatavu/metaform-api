package fi.metatavu.metaform.server.test.functional.builder

/**
 * Permission scope
 */
enum class PermissionScope(val level: Int) {
    ANONYMOUS(0),
    USER(1),
    METAFORM_MANAGER(2),
    METAFORM_ADMIN(3),
    SYSTEM_ADMIN(4)
}