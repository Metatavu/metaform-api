package fi.metatavu.metaform.server.permissions

import fi.metatavu.metaform.server.keycloak.AuthorizationScope
import java.util.UUID

/**
 * Helper class for handling group member permissions while creating or updating
 * replies
 */
data class GroupMemberPermission (

    val memberGroupId: UUID,
    val scope: AuthorizationScope

) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as GroupMemberPermission

        if (memberGroupId != other.memberGroupId) return false
        if (scope != other.scope) return false

        return true
    }

    override fun hashCode(): Int {
        var result = memberGroupId.hashCode()
        result = 31 * result + scope.hashCode()
        return result
    }

}