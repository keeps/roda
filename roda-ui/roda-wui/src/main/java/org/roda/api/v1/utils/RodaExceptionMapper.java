/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.api.v1.utils;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.roda.core.common.AuthorizationDeniedException;
import org.roda.core.common.NotFoundException;
import org.roda.core.common.NotImplementedException;
import org.roda.core.common.RODAException;
import org.roda.wui.common.client.GenericException;

@Provider
public class RodaExceptionMapper implements ExceptionMapper<RODAException> {

  @Override
  public Response toResponse(RODAException e) {
    Response response;
    if (e instanceof AuthorizationDeniedException) {
      response = Response.status(Status.UNAUTHORIZED)
        .entity(new ApiResponseMessage(ApiResponseMessage.ERROR, e.getMessage())).build();
    } else if (e instanceof NotImplementedException) {
      response = Response.serverError().entity(new ApiResponseMessage(ApiResponseMessage.ERROR, "Not yet implemented"))
        .build();
    } else if (e instanceof GenericException) {
      response = Response.serverError().entity(new ApiResponseMessage(ApiResponseMessage.ERROR, e.getMessage()))
        .build();
    } else if (e instanceof NotFoundException) {
      response = Response.status(Status.NOT_FOUND)
        .entity(new ApiResponseMessage(ApiResponseMessage.ERROR, e.getMessage())).build();
    } else {
      response = Response.serverError().entity(new ApiResponseMessage(ApiResponseMessage.ERROR, e.getMessage()))
        .build();
    }
    return response;
  }

}
