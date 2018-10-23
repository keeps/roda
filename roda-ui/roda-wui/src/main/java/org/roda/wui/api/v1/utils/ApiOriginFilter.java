/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.api.v1.utils;

import java.io.IOException;
import java.util.List;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.container.PreMatching;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;

import org.roda.core.RodaCoreFactory;
import org.roda.core.data.common.RodaConstants;

/**
 * Request filter that handles CORS, by manipulating responses to include the
 * appropriate CORS headers. It also finishes the response when the request is a
 * pre-flight.
 *
 * Adapted from https://stackoverflow.com/a/28067653/1483200 and from
 * https://github.com/resteasy/Resteasy/blob/master/resteasy-core/src/main/java/org/jboss/resteasy/plugins/interceptors/CorsFilter.java
 */
@Provider
@PreMatching
public class ApiOriginFilter implements ContainerRequestFilter, ContainerResponseFilter {
  // response headers
  private static final String ACCESS_CONTROL_ALLOW_ORIGIN = "Access-Control-Allow-Origin";
  private static final String ACCESS_CONTROL_ALLOW_CREDENTIALS = "Access-Control-Allow-Credentials";
  private static final String ACCESS_CONTROL_ALLOW_METHODS = "Access-Control-Allow-Methods";
  private static final String ACCESS_CONTROL_ALLOW_HEADERS = "Access-Control-Allow-Headers";
  private static final String ACCESS_CONTROL_EXPOSE_HEADERS = "Access-Control-Expose-Headers";
  private static final String ACCESS_CONTROL_MAX_AGE = "Access-Control-Max-Age";
  private static final String CACHE_VARY = "Vary";

  // request headers
  private static final String ORIGIN = "Origin";
  private static final String ACCESS_CONTROL_REQUEST_METHOD = "Access-Control-Request-Method";
  private static final String ACCESS_CONTROL_REQUEST_HEADERS = "Access-Control-Request-Headers";

  // control property
  private static final String CORS_FAILURE = "cors.failure";

  // defaults
  private static final String ACCESS_CONTROL_ALLOW_CREDENTIALS_DEFAULT = "true";
  private static final String ACCESS_CONTROL_ALLOW_METHODS_DEFAULT = "GET, POST, PUT, DELETE, OPTIONS, HEAD";
  private static final String ACCESS_CONTROL_ALLOW_HEADERS_DEFAULT = "Content-Type";
  private static final String ACCESS_CONTROL_EXPOSE_HEADERS_DEFAULT = null;
  private static final int ACCESS_CONTROL_MAX_AGE_DEFAULT = 3600;

  @Override
  public void filter(ContainerRequestContext requestContext) throws IOException {
    String origin = requestContext.getHeaderString(ORIGIN);
    if (origin != null) {
      handleOrigin(requestContext, origin);
      if (HttpMethod.OPTIONS.equalsIgnoreCase(requestContext.getMethod())) {
        handlePreFlight(requestContext, origin);
      }
    }
  }

  @Override
  public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext)
    throws IOException {
    String origin = requestContext.getHeaderString(ORIGIN);
    if (origin != null && !HttpMethod.OPTIONS.equalsIgnoreCase(requestContext.getMethod())
      && requestContext.getProperty(CORS_FAILURE) == null) {
      responseContext.getHeaders().putSingle(ACCESS_CONTROL_ALLOW_ORIGIN, origin);
      responseContext.getHeaders().putSingle(CACHE_VARY, ORIGIN);
      responseContext.getHeaders().putSingle(ACCESS_CONTROL_ALLOW_CREDENTIALS, getAllowCredentials());
      responseContext.getHeaders().putSingle(ACCESS_CONTROL_EXPOSE_HEADERS, getExposedHeaders());
    }
    // don't do anything if origin is null, its an OPTIONS request, or cors.failure
    // is set
  }

  private void handleOrigin(ContainerRequestContext requestContext, String origin) {
    List<String> allowedOrigins = RodaCoreFactory.getRodaConfigurationAsList(RodaConstants.CORS_ALLOW_ORIGIN);
    if (!allowedOrigins.contains("*") && !allowedOrigins.contains(origin)) {
      requestContext.setProperty(CORS_FAILURE, true);
    }
  }

  private void handlePreFlight(ContainerRequestContext requestContext, String origin) {
    if (requestContext.getProperty(CORS_FAILURE) == null) {
      Response.ResponseBuilder builder = Response.ok();
      builder.header(ACCESS_CONTROL_ALLOW_ORIGIN, origin);
      builder.header(CACHE_VARY, ORIGIN);
      builder.header(ACCESS_CONTROL_ALLOW_CREDENTIALS, getAllowCredentials());
      builder.header(ACCESS_CONTROL_ALLOW_METHODS, getAllowMethods(requestContext));
      builder.header(ACCESS_CONTROL_ALLOW_HEADERS, getAllowHeaders(requestContext));

      int corsMaxAge = getMaxAge();
      if (corsMaxAge > -1) {
        builder.header(ACCESS_CONTROL_MAX_AGE, corsMaxAge);
      }

      requestContext.abortWith(builder.build());
    }
  }

  private String getAllowHeaders(ContainerRequestContext requestContext) {
    String allowedHeaders = null;
    String allowHeaders = requestContext.getHeaderString(ACCESS_CONTROL_REQUEST_HEADERS);
    if (allowHeaders != null) {
      allowedHeaders = RodaCoreFactory.getRodaConfigurationAsString(RodaConstants.CORS_ALLOW_HEADERS);
      if (allowedHeaders == null) {
        allowedHeaders = ACCESS_CONTROL_ALLOW_HEADERS_DEFAULT;
      }
    }
    return allowedHeaders;
  }

  private String getAllowMethods(ContainerRequestContext requestContext) {
    String allowedMethods = null;
    String requestMethods = requestContext.getHeaderString(ACCESS_CONTROL_REQUEST_METHOD);
    if (requestMethods != null) {
      allowedMethods = RodaCoreFactory.getRodaConfigurationAsString(RodaConstants.CORS_ALLOW_METHODS);
      if (allowedMethods == null) {
        allowedMethods = ACCESS_CONTROL_ALLOW_METHODS_DEFAULT;
      }
    }
    return allowedMethods;
  }

  private String getAllowCredentials() {
    String allowCredentials = RodaCoreFactory.getRodaConfigurationAsString(RodaConstants.CORS_ALLOW_CREDENTIALS);
    if (allowCredentials == null
      || (allowCredentials != null && (Boolean.TRUE.toString().equalsIgnoreCase(allowCredentials)
        || !Boolean.FALSE.toString().equalsIgnoreCase(allowCredentials)))) {
      // replace invalid value with default
      allowCredentials = ACCESS_CONTROL_ALLOW_CREDENTIALS_DEFAULT;
    }
    return allowCredentials;
  }

  private String getExposedHeaders() {
    String exposedHeaders = RodaCoreFactory.getRodaConfigurationAsString(RodaConstants.CORS_EXPOSE_HEADERS);
    if (exposedHeaders == null) {
      exposedHeaders = ACCESS_CONTROL_EXPOSE_HEADERS_DEFAULT;
    }
    return exposedHeaders;
  }

  private int getMaxAge() {
    return RodaCoreFactory.getRodaConfigurationAsInt(ACCESS_CONTROL_MAX_AGE_DEFAULT, RodaConstants.CORS_MAX_AGE);
  }
}
