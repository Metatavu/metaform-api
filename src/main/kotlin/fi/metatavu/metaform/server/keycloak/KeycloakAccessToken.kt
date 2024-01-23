package fi.metatavu.metaform.server.keycloak

import com.fasterxml.jackson.annotation.JsonProperty
import io.quarkus.runtime.annotations.RegisterForReflection

/**
 * Jackson data class for Keycloak access tokens
 *
 * @property accessToken access token field value
 * @property expiresIn expires in field value
 * @property refreshExpiresIn refresh expires in field value
 * @property refreshToken refresh token field value
 * @property tokenType token type field value
 * @property notBeforePolicy not before policy field value
 * @property sessionState session state field value
 * @property scope scope field value
 */
@RegisterForReflection
@Suppress ("UNUSED")
class KeycloakAccessToken {

    @JsonProperty("access_token")
    private var accessToken: String? = null

    @JsonProperty("expires_in")
    private var expiresIn: Long? = null

    @JsonProperty("refresh_expires_in")
    private var refreshExpiresIn: Long? = null

    @JsonProperty("refresh_token")
    private var refreshToken: String? = null

    @JsonProperty("token_type")
    private var tokenType: String? = null

    @JsonProperty("not-before-policy")
    private var notBeforePolicy: Long? = null

    @JsonProperty("session_state")
    private var sessionState: String? = null

    @JsonProperty("scope")
    private var scope: String? = null

    fun getAccessToken(): String? {
        return accessToken
    }

    fun setAccessToken(accessToken: String) {
        this.accessToken = accessToken
    }

    fun getExpiresIn(): Long? {
        return this.expiresIn
    }

    fun setExpiresIn(expiresIn: Long) {
        this.expiresIn = expiresIn
    }

    fun getRefreshExpiresIn(): Long? {
        return this.refreshExpiresIn
    }

    fun setRefreshExpiresIn(refreshExpiresIn: Long?) {
        this.refreshExpiresIn = refreshExpiresIn
    }

    fun getRefreshToken(): String? {
        return refreshToken
    }

    fun setRefreshToken(refreshToken: String?) {
        this.refreshToken = refreshToken
    }

    fun getTokenType(): String? {
        return this.tokenType
    }

    fun setTokenType(tokenType: String?) {
        this.tokenType = tokenType
    }

    fun getNotBeforePolicy(): Long? {
        return notBeforePolicy
    }

    fun setNotBeforePolicy(notBeforePolicy: Long?) {
        this.notBeforePolicy = notBeforePolicy
    }

    fun getSessionState(): String? {
        return sessionState
    }

    fun setSessionState(sessionState: String?)  {
        this.sessionState = sessionState
    }

    fun getScope(): String? {
        return this.scope
    }

    fun setScope(scope: String?) {
        this.scope = scope
    }

}