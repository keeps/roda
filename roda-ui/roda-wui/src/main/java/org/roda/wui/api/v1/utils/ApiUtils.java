/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.api.v1.utils;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.StreamingOutput;
import javax.xml.transform.TransformerException;

import org.apache.commons.lang3.StringUtils;
import org.roda.core.RodaCoreFactory;
import org.roda.core.common.ConsumesOutputStream;
import org.roda.core.common.ConsumesSkipableOutputStream;
import org.roda.core.common.DownloadUtils;
import org.roda.core.common.EntityResponse;
import org.roda.core.common.StreamResponse;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.IsModelObject;
import org.roda.core.data.v2.common.Pair;
import org.roda.core.data.v2.common.RODAObjectList;
import org.roda.core.data.v2.index.IndexResult;
import org.roda.core.data.v2.index.IsIndexed;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.AIPs;
import org.roda.core.data.v2.ip.DIP;
import org.roda.core.data.v2.ip.DIPFile;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.IndexedDIP;
import org.roda.core.data.v2.ip.IndexedFile;
import org.roda.core.data.v2.ip.IndexedRepresentation;
import org.roda.core.data.v2.ip.Representations;
import org.roda.core.data.v2.ip.StoragePath;
import org.roda.core.data.v2.ip.TransferredResource;
import org.roda.core.data.v2.jobs.IndexedReport;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.data.v2.log.LogEntry;
import org.roda.core.data.v2.notifications.Notification;
import org.roda.core.data.v2.ri.RepresentationInformation;
import org.roda.core.data.v2.risks.IndexedRisk;
import org.roda.core.data.v2.risks.Risk;
import org.roda.core.data.v2.risks.RiskIncidence;
import org.roda.core.model.utils.ModelUtils;
import org.roda.core.storage.Directory;
import org.roda.core.storage.Resource;
import org.roda.core.storage.StorageService;
import org.roda.wui.api.controllers.MimeTypeHelper;

/**
 * API Utils
 * 
 * @author Hélder Silva <hsilva@keep.pt>
 */
public class ApiUtils {

  private static final String CONTENT_DISPOSITION_FILENAME_ARGUMENT = "filename=";
  private static final String CONTENT_DISPOSITION_INLINE = "inline; ";
  private static final String CONTENT_DISPOSITION_ATTACHMENT = "attachment; ";

  private ApiUtils() {
    // do nothing
  }

  /**
   * Get media type
   *
   * @param acceptFormat
   *          String with required format
   * @param request
   *          http request
   * @return media type
   */
  public static String getMediaType(String acceptFormat, HttpServletRequest request) {
    if (StringUtils.isBlank(acceptFormat) && StringUtils.isNotBlank(request.getParameter("callback"))) {
      return ExtraMediaType.APPLICATION_JAVASCRIPT + "; charset=UTF-8";
    } else {
      return getMediaType(acceptFormat, request.getHeader(RodaConstants.API_HTTP_HEADER_ACCEPT));
    }
  }

  /**
   * Get media type
   * 
   * @param acceptFormat
   *          String with required format
   * @param acceptHeaders
   *          String with request headers
   * @return media type
   */
  public static String getMediaType(final String acceptFormat, final String acceptHeaders) {
    final String applicationJs = "application/javascript; charset=UTF-8";

    String mediaType = MediaType.APPLICATION_JSON + "; charset=UTF-8";

    if (StringUtils.isNotBlank(acceptFormat)) {
      if (RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_XML.equalsIgnoreCase(acceptFormat)) {
        mediaType = MediaType.APPLICATION_XML;
      } else if (RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_JSONP.equalsIgnoreCase(acceptFormat)) {
        mediaType = applicationJs;
      } else if (RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_BIN.equalsIgnoreCase(acceptFormat)) {
        mediaType = MediaType.APPLICATION_OCTET_STREAM;
      } else if (RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_HTML.equalsIgnoreCase(acceptFormat)) {
        mediaType = MediaType.TEXT_HTML;
      } else if (RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_CSV.contains(acceptFormat)) {
        mediaType = ExtraMediaType.TEXT_CSV;
      }
    } else if (StringUtils.isNotBlank(acceptHeaders)) {
      if (acceptHeaders.contains(MediaType.APPLICATION_XML)) {
        mediaType = MediaType.APPLICATION_XML;
      } else if (acceptHeaders.contains(applicationJs)
        || acceptHeaders.contains(ExtraMediaType.APPLICATION_JAVASCRIPT)) {
        mediaType = applicationJs;
      } else if (acceptHeaders.contains(ExtraMediaType.TEXT_CSV)) {
        mediaType = ExtraMediaType.TEXT_CSV;
      }
    }

    return mediaType;

  }

