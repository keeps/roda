/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.model.utils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.io.IOUtils;
import org.apache.commons.net.util.Base64;
import org.roda.core.RodaCoreFactory;
import org.roda.core.common.RodaUtils;
import org.roda.core.data.Report;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.FileFormat;
import org.roda.core.data.v2.LogEntry;
import org.roda.core.data.v2.LogEntryParameter;
import org.roda.core.data.v2.RepresentationState;
import org.roda.core.data.v2.SimpleDescriptionObject;
import org.roda.core.metadata.v2.premis.PremisAgentHelper;
import org.roda.core.metadata.v2.premis.PremisEventHelper;
import org.roda.core.metadata.v2.premis.PremisFileObjectHelper;
import org.roda.core.metadata.v2.premis.PremisMetadataException;
import org.roda.core.metadata.v2.premis.PremisRepresentationObjectHelper;
import org.roda.core.model.AIP;
import org.roda.core.model.DescriptiveMetadata;
import org.roda.core.model.ModelService;
import org.roda.core.model.ModelServiceException;
import org.roda.core.storage.Binary;
import org.roda.core.storage.ClosableIterable;
import org.roda.core.storage.DefaultStoragePath;
import org.roda.core.storage.Resource;
import org.roda.core.storage.StoragePath;
import org.roda.core.storage.StorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import lc.xmlns.premisV2.AgentComplexType;
import lc.xmlns.premisV2.EventComplexType;
import lc.xmlns.premisV2.File;
import lc.xmlns.premisV2.LinkingAgentIdentifierComplexType;
import lc.xmlns.premisV2.Representation;

/**
 * Model related utility class
 * 
 * @author Hélder Silva <hsilva@keep.pt>
 */
public final class ModelUtils {
  private static final Logger LOGGER = LoggerFactory.getLogger(ModelUtils.class);

  public enum PREMIS_TYPE {
    OBJECT, EVENT, AGENT, UNKNOWN
  }

  /**
   * Private empty constructor
   */
  private ModelUtils() {

  }

  /**
   * Builds, from metadata, a {@code FileFormat} object
   * 
   * @param metadata
   *          metadata
   * @throws GenericException
   */
  public static FileFormat getFileFormat(Map<String, Set<String>> metadata) throws GenericException {
    String mimetype = getString(metadata, RodaConstants.STORAGE_META_FORMAT_MIME);
    String version = getString(metadata, RodaConstants.STORAGE_META_FORMAT_VERSION);
    // FIXME how to load format registries if any
    Map<String, String> formatRegistries = new HashMap<String, String>();

    return new FileFormat(mimetype, version, formatRegistries);
  }

  /**
   * Builds, from metadata, a {@code Set<RepresentationState>} object
   * 
   * @param metadata
   *          metadata
   */
  public static Set<RepresentationState> getStatuses(Map<String, Set<String>> metadata) {
    Set<RepresentationState> statuses = new TreeSet<RepresentationState>();
    Set<String> statusesInString = metadata.get(RodaConstants.STORAGE_META_REPRESENTATION_STATUSES);
    for (String statusString : statusesInString) {
      statuses.add(RepresentationState.valueOf(statusString.toUpperCase()));
    }
    return statuses;
  }

  public static <T> T getAs(Map<String, Set<String>> metadata, String key, Class<T> type) throws GenericException {
    T ret;
    Set<String> set = metadata.get(key);
    if (set == null || set.isEmpty()) {
      ret = null;
    } else if (set.size() == 1) {
      String value = set.iterator().next();
      if (type.equals(Date.class)) {
        try {
          ret = type.cast(RodaUtils.parseDate(set.iterator().next()));
        } catch (ParseException e) {
          throw new GenericException("Could not parse date: " + value, e);
        }
      } else if (type.equals(Boolean.class)) {
        ret = type.cast(Boolean.valueOf(value));
      } else if (type.equals(Long.class)) {
        ret = type.cast(Long.valueOf(value));
      } else {
        throw new GenericException(
          "Could not parse date because metadata field has not a single value class is not supported" + type);
      }
    } else {
      throw new GenericException("Could not parse date because metadata field has not a single value, set=" + set);
    }

    return ret;
  }

