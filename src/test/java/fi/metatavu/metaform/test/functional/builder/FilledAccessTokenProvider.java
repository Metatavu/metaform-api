package fi.metatavu.metaform.test.functional.builder;

import fi.metatavu.jaxrs.test.functional.builder.auth.AccessTokenProvider;

import java.io.IOException;

/**
 * Access token provider with prefilled token
 */
public class FilledAccessTokenProvider implements AccessTokenProvider {

  private final String token;

  public FilledAccessTokenProvider(String token) {
    this.token = token;
  }

  @Override
  public String getAccessToken() {
    return token;
  }
}
