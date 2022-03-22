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
public final class CentralEntitiesJsonUtils {
  private static final Logger LOGGER = LoggerFactory.getLogger(CentralEntitiesJsonUtils.class);

  public CentralEntitiesJsonUtils() {
    // do Nothing
  }

  public static void writeListToFile(final List<String> list, final Path path) throws IOException {
    if (!Files.exists(path)) {
      Files.createFile(path);
    }
    OutputStream outputStream = null;
    JsonGenerator jsonGenerator = null;
    try {
      if (path != null) {
        outputStream = new BufferedOutputStream(new FileOutputStream(path.toFile()));
      }
      final JsonFactory jsonFactory = new JsonFactory();
      jsonGenerator = jsonFactory.createGenerator(outputStream, JsonEncoding.UTF8).useDefaultPrettyPrinter();
      jsonGenerator.writeStartArray();
      for (String value : list) {
        jsonGenerator.writeString(value);
      }
      jsonGenerator.writeEndArray();

    } catch (final IOException e) {
      LOGGER.error("Can't create report for removed entities {}", e.getMessage());
    }

    close(outputStream, jsonGenerator);
  }

  public static void writeJsonToFile(final CentralEntities centralEntities, final Path path) throws IOException {
    if (!Files.exists(path)) {
      Files.createFile(path);
    }
    OutputStream outputStream = null;
    JsonGenerator jsonGenerator = null;
    try {
      if (path != null) {
        outputStream = new BufferedOutputStream(new FileOutputStream(path.toFile()));
      }
      final JsonFactory jsonFactory = new JsonFactory();
      jsonGenerator = jsonFactory.createGenerator(outputStream, JsonEncoding.UTF8).useDefaultPrettyPrinter();
      jsonGenerator.writeStartObject();
      writeJsonArray(jsonGenerator, centralEntities.getAipsList(), RodaConstants.SYNCHRONIZATION_REPORT_KEY_LIST_AIP);
      writeJsonArray(jsonGenerator, centralEntities.getDipsList(), RodaConstants.SYNCHRONIZATION_REPORT_KEY_LIST_DIP);
      writeJsonArray(jsonGenerator, centralEntities.getRisksList(), RodaConstants.SYNCHRONIZATION_REPORT_KEY_LIST_RISK);
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
      close(outputStream, jsonGenerator);
    }
  }

  private static void writeJsonArray(final JsonGenerator jsonGenerator, List<String> array, String key)
    throws IOException {
    jsonGenerator.writeFieldName(key);
    jsonGenerator.writeStartArray();
    for (String value : array) {
      jsonGenerator.writeString(value);
    }
    jsonGenerator.writeEndArray();
  }

  private static void close(final OutputStream outputStream, final JsonGenerator jsonGenerator) throws IOException {
    if (jsonGenerator != null) {
      jsonGenerator.close();
    }
    if (outputStream != null) {
      outputStream.close();
    }

  }

  public static JsonParser createJsonParser(Path path) throws IOException {
    JsonFactory jfactory = new JsonFactory();
    return jfactory.createParser(path.toFile());
  }
}