  public static <T> void setAs(Map<String, Set<String>> metadata, String key, T value) throws GenericException {
    if (value instanceof Date) {
      Date dateValue = (Date) value;
      metadata.put(key, new HashSet<>(Arrays.asList(RodaUtils.dateToString(dateValue))));
    } else if (value instanceof Boolean) {
      Boolean booleanValue = (Boolean) value;
      metadata.put(key, new HashSet<>(Arrays.asList(booleanValue.toString())));
    } else if (value instanceof Long) {
      Long longValue = (Long) value;
      metadata.put(key, new HashSet<>(Arrays.asList(longValue.toString())));
    } else {
      throw new GenericException(
        "Could not set data because value class is not supported" + value.getClass().getName());
    }
  }

  /**
   * Reads, from metadata and for a metadata key, an ISO8601 date if it exists
   * 
   * @param metadata
   *          metadata
   * @param key
   *          metadata key
   * @throws GenericException
   */
  public static Date getDate(Map<String, Set<String>> metadata, String key) throws GenericException {
    return getAs(metadata, key, Date.class);
  }

  /**
   * Reads, from metadata and for a metadata key, a boolean if it exists
   * 
   * @param metadata
   *          metadata
   * @param key
   *          metadata key
   * @throws GenericException
   */
  public static Boolean getBoolean(Map<String, Set<String>> metadata, String key) throws GenericException {
    return getAs(metadata, key, Boolean.class);
  }

  /**
   * Reads, from metadata and for a metadata key, a boolean if it exists
   * 
   * @param metadata
   *          metadata
   * @param key
   *          metadata key
   * @throws GenericException
   */
  public static Long getLong(Map<String, Set<String>> metadata, String key) throws GenericException {
    return getAs(metadata, key, Long.class);
  }

  /**
   * Reads, from metadata and for a metadata key, a string if it exists
   * 
   * @param metadata
   *          metadata
   * @param key
   *          metadata key
   * @throws ModelServiceException
   */
  public static String getString(Map<String, Set<String>> metadata, String key) throws GenericException {
    String ret;
    Set<String> set = metadata.get(key);
    if (set == null || set.isEmpty()) {
      ret = null;
    } else if (set.size() == 1) {
      ret = set.iterator().next();
    } else {
      throw new GenericException("Could not parse date because metadata field has multiple values, set=" + set);
    }

    return ret;
  }

  /**
   * Returns a list of ids from the children of a certain resource
   * 
   * @param storage
   *          the storage service containing the parent resource
   * @param path
   *          the storage path for the parent resource
   * @throws NotFoundException
   * @throws GenericException
   * @throws RequestNotValidException
   * @throws AuthorizationDeniedException
   */
  public static List<String> getChildIds(StorageService storage, StoragePath path, boolean failIfParentDoesNotExist)
    throws NotFoundException, GenericException, AuthorizationDeniedException, RequestNotValidException {
    List<String> ids = new ArrayList<String>();
    ClosableIterable<Resource> iterable = null;
    try {
      iterable = storage.listResourcesUnderDirectory(path);
      Iterator<Resource> it = iterable.iterator();
      while (it.hasNext()) {
        Resource next = it.next();
        if (next != null) {
          StoragePath storagePath = next.getStoragePath();
          if (!path.asString().equalsIgnoreCase(storagePath.asString())) {
            ids.add(storagePath.getName());
          }
        } else {
          LOGGER.error("Error while getting IDs for path " + path.asString());
        }
      }
    } catch (NotFoundException e) {
      if (failIfParentDoesNotExist) {
        throw e;
      }
    }

    if (iterable != null) {
      try {
        iterable.close();
      } catch (IOException e) {
        LOGGER.warn("Error closing iterator on getIds()", e);
      }
    }

    return ids;
  }

