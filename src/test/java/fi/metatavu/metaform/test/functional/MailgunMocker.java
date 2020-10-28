package fi.metatavu.metaform.test.functional;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.removeStub;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;

import com.github.tomakehurst.wiremock.stubbing.StubMapping;

/**
 * Mocker for Mailgun API
 * 
 * Inspired by https://github.com/sargue/mailgun/blob/master/src/test/java/net/sargue/mailgun/test/BasicTests.java
 * 
 * @author Antti Leppä
 * @author Heikki Kurhinen
 */
public class MailgunMocker {
  
  private String authHeader;
  private String basePath;
  private String domain;
  private StubMapping okStub;
  
  /**
   * Constructor 
   * 
   * @param basePath base path
   * @param domain Mailgun domain
   * @param apiKey Mailgun API key
   */
  public MailgunMocker(String basePath, String domain, String apiKey) {
    this.basePath = basePath;
    this.domain = domain;
    this.authHeader = Base64.encodeBase64String(String.format("api:%s", apiKey).getBytes());
  }
  
  /**
   * Starts mocking
   */
  public void startMock() {
    okStub = stubFor(post(urlEqualTo(getApiUrl()))
      .withHeader("Authorization", equalTo(String.format("Basic %s", authHeader)))
      .withHeader("Content-Type", equalTo("application/x-www-form-urlencoded"))
      .willReturn(aResponse().withStatus(200)));
  }
  
  /**
   * Ends mocking
   */
  public void stopMock() {
    if (okStub != null) {
      removeStub(okStub);
      okStub = null;
    }
  }

  /**
   * Verifies that HTML email has been sent
   * 
   * @param fromName from name
   * @param fromEmail from email
   * @param to to email
   * @param subject subject
   * @param content content
   */
  public void verifyHtmlMessageSent(String fromName, String fromEmail, String to, String subject, String content) {
    verifyMessageSent(createParameterList(fromName, fromEmail, to, subject, content));
  }
  
  /**
   * Verifies that HTML email has been sent n-times
   * 
   * @param count count
   * @param fromName from name
   * @param fromEmail from email
   * @param to to email
   * @param subject subject
   * @param content content
   */
  public void verifyHtmlMessageSent(int count, String fromName, String fromEmail, String to, String subject, String content) {
    verifyMessageSent(count, createParameterList(fromName, fromEmail, to, subject, content));
  }
  
  private List<NameValuePair> createParameterList(String fromName, String fromEmail, String to, String subject, String content) {
    return Arrays.asList(
      new BasicNameValuePair("to", to),
      new BasicNameValuePair("subject", subject),
      new BasicNameValuePair("html", content),
      new BasicNameValuePair("from", String.format("%s <%s>", fromName, fromEmail))
    );
  }
  
  /**
   * Verifies that email with parameters has been sent
   * 
   * @param parametersList parameters
   */
  private void verifyMessageSent(List<NameValuePair> parametersList) {
    List<NameValuePair> parameters = new ArrayList<>(parametersList);
    String form = URLEncodedUtils.format(parameters, "UTF-8");
    verify(postRequestedFor(urlEqualTo(getApiUrl())).withRequestBody(equalTo(form)));
  }
  
  /**
   * Verifies that email with parameters has been sent n-times
   * 
   * @param count
   * @param parametersList parameters
   */
  private void verifyMessageSent(int count, List<NameValuePair> parametersList) {
    List<NameValuePair> parameters = new ArrayList<>(parametersList);
    String form = URLEncodedUtils.format(parameters, "UTF-8");
    verify(count, postRequestedFor(urlEqualTo(getApiUrl())).withRequestBody(equalTo(form)));
  }
  
  /**
   * Returns API URL
   * 
   * @return API URL
   */
  private String getApiUrl() {
    return String.format("%s/%s/messages", basePath, domain);
  }

}