  /**
   * Returns valid start (pair first element) and limit (pair second element)
   * paging parameters defaulting to start = 0 and limit = 100 if none or
   * invalid values are provided.
   */
  public static Pair<Integer, Integer> processPagingParams(String start, String limit) {
    Integer startInteger;
    Integer limitInteger;
    try {
      startInteger = Integer.parseInt(start);
      if (startInteger < 0) {
        startInteger = 0;
      }
    } catch (NumberFormatException e) {
      startInteger = 0;
    }
    try {
      limitInteger = Integer.parseInt(limit);
      if (limitInteger < 0) {
        limitInteger = 100;
      }
    } catch (NumberFormatException e) {
      limitInteger = 100;
    }

    return Pair.of(startInteger, limitInteger);
  }

  public static Response okResponse(StreamResponse streamResponse, CacheControl cacheControl, Date lastModifiedDate) {
    return okResponse(streamResponse, cacheControl, lastModifiedDate, false);
  }

  public static Response okResponse(StreamResponse streamResponse, CacheControl cacheControl, Date lastModifiedDate,
    boolean inline) {
    StreamingOutput so = new StreamingOutput() {
      @Override
      public void write(OutputStream output) throws IOException, WebApplicationException {
        streamResponse.getStream().consumeOutputStream(output);
      }
    };

    String mediaType = MimeTypeHelper.getContentType(streamResponse.getFilename(), streamResponse.getMediaType());
    Response.ResponseBuilder response = Response.ok(so, mediaType).header(HttpHeaders.CONTENT_DISPOSITION,
      contentDisposition(inline) + CONTENT_DISPOSITION_FILENAME_ARGUMENT + "\"" + streamResponse.getFilename() + "\"");

    if (streamResponse.getFileSize() > 0) {
      response.header(HttpHeaders.CONTENT_LENGTH, streamResponse.getFileSize());
    }

    return response.cacheControl(cacheControl).lastModified(lastModifiedDate).build();
  }

  public static Response okResponse(StreamResponse streamResponse, Request request) {
    return okResponse(streamResponse, false, false, request);
  }

  public static Response okResponse(StreamResponse streamResponse, boolean inline, boolean acceptRanges,
    Request request) {

    StreamingOutput so = new StreamingOutput() {
      @Override
      public void write(OutputStream output) throws IOException, WebApplicationException {
        streamResponse.getStream().consumeOutputStream(output);
      }
    };

    String mediaType = MimeTypeHelper.getContentType(streamResponse.getFilename(), streamResponse.getMediaType());
    Response.ResponseBuilder response = Response.ok(so, mediaType).header(HttpHeaders.CONTENT_DISPOSITION,
      contentDisposition(inline) + CONTENT_DISPOSITION_FILENAME_ARGUMENT + "\"" + streamResponse.getFilename() + "\"");

    if (streamResponse.getFileSize() > 0) {
      response.header(HttpHeaders.CONTENT_LENGTH, streamResponse.getFileSize());
    }

    Date lastModifiedDate = streamResponse.getLastModified();
    if (lastModifiedDate != null) {
      CacheControl cc = new CacheControl();
      cc.setMaxAge(CACHE_CONTROL_MAX_AGE);
      cc.setPrivate(true);
      EntityTag etag = null;
      etag = new EntityTag(Long.toString(lastModifiedDate.getTime()));
      ResponseBuilder builder = request.evaluatePreconditions(etag);
      if (builder != null) {
        return builder.cacheControl(cc).tag(etag).build();
      }

      response.header(HttpHeaders.LAST_MODIFIED, streamResponse.getLastModified());
      response.cacheControl(cc).tag(etag);
    }

    if (acceptRanges) {
      response.header("Accept-Ranges", "bytes");
    }

    return response.build();
  }

  private static final int CACHE_CONTROL_MAX_AGE = 60;

