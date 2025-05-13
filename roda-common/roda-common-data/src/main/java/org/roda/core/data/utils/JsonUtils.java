/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data.utils;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.io.IOUtils;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.v2.ip.metadata.DescriptiveMetadata;
import org.roda.core.data.v2.ip.metadata.DescriptiveMetadataMixIn;
import org.roda.core.data.v2.ip.metadata.TechnicalMetadata;
import org.roda.core.data.v2.ip.metadata.TechnicalMetadataMixIn;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.type.TypeFactory;

public final class JsonUtils {
  private static final Logger LOGGER = LoggerFactory.getLogger(JsonUtils.class);
  private static final String JSON_ERROR_MESSAGE = "Error while parsing JSON";
  private static final String ERROR_TRANSFORMING_OBJECT_TO_JSON_STRING = "Error transforming object '{}' to json string";

  private JsonUtils() {
    // do nothing
  }

  public static <T> T readObjectFromFile(Path jsonFile, Class<T> objectClass) throws GenericException {
    try (InputStream stream = Files.newInputStream(jsonFile)) {
      return getObjectFromJson(stream, objectClass);
    } catch (IOException e) {
      throw new GenericException(e);
    }
  }

  public static void writeObjectToFile(Object object, Path file) throws GenericException {
    try {
      String json = getJsonFromObject(object);
      if (json != null) {
        Files.write(file, json.getBytes(), StandardOpenOption.CREATE);
      }
    } catch (IOException e) {
      throw new GenericException("Error writing object, as json, to file", e);
    }
  }

  public static void appendObjectToFile(Object object, Path file) throws GenericException {
    try {
      String json = getJsonFromObject(object) + "\n";
      Files.write(file, json.getBytes(), StandardOpenOption.APPEND);
    } catch (IOException e) {
      throw new GenericException("Error writing object, as json, to file", e);
    }
  }

  public static Map<String, String> getMapFromJson(String json) {
    Map<String, String> ret = new HashMap<>();
    try {
      ObjectMapper mapper = new ObjectMapper(new JsonFactory());
      ret = mapper.readValue(json, new TypeReference<Map<String, String>>() {});
    } catch (IOException e) {
      LOGGER.error("Error transforming json string to Map<String,String>", e);
    }
    return ret;
  }

  public static String getJsonFromObject(Object object) {
    return getJsonFromObject(object, null);
  }

  public static String getJsonFromObject(Object object, Class<?> mixin) {
    String ret = null;
    try {
      ObjectMapper mapper = new ObjectMapper(new JsonFactory());
      addMixinsToMapper(mapper, object, mixin);
      ret = mapper.writeValueAsString(object);
    } catch (IOException e) {
      LOGGER.error(ERROR_TRANSFORMING_OBJECT_TO_JSON_STRING, object, e);
    }
    return ret;
  }

  public static <T> String getJsonLinesFromObjectList(List<T> objectList) {
    StringBuilder ret = new StringBuilder();
    for (Object object : objectList) {
      try {
        if (!ret.isEmpty()) {
          ret.append("\n");
        }
        ObjectMapper mapper = new ObjectMapper(new JsonFactory());
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
        ret.append(mapper.writer().writeValueAsString(object));
      } catch (IOException e) {
        LOGGER.error(ERROR_TRANSFORMING_OBJECT_TO_JSON_STRING, object, e);
      }
    }
    return ret.toString();
  }

  private static void addMixinsToMapper(ObjectMapper mapper, Object object, Class<?> mixin) {
    if (!(object instanceof DescriptiveMetadata)) {
      if (object instanceof List<?> objectList) {
        if (!objectList.isEmpty() && !(objectList.getFirst() instanceof DescriptiveMetadata)) {
          mapper.addMixIn(DescriptiveMetadata.class, DescriptiveMetadataMixIn.class);
        }
      } else {
        mapper.addMixIn(DescriptiveMetadata.class, DescriptiveMetadataMixIn.class);
      }
    }

    if (!(object instanceof TechnicalMetadata)) {
      if (object instanceof List<?> objectList) {
        if (!objectList.isEmpty() && !(objectList.getFirst() instanceof TechnicalMetadata)) {
          mapper.addMixIn(TechnicalMetadata.class, TechnicalMetadataMixIn.class);
        }
      } else {
        mapper.addMixIn(TechnicalMetadata.class, TechnicalMetadataMixIn.class);
      }
    }

    if (mixin != null) {
      mapper.addMixIn(object.getClass(), mixin);
    }
  }

