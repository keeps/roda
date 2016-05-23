/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.api.controllers;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.StreamingOutput;

import org.roda.core.RodaCoreFactory;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.wui.api.v1.utils.StreamResponse;
import org.roda.wui.common.RodaCoreService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Theme extends RodaCoreService {

  @SuppressWarnings("unused")
  private static final Logger LOGGER = LoggerFactory.getLogger(Theme.class);

  private static final Date INITIAL_DATE = new Date();

  private Theme() {
    super();
  }

  public static StreamResponse getResource(String resourceId) throws IOException, NotFoundException {
    StreamResponse streamResponse = null;

    final Path filePath;

    if (validExternalFile(resourceId)) {
      filePath = RodaCoreFactory.getThemePath().resolve(resourceId);
    } else {
      filePath = Paths.get(Theme.class.getResource(RodaConstants.THEME_RESOURCES_PATH + resourceId).getPath());
    }

    StreamingOutput streamingOutput = new StreamingOutput() {

      @Override
      public void write(OutputStream os) throws IOException, WebApplicationException {
        Files.copy(filePath, os);
      }
    };

    String mimeType;
    if (filePath.endsWith(".html")) {
      mimeType = "text/html";
    } else if (filePath.endsWith(".css")) {
      mimeType = "text/css";
    } else {
      mimeType = Files.probeContentType(filePath);
    }

    streamResponse = new StreamResponse(resourceId, mimeType, streamingOutput);

    return streamResponse;
  }

  public static Date getLastModifiedDate(String resourceId, boolean externalFile) throws IOException {
    Date modifiedDate = new Date();

    if (externalFile) {
      Path filePath = RodaCoreFactory.getThemePath().resolve(resourceId);
      modifiedDate = new Date(Files.getLastModifiedTime(filePath).toMillis());
    } else {
      modifiedDate = INITIAL_DATE;
    }

    return modifiedDate;
  }

  public static boolean exists(String resourceId) {
    return (validExternalFile(resourceId) || validInternalFile(resourceId));
  }

  public static boolean validExternalFile(String resourceId) {
    Path themePath = RodaCoreFactory.getThemePath();
    Path filePath = themePath.resolve(resourceId);

    return Files.exists(filePath) && !Files.isDirectory(filePath)
      && filePath.toAbsolutePath().startsWith(themePath.toAbsolutePath().toString());
  }

  public static boolean validInternalFile(String resourceId) {
    return (Theme.class.getResource(RodaConstants.THEME_RESOURCES_PATH + resourceId) != null
      && !resourceId.contains(".."));
  }
}