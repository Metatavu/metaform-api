package fi.metatavu.metaform.server.test.functional.builder

import fi.metatavu.jaxrs.test.functional.builder.auth.AccessTokenProvider

/**
 * Access token provider with prefilled token
 */
class FilledAccessTokenProvider(private val token: String) : AccessTokenProvider {
    override fun getAccessToken(): String {
        return token
    }
}