package org.roda.wui.api.v2.services;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.roda.core.RodaCoreFactory;
import org.roda.core.common.ProvidesInputStream;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.ConsumesOutputStream;
import org.roda.core.data.v2.StreamResponse;
import org.roda.core.data.v2.common.Pair;
import org.roda.wui.api.v2.utils.MimeTypeUtils;
import org.springframework.stereotype.Service;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
@Service
public class ThemeService {
  private static final Date INITIAL_DATE = new Date();

  public Pair<String, ProvidesInputStream> getThemeResource(String id, String fallbackResourceId, String type) {
    String normalizedID = FilenameUtils.normalize(id);
    String normalizedFallBackResource = FilenameUtils.normalize(fallbackResourceId);
    Pair<String, ProvidesInputStream> ret;
    Path resourcePath;
    String resourceId;

    if (RodaConstants.ResourcesTypes.PLUGINS.toString().equals(type)) {
      resourcePath = RodaCoreFactory.getConfigPath()
        .resolve(RodaConstants.CORE_PLUGINS_FOLDER);
      resourceId = RodaConstants.CORE_PLUGINS_FOLDER + "/" + normalizedID;
    } else {
      resourcePath = RodaCoreFactory.getConfigPath().resolve(RodaConstants.CORE_THEME_FOLDER);
      resourceId = RodaConstants.CORE_THEME_FOLDER + "/" + normalizedID;
    }

    URL url = RodaCoreFactory.getConfigurationFile(resourceId);
    if (url != null) {
      ret = Pair.of(id,
        () -> RodaCoreFactory.getConfigurationFileAsStream(resourcePath, normalizedID));
    } else {
      ret = Pair.of(normalizedFallBackResource,
        () -> RodaCoreFactory.getConfigurationFileAsStream(resourcePath, normalizedFallBackResource));
    }

    return ret;
  }

  public StreamResponse getThemeResourceStreamResponse(final Pair<String, ProvidesInputStream> themeResourceInputStream,
    String type) {
    final String resourceId = themeResourceInputStream.getFirst();
    final String mimeType = MimeTypeUtils.getContentType(resourceId);

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
        try (InputStream in = themeResourceInputStream.getSecond().createInputStream()) {
          IOUtils.copy(in, out);
        }
      }

      @Override
      public Date getLastModified() {
        Date lastModified = null;
        try {
          lastModified = getLastModifiedDate(resourceId, type);
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

  private Date getLastModifiedDate(String resourceId, String type) throws IOException {
    Date modifiedDate;
    URL file;
    if (RodaConstants.ResourcesTypes.PLUGINS.toString().equals(type)) {
      file = RodaCoreFactory
        .getConfigurationFile(RodaConstants.CORE_PLUGINS_FOLDER + "/" + resourceId);
    } else {
      file = RodaCoreFactory.getConfigurationFile(RodaConstants.CORE_THEME_FOLDER + "/" + resourceId);
    }

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
