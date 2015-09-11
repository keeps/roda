package org.roda.api;

import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.server.ResourceConfig;

import io.swagger.jaxrs.config.BeanConfig;
import io.swagger.jaxrs.listing.ApiListingResource;
import io.swagger.jaxrs.listing.SwaggerSerializers;

public class RestApplication extends ResourceConfig {
  public RestApplication() {
    super();
    packages("io.swagger.jaxrs.listing", "org.roda.api");
    register(MultiPartFeature.class);
    register(JacksonFeature.class);

    // https://github.com/swagger-api/swagger-core/wiki/Java-JAXRS-Quickstart
    register(ApiListingResource.class);
    register(SwaggerSerializers.class);
    BeanConfig beanConfig = new BeanConfig();
    beanConfig.setVersion("1");
    beanConfig.setBasePath("/api");
    beanConfig.setResourcePackage("org.roda.api");
    beanConfig.setScan(true);
  }
}
