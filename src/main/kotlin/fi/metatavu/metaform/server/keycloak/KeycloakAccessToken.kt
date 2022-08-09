package fi.metatavu.metaform.server.keycloak

import com.fasterxml.jackson.annotation.JsonProperty

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
data class KeycloakAccessToken (

    @JsonProperty("access_token")
    val accessToken: String,

    @JsonProperty("expires_in")
    val expiresIn: Long,

    @JsonProperty("refresh_expires_in")
    val refreshExpiresIn: Long,

    @JsonProperty("refresh_token")
    val refreshToken: String,

    @JsonProperty("token_type")
    val tokenType: String,

    @JsonProperty("not-before-policy")
    val notBeforePolicy: Long,

    @JsonProperty("session_state")
    val sessionState: String,

    @JsonProperty("scope")
    val scope: String

)