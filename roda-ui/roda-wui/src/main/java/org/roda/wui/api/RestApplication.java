/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.api;

import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.moxy.xml.MoxyXmlFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.roda.core.data.common.RodaConstants;
import org.roda.wui.api.v1.utils.ApiOriginFilter;

import io.swagger.jaxrs.config.BeanConfig;
import io.swagger.jaxrs.listing.ApiListingResource;
import io.swagger.jaxrs.listing.SwaggerSerializers;

public class RestApplication extends ResourceConfig {
  private static final String RODA_API_PACKAGE = "org.roda.wui.api";
  private static final String SWAGGER_PACKAGE = "io.swagger.jaxrs.listing";
  private static final String API_VERSION = "1";

  public RestApplication() {
    super();
    packages(SWAGGER_PACKAGE, RODA_API_PACKAGE);
    register(JacksonFeature.class);
    register(MoxyXmlFeature.class);
    register(MultiPartFeature.class);

    // https://github.com/swagger-api/swagger-core/wiki/Swagger-Core-Jersey-2.X-Project-Setup-1.5
    register(ApiListingResource.class);
    register(SwaggerSerializers.class);
    register(ApiOriginFilter.class);
    BeanConfig beanConfig = new BeanConfig();
    beanConfig.setVersion(API_VERSION);
    beanConfig.setBasePath(RodaConstants.API_BASE_PATH);
    beanConfig.setResourcePackage(RODA_API_PACKAGE);
    beanConfig.setScan(true);
  }
}
