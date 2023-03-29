package fi.metatavu.metaform.server.keycloak

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import io.quarkus.arc.Lock
import org.apache.commons.io.IOUtils
import org.apache.http.NameValuePair
import org.apache.http.client.entity.UrlEncodedFormEntity
import org.apache.http.client.methods.HttpPost
import org.apache.http.impl.client.HttpClients
import org.apache.http.message.BasicNameValuePair
import org.slf4j.Logger
import java.io.IOException
import java.time.OffsetDateTime
import java.util.EnumMap
import javax.annotation.PostConstruct
import javax.enterprise.context.ApplicationScoped
import javax.inject.Inject

/**
 * Access token for Keycloak admin controller
 */
@ApplicationScoped
class KeycloakControllerToken {

    @Inject
    lateinit var logger: Logger

    private lateinit var accessTokens: EnumMap<KeycloakSource, KeycloakAccessToken?>

    private lateinit var accessTokenExpires: EnumMap<KeycloakSource, OffsetDateTime?>

    private val expireSlack = 60L

    /**
     * Post construct method for initializing accessTokens & accessTokenExpires EnumMaps
     */
    @Suppress("unused")
    @PostConstruct
    fun init() {
        accessTokens = EnumMap<KeycloakSource, KeycloakAccessToken?>(KeycloakSource::class.java)
        accessTokenExpires = EnumMap<KeycloakSource, OffsetDateTime?>(KeycloakSource::class.java)
    }

    /**
     * Resolves an admin access token from Keycloak
     *
     * @param keycloakConfiguration KeycloakConfiguration
     * @return access token
     */
    @Lock
    fun getAccessToken(keycloakConfiguration: KeycloakConfiguration, keycloakSource: KeycloakSource): KeycloakAccessToken? {
        try {
            val now = OffsetDateTime.now()
            val expires = accessTokenExpires[keycloakSource]?.minusSeconds(expireSlack)

            if ((accessTokens[keycloakSource] == null) || expires == null || expires.isBefore(now)) {
                accessTokens[keycloakSource] = obtainAccessToken(keycloakConfiguration)
                if (accessTokens[keycloakSource] == null) {
                    logger.error("Could not obtain access token")
                    return null
                }

                val expiresIn = accessTokens[keycloakSource]?.expiresIn
                if (expiresIn == null) {
                    logger.error("Could not resolve access token expires in")
                    return null
                }

                accessTokenExpires[keycloakSource] = OffsetDateTime.now().plusSeconds(expiresIn)
            }

            return accessTokens[keycloakSource]
        } catch (e: Exception) {
            logger.error("Failed to retrieve access token", e)
        }

        return null
    }

    /**
     * Obtains fresh admin access token from Keycloak
     *
     * @param keycloakConfiguration KeycloakConfiguration
     * @return Access token if refresh was successful
     */
    private fun obtainAccessToken(keycloakConfiguration: KeycloakConfiguration): KeycloakAccessToken? {
        logger.info("Obtaining new admin access token...")

        val uri = "${keycloakConfiguration.authServerUrl}/realms/${keycloakConfiguration.realm}/protocol/openid-connect/token"
        try {
            HttpClients.createDefault().use { client ->
                val httpPost = HttpPost(uri)
                val params: MutableList<NameValuePair> = ArrayList()
                params.add(BasicNameValuePair("client_id", keycloakConfiguration.clientId))
                params.add(BasicNameValuePair("grant_type", "password"))
                params.add(BasicNameValuePair("username", keycloakConfiguration.apiAdminUser))
                params.add(BasicNameValuePair("password", keycloakConfiguration.apiAdminPassword))
                params.add(BasicNameValuePair("client_secret", keycloakConfiguration.clientSecret))
                httpPost.entity = UrlEncodedFormEntity(params)
                client.execute(httpPost).use { response ->
                    if (response.statusLine.statusCode != 200) {
                        logger.error("Failed obtain access token: {}", IOUtils.toString(response.entity.content, "UTF-8"))
                        return null
                    }

                    response.entity.content.use { inputStream ->
                        val objectMapper = ObjectMapper()
                        objectMapper.registerModule(JavaTimeModule())
                        objectMapper.registerModule(KotlinModule.Builder().build())
                        return objectMapper.readValue(inputStream, KeycloakAccessToken::class.java)
                    }
                }
            }
        } catch (e: IOException) {
            logger.debug("Failed to retrieve access token", e)
        }

        return null
    }

}