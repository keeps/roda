/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.api.exceptions;

import javax.inject.Inject;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.glassfish.jersey.server.ContainerRequest;
import org.roda.core.data.exceptions.AlreadyExistsException;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.JobAlreadyStartedException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.NotImplementedException;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.wui.api.v1.utils.ApiResponseMessage;
import org.roda.wui.api.v1.utils.ApiUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Provider
public class RodaExceptionMapper implements ExceptionMapper<RODAException> {
  private static final Logger LOGGER = LoggerFactory.getLogger(RodaExceptionMapper.class);

  @Inject
  private javax.inject.Provider<ContainerRequest> containerRequestProvider;

  @Override
  public Response toResponse(RODAException e) {
    ContainerRequest containerRequest = containerRequestProvider.get();
    String parameter = containerRequest.getProperty("acceptFormat") != null
      ? (String) containerRequest.getProperty("acceptFormat") : "";
    String header = containerRequest.getHeaderString("Accept");
    String mediaType = ApiUtils.getMediaType(parameter, header);

    ResponseBuilder responseBuilder;
    String message = e.getClass().getSimpleName() + ": " + e.getMessage();
    if (e.getCause() != null) {
      message += ", caused by " + e.getCause().getClass().getName() + ": " + e.getCause().getMessage();
    }
    LOGGER.debug("Creating error response. MediaType: {}; Message: {}", mediaType, message, e);
    if (e instanceof AuthorizationDeniedException) {
      responseBuilder = Response.status(Status.UNAUTHORIZED)
        .entity(new ApiResponseMessage(ApiResponseMessage.ERROR, message));
    } else if (e instanceof NotImplementedException) {
      responseBuilder = Response.serverError()
        .entity(new ApiResponseMessage(ApiResponseMessage.ERROR, "Not yet implemented"));
    } else if (e instanceof RequestNotValidException) {
      responseBuilder = Response.status(Status.BAD_REQUEST)
        .entity(new ApiResponseMessage(ApiResponseMessage.ERROR, message));
    } else if (e instanceof GenericException) {
      responseBuilder = Response.serverError().entity(new ApiResponseMessage(ApiResponseMessage.ERROR, message));
    } else if (e instanceof NotFoundException) {
      responseBuilder = Response.status(Status.NOT_FOUND)
        .entity(new ApiResponseMessage(ApiResponseMessage.ERROR, message));
    } else if (e instanceof AlreadyExistsException || e instanceof JobAlreadyStartedException) {
      responseBuilder = Response.status(Status.CONFLICT)
        .entity(new ApiResponseMessage(ApiResponseMessage.ERROR, message));
    } else {
      responseBuilder = Response.serverError().entity(new ApiResponseMessage(ApiResponseMessage.ERROR, e.getMessage()));
    }
    return responseBuilder.type(mediaType).build();
  }

}