  // /**
  // * Returns a list of ids from the children of a certain resources, starting
  // * with the prefix defined
  // *
  // * @param storage
  // * the storage service containing the parent resource
  // * @param path
  // * the storage paths for the parent resources
  // * @param prefix
  // * the prefix of the children
  // * @throws StorageServiceException
  // */
  // public static List<String> getIds(StorageService storage, List<StoragePath>
  // paths, String prefix)
  // throws StorageServiceException {
  // List<String> ids = new ArrayList<String>();
  // for (StoragePath path : paths) {
  // if (path.getName().startsWith(prefix)) {
  // ids.add(path.getName());
  // }
  // }
  // return ids;
  //
  // }

  public static StoragePath getAIPcontainerPath() throws RequestNotValidException {
    return DefaultStoragePath.parse(RodaConstants.STORAGE_CONTAINER_AIP);
  }

  public static StoragePath getAIPpath(String aipId) throws RequestNotValidException {
    return DefaultStoragePath.parse(RodaConstants.STORAGE_CONTAINER_AIP, aipId);
  }

  public static StoragePath getMetadataPath(String aipId) throws RequestNotValidException {
    return DefaultStoragePath.parse(RodaConstants.STORAGE_CONTAINER_AIP, aipId,
      RodaConstants.STORAGE_DIRECTORY_METADATA);
  }

  public static StoragePath getDescriptiveMetadataPath(String aipId) throws RequestNotValidException {
    return DefaultStoragePath.parse(RodaConstants.STORAGE_CONTAINER_AIP, aipId,
      RodaConstants.STORAGE_DIRECTORY_METADATA, RodaConstants.STORAGE_DIRECTORY_DESCRIPTIVE);
  }

  public static StoragePath getDescriptiveMetadataPath(String aipId, String descriptiveMetadataBinaryId)
    throws RequestNotValidException {
    return DefaultStoragePath.parse(RodaConstants.STORAGE_CONTAINER_AIP, aipId,
      RodaConstants.STORAGE_DIRECTORY_METADATA, RodaConstants.STORAGE_DIRECTORY_DESCRIPTIVE,
      descriptiveMetadataBinaryId);
  }

  public static StoragePath getRepresentationsPath(String aipId) throws RequestNotValidException {
    return DefaultStoragePath.parse(RodaConstants.STORAGE_CONTAINER_AIP, aipId, RodaConstants.STORAGE_DIRECTORY_DATA);
  }

  public static StoragePath getRepresentationPath(String aipId, String representationId)
    throws RequestNotValidException {
    return DefaultStoragePath.parse(RodaConstants.STORAGE_CONTAINER_AIP, aipId, RodaConstants.STORAGE_DIRECTORY_DATA,
      representationId);
  }

  public static StoragePath getRepresentationFilePath(String aipId, String representationId, String fileId)
    throws RequestNotValidException {
    return DefaultStoragePath.parse(RodaConstants.STORAGE_CONTAINER_AIP, aipId, RodaConstants.STORAGE_DIRECTORY_DATA,
      representationId, fileId);
  }

  public static String getAIPidFromStoragePath(StoragePath path) {
    return path.getDirectoryPath().get(0);
  }

  public static String getRepresentationIdFromStoragePath(StoragePath path) throws GenericException {
    if (path.getDirectoryPath().size() >= 3) {
      return path.getDirectoryPath().get(2);
    } else {
      throw new GenericException(
        "Error while trying to obtain representation id from storage path (length is not 3 or above)");
    }
  }

  public static StoragePath getPreservationPath(String aipId, String representationID) throws RequestNotValidException {
    return DefaultStoragePath.parse(RodaConstants.STORAGE_CONTAINER_AIP, aipId,
      RodaConstants.STORAGE_DIRECTORY_METADATA, RodaConstants.STORAGE_DIRECTORY_PRESERVATION, representationID);

  }

  public static StoragePath getPreservationPath(String aipId) throws RequestNotValidException {
    return DefaultStoragePath.parse(RodaConstants.STORAGE_CONTAINER_AIP, aipId,
      RodaConstants.STORAGE_DIRECTORY_METADATA, RodaConstants.STORAGE_DIRECTORY_PRESERVATION);

  }

