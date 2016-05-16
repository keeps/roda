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
import org.roda.core.data.exceptions.GenericException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;

public class JsonUtils {

  private static final Logger LOGGER = LoggerFactory.getLogger(JsonUtils.class);

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
      JsonFactory factory = new JsonFactory();
      ObjectMapper mapper = new ObjectMapper(factory);
      ret = mapper.readValue(json, new TypeReference<Map<String, String>>() {
      });
    } catch (IOException e) {
      LOGGER.error("Error transforming json string to log entry parameters", e);
    }
    return ret;
  }

  public static String getJsonFromObject(Object object) {
    String ret = null;
    try {
      JsonFactory factory = new JsonFactory();
      ObjectMapper mapper = new ObjectMapper(factory);
      ret = mapper.writeValueAsString(object);
    } catch (IOException e) {
      LOGGER.error("Error transforming object '" + object + "' to json string", e);
    }
    return ret;
  }

  public static <T> T getObjectFromJson(InputStream json, Class<T> objectClass) throws GenericException {
    T ret;
    try {
      String jsonString = IOUtils.toString(json);
      ret = getObjectFromJson(jsonString, objectClass);
    } catch (IOException e) {
      throw new GenericException(e);
    } finally {
      IOUtils.closeQuietly(json);
    }
    return ret;
  }

  public static <T> T getObjectFromJson(String json, Class<T> objectClass) throws GenericException {
    T ret;
    try {
      JsonFactory factory = new JsonFactory();
      ObjectMapper mapper = new ObjectMapper(factory);
      ret = mapper.readValue(json, objectClass);
    } catch (IOException e) {
      throw new GenericException("Error while parsing JSON", e);
    }
    return ret;
  }

  public static <T> List<T> getListFromJson(String json, Class<T> objectClass) throws GenericException {
    List<T> ret;
    try {
      JsonFactory factory = new JsonFactory();
      ObjectMapper mapper = new ObjectMapper(factory);
      TypeFactory t = TypeFactory.defaultInstance();
      ret = mapper.readValue(json, t.constructCollectionType(ArrayList.class, objectClass));
    } catch (IOException e) {
      throw new GenericException("Error while parsing JSON", e);
    }
    return ret;
  }

  /**
   * @deprecated this method should be replaced by a specialized class to
   *             marshal and unmarshal a classification plans
   */
//  @Deprecated
//  public static ObjectNode aipToJSON(IndexedAIP indexedAIP)
//    throws IOException, RequestNotValidException, NotFoundException, GenericException, AuthorizationDeniedException {
//    JsonFactory factory = new JsonFactory();
//    ObjectMapper mapper = new ObjectMapper(factory);
//    ModelService model = RodaCoreFactory.getModelService();
//
//    ObjectNode node = mapper.createObjectNode();
//    if (indexedAIP.getTitle() != null) {
//      node = node.put("title", indexedAIP.getTitle());
//    }
//    if (indexedAIP.getId() != null) {
//      node = node.put("id", indexedAIP.getId());
//    }
//    if (indexedAIP.getParentID() != null) {
//      node = node.put("parentId", indexedAIP.getParentID());
//    }
//    if (indexedAIP.getLevel() != null) {
//      node = node.put("descriptionlevel", indexedAIP.getLevel());
//    }
//
//    AIP modelAIP = model.retrieveAIP(indexedAIP.getId());
//    if (modelAIP != null) {
//      List<DescriptiveMetadata> descriptiveMetadata = modelAIP.getDescriptiveMetadata();
//      if (descriptiveMetadata != null && !descriptiveMetadata.isEmpty()) {
//        ArrayNode metadata = mapper.createArrayNode();
//        for (DescriptiveMetadata dm : descriptiveMetadata) {
//          ObjectNode dmNode = mapper.createObjectNode();
//          if (dm.getId() != null) {
//            dmNode = dmNode.put("id", dm.getId());
//          }
//          if (dm.getType() != null) {
//            dmNode = dmNode.put("type", dm.getType());
//          }
//          Binary b = model.retrieveDescriptiveMetadataBinary(modelAIP.getId(), dm.getId());
//          InputStream is = b.getContent().createInputStream();
//          dmNode = dmNode.put("content", new String(Base64.encodeBase64(IOUtils.toByteArray(is))));
//          IOUtils.closeQuietly(is);
//          dmNode = dmNode.put("contentEncoding", "Base64");
//          metadata = metadata.add(dmNode);
//        }
//        node.set("metadata", metadata);
//      }
//    }
//    return node;
//  }
}
