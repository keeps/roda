package org.roda.wui.client.services;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.fusesource.restygwt.client.DirectRestService;
import org.fusesource.restygwt.client.Options;
import org.glassfish.jersey.server.JSONP;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.v2.index.CountRequest;
import org.roda.core.data.v2.index.FindRequest;
import org.roda.core.data.v2.index.IndexResult;
import org.roda.core.data.v2.index.IsIndexed;
import org.roda.wui.api.v1.utils.ExtraMediaType;

import java.util.List;

/**
 * @author António Lindo <alindo@keep.pt>
 */
@Path("../api/v2/index/")
@Tag(name = "v2")
public interface IndexService extends DirectRestService {

  /**
   * Default value for <i>start</i> parameter.
   */
  static final int DEFAULT_START = 0;
  /**
   * Default value for <i>limit</i> parameter.
   */
  static final int DEFAULT_LIMIT = 100;
  /**
   * Default value for <i>onlyActive</i> parameter.
   */
  static final boolean DEFAULT_ONLY_ACTIVE = true;
  /**
   * Default filename for CSV files.
   */
  static final String DEFAULT_CSV_FILENAME = "export.csv";
  /**
   * CSV type.
   */
  static final String TYPE_CSV = "csv";
  /**
   * Default value for <i>facetLimit</i> parameter.
   */
  static final int DEFAULT_FACET_LIMIT = 100;
  /** CSV field delimiter config key. */
  static final String CONFIG_KEY_CSV_DELIMITER = "csv.delimiter";

  @POST
  @Path("/find")
  @Consumes({MediaType.APPLICATION_JSON})
  @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, ExtraMediaType.TEXT_CSV})
  @Operation(summary = "Find indexed resources", description = "Finds existing indexed resources", responses = {
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = IsIndexed.class)))})
  <T extends IsIndexed> Object find(
    @Parameter(name = "find request", required = true, content = @Content(mediaType = MediaType.APPLICATION_JSON)) FindRequest findRequest,
    @Parameter(description = "language") @QueryParam("lang") String localeString)
    throws RODAException;

  @POST
  @Path("/count")
  @Consumes({MediaType.APPLICATION_JSON})
  @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  @Operation(summary = "Count indexed resources", description = "Counts indexed resources", responses = {
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = Long.class)))})
  Long count(@Parameter(description = "Count parameters") final CountRequest countRequest)
    throws RODAException;
}
