package org.roda.wui.api.v2.utils;

import org.roda.core.RodaCoreFactory;
import org.roda.core.common.DownloadUtils;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.ConsumesOutputStream;
import org.roda.core.data.v2.StreamResponse;
import org.roda.core.storage.BinaryConsumesOutputStream;
import org.roda.core.storage.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRange;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.time.Duration;
import java.util.Date;
import java.util.concurrent.TimeUnit;

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

  public static StreamResponse download(Resource resource)
      throws GenericException, RequestNotValidException, NotFoundException, AuthorizationDeniedException {
    return download(resource, null);
  }

  public static StreamResponse download(Resource resource, String fileName)
      throws GenericException, RequestNotValidException, NotFoundException, AuthorizationDeniedException {
    ConsumesOutputStream download = DownloadUtils.download(RodaCoreFactory.getStorageService(), resource, fileName);
    return new StreamResponse(download);
  }

  public static StreamResponse download(Resource resource, String fileName, boolean addTopDirectory)
      throws GenericException, RequestNotValidException, NotFoundException, AuthorizationDeniedException {
    ConsumesOutputStream download = DownloadUtils.download(RodaCoreFactory.getStorageService(), resource, fileName,
        addTopDirectory);
    return new StreamResponse(download);
  }

  private ApiUtils() {
    // empty constructor
  }
}
