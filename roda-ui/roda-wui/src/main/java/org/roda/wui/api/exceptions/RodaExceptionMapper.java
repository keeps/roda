/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.api.exceptions;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.roda.core.data.common.AuthorizationDeniedException;
import org.roda.core.data.common.NotImplementedException;
import org.roda.core.data.common.RODAException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.wui.api.v1.utils.ApiResponseMessage;
import org.roda.wui.common.client.GenericException;

@Provider
public class RodaExceptionMapper implements ExceptionMapper<RODAException> {

  // XXX while using jetty (e.g. gwt-devmode), this injection causes error
  // during
  // initialization
  // @Context
  // private HttpServletRequest request;

  @Override
  public Response toResponse(RODAException e) {

    // String mediaType =
    // ApiUtils.getMediaType(request.getParameter("acceptFormat"),
    // request.getHeader("Accept"));

    Response response;
    if (e instanceof AuthorizationDeniedException) {
      response = Response.status(Status.UNAUTHORIZED)
        .entity(new ApiResponseMessage(ApiResponseMessage.ERROR, e.getMessage())).build();
    } else if (e instanceof NotImplementedException) {
      response = Response.serverError().entity(new ApiResponseMessage(ApiResponseMessage.ERROR, "Not yet implemented"))
        .build();
    } else if (e instanceof RequestNotValidException) {
      response = Response.status(Status.BAD_REQUEST)
        .entity(new ApiResponseMessage(ApiResponseMessage.ERROR, e.getMessage())).build();
    } else if (e instanceof GenericException) {
      response = Response.serverError().entity(new ApiResponseMessage(ApiResponseMessage.ERROR, e.getMessage()))
        .build();
    } else if (e instanceof NotFoundException) {
      response = Response.status(Status.NOT_FOUND)
        .entity(new ApiResponseMessage(ApiResponseMessage.ERROR, e.getMessage())).build();
    } else if (e instanceof AlreadyExistsException) {
      response = Response.status(Status.CONFLICT)
        .entity(new ApiResponseMessage(ApiResponseMessage.ERROR, e.getMessage())).build();
    } else {
      // response = Response.serverError().type(mediaType)
      // .entity(new ApiResponseMessage(ApiResponseMessage.ERROR,
      // e.getMessage())).build();
      response = Response.serverError().entity(new ApiResponseMessage(ApiResponseMessage.ERROR, e.getMessage()))
        .build();
    }
    return response;
  }

}
