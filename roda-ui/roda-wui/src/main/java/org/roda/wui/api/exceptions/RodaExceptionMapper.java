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

@Provider
public class RodaExceptionMapper implements ExceptionMapper<RODAException> {

  @Inject
  private javax.inject.Provider<ContainerRequest> containerRequestProvider;

  @Override
  public Response toResponse(RODAException e) {
    ContainerRequest containerRequest = containerRequestProvider.get();
    String parameter = containerRequest.getProperty("acceptFormat") != null
      ? (String) containerRequest.getProperty("acceptFormat") : "";
    String header = containerRequest.getHeaderString("Accept");
    String mediaType = ApiUtils.getMediaType(parameter, header);

    Response response;
    String message = e.getClass().getSimpleName() + ": " + e.getMessage();
    if (e.getCause() != null) {
      message += ", caused by " + e.getCause().getClass().getName() + ": " + e.getCause().getMessage();
    }
    if (e instanceof AuthorizationDeniedException) {
      response = Response.status(Status.UNAUTHORIZED).entity(new ApiResponseMessage(ApiResponseMessage.ERROR, message))
        .build();
    } else if (e instanceof NotImplementedException) {
      response = Response.serverError().entity(new ApiResponseMessage(ApiResponseMessage.ERROR, "Not yet implemented"))
        .build();
    } else if (e instanceof RequestNotValidException) {
      response = Response.status(Status.BAD_REQUEST).entity(new ApiResponseMessage(ApiResponseMessage.ERROR, message))
        .build();
    } else if (e instanceof GenericException) {
      response = Response.serverError().entity(new ApiResponseMessage(ApiResponseMessage.ERROR, message)).build();
    } else if (e instanceof NotFoundException) {
      response = Response.status(Status.NOT_FOUND).entity(new ApiResponseMessage(ApiResponseMessage.ERROR, message))
        .build();
    } else if (e instanceof AlreadyExistsException || e instanceof JobAlreadyStartedException) {
      response = Response.status(Status.CONFLICT).entity(new ApiResponseMessage(ApiResponseMessage.ERROR, message))
        .build();
    } else {
      response = Response.serverError().type(mediaType)
        .entity(new ApiResponseMessage(ApiResponseMessage.ERROR, e.getMessage())).build();
    }
    return response;
  }

}
