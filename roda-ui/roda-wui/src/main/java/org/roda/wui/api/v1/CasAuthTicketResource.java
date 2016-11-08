/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.api.v1;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.roda.core.RodaCoreFactory;
import org.roda.core.data.exceptions.AuthenticationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.wui.filter.CasClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * REST API CAS authentication tickets resource.
 *
 * @author Rui Castro <rui.castro@gmail.com>
 */
@Path("/v1/auth/ticket")
@Api(value = "auth ticket")
public class CasAuthTicketResource {
  /** Logger. */
  private static final Logger LOGGER = LoggerFactory.getLogger(CasAuthTicketResource.class);

  /** HTTP request. */
  @Context
  private HttpServletRequest request;

  /**
   * Create a <strong>Ticket Granting Ticket</strong> for the specified user.
   * 
   * @param username
   *          the user username.
   * @param password
   *          the user password.
   *
   * @return a {@link Response} with the <strong>Ticket Granting
   *         Ticket</strong>.
   * @throws GenericException
   *           if some error occurred.
   */
  @POST
  @Produces(MediaType.TEXT_PLAIN)
  @ApiOperation(value = "Create an authorization ticket", response = String.class)
  public Response create(@FormParam("username") final String username, @FormParam("password") final String password)
    throws GenericException {
    final String casServerUrlPrefix = RodaCoreFactory.getRodaConfiguration().getString("ui.auth.cas.base_url");
    final CasClient casClient = new CasClient(casServerUrlPrefix);
    try {
      final String tgt = casClient.getTicketGrantingTicket(username, password);
      return Response.status(Response.Status.CREATED).entity(tgt).build();
    } catch (final AuthenticationDeniedException e) {
      LOGGER.debug(e.getMessage(), e);
      return Response.status(Response.Status.UNAUTHORIZED).entity(e.getMessage()).build();
    }
  }

}
