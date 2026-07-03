/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
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

import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.HttpEntity;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import org.apache.commons.io.IOUtils;
import org.roda.core.RodaCoreFactory;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.MarketException;
import org.roda.core.data.utils.JsonUtils;
import org.roda.core.data.v2.jobs.MarketInfo;
import org.roda.core.data.v2.synchronization.local.LocalInstance;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class MarketUtils {
  private static String retrieveRodaVersion() throws MarketException {
    try (InputStream inputStream = MarketUtils.class.getClassLoader().getResourceAsStream("static/version.json");
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

  public static String getResultNodeFromJson(String jsonString) throws GenericException, JacksonException {
    JsonMapper mapper = JsonMapper.builder().build();
    JsonNode rootNode = mapper.readTree(jsonString);
    JsonNode resultNode = rootNode.get("result");
    if (resultNode != null) {
      return resultNode.toString();
    } else {
      throw new GenericException("Unable to find 'result' field in JSON");
    }
  }

  public static void retrievePluginsListFromAPI(LocalInstance instance) throws MarketException {
    try {
      boolean collectVersion = Boolean.parseBoolean(RodaCoreFactory
        .getProperty(RodaConstants.ENVIRONMENT_COLLECT_VERSION, RodaConstants.DEFAULT_ENVIRONMENT_COLLECT_VERSION));

      String rodaVersion = collectVersion ? retrieveRodaVersion() : "development";

      String pluginUrl = RodaCoreFactory.getProperty(RodaConstants.MARKET_INFO_URL_PROPERTY,
        RodaConstants.DEFAULT_MARKET_INFO_URL);

      Path pluginInfoPath = RodaCoreFactory.getMarketDirectoryPath().resolve(RodaConstants.CORE_MARKET_FILE);

      CloseableHttpClient httpClient = HttpClientBuilder.create().build();
      HttpGet httpGet = new HttpGet(pluginUrl);
      httpGet.addHeader("Accept", "application/json");
      // TODO: RODA-LOCAL must have an instanceId at startup
      if (instance != null) {
        httpGet.addHeader("X-RODA-INSTANCE-ID", instance.getId());
      }
      httpGet.addHeader("X-RODA-VERSION", rodaVersion);
      ClassicHttpResponse response = httpClient.execute(httpGet);
      int responseStatusCode = response.getCode();

      if (responseStatusCode == 200) {
        HttpEntity entity = response.getEntity();
        if (entity != null) {
          String pluginInfo = null;
          // validate
          try (InputStream content = entity.getContent()) {
            String jsonString = getResultNodeFromJson(IOUtils.toString(content, StandardCharsets.UTF_8));
            List<MarketInfo> listFromJsonLines = JsonUtils.getListFromJson(jsonString, MarketInfo.class);
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
    } catch (IOException | GenericException | JacksonException e) {
      throw new MarketException("Unable to retrieve plugin list info from API", e);
    }
  }
}