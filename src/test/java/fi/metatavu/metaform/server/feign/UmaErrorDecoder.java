package fi.metatavu.metaform.server.feign;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.entity.EntityBuilder;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import feign.Response;
import feign.RetryableException;
import feign.codec.ErrorDecoder.Default;
import fi.metatavu.metaform.ApiClient;

/**
 * Feign error decoder that handles UMA token switching
 * 
 * @author Antti Leppä
 */
public class UmaErrorDecoder extends Default {
  
  private String authorization;
  private ApiClient apiClient;
  
  /**
   * Constructor
   * 
   * @param authorization original authorization string
   * @param apiClient api client
   */
  public UmaErrorDecoder(String authorization, ApiClient apiClient) {
    super();
    this.authorization = authorization;
    this.apiClient = apiClient;
  }

  @Override
  public Exception decode(String methodKey, Response response) {
    if (response.status() == 401) {      
      Map<String, String> umaTicket = getUmaTicket(response);
      if (umaTicket != null) {
        try {
          String rpt = getRPT(authorization, umaTicket);
          if (rpt != null) {
            apiClient.setApiKey(String.format("Bearer %s", rpt));
            return new RetryableException("UMA", null);
          } else {
            return new RptForbiddenFeignException("No RPT Token");
          }
          
        } catch (UnsupportedOperationException | IOException e) {
          return e;
        }
      } else {
        return new RptForbiddenFeignException("No UMA Ticket");
      }
    }
    
    return super.decode(methodKey, response);
  }
  
  /**
   * Returns UMA ticket from www-authenticate header or null if not found
   * 
   * @param response response
   * @returns {Object} UMA ticket components
   */
  private Map<String, String> getUmaTicket(Response response) {
    Collection<String> headerValues = response.headers().get("www-authenticate");
    if (!headerValues.isEmpty()) {
      String authenticate = headerValues.iterator().next();
      if (StringUtils.startsWith(authenticate, "UMA ")) {
        return Arrays.stream(StringUtils.split(StringUtils.stripStart(authenticate, "UMA "), ','))
          .collect(Collectors.toMap((component) -> {
            return StringUtils.strip(StringUtils.substringBefore(component, "="), "\"");
          }, (component) -> {
            return StringUtils.strip(StringUtils.substringAfter(component, "="), "\"");
          }));
      }
    }
    
    return null;
  }
  
  /**
   * Retrieves RPT token
   * 
   * @param authorization authorization
   * @param ticket UMA ticket
   * @return token token or null if token creation has fails
   * @throws IOException when io error fails
   */
  private String getRPT(String authorization, Map<String, String> ticket) throws IOException {
    String url = String.format("%s/protocol/openid-connect/token", ticket.get("as_uri"));
    
    try (CloseableHttpClient client = HttpClientBuilder.create().build()) {
      HttpPost post = new HttpPost(url);
      EntityBuilder entityBuilder = EntityBuilder.create();
      
      entityBuilder.setParameters(
        new BasicNameValuePair("grant_type", "urn:ietf:params:oauth:grant-type:uma-ticket"),
        new BasicNameValuePair("ticket", ticket.get("ticket")),
        new BasicNameValuePair("submit_request", "false")
      );
      
      post.setHeader("Authorization", authorization);
      post.setHeader("Content-Type", "application/x-www-form-urlencoded");
      
      post.setEntity(entityBuilder.build());
      HttpResponse response = client.execute(post);
      HttpEntity httpEntity = response.getEntity();

      try (InputStream body = httpEntity.getContent()) {
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> token = objectMapper.readValue(body, new TypeReference<Map<String, Object>>() { });
        return (String) token.get("access_token");
      }
    }
  }
  
}
