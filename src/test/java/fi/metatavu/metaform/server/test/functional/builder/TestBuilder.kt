package fi.metatavu.metaform.server.test.functional.builder

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import fi.metatavu.jaxrs.test.functional.builder.AbstractAccessTokenTestBuilder
import fi.metatavu.jaxrs.test.functional.builder.AbstractTestBuilder
import fi.metatavu.jaxrs.test.functional.builder.auth.AccessTokenProvider
import fi.metatavu.jaxrs.test.functional.builder.auth.AuthorizedTestBuilderAuthentication
import fi.metatavu.jaxrs.test.functional.builder.auth.KeycloakAccessTokenProvider
import fi.metatavu.metaform.api.client.infrastructure.ApiClient
import fi.metatavu.metaform.api.client.models.MetaformMember
import fi.metatavu.metaform.api.client.models.MetaformMemberRole
import fi.metatavu.metaform.server.keycloak.NotNullResteasyJackson2Provider
import fi.metatavu.metaform.server.test.functional.builder.auth.TestBuilderAuthentication
import fi.metatavu.metaform.server.test.functional.builder.resources.KeycloakResource
import io.restassured.RestAssured
import org.apache.commons.codec.binary.Base64
import org.eclipse.microprofile.config.ConfigProvider
import org.jboss.resteasy.client.jaxrs.ResteasyClient
import org.junit.Assert
import org.keycloak.OAuth2Constants
import org.keycloak.admin.client.ClientBuilderWrapper
import org.keycloak.admin.client.KeycloakBuilder
import org.keycloak.representations.idm.CredentialRepresentation
import org.slf4j.LoggerFactory
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.util.UUID

/**
 * Test builder class
 *
 * @author Antti Leppä
 */
class TestBuilder : AbstractAccessTokenTestBuilder<ApiClient>() {
    private val logger = LoggerFactory.getLogger(TestBuilder::class.java)
    private var anonymousTokenCached: TestBuilderAuthentication? = null
    private val serverUrl = ConfigProvider.getConfig().getValue("metaforms.keycloak.admin.host", String::class.java)
    var metaformAdmin = createTestBuilderAuthentication("metaform-admin", "test")
    var metaformSuper = createTestBuilderAuthentication("metaform-super", "test")
    var test1 = createTestBuilderAuthentication("test1.realm1", "test")
    var test2 = createTestBuilderAuthentication("test2.realm1", "test")
    var test3 = createTestBuilderAuthentication("test3.realm1", "test")
    var answerer1 = createTestBuilderAuthentication("answerer-1", "test")
    var answerer2 = createTestBuilderAuthentication("answerer-2", "test")
    var anon = createTestBuilderAuthentication("anonymous", "anonymous")

    /**
     * Returns anonymous token auth
     *
     * @return anonymous user instance of test builder authentication
     * @throws IOException thrown on communication errors
     */
    val anonymousToken: TestBuilderAuthentication
        @Throws(IOException::class)
        get () {
            if (anonymousTokenCached != null) {
                return anonymousTokenCached!!
            }

            val path = String.format("/realms/%s/protocol/openid-connect/token", REALM_1)
            val password = String.format("%s:%s", DEFAULT_UI_CLIENT_ID, DEFAULT_UI_CLIENT_SECRET)
            val passwordEncoded = Base64.encodeBase64String(password.toByteArray(StandardCharsets.UTF_8))
            val authorization = String.format("Basic %s", passwordEncoded)
            val response = RestAssured.given()
                    .baseUri(serverUrl)
                    .header("Authorization", authorization)
                    .formParam("grant_type", "client_credentials")
                    .post(path)
                    .body
                    .asString()
            val objectMapper = ObjectMapper()
            val responseMap: Map<String, Any> = objectMapper.readValue(response, object : TypeReference<Map<String, Any>>() {})
            val token = responseMap["access_token"] as String
            Assert.assertNotNull(token)
            return TestBuilderAuthentication(this, FilledAccessTokenProvider(token)).also { anonymousTokenCached = it }
        }

