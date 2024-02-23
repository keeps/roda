/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.api.exceptions;

import java.util.UUID;

import org.glassfish.jersey.server.ContainerRequest;
import org.roda.core.data.exceptions.AlreadyExistsException;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.IsStillUpdatingException;
import org.roda.core.data.exceptions.JobAlreadyStartedException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.NotImplementedException;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.exceptions.TechnicalMetadataNotFoundException;
import org.roda.wui.api.v1.utils.ApiResponseMessage;
import org.roda.wui.api.v1.utils.ApiUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.ResponseBuilder;
import jakarta.ws.rs.core.Response.Status;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class RodaExceptionMapper implements ExceptionMapper<RODAException> {
  private static final Logger LOGGER = LoggerFactory.getLogger(RodaExceptionMapper.class);

  @Inject
  private jakarta.inject.Provider<ContainerRequest> containerRequestProvider;

  @Override
  public Response toResponse(RODAException e) {
    ContainerRequest containerRequest = containerRequestProvider.get();
    String parameter = containerRequest.getProperty("acceptFormat") != null
      ? (String) containerRequest.getProperty("acceptFormat")
      : "";
    String header = containerRequest.getHeaderString("Accept");
    String mediaType = ApiUtils.getMediaType(parameter, header);

    ResponseBuilder responseBuilder;
    UUID errorID = UUID.randomUUID();
    String message = "An error has occurred, to get more details use the error identifier: " + errorID;
    String warn = "ERROR_ID: " + errorID + " - " + e.getClass().getSimpleName() + ": " + e.getMessage();
    if (e.getCause() != null) {
      warn += ", caused by " + e.getCause().getClass().getName() + ": " + e.getCause().getMessage();
    }
    LOGGER.debug("Creating error response. MediaType: {}; Message: {}", mediaType, message, e);
    LOGGER.warn(warn);
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
    } else if (e instanceof NotFoundException || e instanceof TechnicalMetadataNotFoundException) {
      responseBuilder = Response.status(Status.NOT_FOUND)
        .entity(new ApiResponseMessage(ApiResponseMessage.ERROR, message));
    } else if (e instanceof AlreadyExistsException || e instanceof JobAlreadyStartedException
      || e instanceof IsStillUpdatingException) {
      responseBuilder = Response.status(Status.CONFLICT)
        .entity(new ApiResponseMessage(ApiResponseMessage.ERROR, message));
    } else {
      responseBuilder = Response.serverError().entity(new ApiResponseMessage(ApiResponseMessage.ERROR, e.getMessage()));
    }
    return responseBuilder.type(mediaType).build();
  }

}
