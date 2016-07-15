/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.api.controllers;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.StreamingOutput;

import org.apache.commons.io.IOUtils;
import org.roda.core.RodaCoreFactory;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.v2.common.Pair;
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

  public static Pair<String, InputStream> getThemeResource(String resourceId, String fallbackResourceId) {
    InputStream themeResourceInputstream = RodaCoreFactory
      .getConfigurationFileAsStream(RodaConstants.CORE_THEME_FOLDER + "/" + resourceId);
    if (themeResourceInputstream == null) {
      themeResourceInputstream = RodaCoreFactory
        .getConfigurationFileAsStream(RodaConstants.CORE_THEME_FOLDER + "/" + fallbackResourceId);
      resourceId = fallbackResourceId;
    }
    return new Pair<>(resourceId, themeResourceInputstream);
  }

  public static StreamResponse getThemeResourceStreamResponse(final Pair<String, InputStream> themeResourceInputstream)
    throws IOException, NotFoundException {
    StreamResponse streamResponse = null;

    StreamingOutput streamingOutput = new StreamingOutput() {
      @Override
      public void write(OutputStream os) throws IOException, WebApplicationException {
        IOUtils.copy(themeResourceInputstream.getSecond(), os);
      }
    };

    String resourceId = themeResourceInputstream.getFirst();
    String mimeType;
    if (resourceId.endsWith(".html")) {
      mimeType = MediaType.TEXT_HTML;
    } else if (resourceId.endsWith(".css")) {
      mimeType = "text/css";
    } else if (resourceId.endsWith(".png")) {
      mimeType = "image/png";
    } else if (resourceId.endsWith(".js")) {
      mimeType = "text/javascript";
    } else {
      mimeType = MediaType.APPLICATION_OCTET_STREAM;
    }

    streamResponse = new StreamResponse(resourceId, mimeType, streamingOutput);

    return streamResponse;
  }

  public static Date getLastModifiedDate(String resourceId) throws IOException {
    Date modifiedDate;
    URL file = RodaCoreFactory.getConfigurationFile(RodaConstants.CORE_THEME_FOLDER + "/" + resourceId);

    if ("file".equalsIgnoreCase(file.getProtocol())) {
      try {
        Path filePath = Paths.get(file.toURI());
        modifiedDate = new Date(Files.getLastModifiedTime(filePath).toMillis());
      } catch (URISyntaxException e) {
        modifiedDate = INITIAL_DATE;
      }
    } else {
      modifiedDate = INITIAL_DATE;
    }

    return modifiedDate;
  }

}