    override fun createTestBuilderAuthentication(
            abstractTestBuilder: AbstractTestBuilder<ApiClient, AccessTokenProvider>,
            accessTokenProvider: AccessTokenProvider
    ): AuthorizedTestBuilderAuthentication<ApiClient, AccessTokenProvider> {
        return TestBuilderAuthentication(this, accessTokenProvider)
    }

    /**
     * Resets metaform member password
     *
     * @param memberId member id
     * @param newPassword new password
     */
    fun resetMetaformMemberPassword(memberId: UUID, newPassword: String) {
        val clientId = ConfigProvider.getConfig().getValue("metaforms.keycloak.admin.admin_client_id", String::class.java)
        val clientSecret = ConfigProvider.getConfig().getValue("metaforms.keycloak.admin.secret", String::class.java)
        val clientBuilder = ClientBuilderWrapper.create(null, false)
        clientBuilder.register(NotNullResteasyJackson2Provider(), 100)
        val accessTokenProvider = KeycloakAccessTokenProvider(
            serverUrl,
            "master",
            "admin-cli",
            KeycloakResource.serverAdminUser,
            KeycloakResource.serverAdminPass,
            null
        )

        val client = KeycloakBuilder.builder()
            .serverUrl(serverUrl)
            .realm(REALM_1)
            .grantType(OAuth2Constants.PASSWORD)
            .clientId(clientId)
            .clientSecret(clientSecret)
            .username(KeycloakResource.serverAdminUser)
            .password(KeycloakResource.serverAdminPass)
            .resteasyClient(clientBuilder.build() as ResteasyClient)
            .authorization(String.format("Bearer %s", accessTokenProvider.accessToken))
            .build()

        client.realm(REALM_1).users().get(memberId.toString()).resetPassword(CredentialRepresentation().apply {
            type = "password"
            isTemporary = false
            value = newPassword
        })
    }

    /**
     * Creates metaform admin authentication
     *
     * @param metaformId metaform id
     * @return metaform admin test builder authentication
     */
    fun createMetaformAdminAuthentication(metaformId: UUID): TestBuilderAuthentication {
        val metaformMember = metaformAdmin.metaformMembers.create(
            metaformId,
            MetaformMember(
                email = "tommi@example.com",
                firstName = "tommi",
                lastName = "tommi",
                role = MetaformMemberRole.aDMINISTRATOR
            )
        )
        val metaformAdminPass = UUID.randomUUID().toString()
        resetMetaformMemberPassword(metaformMember.id!!, metaformAdminPass)

        return createTestBuilderAuthentication(metaformMember.firstName, metaformAdminPass)
    }

    /**
     * Creates metaform manager authentication
     *
     * @param metaformId metaform id
     * @return metaform manager test builder authentication
     */
    fun createMetaformManagerAuthentication(metaformId: UUID): TestBuilderAuthentication {
        val metaformMember = metaformAdmin.metaformMembers.create(
            metaformId,
            MetaformMember(
                email = "manager@example.com",
                firstName = "manager",
                lastName = "manager",
                role = MetaformMemberRole.mANAGER
            )
        )
        val metaformAdminPass = UUID.randomUUID().toString()
        resetMetaformMemberPassword(metaformMember.id!!, metaformAdminPass)

        return createTestBuilderAuthentication(metaformMember.firstName, metaformAdminPass)
    }

    /**
     * Creates test builder authentication
     *
     * @param username username
     * @param password password
     * @return created test builder authentication
     */
    fun createTestBuilderAuthentication(username: String, password: String): TestBuilderAuthentication {
        return try {
            val accessTokenProvider = KeycloakAccessTokenProvider(serverUrl, REALM_1, DEFAULT_UI_CLIENT_ID, username, password, DEFAULT_UI_CLIENT_SECRET)
            TestBuilderAuthentication(this, accessTokenProvider)
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
    }

    companion object {
        private const val DEFAULT_UI_CLIENT_SECRET = "22614bd2-6a85-441c-857d-7606f4359e5b"
        protected const val DEFAULT_UI_CLIENT_ID = "ui"
        protected const val REALM_1 = "test-1"
    }
}