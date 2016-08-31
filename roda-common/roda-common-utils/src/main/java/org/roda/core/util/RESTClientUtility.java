/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.util;

import java.io.IOException;
import java.io.Serializable;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.utils.JsonUtils;

/**
 * 20160824 hsilva: I think someone, outside RODA code base, is using this
 * method via maven dependency
 * 
 * @author Hélder Silva <hsilva@keep.pt>
 */
public final class RESTClientUtility {

  /** Private empty constructor */
  private RESTClientUtility() {
  }

  public static <T extends Serializable> T sendPostRequest(T element, Class<T> elementClass, String url,
    String resource, String username, String password) throws RODAException {
    CloseableHttpClient httpClient = HttpClientBuilder.create().build();
    String basicAuthToken = new String(Base64.encode((username + ":" + password).getBytes()));
    HttpPost httpPost = new HttpPost(url + resource);
    httpPost.setHeader("Authorization", "Basic " + basicAuthToken);
    httpPost.addHeader("content-type", "application/json");
    httpPost.addHeader("Accept", "application/json");

    try {
      httpPost.setEntity(new StringEntity(JsonUtils.getJsonFromObject(element)));
      HttpResponse response;
      response = httpClient.execute(httpPost);
      HttpEntity responseEntity = response.getEntity();

      int responseStatusCode = response.getStatusLine().getStatusCode();
      if (responseStatusCode == 201) {
        return JsonUtils.getObjectFromJson(responseEntity.getContent(), elementClass);
      } else {
        throw new RODAException("POST request response status code: " + responseStatusCode);
      }
    } catch (IOException e) {
      throw new RODAException("Error sending POST request", e);
    }
  }

}
