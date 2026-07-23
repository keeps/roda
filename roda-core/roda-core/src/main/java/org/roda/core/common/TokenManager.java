/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.common;

import java.io.IOException;
import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.Date;

import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.AuthenticationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.utils.JsonUtils;
import org.roda.core.data.v2.accessToken.AccessToken;
import org.roda.core.data.v2.synchronization.local.LocalInstance;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class TokenManager {
  private static TokenManager instance;
  private AccessToken currentToken;
  private Date expirationTime;

  private TokenManager() {
    // do nothing
  }

  public static TokenManager getInstance() {
    if (instance == null) {
      instance = new TokenManager();
    }
    return instance;
  }

  public AccessToken getAccessToken(LocalInstance localInstance)
    throws AuthenticationDeniedException, GenericException {
    try {
      if (currentToken != null) {
        if (!tokenExpired()) {
          return currentToken;
        }
      }
      currentToken = grantToken(localInstance);
      setExpirationTime();
      return currentToken;
    } catch (RODAException e) {
      currentToken = null;
      throw e;
    }
  }

  public AccessToken grantToken(LocalInstance localInstance) throws GenericException, AuthenticationDeniedException {
    validateCentralInstanceUrl(localInstance.getCentralInstanceURL());

    CloseableHttpClient httpClient = HttpClientBuilder.create().build();
    String url = localInstance.getCentralInstanceURL() + RodaConstants.API_SEP + RodaConstants.API_REST_V2_MEMBERS
      + RodaConstants.API_PATH_PARAM_AUTH_TOKEN;
    HttpPost httpPost = new HttpPost(url);
    httpPost.addHeader("Authorization", "Bearer " + localInstance.getAccessKey());
    httpPost.addHeader("content-type", "application/json");

    try {
      httpPost.setEntity(new StringEntity(localInstance.getAccessKey()));
      ClassicHttpResponse response = httpClient.execute(httpPost);
      HttpEntity responseEntity = response.getEntity();
      int responseStatusCode = response.getCode();

      if (responseStatusCode == 200) {
        return JsonUtils.getObjectFromJson(responseEntity.getContent(), AccessToken.class);
      } else if (responseStatusCode == 401) {
        throw new AuthenticationDeniedException("Cannot authenticate on central instance with current configuration");
      } else {
        throw new GenericException("url: " + url + ", response code; " + responseStatusCode);
      }
    } catch (IOException e) {
      throw new GenericException("Error sending POST request", e);
    }
  }

  /**
   * Guards against Server-Side Request Forgery: the central instance URL comes straight from an
   * HTTP request body (local instance registration/configuration), and this method sends the
   * instance's bearer access key to it, so an unvalidated URL would let a caller make this server
   * POST a live credential to an arbitrary host (e.g. an internal service or a cloud metadata
   * endpoint).
   */
  private static void validateCentralInstanceUrl(String centralInstanceUrl) throws GenericException {
    URI uri;
    try {
      uri = new URI(centralInstanceUrl);
    } catch (URISyntaxException | NullPointerException e) {
      throw new GenericException("Invalid central instance URL: " + centralInstanceUrl);
    }

    String scheme = uri.getScheme();
    if (scheme == null || !(scheme.equalsIgnoreCase("http") || scheme.equalsIgnoreCase("https"))) {
      throw new GenericException("Invalid central instance URL scheme: " + centralInstanceUrl);
    }

    String host = uri.getHost();
    if (host == null) {
      throw new GenericException("Invalid central instance URL: " + centralInstanceUrl);
    }

    try {
      InetAddress address = InetAddress.getByName(host);
      if (address.isLoopbackAddress() || address.isLinkLocalAddress() || address.isSiteLocalAddress()
        || address.isAnyLocalAddress() || address.isMulticastAddress()) {
        throw new GenericException("Central instance URL resolves to a disallowed address: " + centralInstanceUrl);
      }
    } catch (UnknownHostException e) {
      throw new GenericException("Could not resolve central instance URL host: " + centralInstanceUrl, e);
    }
  }

  private void setExpirationTime() {
    long today = new Date().getTime();
    expirationTime = new Date(today + currentToken.getExpiresIn());
  }

  private boolean tokenExpired() {
    return new Date().after(expirationTime);
  }

  public void removeToken() {
    this.currentToken = null;
  }
}