  public static Response okResponse(StreamResponse streamResponse, boolean inline, final String range,
    Request request) {

    // range not requested : Firefox, Opera, IE do not send range headers
    // cannot skip content
    // cannot calculate file size
    if (range == null || !(streamResponse.getStream() instanceof ConsumesSkipableOutputStream)
      || streamResponse.getFileSize() < 0) {
      return okResponse(streamResponse, inline, range == null, request);
    }

    String[] ranges = range.split("=")[1].split("-");
    final int from = Integer.parseInt(ranges[0]);

    long fileSize = streamResponse.getFileSize();
    int to = (int) (fileSize - 1);
    if (ranges.length == 2) {
      to = Integer.parseInt(ranges[1]);
    }

    final String responseRange = String.format("bytes %d-%d/%d", from, to, fileSize);
    final int len = to - from + 1;

    StreamingOutput so = new StreamingOutput() {
      @Override
      public void write(OutputStream output) throws IOException, WebApplicationException {
        try {
          ((ConsumesSkipableOutputStream) streamResponse.getStream()).consumeOutputStream(output, from, len);
        } catch (IOException e) {
          // ignoring
        }
      }
    };

    String mediaType = MimeTypeHelper.getContentType(streamResponse.getFilename(), streamResponse.getMediaType());

    Response.ResponseBuilder response = Response.status(Status.PARTIAL_CONTENT).entity(so)
      .header(HttpHeaders.CONTENT_TYPE, mediaType).header("Accept-Ranges", "bytes")
      .header("Content-Range", responseRange).header(HttpHeaders.CONTENT_LENGTH, len)
      .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition(inline) + CONTENT_DISPOSITION_FILENAME_ARGUMENT + "\""
        + streamResponse.getFilename() + "\"");

    Date lastModifiedDate = streamResponse.getLastModified();
    if (lastModifiedDate != null) {
      CacheControl cc = new CacheControl();
      cc.setMaxAge(CACHE_CONTROL_MAX_AGE);
      cc.setPrivate(true);
      EntityTag etag = null;
      etag = new EntityTag(Long.toString(lastModifiedDate.getTime()));
      ResponseBuilder builder = request.evaluatePreconditions(etag);
      if (builder != null) {
        return builder.cacheControl(cc).tag(etag).build();
      }

      response.header(HttpHeaders.LAST_MODIFIED, streamResponse.getLastModified());
      response.cacheControl(cc).tag(etag);
    }