  public static <T> T getObjectFromJson(Path json, Class<T> objectClass) throws GenericException {
    T ret;
    InputStream stream = null;
    try {
      stream = Files.newInputStream(json);
      String jsonString = IOUtils.toString(stream, StandardCharsets.UTF_8);
      ret = getObjectFromJson(jsonString, objectClass);
    } catch (IOException e) {
      throw new GenericException(JSON_ERROR_MESSAGE, e);
    } finally {
      IOUtils.closeQuietly(stream);
    }
    return ret;
  }

  public static <T> T getObjectFromJson(InputStream json, Class<T> objectClass) throws GenericException {
    T ret;
    try {
      String jsonString = IOUtils.toString(json, StandardCharsets.UTF_8);
      ret = getObjectFromJson(jsonString, objectClass);
    } catch (IOException e) {
      throw new GenericException(e);
    } finally {
      IOUtils.closeQuietly(json);
    }
    return ret;
  }

  public static <T> T getObjectFromJson(String json, Class<T> objectClass) throws GenericException {
    try {
      ObjectMapper mapper = new ObjectMapper(new JsonFactory());
      return mapper.readValue(json, objectClass);
    } catch (IOException e) {
      throw new GenericException(JSON_ERROR_MESSAGE, e);
    }
  }

  public static <T> List<T> getListFromJsonLines(InputStream jsonLines, Class<T> objectClass) throws GenericException {
    ArrayList<T> list = new ArrayList<>();
    try {
      InputStreamReader inputStreamReader = new InputStreamReader(jsonLines, StandardCharsets.UTF_8);
      BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
      String json;
      while ((json = bufferedReader.readLine()) != null) {
        list.add(getObjectFromJson(json, objectClass));
      }
    } catch (IOException e) {
      throw new GenericException(JSON_ERROR_MESSAGE, e);
    }
    return list;
  }

  public static <T> List<T> getListFromJson(String json, Class<T> objectClass) throws GenericException {
    try {
      ObjectMapper mapper = new ObjectMapper(new JsonFactory());
      TypeFactory t = TypeFactory.defaultInstance();
      return mapper.readValue(json, t.constructCollectionType(ArrayList.class, objectClass));
    } catch (IOException e) {
      throw new GenericException(JSON_ERROR_MESSAGE, e);
    }
  }

  public static JsonNode parseJson(String json) throws GenericException {
    try {
      ObjectMapper mapper = new ObjectMapper(new JsonFactory());
      return mapper.readTree(json);
    } catch (IOException e) {
      throw new GenericException(JSON_ERROR_MESSAGE, e);
    }
  }

  public static JsonNode parseJson(InputStream json) throws GenericException {
    try {
      ObjectMapper mapper = new ObjectMapper(new JsonFactory());
      return mapper.readTree(json);
    } catch (IOException e) {
      throw new GenericException(JSON_ERROR_MESSAGE, e);
    } finally {
      IOUtils.closeQuietly(json);
    }
  }

  public static String getJsonFromNode(JsonNode node) {
    String ret = null;
    try {
      ObjectMapper mapper = new ObjectMapper(new JsonFactory());
      ret = mapper.writeValueAsString(node);
    } catch (IOException e) {
      LOGGER.error(ERROR_TRANSFORMING_OBJECT_TO_JSON_STRING, node, e);
    }
    return ret;
  }

  public static ObjectNode refactor(ObjectNode obj, Map<String, String> mapping) {
    for (Entry<String, String> entry : mapping.entrySet()) {
      String oldName = entry.getKey();
      String newName = entry.getValue();

      JsonNode jsonNode = obj.get(oldName);

      if (jsonNode != null) {
        obj.set(newName, jsonNode);
        obj.remove(oldName);
      }
    }

    return obj;
  }

  public static long calculateNumberOfLines(Path file) {
    long res = 0;
    try (InputStream is = new BufferedInputStream(Files.newInputStream(file))) {
      byte[] c = new byte[1024];

      int readChars = is.read(c);
      if (readChars == -1) {
        // bail out if nothing to read
        return res;
      }

      // make it easy for the optimizer to tune this loop
      while (readChars == 1024) {
        for (int i = 0; i < 1024; i++) {
          if (c[i] == '\n') {
            ++res;
          }
        }
        readChars = is.read(c);
      }

      // count remaining characters
      while (readChars != -1) {
        for (int i = 0; i < readChars; ++i) {
          if (c[i] == '\n') {
            ++res;
          }
        }
        readChars = is.read(c);
      }

    } catch (IOException e) {
      // do nothing
    }
    return res;
  }

}
