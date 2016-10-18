/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.api.v1.utils;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.transform.TransformerException;

import org.apache.commons.lang.StringUtils;
import org.roda.core.RodaCoreFactory;
import org.roda.core.common.StreamResponse;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.common.Pair;
import org.roda.core.data.v2.common.RODAObjectList;
import org.roda.core.data.v2.formats.Format;
import org.roda.core.data.v2.index.IndexResult;
import org.roda.core.data.v2.index.IsIndexed;
import org.roda.core.data.v2.ip.AIPs;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.IndexedFile;
import org.roda.core.data.v2.ip.IndexedRepresentation;
import org.roda.core.data.v2.ip.Representations;
import org.roda.core.data.v2.ip.TransferredResource;
import org.roda.core.data.v2.log.LogEntry;
import org.roda.core.data.v2.notifications.Notification;
import org.roda.core.data.v2.risks.IndexedRisk;
import org.roda.core.data.v2.risks.Risk;
import org.roda.core.data.v2.risks.RiskIncidence;
import org.roda.core.data.v2.user.RODAMember;

/**
 * API Utils
 * 
 * @author Hélder Silva <hsilva@keep.pt>
 */
public class ApiUtils {

  private static final String CONTENT_DISPOSITION_FILENAME_ARGUMENT = "filename=";
  private static final String CONTENT_DISPOSITION_INLINE = "inline; ";
  private static final String CONTENT_DISPOSITION_ATTACHMENT = "attachment; ";

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
    final String APPLICATION_JS = "application/javascript; charset=UTF-8";

    String mediaType = MediaType.APPLICATION_JSON + "; charset=UTF-8";

    if (StringUtils.isNotBlank(acceptFormat)) {
      if (acceptFormat.equalsIgnoreCase("XML")) {
        mediaType = MediaType.APPLICATION_XML;
      } else if (acceptFormat.equalsIgnoreCase("JSONP")) {
        mediaType = APPLICATION_JS;
      } else if (acceptFormat.equalsIgnoreCase("bin")) {
        mediaType = MediaType.APPLICATION_OCTET_STREAM;
      } else if (acceptFormat.equalsIgnoreCase("html")) {
        mediaType = MediaType.TEXT_HTML;
      }
    } else if (StringUtils.isNotBlank(acceptHeaders)) {
      if (acceptHeaders.contains(MediaType.APPLICATION_XML)) {
        mediaType = MediaType.APPLICATION_XML;
      } else if (acceptHeaders.contains(APPLICATION_JS)) {
        mediaType = APPLICATION_JS;
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
    Integer startInteger, limitInteger;
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

    return new Pair<Integer, Integer>(startInteger, limitInteger);
  }

  public static Response okResponse(StreamResponse streamResponse, CacheControl cacheControl, Date lastModifiedDate) {
    return okResponse(streamResponse, cacheControl, lastModifiedDate, false);
  }

  public static Response okResponse(StreamResponse streamResponse, CacheControl cacheControl, Date lastModifiedDate,
    boolean inline) {
    return Response.ok(streamResponse.getStream(), streamResponse.getMediaType())
      .header(HttpHeaders.CONTENT_DISPOSITION,
        contentDisposition(inline) + CONTENT_DISPOSITION_FILENAME_ARGUMENT + "\"" + streamResponse.getFilename() + "\"")
      .cacheControl(cacheControl).lastModified(lastModifiedDate).build();
  }

  public static Response okResponse(StreamResponse streamResponse, CacheControl cacheControl, EntityTag tag) {
    return okResponse(streamResponse, cacheControl, tag, false);
  }

  public static Response okResponse(StreamResponse streamResponse, CacheControl cacheControl, EntityTag tag,
    boolean inline) {
    return Response.ok(streamResponse.getStream(), streamResponse.getMediaType())
      .header(HttpHeaders.CONTENT_DISPOSITION,
        contentDisposition(inline) + CONTENT_DISPOSITION_FILENAME_ARGUMENT + "\"" + streamResponse.getFilename() + "\"")
      .cacheControl(cacheControl).tag(tag).build();
  }

  public static Response okResponse(StreamResponse streamResponse) {
    return okResponse(streamResponse, false);
  }

  public static Response okResponse(StreamResponse streamResponse, boolean inline) {
    return Response.ok(streamResponse.getStream(), streamResponse.getMediaType())
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

  public static <T extends IsIndexed> RODAObjectList<?> indexedResultToRODAObjectList(Class<T> objectClass,
    IndexResult<T> result)
    throws RequestNotValidException, NotFoundException, GenericException, AuthorizationDeniedException {
    if (objectClass.equals(IndexedAIP.class)) {
      AIPs aips = new AIPs();
      for (T object : result.getResults()) {
        IndexedAIP aip = (IndexedAIP) object;
        aips.addObject(RodaCoreFactory.getModelService().retrieveAIP(aip.getId()));
      }
      return aips;
    } else if (objectClass.equals(IndexedRepresentation.class)) {
      Representations representations = new Representations();
      for (T object : result.getResults()) {
        IndexedRepresentation representation = (IndexedRepresentation) object;
        representations.addObject(
          RodaCoreFactory.getModelService().retrieveRepresentation(representation.getAipId(), representation.getId()));
      }
      return representations;
    } else if (objectClass.equals(IndexedFile.class)) {
      org.roda.core.data.v2.ip.Files files = new org.roda.core.data.v2.ip.Files();
      for (T object : result.getResults()) {
        IndexedFile file = (IndexedFile) object;
        files.addObject(RodaCoreFactory.getModelService().retrieveFile(file.getAipId(), file.getRepresentationId(),
          file.getPath(), file.getId()));
      }
      return files;
    } else if (objectClass.equals(IndexedRisk.class)) {
      List<Risk> risks = new ArrayList<Risk>();
      for (T res : result.getResults()) {
        IndexedRisk irisk = (IndexedRisk) res;
        risks.add(irisk);
      }
      return new org.roda.core.data.v2.risks.Risks(risks);
    } else if (objectClass.equals(TransferredResource.class)) {
      return new org.roda.core.data.v2.ip.TransferredResources((List<TransferredResource>) result.getResults());
    } else if (objectClass.equals(Format.class)) {
      return new org.roda.core.data.v2.formats.Formats((List<Format>) result.getResults());
    } else if (objectClass.equals(Notification.class)) {
      return new org.roda.core.data.v2.notifications.Notifications((List<Notification>) result.getResults());
    } else if (objectClass.equals(LogEntry.class)) {
      return new org.roda.core.data.v2.log.LogEntries((List<LogEntry>) result.getResults());
    } else if (objectClass.equals(RiskIncidence.class)) {
      return new org.roda.core.data.v2.risks.RiskIncidences((List<RiskIncidence>) result.getResults());
    } else if (objectClass.equals(RODAMember.class)) {
      return new org.roda.core.data.v2.user.RODAMembers((List<RODAMember>) result.getResults());
    } else {
      throw new GenericException("Unsupported object class: " + objectClass);
    }
  }

}
