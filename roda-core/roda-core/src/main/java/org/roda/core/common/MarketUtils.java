package org.roda.core.common;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.roda.core.RodaCoreFactory;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.MarketException;
import org.roda.core.data.utils.JsonUtils;
import org.roda.core.data.v2.jobs.MarketInfo;
import org.roda.core.data.v2.synchronization.local.LocalInstance;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class MarketUtils {
  private static String retrieveRodaVersion() throws MarketException {
    Path versionFile = Paths.get(MarketUtils.class.getClassLoader().getResource("version.json").getPath());
    if (!Files.exists(versionFile)) {
      throw new MarketException("Unable to retrieve RODA version");
    }

    try (BufferedReader bufferedReader = new BufferedReader(new FileReader(versionFile.toFile()))) {
      StringBuilder builder = new StringBuilder();
      for (String line = null; (line = bufferedReader.readLine()) != null;) {
        builder.append(line).append("\n");
      }

      JsonNode jsonObject = JsonUtils.parseJson(builder.toString());
      String version = jsonObject.get("git.build.version").textValue();
      if (version == null || version.isEmpty()) {
        throw new MarketException("Unable to retrieve RODA version");
      }
      return version;
    } catch (Exception e) {
      throw new MarketException("Unable to retrieve RODA version", e);
    }
  }

  public static void retrievePluginsListFromAPI(LocalInstance instance) throws MarketException {
    try {
      String rodaVersion = retrieveRodaVersion();
      String pluginUrl = RodaCoreFactory.getProperty(RodaConstants.MARKET_INFO_URL_PROPERTY,
        RodaConstants.DEFAULT_MARKET_INFO_URL) + "?rodaVersion=" + rodaVersion;

      Path pluginInfoPath = RodaCoreFactory.getMarketDirectoryPath().resolve(RodaConstants.CORE_MARKET_FILE);

      CloseableHttpClient httpClient = HttpClientBuilder.create().build();
      HttpGet httpGet = new HttpGet(pluginUrl);
      httpGet.addHeader("Accept", "application/jsonlines");
      httpGet.addHeader("X-RODA-INSTANCE-ID", instance.getId());
      httpGet.addHeader("X-RODA-VERSION", rodaVersion);
      HttpResponse response = httpClient.execute(httpGet);
      int responseStatusCode = response.getStatusLine().getStatusCode();

      if (responseStatusCode == 200) {
        HttpEntity entity = response.getEntity();
        if (entity != null) {
          String pluginInfo = null;
          // validate
          try (InputStream content = entity.getContent()) {
            List<MarketInfo> listFromJsonLines = JsonUtils.getListFromJsonLines(content, MarketInfo.class);
            pluginInfo = JsonUtils.getJsonLinesFromObjectList(listFromJsonLines);
          }
          // Create/update Market File
          if (!pluginInfo.isEmpty()) {
            Files.deleteIfExists(pluginInfoPath);
            Files.createFile(pluginInfoPath);
            try (FileOutputStream outputStream = new FileOutputStream(pluginInfoPath.toFile())) {
              IOUtils.copy(new ByteArrayInputStream(pluginInfo.getBytes()), outputStream);
            }
          }
        }
      }
    } catch (Exception e) {
      throw new MarketException("Unable to retrieve plugin list info from API", e);
    }
  }
}