    return response.build();
  }

  public static Response okResponse(StreamResponse streamResponse) {
    return okResponse(streamResponse, false);
  }

  public static Response okResponse(StreamResponse streamResponse, boolean inline) {
    StreamingOutput so = new StreamingOutput() {
      @Override
      public void write(OutputStream output) throws IOException, WebApplicationException {
        streamResponse.getStream().consumeOutputStream(output);
      }
    };

    String mediaType = MimeTypeHelper.getContentType(streamResponse.getFilename(), streamResponse.getMediaType());

    Response.ResponseBuilder response = Response.ok(so, mediaType).header(HttpHeaders.CONTENT_DISPOSITION,
      contentDisposition(inline) + CONTENT_DISPOSITION_FILENAME_ARGUMENT + "\"" + streamResponse.getFilename() + "\"");

    if (streamResponse.getFileSize() > 0) {
      response.header(HttpHeaders.CONTENT_LENGTH, streamResponse.getFileSize());
    }

    return response.build();
  }

  private static String contentDisposition(boolean inline) {
    return inline ? CONTENT_DISPOSITION_INLINE : CONTENT_DISPOSITION_ATTACHMENT;
  }

  public static Response errorResponse(TransformerException e) {
    String message;

    if (e.getCause() != null) {
      message = e.getCause().getMessage();
    } else {
      message = e.getMessage();
    }

    return Response.serverError().entity(new ApiResponseMessage(ApiResponseMessage.ERROR, message)).build();
  }

  public static URI getUriFromRequest(HttpServletRequest request) throws RODAException {
    try {
      return new URI(request.getRequestURI());
    } catch (URISyntaxException e) {
      throw new GenericException("Error creating URI from String: " + e.getMessage());
    }
  }

  public static <T extends IsIndexed, R extends IsModelObject> RODAObjectList<R> indexedResultToRODAObjectList(
    Class<T> objectClass, IndexResult<T> result)
    throws RequestNotValidException, NotFoundException, GenericException, AuthorizationDeniedException {

    RODAObjectList<? extends IsModelObject> ret;

    if (objectClass.equals(IndexedAIP.class)) {
      AIPs aips = new AIPs();
      for (T object : result.getResults()) {
        IndexedAIP aip = (IndexedAIP) object;
        aips.addObject(RodaCoreFactory.getModelService().retrieveAIP(aip.getId()));
      }
      ret = aips;
    } else if (objectClass.equals(IndexedRepresentation.class)) {
      Representations representations = new Representations();
      for (T object : result.getResults()) {
        IndexedRepresentation representation = (IndexedRepresentation) object;
        representations.addObject(
          RodaCoreFactory.getModelService().retrieveRepresentation(representation.getAipId(), representation.getId()));
      }
      ret = representations;
    } else if (objectClass.equals(IndexedFile.class)) {
      org.roda.core.data.v2.ip.Files files = new org.roda.core.data.v2.ip.Files();
      for (T object : result.getResults()) {
        IndexedFile file = (IndexedFile) object;
        files.addObject(RodaCoreFactory.getModelService().retrieveFile(file.getAipId(), file.getRepresentationId(),
          file.getPath(), file.getId()));
      }
      ret = files;
    } else if (objectClass.equals(IndexedRisk.class)) {
      List<Risk> risks = result.getResults().stream().map(risk -> (Risk) risk).collect(Collectors.toList());
      ret = new org.roda.core.data.v2.risks.Risks(risks);
    } else if (objectClass.equals(TransferredResource.class)) {
      ret = new org.roda.core.data.v2.ip.TransferredResources((List<TransferredResource>) result.getResults());
    } else if (objectClass.equals(RepresentationInformation.class)) {
      ret = new org.roda.core.data.v2.ri.RepresentationInformationList(
        (List<RepresentationInformation>) result.getResults());
    } else if (objectClass.equals(Notification.class)) {
      ret = new org.roda.core.data.v2.notifications.Notifications((List<Notification>) result.getResults());
    } else if (objectClass.equals(LogEntry.class)) {
      ret = new org.roda.core.data.v2.log.LogEntries((List<LogEntry>) result.getResults());
    } else if (objectClass.equals(RiskIncidence.class)) {
      ret = new org.roda.core.data.v2.risks.RiskIncidences((List<RiskIncidence>) result.getResults());
    } else if (objectClass.equals(IndexedDIP.class)) {
      List<DIP> dips = result.getResults().stream().map(dip -> (DIP) dip).collect(Collectors.toList());
      ret = new org.roda.core.data.v2.ip.DIPs(dips);
    } else if (objectClass.equals(DIPFile.class)) {
      ret = new org.roda.core.data.v2.ip.DIPFiles((List<DIPFile>) result.getResults());
    } else if (objectClass.equals(IndexedReport.class)) {
      List<Report> reports = result.getResults().stream().map(report -> (Report) report).collect(Collectors.toList());
      ret = new org.roda.core.data.v2.jobs.Reports(reports);
    } else {
      throw new GenericException("Unsupported object class: " + objectClass);
    }

    return (RODAObjectList<R>) ret;
  }

  public static StreamResponse download(Resource resource)
    throws GenericException, RequestNotValidException, NotFoundException, AuthorizationDeniedException {
    return download(resource, null);
  }

  public static StreamResponse download(Resource resource, String fileName)
    throws GenericException, RequestNotValidException, NotFoundException, AuthorizationDeniedException {
    ConsumesOutputStream download = DownloadUtils.download(RodaCoreFactory.getStorageService(), resource, fileName);
    return new StreamResponse(download);
  }

  public static <T extends IsIndexed> Response okResponse(T indexed, String acceptFormat, String mediaType)
    throws RequestNotValidException, NotFoundException, GenericException, AuthorizationDeniedException {
    EntityResponse response;

    if (indexed instanceof IndexedAIP) {
      IndexedAIP indexedAIP = (IndexedAIP) indexed;
      if (RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_ZIP.equals(acceptFormat)) {
        StoragePath storagePath = ModelUtils.getAIPStoragePath(indexedAIP.getId());
        StorageService storage = RodaCoreFactory.getStorageService();
        Directory directory = storage.getDirectory(storagePath);
        response = download(directory, indexedAIP.getTitle());
      } else if (RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_JSON.equals(acceptFormat)
        || RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_XML.equals(acceptFormat)
        || RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_JSONP.equals(acceptFormat)) {
        AIP aip = RodaCoreFactory.getModelService().retrieveAIP(indexedAIP.getId());
        response = new ObjectResponse<>(acceptFormat, aip);
      } else {
        throw new GenericException("Unsupported class: " + acceptFormat);
      }

      if (response instanceof ObjectResponse) {
        ObjectResponse<AIP> aip = (ObjectResponse<AIP>) response;
        return Response.ok(aip.getObject(), mediaType).build();
      } else {
        return ApiUtils.okResponse((StreamResponse) response);
      }
    } else {
      throw new GenericException("Unsupported accept format: " + acceptFormat);
    }
  }

}
