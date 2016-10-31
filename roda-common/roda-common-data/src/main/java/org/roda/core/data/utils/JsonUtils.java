/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data.utils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.v2.ip.metadata.DescriptiveMetadata;
import org.roda.core.data.v2.ip.metadata.DescriptiveMetadataMixIn;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;

public final class JsonUtils {

  private static final Logger LOGGER = LoggerFactory.getLogger(JsonUtils.class);

  /** Private empty constructor */
  private JsonUtils() {

  }

  public static <T> T readObjectFromFile(Path jsonFile, Class<T> objectClass) throws GenericException {
    InputStream stream;
    try {
      stream = Files.newInputStream(jsonFile);
      T obj = getObjectFromJson(stream, objectClass);
      IOUtils.closeQuietly(stream);
      return obj;
    } catch (IOException e) {
      throw new GenericException(e);
    }
  }

  public static void writeObjectToFile(Object object, Path file) throws GenericException {
    try {
      String json = getJsonFromObject(object);
      Files.write(file, json.getBytes(), StandardOpenOption.CREATE);
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
    Map<String, String> ret = new HashMap<String, String>();
    try {
      ObjectMapper mapper = new ObjectMapper(new JsonFactory());
      ret = mapper.readValue(json, new TypeReference<Map<String, String>>() {});
    } catch (IOException e) {
      LOGGER.error("Error transforming json string to Map<String,String>", e);
    }
    return ret;
  }

  public static String getJsonFromObject(Object object) {
    String ret = null;
    try {
      ObjectMapper mapper = new ObjectMapper(new JsonFactory());
      mapper = addMixinsToMapper(mapper, object);
      ret = mapper.writeValueAsString(object);
    } catch (IOException e) {
      LOGGER.error("Error transforming object '{}' to json string", object, e);
    }
    return ret;
  }

  private static ObjectMapper addMixinsToMapper(ObjectMapper mapper, Object object) {
    if (!(object instanceof DescriptiveMetadata)) {
      if ((object instanceof List<?>)) {
        List<?> objectList = (List<?>) object;
        if (!objectList.isEmpty() && !(objectList.get(0) instanceof DescriptiveMetadata)) {
          mapper.addMixIn(DescriptiveMetadata.class, DescriptiveMetadataMixIn.class);
        }
      } else {
        mapper.addMixIn(DescriptiveMetadata.class, DescriptiveMetadataMixIn.class);
      }
    }

    return mapper;
  }

  public static <T> T getObjectFromJson(Path json, Class<T> objectClass) throws GenericException {
    T ret;
    InputStream stream = null;
    try {
      stream = Files.newInputStream(json);
      String jsonString = IOUtils.toString(stream, RodaConstants.DEFAULT_ENCODING);
      ret = getObjectFromJson(jsonString, objectClass);
    } catch (IOException e) {
      throw new GenericException("Error while parsing json file", e);
    } finally {
      IOUtils.closeQuietly(stream);
    }
    return ret;
  }

  public static <T> T getObjectFromJson(InputStream json, Class<T> objectClass) throws GenericException {
    T ret;
    try {
      String jsonString = IOUtils.toString(json, RodaConstants.DEFAULT_ENCODING);
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
      throw new GenericException("Error while parsing JSON", e);
    }
  }

  public static <T> List<T> getListFromJson(String json, Class<T> objectClass) throws GenericException {
    try {
      ObjectMapper mapper = new ObjectMapper(new JsonFactory());
      TypeFactory t = TypeFactory.defaultInstance();
      return mapper.readValue(json, t.constructCollectionType(ArrayList.class, objectClass));
    } catch (IOException e) {
      throw new GenericException("Error while parsing JSON", e);
    }
  }

  public static JsonNode parseJson(String json) throws GenericException {
    try {
      ObjectMapper mapper = new ObjectMapper(new JsonFactory());
      return mapper.readTree(json);
    } catch (IOException e) {
      throw new GenericException("Error while parsing JSON", e);
    }
  }

}
