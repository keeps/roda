package org.roda.model.utils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.roda.common.RodaUtils;
import org.roda.model.FileFormat;
import org.roda.model.ModelServiceException;
import org.roda.storage.Binary;
import org.roda.storage.ClosableIterable;
import org.roda.storage.DefaultStoragePath;
import org.roda.storage.Resource;
import org.roda.storage.StoragePath;
import org.roda.storage.StorageService;
import org.roda.storage.StorageServiceException;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import lc.xmlns.premisV2.AgentComplexType;
import lc.xmlns.premisV2.EventComplexType;
import lc.xmlns.premisV2.File;
import lc.xmlns.premisV2.LinkingAgentIdentifierComplexType;
import lc.xmlns.premisV2.Representation;
import pt.gov.dgarq.roda.core.common.RodaConstants;
import pt.gov.dgarq.roda.core.data.v2.EventPreservationObject;
import pt.gov.dgarq.roda.core.data.v2.LogEntry;
import pt.gov.dgarq.roda.core.data.v2.LogEntryParameter;
import pt.gov.dgarq.roda.core.data.v2.RepresentationState;
import pt.gov.dgarq.roda.core.data.v2.SIPReport;
import pt.gov.dgarq.roda.core.metadata.v2.premis.PremisAgentHelper;
import pt.gov.dgarq.roda.core.metadata.v2.premis.PremisEventHelper;
import pt.gov.dgarq.roda.core.metadata.v2.premis.PremisFileObjectHelper;
import pt.gov.dgarq.roda.core.metadata.v2.premis.PremisMetadataException;
import pt.gov.dgarq.roda.core.metadata.v2.premis.PremisRepresentationObjectHelper;

/**
 * Model related utility class
 * 
 * @author Hélder Silva <hsilva@keep.pt>
 */
