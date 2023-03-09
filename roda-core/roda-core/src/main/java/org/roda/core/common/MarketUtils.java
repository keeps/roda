package org.roda.core.common;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.roda.core.RodaCoreFactory;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.GenericException;
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
    try (InputStream inputStream = MarketUtils.class.getClassLoader().getResourceAsStream("version.json");
      BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
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
    } catch (GenericException | IOException e) {
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
      // TODO: RODA-LOCAL must have an instanceId at startup
      if(instance != null){
        httpGet.addHeader("X-RODA-INSTANCE-ID", instance.getId());
      }
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
    } catch (IOException | GenericException e) {
      throw new MarketException("Unable to retrieve plugin list info from API", e);
    }
  }
}
