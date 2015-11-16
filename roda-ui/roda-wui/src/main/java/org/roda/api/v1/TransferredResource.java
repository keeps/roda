/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.api.v1;

import java.io.InputStream;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.roda.common.RodaCoreFactory;
import org.roda.common.UserUtility;
import org.roda.core.common.RODAException;
import org.roda.core.data.v2.RodaUser;

import io.swagger.annotations.Api;

@Path(TransferredResource.ENDPOINT)
@Api(value = TransferredResource.SWAGGER_ENDPOINT)
public class TransferredResource {
  public static final String ENDPOINT = "/v1/transferred";
  public static final String SWAGGER_ENDPOINT = "v1 transferred";

  @Context
  private HttpServletRequest request;

  @POST
  @Path("/new")
  public Response uploadFiles(
    @FormDataParam("upl") InputStream inputStream, @FormDataParam("upl") FormDataContentDisposition fileDetail)
      throws RODAException {
    // get user
    RodaUser user = UserUtility.getApiUser(request, RodaCoreFactory.getIndexService());
    // delegate action to controller
    // do nothing

    // FIXME give a better answer
    return Response.ok().entity("{'status':'success'}").build();
  }


}
