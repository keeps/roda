package org.roda.core.common;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.roda.core.RodaCoreFactory;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.synchronization.local.LocalInstance;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class MarketUtils {
  public static void retrievePluginsListFromAPI(LocalInstance instance) throws IOException {
    String pluginUrl = RodaCoreFactory.getProperty(RodaConstants.MARKET_INFO_URL_PROPERTY,
      RodaConstants.DEFAULT_MARKET_INFO_URL) + "?instanceId=" + instance.getId();

    Path pluginInfoPath = RodaCoreFactory.getMarketDirectoryPath().resolve(RodaConstants.CORE_MARKET_FILE);

    CloseableHttpClient httpClient = HttpClientBuilder.create().build();
    HttpGet httpGet = new HttpGet(pluginUrl);
    HttpResponse response = httpClient.execute(httpGet);
    int responseStatusCode = response.getStatusLine().getStatusCode();

    if (responseStatusCode == 200) {
      HttpEntity entity = response.getEntity();
      if (entity != null) {
        Files.deleteIfExists(pluginInfoPath);
        Files.createFile(pluginInfoPath);

        try (InputStream content = entity.getContent();
          FileOutputStream outputStream = new FileOutputStream(pluginInfoPath.toFile())) {
          IOUtils.copy(content, outputStream);
        }
      }
    }
  }
}
