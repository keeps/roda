package org.roda.wui.api.v2.exceptions;

import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.glassfish.jersey.server.ContainerRequest;
import org.roda.wui.api.v1.utils.ApiResponseMessage;
import org.roda.wui.api.v1.utils.ApiUtils;
import org.roda.wui.api.v2.exceptions.RESTException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * @author António Lindo <alindo@keep.pt>
 */
@Provider
public class RESTExceptionMapper implements ExceptionMapper<RESTException> {
  private static final Logger LOGGER = LoggerFactory.getLogger(RESTExceptionMapper.class);

  @Inject
  private jakarta.inject.Provider<ContainerRequest> containerRequestProvider;

  @Override
  public Response toResponse(RESTException e) {
      ContainerRequest containerRequest = containerRequestProvider.get();
      String parameter = containerRequest.getProperty("acceptFormat") != null
        ? (String) containerRequest.getProperty("acceptFormat")
        : "";
      String header = containerRequest.getHeaderString("Accept");
      String mediaType = ApiUtils.getMediaType(parameter, header);

      Response.ResponseBuilder responseBuilder;
      String message = e.getClass().getSimpleName() + ": " + e.getMessage();
      if (e.getCause() != null) {
        message += ", caused by " + e.getCause().getClass().getName() + ": " + e.getCause().getMessage();
      }
      LOGGER.debug("Creating error response. MediaType: {}; Message: {}", mediaType, message, e);
      responseBuilder = Response.status(e.getStatus()).entity(new ApiResponseMessage(ApiResponseMessage.ERROR, message));

      return responseBuilder.type(mediaType).build();
    }
  }
