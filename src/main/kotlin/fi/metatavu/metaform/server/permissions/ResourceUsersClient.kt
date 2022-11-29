package fi.metatavu.metaform.server.permissions

import fi.metatavu.metaform.keycloak.client.infrastructure.ApiClient
import fi.metatavu.metaform.keycloak.client.infrastructure.ApiResponse
import fi.metatavu.metaform.keycloak.client.infrastructure.RequestConfig
import fi.metatavu.metaform.keycloak.client.infrastructure.RequestMethod
import fi.metatavu.metaform.keycloak.client.models.UserRepresentation

/**
 * Resource users client
 *
 * @param basePath Keycloak base path
 * @param resourceClientId resource server id
 * @param keycloakAdminRealm Keycloak admin realm
 */
class ResourceUsersClient(basePath: String, private val keycloakAdminRealm: String, private val resourceClientId: String) : ApiClient(basePath) {

    /**
     * Lists users with access to specific resource in keycloak
     *
     * @param accessToken access token
     * @param resourceId Id of resource to check user access to
     * @param scope require users to have given scope to the resource defined in resourceId parameter
     * @param search String to search the user for
     * @param first first result
     * @param max last result
     *
     * @return list of users
     */
    fun listResourceUsers(
        accessToken: String,
        resourceId: String,
        scope: String?,
        search: String?,
        first: Int?,
        max: Int?
    ): ApiResponse<List<UserRepresentation>?> {

        val queryParameters = mutableMapOf<String, List<String>>()
        search?.let { queryParameters.put("search", listOf(search)) }
        first?.let { queryParameters.put("first", listOf(first.toString())) }
        max?.let { queryParameters.put("max", listOf(max.toString())) }
        scope?.let { queryParameters.put("scopes", listOf(scope)) }

        val requestConfig = RequestConfig<List<UserRepresentation>>(
            method = RequestMethod.GET,
            path = "/realms/$keycloakAdminRealm/authz-resource-users/clients/$resourceClientId/resource/$resourceId/users",
            headers = mutableMapOf(
                Pair("Authorization", "Bearer ${accessToken}")
            ),
            query = queryParameters
        )

        return request(requestConfig)
    }


}