  public static StoragePath getPreservationFilePath(String aipId, String representationID, String fileID)
    throws RequestNotValidException {
    return DefaultStoragePath.parse(RodaConstants.STORAGE_CONTAINER_AIP, aipId,
      RodaConstants.STORAGE_DIRECTORY_METADATA, RodaConstants.STORAGE_DIRECTORY_PRESERVATION, representationID, fileID);

  }

  public static StoragePath getPreservationFilePath(String aipId, String fileID) throws RequestNotValidException {
    return DefaultStoragePath.parse(RodaConstants.STORAGE_CONTAINER_AIP, aipId,
      RodaConstants.STORAGE_DIRECTORY_METADATA, RodaConstants.STORAGE_DIRECTORY_PRESERVATION, fileID);

  }

  public static Representation getPreservationRepresentationObject(Binary preservationBinary) {
    Representation representation = null;
    InputStream binaryInputStream = null;
    try {
      binaryInputStream = preservationBinary.getContent().createInputStream();
      representation = PremisRepresentationObjectHelper.newInstance(binaryInputStream).getRepresentation();
    } catch (PremisMetadataException | IOException | ClassCastException e) {
      representation = null;
    } finally {
      if (binaryInputStream != null) {
        try {
          binaryInputStream.close();
        } catch (IOException e1) {
          LOGGER.warn("Cannot close file inputstream", e1);
        }
      }
    }
    return representation;
  }

  public static EventComplexType getPreservationEvent(Binary preservationBinary) {
    EventComplexType event = null;
    InputStream binaryInputStream = null;
    try {
      binaryInputStream = preservationBinary.getContent().createInputStream();
      event = PremisEventHelper.newInstance(binaryInputStream).getEvent();
    } catch (PremisMetadataException | IOException | ClassCastException e) {
      event = null;
    } finally {
      if (binaryInputStream != null) {
        try {
          binaryInputStream.close();
        } catch (IOException e1) {
          LOGGER.warn("Cannot close file inputstream", e1);
        }
      }
    }
    return event;
  }

  public static File getPreservationFileObject(Binary preservationBinary) {
    File file = null;
    InputStream binaryInputStream = null;
    try {
      binaryInputStream = preservationBinary.getContent().createInputStream();
      file = PremisFileObjectHelper.newInstance(binaryInputStream).getFile();
    } catch (PremisMetadataException | IOException | ClassCastException e) {
      file = null;
    } finally {
      if (binaryInputStream != null) {
        try {
          binaryInputStream.close();
        } catch (IOException e1) {
          LOGGER.warn("Cannot close file inputstream", e1);
        }
      }
    }
    return file;
  }

  public static AgentComplexType getPreservationAgentObject(Binary preservationBinary) {
    AgentComplexType agent = null;
    InputStream binaryInputStream = null;
    try {
      binaryInputStream = preservationBinary.getContent().createInputStream();
      agent = PremisAgentHelper.newInstance(binaryInputStream).getAgent();
    } catch (PremisMetadataException | IOException | ClassCastException e) {
      agent = null;
    } finally {
      if (binaryInputStream != null) {
        try {
          binaryInputStream.close();
        } catch (IOException e1) {
          LOGGER.warn("Cannot close file inputstream", e1);
        }
      }
    }
    return agent;
  }

  public static StoragePath getPreservationAgentPath(String agentID) throws RequestNotValidException {
    return DefaultStoragePath.parse(RodaConstants.STORAGE_CONTAINER_PRESERVATION,
      RodaConstants.STORAGE_DIRECTORY_AGENTS, agentID);
  }

  public static StoragePath getLogPath(String logFile) throws RequestNotValidException {
    return DefaultStoragePath.parse(RodaConstants.STORAGE_CONTAINER_ACTIONLOG, logFile);
  }

