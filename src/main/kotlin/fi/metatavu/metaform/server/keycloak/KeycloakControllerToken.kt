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
import org.eclipse.microprofile.config.inject.ConfigProperty
import org.slf4j.Logger
import java.io.IOException
import java.time.OffsetDateTime
import javax.enterprise.context.ApplicationScoped
import javax.inject.Inject

/**
 * Access token for Keycloak admin controller
 */
@ApplicationScoped
class KeycloakControllerToken {

    @Inject
    private lateinit var logger: Logger

    @ConfigProperty(name = "metaforms.keycloak.admin.realm")
    private lateinit var realm: String

    @ConfigProperty(name = "metaforms.keycloak.admin.admin_client_id")
    private lateinit var clientId: String

    @ConfigProperty(name = "metaforms.keycloak.admin.secret")
    private lateinit var clientSecret: String

    @ConfigProperty(name = "metaforms.keycloak.admin.user")
    private lateinit var apiAdminUser: String

    @ConfigProperty(name = "metaforms.keycloak.admin.password")
    private lateinit var apiAdminPassword: String

    @ConfigProperty(name = "metaforms.keycloak.admin.host")
    private lateinit var authServerUrl: String

    private val expireSlack = 60L

    private var accessToken: KeycloakAccessToken? = null

    private var accessTokenExpires: OffsetDateTime? = null

    /**
     * Resolves a admin access token from Keycloak
     *
     * @return access token
     */
    @Lock
    fun getAccessToken(): KeycloakAccessToken? {
        try {
            val now = OffsetDateTime.now()
            val expires = accessTokenExpires?.minusSeconds(expireSlack)

            if ((accessToken == null) || expires == null || expires.isBefore(now)) {
                accessToken = obtainAccessToken()
                if (accessToken == null) {
                    logger.error("Could not obtain access token")
                    return null
                }

                val expiresIn = accessToken?.expiresIn
                if (expiresIn == null) {
                    logger.error("Could not resolve access token expires in")
                    return null
                }

                accessTokenExpires = OffsetDateTime.now().plusSeconds(expiresIn)
            }

            return accessToken
        } catch (e: Exception) {
            logger.error("Failed to retrieve access token", e)
        }

        return null
    }

    /**
     * Obtains fresh admin access token from Keycloak
     *
     * @return Access token if refresh was successfull
     */
    private fun obtainAccessToken(): KeycloakAccessToken? {
        logger.info("Obtaining new admin access token...")

        val uri = "$authServerUrl/realms/$realm/protocol/openid-connect/token"
        try {
            HttpClients.createDefault().use { client ->
                val httpPost = HttpPost(uri)
                val params: MutableList<NameValuePair> = ArrayList()
                params.add(BasicNameValuePair("client_id", clientId))
                params.add(BasicNameValuePair("grant_type", "password"))
                params.add(BasicNameValuePair("username", apiAdminUser))
                params.add(BasicNameValuePair("password", apiAdminPassword))
                params.add(BasicNameValuePair("client_secret", clientSecret))
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