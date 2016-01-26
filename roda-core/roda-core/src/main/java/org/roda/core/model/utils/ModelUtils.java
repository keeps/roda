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

import org.apache.commons.io.IOUtils;
import org.apache.commons.net.util.Base64;
import org.roda.core.RodaCoreFactory;
import org.roda.core.common.RodaUtils;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.File;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.Representation;
import org.roda.core.data.v2.ip.StoragePath;
import org.roda.core.data.v2.ip.metadata.DescriptiveMetadata;
import org.roda.core.data.v2.ip.metadata.PreservationLinkingAgent;
import org.roda.core.data.v2.ip.metadata.PreservationLinkingObject;
import org.roda.core.data.v2.ip.metadata.PreservationMetadata;
import org.roda.core.data.v2.ip.metadata.PreservationMetadata.PreservationMetadataType;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.JobReport;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.data.v2.log.LogEntry;
import org.roda.core.data.v2.log.LogEntryParameter;
import org.roda.core.metadata.v2.premis.PremisAgentHelper;
import org.roda.core.metadata.v2.premis.PremisEventHelper;
import org.roda.core.metadata.v2.premis.PremisFileObjectHelper;
import org.roda.core.metadata.v2.premis.PremisMetadataException;
import org.roda.core.metadata.v2.premis.PremisRepresentationObjectHelper;
import org.roda.core.model.ModelService;
import org.roda.core.storage.Binary;
import org.roda.core.storage.ClosableIterable;
import org.roda.core.storage.DefaultStoragePath;
import org.roda.core.storage.Resource;
import org.roda.core.storage.StorageService;
import org.roda.core.storage.StringContentPayload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import lc.xmlns.premisV2.AgentComplexType;
import lc.xmlns.premisV2.EventComplexType;
import lc.xmlns.premisV2.LinkingAgentIdentifierComplexType;
import lc.xmlns.premisV2.LinkingObjectIdentifierComplexType;

/**
 * Model related utility class
 * 
 * @author Hélder Silva <hsilva@keep.pt>
 */
public final class ModelUtils {
  private static final Logger LOGGER = LoggerFactory.getLogger(ModelUtils.class);

  /**
   * @deprecated
   * @see PreservationMetadataType
   */
  @Deprecated
  public enum PREMIS_TYPE {
    REPRESENTATION, FILE, EVENT, AGENT, UNKNOWN
  }

