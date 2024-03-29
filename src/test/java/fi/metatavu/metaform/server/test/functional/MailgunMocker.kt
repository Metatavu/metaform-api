package fi.metatavu.metaform.server.test.functional

import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import org.apache.commons.codec.binary.Base64
import org.apache.http.NameValuePair
import org.apache.http.client.utils.URLEncodedUtils
import org.apache.http.message.BasicNameValuePair
import java.util.*

/**
 * Mocker for Mailgun API
 *
 *
 * Inspired by https://github.com/sargue/mailgun/blob/master/src/test/java/net/sargue/mailgun/test/BasicTests.java
 *
 * @author Antti Leppä
 * @author Heikki Kurhinen
 */
class MailgunMocker(private val basePath: String, private val domain: String, apiKey: String?) {
    private val authHeader: String
    private var okStub: StubMapping? = null

    init {
        authHeader = Base64.encodeBase64String(String.format("api:%s", apiKey).toByteArray())
    }

    /**
     * Starts mocking
     */
    fun startMock() {
        okStub = WireMock.stubFor(WireMock.post(WireMock.urlEqualTo(apiUrl))
                .withHeader("Authorization", WireMock.equalTo(String.format("Basic %s", authHeader)))
                .withHeader("Content-Type", WireMock.equalTo("application/x-www-form-urlencoded"))
                .willReturn(WireMock.aResponse().withStatus(200)))
    }

    /**
     * Ends mocking
     */
    fun stopMock() {
        if (okStub != null) {
            WireMock.removeStub(okStub)
            WireMock.reset()
            okStub = null
        }
    }

    /**
     * Verifies that HTML email has been sent
     *
     * @param fromName  from name
     * @param fromEmail from email
     * @param to        to email
     * @param subject   subject
     * @param content   content
     */
    fun verifyHtmlMessageSent(fromName: String, fromEmail: String, to: String, subject: String, content: String) {
        verifyMessageSent(createParameterList(fromName, fromEmail, to, subject, content))
    }

    /**
     * Verifies that HTML email has been sent n-times
     *
     * @param count     count
     * @param fromName  from name
     * @param fromEmail from email
     * @param to        to email
     * @param subject   subject
     * @param content   content
     */
    fun verifyHtmlMessageSent(count: Int, fromName: String, fromEmail: String, to: String, subject: String, content: String) {
        verifyMessageSent(count, createParameterList(fromName, fromEmail, to, subject, content))
    }

    /**
     * Creates parameter list
     *
     * @param fromName  from name
     * @param fromEmail from email
     * @param to        to email
     * @param subject   subject
     * @param content   content
     */
    private fun createParameterList(fromName: String, fromEmail: String, to: String, subject: String, content: String): List<NameValuePair> {
        return listOf<NameValuePair>(
                BasicNameValuePair("to", to),
                BasicNameValuePair("subject", subject),
                BasicNameValuePair("html", content),
                BasicNameValuePair("from", String.format("%s <%s>", fromName, fromEmail))
        )
    }

    /**
     * Verifies that email with parameters has been sent
     *
     * @param parametersList parameters
     */
    private fun verifyMessageSent(parametersList: List<NameValuePair>) {
        val parameters: List<NameValuePair> = ArrayList(parametersList)
        val form = URLEncodedUtils.format(parameters, "UTF-8")
        WireMock.verify(WireMock.postRequestedFor(WireMock.urlEqualTo(apiUrl)).withRequestBody(WireMock.equalTo(form)))
    }

    /**
     * Verifies that email with parameters has been sent n-times
     *
     * @param count count
     * @param parametersList parameters
     */
    private fun verifyMessageSent(count: Int, parametersList: List<NameValuePair>) {
        val parameters: List<NameValuePair> = ArrayList(parametersList)
        val form = URLEncodedUtils.format(parameters, "UTF-8")
        WireMock.verify(count, WireMock.postRequestedFor(WireMock.urlEqualTo(apiUrl)).withRequestBody(WireMock.equalTo(form)))
    }

    /**
     * Returns API URL
     *
     * @return API URL
     */
    private val apiUrl: String
        get() = String.format("%s/%s/messages", basePath, domain)
}