  // @Deprecated
  // public static StoragePath getLogPath(Date d) throws StorageServiceException
  // {
  // SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
  // String logFile = sdf.format(d) + ".log";
  // return DefaultStoragePath.parse(RodaConstants.STORAGE_CONTAINER_ACTIONLOG,
  // logFile);
  // }

  public static void writeLogEntryToFile(LogEntry logEntry, Path logFile) throws GenericException {
    try {
      String entryJSON = ModelUtils.getJsonFromObject(logEntry) + "\n";
      Files.write(logFile, entryJSON.getBytes(), StandardOpenOption.APPEND);
    } catch (IOException e) {
      throw new GenericException("Error writing log entry to file", e);
    }
  }

  public static void writeObjectToFile(Object object, Path file) throws GenericException {
    try {
      String json = ModelUtils.getJsonFromObject(object) + "\n";
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

  @Deprecated
  public static Map<String, Report> getJobReportsFromJson(String json) {
    Map<String, Report> ret = new HashMap<String, Report>();
    try {
      JsonFactory factory = new JsonFactory();
      ObjectMapper mapper = new ObjectMapper(factory);
      ret = mapper.readValue(json, new TypeReference<Map<String, Report>>() {
      });
    } catch (IOException e) {
      LOGGER.error("Error transforming json string to job reports", e);
    }
    return ret;
  }

  public static Report getJobReportFromJson(String json) {
    Report ret = new Report();
    try {
      JsonFactory factory = new JsonFactory();
      ObjectMapper mapper = new ObjectMapper(factory);
      ret = mapper.readValue(json, new TypeReference<Report>() {
      });
    } catch (IOException e) {
      LOGGER.error("Error transforming json string to job reports", e);
    }
    return ret;
  }

  public static String getJsonLogEntryParameters(List<LogEntryParameter> parameters) {
    String ret = "";
    try {
      JsonFactory factory = new JsonFactory();
      ObjectMapper mapper = new ObjectMapper(factory);
      ret = mapper.writeValueAsString(parameters);
    } catch (IOException e) {
      LOGGER.error("Error transforming log entry parameter to json string", e);
    }
    return ret;
  }

  public static List<LogEntryParameter> getLogEntryParameters(String json) {
    List<LogEntryParameter> ret = new ArrayList<LogEntryParameter>();
    try {
      JsonFactory factory = new JsonFactory();
      ObjectMapper mapper = new ObjectMapper(factory);
      ret = mapper.readValue(json, new TypeReference<List<LogEntryParameter>>() {
      });
    } catch (IOException e) {
      LOGGER.error("Error transforming json string to log entry parameters", e);
    }
    return ret;
  }

  public static LogEntry getLogEntry(String json) {
    LogEntry ret = null;
    try {
      JsonFactory factory = new JsonFactory();
      ObjectMapper mapper = new ObjectMapper(factory);
      ret = mapper.readValue(json, LogEntry.class);
    } catch (IOException e) {
      LOGGER.error("Error transforming json string to log entry", e);
    }
    return ret;
  }

  public static <T> List<String> extractAgentIdsFromPreservationBinary(Binary b, Class<T> c) {
    List<String> ids = new ArrayList<String>();
    if (c.equals(File.class)) {
      // TODO
      LOGGER.error("Not implemented!");
    } else if (c.equals(EventComplexType.class)) {
      EventComplexType event = getPreservationEvent(b);
      List<LinkingAgentIdentifierComplexType> identifiers = event.getLinkingAgentIdentifierList();
      if (identifiers != null) {
        for (LinkingAgentIdentifierComplexType laict : identifiers) {
          ids.add(laict.getLinkingAgentIdentifierValue());
        }
      }
    } else if (c.equals(Representation.class)) {
      // TODO
      LOGGER.error("Not implemented!");
    } else {
      // TODO
      LOGGER.error("Not implemented!");
    }
    return ids;
  }

  public static String getPreservationType(Binary binary) {
    String type = "";
    EventComplexType event = ModelUtils.getPreservationEvent(binary);
    if (event != null) {
      type = "event";
    } else {
      lc.xmlns.premisV2.File file = ModelUtils.getPreservationFileObject(binary);
      if (file != null) {
        type = "file";
      } else {
        AgentComplexType agent = ModelUtils.getPreservationAgentObject(binary);
        if (agent != null) {
          type = "agent";
        } else {
          Representation representation = ModelUtils.getPreservationRepresentationObject(binary);
          if (representation != null) {
            type = "representation";
          } else {
            type = "unknown";
          }
        }
      }
    }
    return type;

  }

  public static StoragePath getOtherMetadataDirectory(String aipID) throws RequestNotValidException {
    return DefaultStoragePath.parse(RodaConstants.STORAGE_CONTAINER_AIP, aipID,
      RodaConstants.STORAGE_DIRECTORY_METADATA, RodaConstants.STORAGE_DIRECTORY_OTHER);
  }

  public static StoragePath getToolMetadataDirectory(String aipID, String type) throws RequestNotValidException {
    return DefaultStoragePath.parse(RodaConstants.STORAGE_CONTAINER_AIP, aipID,
      RodaConstants.STORAGE_DIRECTORY_METADATA, RodaConstants.STORAGE_DIRECTORY_OTHER, type);
  }

  public static StoragePath getToolRepresentationMetadataDirectory(String aipID, String representationId, String type)
    throws RequestNotValidException {
    return DefaultStoragePath.parse(RodaConstants.STORAGE_CONTAINER_AIP, aipID,
      RodaConstants.STORAGE_DIRECTORY_METADATA, RodaConstants.STORAGE_DIRECTORY_OTHER, type, representationId);
  }

  public static StoragePath getToolMetadataPath(String aipID, String representationId, String fileName, String type)
    throws RequestNotValidException {
    return DefaultStoragePath.parse(RodaConstants.STORAGE_CONTAINER_AIP, aipID,
      RodaConstants.STORAGE_DIRECTORY_METADATA, RodaConstants.STORAGE_DIRECTORY_OTHER, type, representationId,
      fileName);

  }

  public static ObjectNode sdoToJSON(SimpleDescriptionObject sdo)
    throws IOException, RequestNotValidException, NotFoundException, GenericException, AuthorizationDeniedException {
    JsonFactory factory = new JsonFactory();
    ObjectMapper mapper = new ObjectMapper(factory);
    ModelService model = RodaCoreFactory.getModelService();

    ObjectNode node = mapper.createObjectNode();
    if (sdo.getTitle() != null) {
      node = node.put("title", sdo.getTitle());
    }
    if (sdo.getId() != null) {
      node = node.put("id", sdo.getId());
    }
    if (sdo.getParentID() != null) {
      node = node.put("parentId", sdo.getParentID());
    }
    if (sdo.getLevel() != null) {
      node = node.put("descriptionlevel", sdo.getLevel());
    }

    AIP aip = model.retrieveAIP(sdo.getId());
    if (aip != null) {
      List<String> descriptiveMetadaIds = aip.getDescriptiveMetadataIds();
      if (descriptiveMetadaIds != null && descriptiveMetadaIds.size() > 0) {
        ArrayNode metadata = mapper.createArrayNode();
        for (String descriptiveMetadataID : descriptiveMetadaIds) {
          DescriptiveMetadata dm = model.retrieveDescriptiveMetadata(aip.getId(), descriptiveMetadataID);
          ObjectNode dmNode = mapper.createObjectNode();
          if (dm.getId() != null) {
            dmNode = dmNode.put("id", dm.getId());
          }
          if (dm.getType() != null) {
            dmNode = dmNode.put("type", dm.getType());
          }
          Binary b = model.retrieveDescriptiveMetadataBinary(aip.getId(), dm.getId());
          InputStream is = b.getContent().createInputStream();
          dmNode = dmNode.put("content", new String(Base64.encodeBase64(IOUtils.toByteArray(is))));
          dmNode = dmNode.put("contentEncoding", "Base64");
          metadata = metadata.add(dmNode);
        }
        node.set("metadata", metadata);
      }
    }
    return node;
  }

}
