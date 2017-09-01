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
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import javax.xml.transform.TransformerException;

import org.apache.commons.lang3.StringUtils;
import org.roda.core.RodaCoreFactory;
import org.roda.core.common.ConsumesOutputStream;
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
import org.roda.core.data.v2.formats.Format;
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
import org.roda.core.data.v2.risks.IndexedRisk;
import org.roda.core.data.v2.risks.Risk;
import org.roda.core.data.v2.risks.RiskIncidence;
import org.roda.core.data.v2.user.RODAMember;
import org.roda.core.model.utils.ModelUtils;
import org.roda.core.storage.Directory;
import org.roda.core.storage.Resource;
import org.roda.core.storage.StorageService;

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
    return getMediaType(acceptFormat, request.getHeader(RodaConstants.API_HTTP_HEADER_ACCEPT));
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
      } else if (RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_JSON.equalsIgnoreCase(acceptFormat)) {
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
    return Response.ok(so, streamResponse.getMediaType())
      .header(HttpHeaders.CONTENT_DISPOSITION,
        contentDisposition(inline) + CONTENT_DISPOSITION_FILENAME_ARGUMENT + "\"" + streamResponse.getFilename() + "\"")
      .cacheControl(cacheControl).lastModified(lastModifiedDate).build();
  }

  public static Response okResponse(StreamResponse streamResponse, CacheControl cacheControl, EntityTag tag) {
    return okResponse(streamResponse, cacheControl, tag, false);
  }

  public static Response okResponse(StreamResponse streamResponse, CacheControl cacheControl, EntityTag tag,
    boolean inline) {
    StreamingOutput so = new StreamingOutput() {

      @Override
      public void write(OutputStream output) throws IOException, WebApplicationException {
        streamResponse.getStream().consumeOutputStream(output);

      }
    };
    return Response.ok(so, streamResponse.getMediaType())
      .header(HttpHeaders.CONTENT_DISPOSITION,
        contentDisposition(inline) + CONTENT_DISPOSITION_FILENAME_ARGUMENT + "\"" + streamResponse.getFilename() + "\"")
      .cacheControl(cacheControl).tag(tag).build();
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
    return Response.ok(so, streamResponse.getMediaType())
      .header(HttpHeaders.CONTENT_DISPOSITION,
        contentDisposition(inline) + CONTENT_DISPOSITION_FILENAME_ARGUMENT + "\"" + streamResponse.getFilename() + "\"")
      .build();
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

  @SuppressWarnings("unchecked")
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
    } else if (objectClass.equals(Format.class)) {
      ret = new org.roda.core.data.v2.formats.Formats((List<Format>) result.getResults());
    } else if (objectClass.equals(Notification.class)) {
      ret = new org.roda.core.data.v2.notifications.Notifications((List<Notification>) result.getResults());
    } else if (objectClass.equals(LogEntry.class)) {
      ret = new org.roda.core.data.v2.log.LogEntries((List<LogEntry>) result.getResults());
    } else if (objectClass.equals(RiskIncidence.class)) {
      ret = new org.roda.core.data.v2.risks.RiskIncidences((List<RiskIncidence>) result.getResults());
    } else if (objectClass.equals(RODAMember.class)) {
      ret = new org.roda.core.data.v2.user.RODAMembers((List<RODAMember>) result.getResults());
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

  public static StreamResponse download(Resource resource) {
    return download(resource, null);
  }

  public static StreamResponse download(Resource resource, String fileName) {
    ConsumesOutputStream download = DownloadUtils.download(RodaCoreFactory.getStorageService(), resource, fileName);
    return new StreamResponse(download.getFileName(), download.getMediaType(), download);
  }

  public static <T extends IsIndexed> Response okResponse(T indexed, String acceptFormat, String mediaType)
    throws RequestNotValidException, NotFoundException, GenericException, AuthorizationDeniedException {
    EntityResponse representation;

    if (indexed instanceof IndexedAIP) {
      IndexedAIP indexedAIP = (IndexedAIP) indexed;
      if (RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_ZIP.equals(acceptFormat)) {
        StoragePath storagePath = ModelUtils.getAIPStoragePath(indexedAIP.getId());
        StorageService storage = RodaCoreFactory.getStorageService();
        Directory directory = storage.getDirectory(storagePath);
        representation = download(directory, indexedAIP.getTitle());
      } else if (RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_JSON.equals(acceptFormat)
        || RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_XML.equals(acceptFormat)) {
        AIP aip = RodaCoreFactory.getModelService().retrieveAIP(indexedAIP.getId());
        representation = new ObjectResponse<AIP>(acceptFormat, aip);
      } else {
        throw new GenericException("Unsupported class: " + acceptFormat);
      }

      if (representation instanceof ObjectResponse) {
        ObjectResponse<AIP> aip = (ObjectResponse<AIP>) representation;
        return Response.ok(aip.getObject(), mediaType).build();
      } else {
        return ApiUtils.okResponse((StreamResponse) representation);
      }
    } else {
      throw new GenericException("Unsupported accept format: " + acceptFormat);
    }
  }

}
