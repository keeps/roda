package org.roda.wui.api.v2.utils;

import java.time.Duration;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import org.roda.core.RodaCoreFactory;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.ConsumesOutputStream;
import org.roda.core.data.v2.IsRODAObject;
import org.roda.core.data.v2.LiteRODAObject;
import org.roda.core.data.v2.StreamResponse;
import org.roda.core.data.v2.common.Pair;
import org.roda.core.model.ModelService;
import org.roda.core.storage.BinaryConsumesOutputStream;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRange;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

public class ApiUtils {

  public static ResponseEntity<StreamingResponseBody> okResponse(StreamResponse streamResponse) {
    return okResponse(streamResponse, null);
  }

  public static ResponseEntity<StreamingResponseBody> okResponse(StreamResponse streamResponse, WebRequest request) {
    if (request != null && request.checkNotModified(streamResponse.getLastModified().getTime())) {
      return ResponseEntity.status(304).build();
    }

    HttpHeaders responseHeaders = new HttpHeaders();
    StreamingResponseBody responseStream = outputStream -> streamResponse.getStream().consumeOutputStream(outputStream);

    responseHeaders.add("Content-Type", streamResponse.getStream().getMediaType());
    responseHeaders.add(HttpHeaders.CONTENT_DISPOSITION,
      "attachment; filename=\"" + streamResponse.getStream().getFileName() + "\"");
    responseHeaders.add("Content-Length", String.valueOf(streamResponse.getStream().getSize()));

    Date lastModifiedDate = streamResponse.getStream().getLastModified();

    if (lastModifiedDate != null) {
      CacheControl cacheControl = CacheControl.maxAge(1, TimeUnit.HOURS).cachePrivate().noTransform();
      String eTag = lastModifiedDate.toInstant().toString();
      return ResponseEntity.ok().headers(responseHeaders).cacheControl(cacheControl).eTag(eTag).body(responseStream);
    }

    return ResponseEntity.ok().headers(responseHeaders).body(responseStream);
  }

  public static ResponseEntity<StreamingResponseBody> rangeResponse(HttpHeaders headers,
    ConsumesOutputStream consumesOutputStream) {
    final HttpHeaders responseHeaders = new HttpHeaders();
    StreamingResponseBody responseStream;

    if (headers.getRange().isEmpty()) {
      responseStream = consumesOutputStream::consumeOutputStream;
      responseHeaders.add("Content-Type", consumesOutputStream.getMediaType());
      responseHeaders.add(HttpHeaders.CONTENT_DISPOSITION,
        "attachment; filename=\"" + consumesOutputStream.getFileName() + "\"");
      responseHeaders.add("Content-Length", String.valueOf(consumesOutputStream.getSize()));

      return ResponseEntity.ok().headers(responseHeaders).body(responseStream);
    }

    HttpRange range = headers.getRange().getFirst();
    long start = range.getRangeStart(consumesOutputStream.getSize());
    long end = range.getRangeEnd(consumesOutputStream.getSize());

    String contentLength = String.valueOf((end - start) + 1);
    responseHeaders.add(HttpHeaders.CONTENT_TYPE, consumesOutputStream.getMediaType());
    responseHeaders.add(HttpHeaders.CONTENT_LENGTH, contentLength);
    responseHeaders.add(HttpHeaders.CONTENT_DISPOSITION,
      "inline; filename=\"" + consumesOutputStream.getFileName() + "\"");
    responseHeaders.add(HttpHeaders.ACCEPT_RANGES, "bytes");
    responseHeaders.add(HttpHeaders.CONTENT_RANGE,
      "bytes" + " " + start + "-" + end + "/" + consumesOutputStream.getSize());

    responseStream = os -> ((BinaryConsumesOutputStream) consumesOutputStream).consumeOutputStream(os, start, end);

    Date lastModifiedDate = consumesOutputStream.getLastModified();
    if (lastModifiedDate != null) {
      CacheControl cacheControl = CacheControl.empty().cachePrivate().sMaxAge(Duration.ofSeconds(60));
      responseHeaders.add(HttpHeaders.CACHE_CONTROL, cacheControl.getHeaderValue());
      responseHeaders.setETag(Long.toString(lastModifiedDate.getTime()));
      responseHeaders.add(HttpHeaders.LAST_MODIFIED, consumesOutputStream.getLastModified().toString());
    }

    return new ResponseEntity<>(responseStream, responseHeaders, HttpStatus.PARTIAL_CONTENT);
  }

  public static StreamResponse download(IsRODAObject object, String... pathPartials)
      throws GenericException, RequestNotValidException, NotFoundException, AuthorizationDeniedException {
    return download(object, null, false, pathPartials);
  }

  public static StreamResponse download(LiteRODAObject lite, String... pathPartials)
          throws GenericException, RequestNotValidException, NotFoundException, AuthorizationDeniedException {
    return download(lite, null, false, pathPartials);
  }

  public static StreamResponse download(IsRODAObject object, String fileName, boolean addTopDirectory,
    String... pathPartials)
      throws GenericException, RequestNotValidException, NotFoundException, AuthorizationDeniedException {
    ModelService model = RodaCoreFactory.getModelService();
    ConsumesOutputStream download = model.exportObjectToStream(object, fileName, addTopDirectory, pathPartials);
    return new StreamResponse(download);
  }

  public static StreamResponse download(LiteRODAObject lite, String fileName, boolean addTopDirectory,
                                        String... pathPartials)
          throws GenericException, RequestNotValidException, NotFoundException, AuthorizationDeniedException {
    ModelService model = RodaCoreFactory.getModelService();
    ConsumesOutputStream download = model.exportObjectToStream(lite, fileName, addTopDirectory, pathPartials);
    return new StreamResponse(download);
  }

  /**
   * Returns valid start (pair first element) and limit (pair second element)
   * paging parameters defaulting to start = 0 and limit = 100 if none or invalid
   * values are provided.
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

  private ApiUtils() {
    // empty constructor
  }
}
