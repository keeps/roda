package org.roda.core.data.utils;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.synchronization.bundle.CentralEntities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;

/**
 * {@author João Gomes <jgomes@keep.pt>}.
 */
public class CentralEntitiesJsonUtils {
  private static final Logger LOGGER = LoggerFactory.getLogger(CentralEntitiesJsonUtils.class);
  private static OutputStream outputStream = null;
  private static JsonGenerator jsonGenerator = null;

  public CentralEntitiesJsonUtils() {
    // do Nothing
  }

  public static void init(Path path) throws IOException {
    if (!Files.exists(path)) {
      Files.createFile(path);
    }

    if (path != null) {
      outputStream = new BufferedOutputStream(new FileOutputStream(path.toFile()));
      final JsonFactory jsonFactory = new JsonFactory();
      jsonGenerator = jsonFactory.createGenerator(outputStream, JsonEncoding.UTF8).useDefaultPrettyPrinter();
    }

    jsonGenerator.writeStartArray();
  }

  public static void writeString(String value) throws IOException {
    jsonGenerator.writeString(value);
  }

  /**
   * Write the {@link CentralEntities} lists to a file.
   * 
   * @param centralEntities
   *          {@link CentralEntities}.
   * @param path
   *          {@link Path}.
   * @throws IOException
   *           if some i/o error occurs.
   */
  public static void writeJsonToFile(final CentralEntities centralEntities, final Path path) throws IOException {
    if (!Files.exists(path)) {
      Files.createFile(path);
    }
    try {
      if (path != null && outputStream == null && jsonGenerator == null) {
        outputStream = new BufferedOutputStream(new FileOutputStream(path.toFile()));
        final JsonFactory jsonFactory = new JsonFactory();
        jsonGenerator = jsonFactory.createGenerator(outputStream, JsonEncoding.UTF8).useDefaultPrettyPrinter();
      }

      jsonGenerator.writeStartObject();
      writeJsonArray(jsonGenerator, centralEntities.getAipsList(),
        RodaConstants.SYNCHRONIZATION_REPORT_KEY_LIST_REMOVED_AIP);
      writeJsonArray(jsonGenerator, centralEntities.getDipsList(),
        RodaConstants.SYNCHRONIZATION_REPORT_KEY_LIST_REMOVED_DIP);
      writeJsonArray(jsonGenerator, centralEntities.getRisksList(),
        RodaConstants.SYNCHRONIZATION_REPORT_KEY_LIST_REMOVED_RISK);
      writeJsonArray(jsonGenerator, centralEntities.getMissingAips(),
        RodaConstants.SYNCHRONIZATION_REPORT_KEY_LIST_MISSING_AIP);
      writeJsonArray(jsonGenerator, centralEntities.getMissingDips(),
        RodaConstants.SYNCHRONIZATION_REPORT_KEY_LIST_MISSING_DIP);
      writeJsonArray(jsonGenerator, centralEntities.getMissingRisks(),
        RodaConstants.SYNCHRONIZATION_REPORT_KEY_LIST_MISSING_RISK);
      jsonGenerator.writeEndObject();
    } catch (final IOException e) {
      LOGGER.error("Can't create report for removed entities {}", e.getMessage());
    } finally {
      close(false);
    }
  }

  /**
   * Write an JSON array in the file.
   * 
   * @param jsonGenerator
   *          {@link JsonGenerator}.
   * @param array
   *          {@link List}.
   * @param key
   *          the key to array object.
   * @throws IOException
   *           if some i/o error occurs.
   */
  private static void writeJsonArray(final JsonGenerator jsonGenerator, List<String> array, String key)
    throws IOException {
    jsonGenerator.writeFieldName(key);
    jsonGenerator.writeStartArray();
    if (!array.isEmpty()) {
      for (String value : array) {
        jsonGenerator.writeString(value);
      }
    }
    jsonGenerator.writeEndArray();
  }

  /**
   * Close the {@link JsonGenerator} and the {@link OutputStream}.
   *
   * @throws IOException
   *           if some i/o error occurs.
   */
  public static void close(boolean closeArray) throws IOException {
    if (closeArray) {
      jsonGenerator.writeEndArray();
    }

    if (jsonGenerator != null) {
      jsonGenerator.close();
    }
    if (outputStream != null) {
      outputStream.close();
    }

  }

  /**
   * Create {@link JsonParser} to read the file.
   * 
   * @param path
   *          {@link Path}.
   * @return {@link JsonParser}.
   * @throws IOException
   *           if some i/o error occurs.
   */
  public static JsonParser createJsonParser(Path path) throws IOException {
    final JsonFactory jfactory = new JsonFactory();
    return jfactory.createParser(path.toFile());
  }
}
