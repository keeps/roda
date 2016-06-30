/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.util;

import java.io.Serializable;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.roda.core.data.exceptions.RODAException;

import com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider;

public final class RESTClientUtility {

  private RESTClientUtility() {
  }

  public static <T extends Serializable> T sendPostRequest(T element, Class<T> elementClass, String url,
    String resource, String username, String password) throws RODAException {
    Client client = ClientBuilder.newClient(new ClientConfig().register(JacksonJaxbJsonProvider.class));
    HttpAuthenticationFeature authentication = HttpAuthenticationFeature.universal(username, password);
    client.register(authentication);

    WebTarget webTarget = client.target(url).path(resource);
    Response response = webTarget.request(MediaType.APPLICATION_JSON)
      .post(Entity.entity(element, MediaType.APPLICATION_JSON));

    if (response.getStatus() == 201) {
      return response.readEntity(elementClass);
    } else {
      throw new RODAException("POST Request response was " + response.getStatus());
    }
  }

}
