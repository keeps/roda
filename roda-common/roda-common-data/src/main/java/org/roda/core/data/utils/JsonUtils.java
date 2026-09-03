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
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.io.IOUtils;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.v2.ip.metadata.DescriptiveMetadata;
import org.roda.core.data.v2.ip.metadata.DescriptiveMetadataMixIn;
import org.roda.core.data.v2.ip.metadata.TechnicalMetadata;
import org.roda.core.data.v2.ip.metadata.TechnicalMetadataMixIn;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonInclude;
import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ObjectNode;
import tools.jackson.databind.ser.FilterProvider;
import tools.jackson.databind.ser.std.SimpleBeanPropertyFilter;
import tools.jackson.databind.ser.std.SimpleFilterProvider;
import tools.jackson.databind.type.TypeFactory;

public final class JsonUtils {
  private static final Logger LOGGER = LoggerFactory.getLogger(JsonUtils.class);
  private static final String JSON_ERROR_MESSAGE = "Error while parsing JSON";
  private static final String ERROR_TRANSFORMING_OBJECT_TO_JSON_STRING = "Error transforming object '{}' to json string";

  // Jackson mappers are expensive to build (introspection, module registration)
  // but are thread-safe for concurrent reads/writes once configured, so they
  // are built once and reused rather than per call -- building one per call
  // was measured to dominate the cost of hydrating rows with JSON-converted
  // columns (e.g. Report.reports, Report.sourceObjectOriginalIds).
  private static final JsonMapper PLAIN_MAPPER = JsonMapper.builder().build();
  private static final JsonMapper DEFAULT_FILTERS_MAPPER = createJsonMapperBuilder().build();
  private static final JsonMapper NON_EMPTY_MAPPER = createJsonMapperBuilder()
    .changeDefaultPropertyInclusion(incl -> incl.withValueInclusion(JsonInclude.Include.NON_EMPTY)).build();

  // getJsonFromObject(Object, Class) needs a mapper configured with a mixin
  // combination that depends on the object's runtime type; that combination is
  // drawn from a small, bounded set, so mappers are built once per combination
  // and cached rather than rebuilt on every call.
  private record MixinCacheKey(boolean descriptiveMetadataMixin, boolean technicalMetadataMixin, Class<?> mixin,
    Class<?> mixinTarget) {
  }

  private static final ConcurrentHashMap<MixinCacheKey, JsonMapper> MIXIN_MAPPER_CACHE = new ConcurrentHashMap<>();

  private JsonUtils() {
    // do nothing
  }

  private static JsonMapper.Builder createJsonMapperBuilder() {
    FilterProvider defaultFilters = new SimpleFilterProvider()
            .addFilter("aipPermissionFilter", SimpleBeanPropertyFilter.serializeAll())
            .setFailOnUnknownId(false);

    return JsonMapper.builder().filterProvider(defaultFilters);
  }

  public static byte[] toByteArray(Object object) {
    return DEFAULT_FILTERS_MAPPER.writeValueAsBytes(object);
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
      ret = PLAIN_MAPPER.readValue(json, new TypeReference<Map<String, String>>() {});
    } catch (JacksonException e) {
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
      JsonMapper mapper = getMapperForMixins(object, mixin);
      ret = mapper.writeValueAsString(object);
    } catch (JacksonException e) {
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

        ret.append(NON_EMPTY_MAPPER.writer().writeValueAsString(object));
      } catch (JacksonException e) {
        LOGGER.error(ERROR_TRANSFORMING_OBJECT_TO_JSON_STRING, object, e);
      }
    }
    return ret.toString();
  }

  private static JsonMapper getMapperForMixins(Object object, Class<?> mixin) {
    boolean needsDescriptiveMetadataMixin = needsDescriptiveMetadataMixin(object);
    boolean needsTechnicalMetadataMixin = needsTechnicalMetadataMixin(object);
    Class<?> mixinTarget = mixin != null ? object.getClass() : null;
    MixinCacheKey key = new MixinCacheKey(needsDescriptiveMetadataMixin, needsTechnicalMetadataMixin, mixin,
      mixinTarget);

    return MIXIN_MAPPER_CACHE.computeIfAbsent(key, JsonUtils::buildMapperForMixins);
  }

  private static JsonMapper buildMapperForMixins(MixinCacheKey key) {
    JsonMapper.Builder builder = createJsonMapperBuilder();

    if (key.descriptiveMetadataMixin()) {
      builder.addMixIn(DescriptiveMetadata.class, DescriptiveMetadataMixIn.class);
    }
    if (key.technicalMetadataMixin()) {
      builder.addMixIn(TechnicalMetadata.class, TechnicalMetadataMixIn.class);
    }
    if (key.mixin() != null) {
      builder.addMixIn(key.mixinTarget(), key.mixin());
    }

    return builder.build();
  }

  private static boolean needsDescriptiveMetadataMixin(Object object) {
    if (object instanceof DescriptiveMetadata) {
      return false;
    }
    if (object instanceof List<?> objectList) {
      return !objectList.isEmpty() && !(objectList.getFirst() instanceof DescriptiveMetadata);
    }
    return true;
  }

  private static boolean needsTechnicalMetadataMixin(Object object) {
    if (object instanceof TechnicalMetadata) {
      return false;
    }
    if (object instanceof List<?> objectList) {
      return !objectList.isEmpty() && !(objectList.getFirst() instanceof TechnicalMetadata);
    }
    return true;
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
      return PLAIN_MAPPER.readValue(json, objectClass);
    } catch (JacksonException e) {
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
      TypeFactory t = PLAIN_MAPPER.getTypeFactory();
      return PLAIN_MAPPER.readValue(json, t.constructCollectionType(ArrayList.class, objectClass));
    } catch (JacksonException e) {
      throw new GenericException(JSON_ERROR_MESSAGE, e);
    }
  }

  public static JsonNode parseJson(String json) throws GenericException {
    try {
      return PLAIN_MAPPER.readTree(json);
    } catch (JacksonException e) {
      throw new GenericException(JSON_ERROR_MESSAGE, e);
    }
  }

  public static JsonNode parseJson(InputStream json) throws GenericException {
    try {
      return PLAIN_MAPPER.readTree(json);
    } catch (JacksonException e) {
      throw new GenericException(JSON_ERROR_MESSAGE, e);
    } finally {
      IOUtils.closeQuietly(json);
    }
  }

  public static String getJsonFromNode(JsonNode node) {
    String ret = null;
    try {
      ret = DEFAULT_FILTERS_MAPPER.writeValueAsString(node);
    } catch (JacksonException e) {
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