public final class ModelUtils {
  private static final Logger LOGGER = Logger.getLogger(ModelUtils.class);

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
   * @throws ModelServiceException
   */
  public static FileFormat getFileFormat(Map<String, Set<String>> metadata) throws ModelServiceException {
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

  public static <T> T getAs(Map<String, Set<String>> metadata, String key, Class<T> type) throws ModelServiceException {
    T ret;
    Set<String> set = metadata.get(key);
    if (set == null || set.size() == 0) {
      ret = null;
    } else if (set.size() == 1) {
      String value = set.iterator().next();
      if (type.equals(Date.class)) {
        try {
          ret = type.cast(RodaUtils.parseDate(set.iterator().next()));
        } catch (ParseException e) {
          throw new ModelServiceException("Could not parse date: " + value, ModelServiceException.INTERNAL_SERVER_ERROR,
            e);
        }
      } else if (type.equals(Boolean.class)) {
        ret = type.cast(Boolean.valueOf(value));
      } else if (type.equals(Long.class)) {
        ret = type.cast(Long.valueOf(value));
      } else {
        throw new ModelServiceException(
          "Could not parse date because metadata field has not a single value class is not supported" + type,
          ModelServiceException.INTERNAL_SERVER_ERROR);
      }
    } else {
      throw new ModelServiceException("Could not parse date because metadata field has not a single value, set=" + set,
        ModelServiceException.INTERNAL_SERVER_ERROR);
    }

    return ret;
  }

  /**
   * Reads, from metadata and for a metadata key, an ISO8601 date if it exists
   * 
   * @param metadata
   *          metadata
   * @param key
   *          metadata key
   * @throws ModelServiceException
   */
  public static Date getDate(Map<String, Set<String>> metadata, String key) throws ModelServiceException {
    return getAs(metadata, key, Date.class);
  }

  /**
   * Reads, from metadata and for a metadata key, a boolean if it exists
   * 
   * @param metadata
   *          metadata
   * @param key
   *          metadata key
   * @throws ModelServiceException
   */
  public static Boolean getBoolean(Map<String, Set<String>> metadata, String key) throws ModelServiceException {
    return getAs(metadata, key, Boolean.class);
  }

  /**
   * Reads, from metadata and for a metadata key, a boolean if it exists
   * 
   * @param metadata
   *          metadata
   * @param key
   *          metadata key
   * @throws ModelServiceException
   */
  public static Long getLong(Map<String, Set<String>> metadata, String key) throws ModelServiceException {
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
  public static String getString(Map<String, Set<String>> metadata, String key) throws ModelServiceException {
    String ret;
    Set<String> set = metadata.get(key);
    if (set == null || set.size() == 0) {
      ret = null;
    } else if (set.size() == 1) {
      ret = set.iterator().next();
    } else {
      throw new ModelServiceException("Could not parse date because metadata field has multiple values, set=" + set,
        ModelServiceException.INTERNAL_SERVER_ERROR);
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
   * @throws ModelServiceException
   */
  public static List<String> getChildIds(StorageService storage, StoragePath path, boolean failIfParentDoesNotExist)
    throws ModelServiceException {
    List<String> ids = new ArrayList<String>();
    ClosableIterable<Resource> iterable = null;
    try {
      iterable = storage.listResourcesUnderDirectory(path);
      Iterator<Resource> it = iterable.iterator();
      while (it.hasNext()) {
        Resource next = it.next();
        if (next != null) {
          StoragePath storagePath = next.getStoragePath();
          ids.add(storagePath.getName());
        } else {
          LOGGER.error("Error while getting IDs for path " + path.asString());
        }
      }
    } catch (StorageServiceException e) {
      if (e.getCode() != StorageServiceException.NOT_FOUND || failIfParentDoesNotExist) {
        throw new ModelServiceException("Could not get ids", e.getCode(), e);
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
  // * Returns a list of ids from the children of a certain resources
  // *
  // * @param storage
  // * the storage service containing the parent resource
  // * @param path
  // * the storage paths for the parent resources
  // * @throws StorageActionException
  // */
  // public static List<String> getIds(StorageService storage,
  // List<StoragePath> paths) throws StorageActionException {
  // List<String> ids = new ArrayList<String>();
  // for (StoragePath path : paths) {
  // Iterator<Resource> it =
  // storage.listResourcesUnderDirectory(path).iterator();
  // while (it.hasNext()) {
  // Resource next = it.next();
  // StoragePath storagePath = next.getStoragePath();
  // ids.add(storagePath.getName());
  // }
  // }
  // return ids;
  //
  // }

  /**
   * Returns a list of ids from the children of a certain resources, starting
   * with the prefix defined
   * 
   * @param storage
   *          the storage service containing the parent resource
   * @param path
   *          the storage paths for the parent resources
   * @param prefix
   *          the prefix of the children
   * @throws StorageServiceException
   */
  public static List<String> getIds(StorageService storage, List<StoragePath> paths, String prefix)
    throws StorageServiceException {
    List<String> ids = new ArrayList<String>();
    for (StoragePath path : paths) {
      if (path.getName().startsWith(prefix)) {
        ids.add(path.getName());
      }
    }
    return ids;

  }

  // /**
  // * Returns a list of storagepath from the children of a certain resource
  // *
  // * @param storage
  // * the storage service containing the parent resource
  // * @param path
  // * the storage path for the parent resource
  // */
  // public static List<StoragePath> getStoragePaths(StorageService storage,
  // StoragePath path)
  // throws StorageActionException {
  // List<StoragePath> paths = new ArrayList<StoragePath>();
  // Iterator<Resource> it =
  // storage.listResourcesUnderDirectory(path).iterator();
  // while (it.hasNext()) {
  // Resource next = it.next();
  // StoragePath storagePath = next.getStoragePath();
  // paths.add(storagePath);
  // }
  //
  // return paths;
  // }

  public static StoragePath getAIPcontainerPath() throws StorageServiceException {
    return DefaultStoragePath.parse(RodaConstants.STORAGE_CONTAINER_AIP);
  }

  public static StoragePath getAIPpath(String aipId) throws StorageServiceException {
    return DefaultStoragePath.parse(RodaConstants.STORAGE_CONTAINER_AIP, aipId);
  }

  public static StoragePath getDescriptiveMetadataPath(String aipId) throws StorageServiceException {
    return DefaultStoragePath.parse(RodaConstants.STORAGE_CONTAINER_AIP, aipId,
      RodaConstants.STORAGE_DIRECTORY_METADATA, RodaConstants.STORAGE_DIRECTORY_DESCRIPTIVE);
  }

  public static StoragePath getDescriptiveMetadataPath(String aipId, String descriptiveMetadataBinaryId)
    throws StorageServiceException {
    return DefaultStoragePath.parse(RodaConstants.STORAGE_CONTAINER_AIP, aipId,
      RodaConstants.STORAGE_DIRECTORY_METADATA, RodaConstants.STORAGE_DIRECTORY_DESCRIPTIVE,
      descriptiveMetadataBinaryId);
  }

  public static StoragePath getRepresentationsPath(String aipId) throws StorageServiceException {
    return DefaultStoragePath.parse(RodaConstants.STORAGE_CONTAINER_AIP, aipId, RodaConstants.STORAGE_DIRECTORY_DATA);
  }

  public static StoragePath getRepresentationPath(String aipId, String representationId)
    throws StorageServiceException {
    return DefaultStoragePath.parse(RodaConstants.STORAGE_CONTAINER_AIP, aipId, RodaConstants.STORAGE_DIRECTORY_DATA,
      representationId);
  }

  public static StoragePath getRepresentationFilePath(String aipId, String representationId, String fileId)
    throws StorageServiceException {
    return DefaultStoragePath.parse(RodaConstants.STORAGE_CONTAINER_AIP, aipId, RodaConstants.STORAGE_DIRECTORY_DATA,
      representationId, fileId);
  }

  public static String getAIPidFromStoragePath(StoragePath path) {
    return path.getDirectoryPath().get(0);
  }

  public static String getRepresentationIdFromStoragePath(StoragePath path) throws ModelServiceException {
    if (path.getDirectoryPath().size() >= 3) {
      return path.getDirectoryPath().get(2);
    } else {
      throw new ModelServiceException(
        "Error while trying to obtain representation id from storage path (length is not 3 or above)",
        ModelServiceException.INTERNAL_SERVER_ERROR);
    }
  }

  public static StoragePath getPreservationPath(String aipId, String representationID) throws StorageServiceException {
    return DefaultStoragePath.parse(RodaConstants.STORAGE_CONTAINER_AIP, aipId,
      RodaConstants.STORAGE_DIRECTORY_METADATA, RodaConstants.STORAGE_DIRECTORY_PRESERVATION, representationID);

  }

  public static StoragePath getPreservationFilePath(String aipId, String representationID, String fileID)
    throws StorageServiceException {
    return DefaultStoragePath.parse(RodaConstants.STORAGE_CONTAINER_AIP, aipId,
      RodaConstants.STORAGE_DIRECTORY_METADATA, RodaConstants.STORAGE_DIRECTORY_PRESERVATION, representationID, fileID);

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
      event=null;
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


  // FIXME finish to implement this for deleting the 3 methods above
  // (isPreservation*)
  public static PREMIS_TYPE getPremisType(Binary preservationBinary) {
    PREMIS_TYPE ret = PREMIS_TYPE.UNKNOWN;
    InputStream binaryInputStream = null;
    try {
      binaryInputStream = preservationBinary.getContent().createInputStream();
      PremisFileObjectHelper.newInstance(binaryInputStream);
    } catch (PremisMetadataException | IOException | ClassCastException e) {

    } finally {
      if (binaryInputStream != null) {
        try {
          binaryInputStream.close();
        } catch (IOException e1) {
          LOGGER.warn("Cannot close file inputstream", e1);
        }
      }
    }
    return ret;
  }

  public static StoragePath getPreservationAgentPath(String agentID) throws StorageServiceException {
    return DefaultStoragePath.parse(RodaConstants.STORAGE_CONTAINER_PRESERVATION,
      RodaConstants.STORAGE_DIRECTORY_AGENTS, agentID);
  }

  public static StoragePath getLogPath(String logFile) throws StorageServiceException {
    return DefaultStoragePath.parse(RodaConstants.STORAGE_CONTAINER_ACTIONLOG, logFile);
  }

  @Deprecated
  public static StoragePath getLogPath(Date d) throws StorageServiceException {
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    String logFile = sdf.format(d) + ".log";
    return DefaultStoragePath.parse(RodaConstants.STORAGE_CONTAINER_ACTIONLOG, logFile);
  }

  public static void writeLogEntryToFile(LogEntry logEntry, Path logFile) throws ModelServiceException {
    try {
      String entryJSON = ModelUtils.getJsonLogEntry(logEntry) + "\n";
      Files.write(logFile, entryJSON.getBytes(), StandardOpenOption.APPEND);
    } catch (IOException e) {
      throw new ModelServiceException("Error writing log entry to file", ModelServiceException.INTERNAL_SERVER_ERROR,
        e);
    }
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
      ret = mapper.readValue(json, new TypeReference<List<LogEntryParameter>>(){});
    } catch (IOException e) {
      LOGGER.error("Error transforming json string to log entry parameters", e);
    }
    return ret;
  }
  
  public static String getJsonLogEntry(LogEntry entry) {
    String ret = null;
    try {
      JsonFactory factory = new JsonFactory();
      ObjectMapper mapper = new ObjectMapper(factory);
      ret = mapper.writeValueAsString(entry);
    } catch (IOException e) {
      LOGGER.error("Error transforming log entry to json string", e);
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

  public static String getJsonSipState(SIPReport sipState) {
    try {
      JsonFactory factory = new JsonFactory();
      ObjectMapper mapper = new ObjectMapper(factory);
      return mapper.writeValueAsString(sipState);
    } catch (IOException ioe) {

    }
    return null;
  }

  public static SIPReport getSipState(String json) {
    try {
      JsonFactory factory = new JsonFactory();
      ObjectMapper mapper = new ObjectMapper(factory);
      return mapper.readValue(json, SIPReport.class);
    } catch (IOException ioe) {

    }
    return null;
  }

  public static <T> List<String> extractAgentIdsFromPreservationBinary(Binary b, Class<T> c)
    throws ModelServiceException {
    List<String> ids = new ArrayList<String>();
    if (c.equals(File.class)) {
      //
    } else if (c.equals(EventComplexType.class)) {
      EventComplexType event = getPreservationEvent(b);
      List<LinkingAgentIdentifierComplexType> identifiers = event.getLinkingAgentIdentifierList();
      if (identifiers != null && identifiers.size() > 0) {
        for (LinkingAgentIdentifierComplexType laict : identifiers) {
          ids.add(laict.getLinkingAgentIdentifierValue());
        }
      }
    } else if (c.equals(Representation.class)) {
      // TODO
    } else {
      // TODO
    }
    return ids;
  };


}
