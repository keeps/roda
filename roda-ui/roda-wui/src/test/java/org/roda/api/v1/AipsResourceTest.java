/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.api.v1;

import static org.junit.Assert.assertEquals;

import javax.ws.rs.core.Application;

import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.Test;
import org.roda.wui.api.v1.AipsResource;

public class AipsResourceTest extends JerseyTest {

  @Override
  protected Application configure() {
    ResourceConfig resourceConfig = new ResourceConfig();
    resourceConfig.register(AipsResource.class);
    resourceConfig.register(MultiPartFeature.class);
    return resourceConfig;
  }

  @Test
  public void testAipsGet() {
    final String textOutput = target(AipsResource.ENDPOINT).request().get(String.class);
    assertEquals(
      "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><apiResponseMessage><message>magic!</message><type>ok</type></apiResponseMessage>",
      textOutput);
  }
}