  /**
   * Private empty constructor
   */
  private ModelUtils() {

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
   * @throws GenericException
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

  public static StoragePath getDescriptiveMetadataStoragePath(DescriptiveMetadata descriptiveMetadata)
    throws RequestNotValidException {
    return getDescriptiveMetadataPath(descriptiveMetadata.getAipId(), descriptiveMetadata.getId());
  }

  public static StoragePath getOtherMetadataPath(String aipId, String otherMetadataBinaryId)
    throws RequestNotValidException {
    return DefaultStoragePath.parse(RodaConstants.STORAGE_CONTAINER_AIP, aipId,
      RodaConstants.STORAGE_DIRECTORY_METADATA, RodaConstants.STORAGE_DIRECTORY_OTHER, otherMetadataBinaryId);
  }

  public static StoragePath getRepresentationsPath(String aipId) throws RequestNotValidException {
    return DefaultStoragePath.parse(RodaConstants.STORAGE_CONTAINER_AIP, aipId, RodaConstants.STORAGE_DIRECTORY_DATA);
  }

  public static StoragePath getRepresentationPath(String aipId, String representationId)
    throws RequestNotValidException {
    return DefaultStoragePath.parse(RodaConstants.STORAGE_CONTAINER_AIP, aipId, RodaConstants.STORAGE_DIRECTORY_DATA,
      representationId);
  }

  public static StoragePath getRepresentationFilePath(String aipId, String representationId, String... fileId)
    throws RequestNotValidException {
    List<String> path = new ArrayList<>();
    path.add(RodaConstants.STORAGE_CONTAINER_AIP);
    path.add(aipId);
    path.add(RodaConstants.STORAGE_DIRECTORY_DATA);
    path.add(representationId);
    for (String fileIdPartial : fileId) {
      path.add(fileIdPartial);
    }

    return DefaultStoragePath.parse(path);
  }

  public static StoragePath getRepresentationFilePath(File f) throws RequestNotValidException {
    // TODO make a better method for getting file path
    List<String> fileId = new ArrayList<>();
    if (f.getPath() != null) {
      fileId.addAll(f.getPath());
    }
    fileId.add(f.getId());
    return getRepresentationFilePath(f.getAipId(), f.getRepresentationId(), fileId.toArray(new String[] {}));

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

  public static List<String> getFilePathFromStoragePath(StoragePath path) throws GenericException {
    List<String> directoryPath = path.getDirectoryPath();
    if (directoryPath.size() >= 3) {
      return directoryPath.subList(3, directoryPath.size());
    } else {
      throw new GenericException(
        "Error while trying to obtain representation id from storage path (length is not 3 or above)");
    }
  }

  public static StoragePath getPreservationMetadataStoragePath(PreservationMetadata pm)
    throws RequestNotValidException {
    // TODO review this method
    return getPreservationFilePath(pm.getAipId(), pm.getRepresentationID(), pm.getId());
  }

  /**
   * @deprecated
   * @see #getAIPRepresentationPreservationPath(String, String)
   */
  @Deprecated
  public static StoragePath getPreservationPath(String aipId, String representationID) throws RequestNotValidException {
    // TODO check if this method should be removed
    return DefaultStoragePath.parse(RodaConstants.STORAGE_CONTAINER_AIP, aipId,
      RodaConstants.STORAGE_DIRECTORY_METADATA, RodaConstants.STORAGE_DIRECTORY_PRESERVATION, representationID);

  }

  public static StoragePath getPreservationPath(String aipId) throws RequestNotValidException {
    return DefaultStoragePath.parse(RodaConstants.STORAGE_CONTAINER_AIP, aipId,
      RodaConstants.STORAGE_DIRECTORY_METADATA, RodaConstants.STORAGE_DIRECTORY_PRESERVATION);

  }

  public static StoragePath getPreservationFilePath(String aipId, String representationId, String fileId)
    throws RequestNotValidException {
    return DefaultStoragePath.parse(getAIPRepresentationPreservationPath(aipId, representationId),
      fileId + ".file.premis.xml");
  }

  public static StoragePath getPreservationFilePath(String aipId, String fileID) throws RequestNotValidException {
    return DefaultStoragePath.parse(RodaConstants.STORAGE_CONTAINER_AIP, aipId,
      RodaConstants.STORAGE_DIRECTORY_METADATA, RodaConstants.STORAGE_DIRECTORY_PRESERVATION, fileID);

  }

  public static lc.xmlns.premisV2.Representation getPreservationRepresentationObject(Binary preservationBinary) {
    lc.xmlns.premisV2.Representation representation = null;
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

  public static lc.xmlns.premisV2.File getPreservationFileObject(Binary preservationBinary) {
    lc.xmlns.premisV2.File file = null;
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

  public static StoragePath getLogPath(String logFile) throws RequestNotValidException {
    return DefaultStoragePath.parse(RodaConstants.STORAGE_CONTAINER_ACTIONLOG, logFile);
  }

  public static StoragePath getJobPath(String jobId) throws RequestNotValidException {
    return DefaultStoragePath.parse(RodaConstants.STORAGE_CONTAINER_JOB, jobId);
  }

  public static StoragePath getJobReportPath(String jobReportId) throws RequestNotValidException {
    return DefaultStoragePath.parse(RodaConstants.STORAGE_CONTAINER_JOB_REPORT, jobReportId);
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

  public static void writeJobToFile(Job job, Path logFile) throws GenericException {
    try {
      String entryJSON = ModelUtils.getJsonFromObject(job);
      Files.write(logFile, entryJSON.getBytes(), StandardOpenOption.CREATE);
    } catch (IOException e) {
      throw new GenericException("Error writing job to file", e);
    }
  }

  public static void writeJobReportToFile(JobReport jobReport, Path logFile) throws GenericException {
    try {
      String entryJSON = ModelUtils.getJsonFromObject(jobReport);
      Files.write(logFile, entryJSON.getBytes(), StandardOpenOption.CREATE);
    } catch (IOException e) {
      throw new GenericException("Error writing job report to file", e);
    }
  }

  public static void createOrUpdateJobInStorage(StorageService storage, Job job) throws GenericException {
    try {
      String jobAsJson = ModelUtils.getJsonFromObject(job);
      StoragePath jobPath = ModelUtils.getJobPath(job.getId());
      storage.updateBinaryContent(jobPath, new StringContentPayload(jobAsJson), false, true);
    } catch (GenericException | RequestNotValidException | AuthorizationDeniedException | NotFoundException e) {
      LOGGER.error("Error creating/updating job in storage", e);
    }
  }

  public static void createOrUpdateJobReportInStorage(StorageService storage, JobReport jobReport)
    throws GenericException {
    try {
      String jobReportAsJson = ModelUtils.getJsonFromObject(jobReport);
      StoragePath jobReportPath = ModelUtils.getJobReportPath(jobReport.getId());
      storage.updateBinaryContent(jobReportPath, new StringContentPayload(jobReportAsJson), false, true);
    } catch (GenericException | RequestNotValidException | AuthorizationDeniedException | NotFoundException e) {
      LOGGER.error("Error creating/updating job report in storage", e);
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

  public static <T> List<PreservationLinkingAgent> extractAgentsFromPreservationBinary(Binary b, Class<T> c) {
    List<PreservationLinkingAgent> agents = new ArrayList<PreservationLinkingAgent>();
    if (c.equals(lc.xmlns.premisV2.File.class)) {
      // TODO check if files has agents
      LOGGER.error("Not implemented!");
    } else if (c.equals(EventComplexType.class)) {
      EventComplexType event = getPreservationEvent(b);
      List<LinkingAgentIdentifierComplexType> identifiers = event.getLinkingAgentIdentifierList();
      if (identifiers != null) {
        for (LinkingAgentIdentifierComplexType laict : identifiers) {
          PreservationLinkingAgent agent = new PreservationLinkingAgent();
          agent.setTitle(laict.getTitle());
          agent.setIdentifierType(laict.getLinkingAgentIdentifierType());
          agent.setIdentifierValue(laict.getLinkingAgentIdentifierValue());
          agent.setRole(laict.getRole());
          agent.setType(laict.getType());
          agents.add(agent);
        }
      }
    } else if (c.equals(lc.xmlns.premisV2.Representation.class)) {
      // TODO
      LOGGER.error("Not implemented!");
    } else {
      // TODO
      LOGGER.error("Not implemented!");
    }
    return agents;
  }

  public static <T> List<PreservationLinkingObject> extractLinkingObjectsFromPreservationBinary(Binary b, Class<T> c) {
    List<PreservationLinkingObject> objects = new ArrayList<PreservationLinkingObject>();
    if (c.equals(File.class)) {
      LOGGER.error("Not implemented!");
    } else if (c.equals(EventComplexType.class)) {
      EventComplexType event = getPreservationEvent(b);
      List<LinkingObjectIdentifierComplexType> identifiers = event.getLinkingObjectIdentifierList();
      if (identifiers != null) {
        for (LinkingObjectIdentifierComplexType loict : identifiers) {
          PreservationLinkingObject object = new PreservationLinkingObject();

          object.setTitle(loict.getTitle());
          object.setIdentifierType(loict.getLinkingObjectIdentifierType());
          object.setIdentifierValue(loict.getLinkingObjectIdentifierValue());
          object.setRole(loict.getRole());
          object.setType(loict.getType());
          objects.add(object);
        }
      }
    } else if (c.equals(Representation.class)) {
      // TODO
      LOGGER.error("Not implemented!");
    } else {
      // TODO
      LOGGER.error("Not implemented!");
    }
    return objects;
  }

  public static PreservationMetadataType getPreservationType(Binary binary) {
    PreservationMetadataType type;
    EventComplexType event = ModelUtils.getPreservationEvent(binary);
    if (event != null) {
      type = PreservationMetadataType.EVENT;
    } else {
      lc.xmlns.premisV2.File file = ModelUtils.getPreservationFileObject(binary);
      if (file != null) {
        type = PreservationMetadataType.OBJECT_FILE;
      } else {
        AgentComplexType agent = ModelUtils.getPreservationAgentObject(binary);
        if (agent != null) {
          type = PreservationMetadataType.AGENT;
        } else {
          lc.xmlns.premisV2.Representation representation = ModelUtils.getPreservationRepresentationObject(binary);
          if (representation != null) {
            type = PreservationMetadataType.OBJECT_REPRESENTATION;
          } else {
            // TODO send log
            // TODO support remaining types
            type = null;
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

  public static ObjectNode aipToJSON(IndexedAIP indexedAIP)
    throws IOException, RequestNotValidException, NotFoundException, GenericException, AuthorizationDeniedException {
    JsonFactory factory = new JsonFactory();
    ObjectMapper mapper = new ObjectMapper(factory);
    ModelService model = RodaCoreFactory.getModelService();

    ObjectNode node = mapper.createObjectNode();
    if (indexedAIP.getTitle() != null) {
      node = node.put("title", indexedAIP.getTitle());
    }
    if (indexedAIP.getId() != null) {
      node = node.put("id", indexedAIP.getId());
    }
    if (indexedAIP.getParentID() != null) {
      node = node.put("parentId", indexedAIP.getParentID());
    }
    if (indexedAIP.getLevel() != null) {
      node = node.put("descriptionlevel", indexedAIP.getLevel());
    }

    AIP modelAIP = model.retrieveAIP(indexedAIP.getId());
    if (modelAIP != null) {
      List<DescriptiveMetadata> descriptiveMetadata = modelAIP.getMetadata().getDescriptiveMetadata();
      if (descriptiveMetadata != null && descriptiveMetadata.size() > 0) {
        ArrayNode metadata = mapper.createArrayNode();
        for (DescriptiveMetadata dm : descriptiveMetadata) {
          ObjectNode dmNode = mapper.createObjectNode();
          if (dm.getId() != null) {
            dmNode = dmNode.put("id", dm.getId());
          }
          if (dm.getType() != null) {
            dmNode = dmNode.put("type", dm.getType());
          }
          Binary b = model.retrieveDescriptiveMetadataBinary(modelAIP.getId(), dm.getId());
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

  // agent path -> /Preservation/Agents/[agentID].agent.premis.xml
  public static StoragePath getPreservationAgentPath(String agentID) throws RequestNotValidException {
    return DefaultStoragePath.parse(RodaConstants.STORAGE_CONTAINER_PRESERVATION,
      RodaConstants.STORAGE_DIRECTORY_AGENTS, agentID + ".agent.premis.xml");
  }

  // aip metadata path -> /AIP/[aipId]/Metadata/Preservation
  public static StoragePath getAIPPreservationMetadataPath(String aipId) throws RequestNotValidException {
    return DefaultStoragePath.parse(RodaConstants.STORAGE_CONTAINER_AIP, aipId,
      RodaConstants.STORAGE_DIRECTORY_METADATA, RodaConstants.STORAGE_DIRECTORY_PRESERVATION);
  }

  // representation metadata path ->
  // /AIP/[aipId]/Metadata/Preservation/[representationId]
  public static StoragePath getAIPRepresentationPreservationPath(String aipId, String representationID)
    throws RequestNotValidException {
    return DefaultStoragePath.parse(RodaConstants.STORAGE_CONTAINER_AIP, aipId,
      RodaConstants.STORAGE_DIRECTORY_METADATA, RodaConstants.STORAGE_DIRECTORY_PRESERVATION, representationID);
  }

  public static StoragePath getPreservationRepresentationPath(String aipID, String representationID)
    throws RequestNotValidException {
    return DefaultStoragePath.parse(RodaConstants.STORAGE_CONTAINER_AIP, aipID,
      RodaConstants.STORAGE_DIRECTORY_METADATA, RodaConstants.STORAGE_DIRECTORY_PRESERVATION, representationID,
      representationID + ".representation.premis.xml");
  }

  public static StoragePath getPath(StoragePath parent, String children) throws RequestNotValidException {
    return DefaultStoragePath.parse(parent, children);
  }

  // TODO right now, all premis for files are saved under the representation
  // folder... if need, add parameter "fileID"...
  public static StoragePath getPreservationFilePathRaw(String aipId, String representationId, String preservationID)
    throws RequestNotValidException {
    return DefaultStoragePath.parse(getAIPRepresentationPreservationPath(aipId, representationId), preservationID);
  }

  /**
   * @deprecated
   * @see #getPreservationMetadataStoragePath(PreservationMetadata)
   */
  @Deprecated
  public static StoragePath buildPreservationPath(PREMIS_TYPE type, String aipId, String representationId,
    String fileId, String preservationId) throws RequestNotValidException {
    StoragePath path = null;
    if (type.toString().equalsIgnoreCase(PREMIS_TYPE.AGENT.toString())) {
      path = DefaultStoragePath.parse(RodaConstants.STORAGE_CONTAINER_PRESERVATION,
        RodaConstants.STORAGE_DIRECTORY_AGENTS, preservationId + ".agent.premis.xml");
    } else if (type.toString().equalsIgnoreCase(PREMIS_TYPE.REPRESENTATION.toString())) {
      path = DefaultStoragePath.parse(RodaConstants.STORAGE_CONTAINER_AIP, aipId,
        RodaConstants.STORAGE_DIRECTORY_METADATA, RodaConstants.STORAGE_DIRECTORY_PRESERVATION, representationId,
        representationId + ".representation.premis.xml");
    } else if (type.toString().equalsIgnoreCase(PREMIS_TYPE.EVENT.toString())) {
      // TODO HANDLE AIP and REPRESENTATION EVENTS
      path = DefaultStoragePath.parse(RodaConstants.STORAGE_CONTAINER_AIP, aipId,
        RodaConstants.STORAGE_DIRECTORY_METADATA, RodaConstants.STORAGE_DIRECTORY_PRESERVATION, representationId,
        preservationId + ".event.premis.xml");
    } else if (type.toString().equalsIgnoreCase(PREMIS_TYPE.FILE.toString())) {
      path = DefaultStoragePath.parse(RodaConstants.STORAGE_CONTAINER_AIP, aipId,
        RodaConstants.STORAGE_DIRECTORY_METADATA, RodaConstants.STORAGE_DIRECTORY_PRESERVATION, representationId,
        fileId + ".file.premis.xml");
    }
    return path;
  }

}
