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

import org.apache.commons.io.IOUtils;
import org.roda.core.RodaCoreFactory;
import org.roda.core.common.ConsumesOutputStream;
import org.roda.core.common.StreamResponse;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.common.Pair;
import org.roda.wui.common.RodaWuiController;

public class Theme extends RodaWuiController {
  private static final Date INITIAL_DATE = new Date();

  private Theme() {
    super();
  }

  public static Pair<String, InputStream> getThemeResource(String id, String fallbackResourceId) {
    String resourceId = id;
    InputStream themeResourceInputstream = RodaCoreFactory
      .getConfigurationFileAsStream(RodaConstants.CORE_THEME_FOLDER + "/" + resourceId);

    if (themeResourceInputstream == null) {
      themeResourceInputstream = RodaCoreFactory
        .getConfigurationFileAsStream(RodaConstants.CORE_THEME_FOLDER + "/" + fallbackResourceId);
      resourceId = fallbackResourceId;
    }

    return Pair.of(resourceId, themeResourceInputstream);
  }

  public static StreamResponse getThemeResourceStreamResponse(
    final Pair<String, InputStream> themeResourceInputstream) {
    final String resourceId = themeResourceInputstream.getFirst();
    final String mimeType = MimeTypeHelper.getContentType(resourceId);

    ConsumesOutputStream stream = new ConsumesOutputStream() {

      @Override
      public String getMediaType() {
        return mimeType;
      }

      @Override
      public String getFileName() {
        return resourceId;
      }

      @Override
      public void consumeOutputStream(OutputStream out) throws IOException {
        try (InputStream in = themeResourceInputstream.getSecond()) {
          IOUtils.copy(in, out);
        }
      }

      @Override
      public Date getLastModified() {
        Date lastModified = null;
        try {
          lastModified = Theme.getLastModifiedDate(resourceId);
        } catch (IOException e) {
          // do nothing
        }
        return lastModified;
      }

      @Override
      public long getSize() {
        return -1;
      }
    };

    return new StreamResponse(stream);
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