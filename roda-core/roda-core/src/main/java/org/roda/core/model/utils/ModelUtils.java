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
import org.apache.xmlbeans.XmlException;
import org.roda.core.RodaCoreFactory;
import org.roda.core.common.PremisUtils;
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
import org.roda.core.data.v2.ip.metadata.IndexedPreservationObject;
import org.roda.core.data.v2.ip.metadata.PreservationMetadata;
import org.roda.core.data.v2.ip.metadata.PreservationMetadata.PreservationMetadataType;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.JobReport;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.data.v2.log.LogEntry;
import org.roda.core.data.v2.validation.ValidationException;
import org.roda.core.model.ModelService;
import org.roda.core.storage.Binary;
import org.roda.core.storage.ClosableIterable;
import org.roda.core.storage.ContentPayload;
import org.roda.core.storage.DefaultStoragePath;
import org.roda.core.storage.Resource;
import org.roda.core.storage.StorageService;
import org.roda.core.storage.StringContentPayload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import lc.xmlns.premisV2.EventComplexType;
import lc.xmlns.premisV2.LinkingObjectIdentifierComplexType;

/**
 * Model related utility class
 * 
 * @author Hélder Silva <hsilva@keep.pt>
 */
public final class ModelUtils {

  private static final Logger LOGGER = LoggerFactory.getLogger(ModelUtils.class);

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

  public static StoragePath getFileStoragePath(String aipId, String representationId, List<String> directoryPath,
    String fileId) throws RequestNotValidException {
    List<String> path = new ArrayList<>();
    path.add(RodaConstants.STORAGE_CONTAINER_AIP);
    path.add(aipId);
    path.add(RodaConstants.STORAGE_DIRECTORY_DATA);
    path.add(representationId);
    if (directoryPath != null) {
      path.addAll(directoryPath);
    }
    path.add(fileId);

    return DefaultStoragePath.parse(path);
  }

  public static StoragePath getFileStoragePath(File f) throws RequestNotValidException {
    return getFileStoragePath(f.getAipId(), f.getRepresentationId(), f.getPath(), f.getId());
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
    return getPreservationMetadataStoragePath(pm.getId(), pm.getType(), pm.getAipId(), pm.getRepresentationId(),
      pm.getFileDirectoryPath(), pm.getFileId());
  }

  public static StoragePath getPreservationMetadataStoragePath(String id, PreservationMetadataType type)
    throws RequestNotValidException {
    return getPreservationMetadataStoragePath(id, type, null, null, null, null);
  }

  public static StoragePath getPreservationMetadataStoragePath(String id, PreservationMetadataType type, String aipId)
    throws RequestNotValidException {
    return getPreservationMetadataStoragePath(id, type, aipId, null, null, null);
  }

  public static StoragePath getPreservationMetadataStoragePath(String id, PreservationMetadataType type, String aipId,
    String representationId) throws RequestNotValidException {
    return getPreservationMetadataStoragePath(id, type, aipId, representationId, null, null);
  }

  public static StoragePath getPreservationMetadataStoragePath(String id, PreservationMetadataType type, String aipId,
    String representationId, List<String> fileDirectoryPath, String fileId) throws RequestNotValidException {
    // TODO review this method
    StoragePath path = null;
    if (type != null) {
      if (type.equals(PreservationMetadataType.AGENT)) {
        path = DefaultStoragePath.parse(RodaConstants.STORAGE_CONTAINER_PRESERVATION,
          RodaConstants.STORAGE_DIRECTORY_AGENTS, id + RodaConstants.PREMIS_AGENT_SUFFIX);
      } else if (type.equals(PreservationMetadataType.OBJECT_REPRESENTATION)) {
        path = DefaultStoragePath.parse(RodaConstants.STORAGE_CONTAINER_AIP, aipId,
          RodaConstants.STORAGE_DIRECTORY_METADATA, RodaConstants.STORAGE_DIRECTORY_PRESERVATION, representationId,
          id + RodaConstants.PREMIS_REPRESENTATION_SUFFIX);
      } else if (type.equals(PreservationMetadataType.EVENT)) {
        // TODO HANDLE AIP and REPRESENTATION EVENTS
        path = DefaultStoragePath.parse(RodaConstants.STORAGE_CONTAINER_AIP, aipId,
          RodaConstants.STORAGE_DIRECTORY_METADATA, RodaConstants.STORAGE_DIRECTORY_PRESERVATION, representationId,
          id + RodaConstants.PREMIS_EVENT_SUFFIX);
      } else if (type.equals(PreservationMetadataType.OBJECT_FILE)) {

        List<String> p = new ArrayList<>();
        p.add(RodaConstants.STORAGE_CONTAINER_AIP);
        p.add(aipId);
        p.add(RodaConstants.STORAGE_DIRECTORY_METADATA);
        p.add(RodaConstants.STORAGE_DIRECTORY_PRESERVATION);
        p.add(representationId);
        if (fileDirectoryPath != null) {
          p.addAll(fileDirectoryPath);
        }
        p.add(fileId + RodaConstants.PREMIS_FILE_SUFFIX);

        path = DefaultStoragePath.parse(p);
      }
    }
    return path;
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

  public static lc.xmlns.premisV2.Representation getPreservationRepresentationObject(ContentPayload payload)
    throws GenericException {
    lc.xmlns.premisV2.Representation representation = null;
    InputStream binaryInputStream = null;
    try {
      binaryInputStream = payload.createInputStream();
      representation = PremisUtils.binaryToRepresentation(binaryInputStream);
    } catch (IOException | ClassCastException | XmlException e) {
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

  public static StoragePath getLogPath(String logFile) throws RequestNotValidException {
    return DefaultStoragePath.parse(RodaConstants.STORAGE_CONTAINER_ACTIONLOG, logFile);
  }

  public static StoragePath getJobContainerPath() throws RequestNotValidException {
    return DefaultStoragePath.parse(RodaConstants.STORAGE_CONTAINER_JOB);
  }

  public static StoragePath getJobPath(String jobId) throws RequestNotValidException {
    return DefaultStoragePath.parse(RodaConstants.STORAGE_CONTAINER_JOB, jobId);
  }

  public static StoragePath getJobReportContainerPath() throws RequestNotValidException {
    return DefaultStoragePath.parse(RodaConstants.STORAGE_CONTAINER_JOB_REPORT);
  }

  public static String getJobReportId(String jobId, String aipId) {
    return jobId + "-" + aipId;
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
    } catch (IOException  e) {
      throw new GenericException("Error while parsing JSON", e);
    }
    return ret;
  }

  public static <T> List<T> getListFromJson(String json, Class<T> objectClass) throws GenericException {
    List<T> ret;
    try {
      JsonFactory factory = new JsonFactory();
      ObjectMapper mapper = new ObjectMapper(factory);
      ret = mapper.readValue(json, new TypeReference<List<T>>() {
      });
    } catch (IOException e) {
      throw new GenericException("Error while parsing JSON", e);
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

  public static <T> List<IndexedPreservationObject> extractLinkingObjectsFromPreservationBinary(ContentPayload payload,
    Class<T> c) throws ValidationException, GenericException {
    boolean validatePremis = false;
    List<IndexedPreservationObject> objects = new ArrayList<IndexedPreservationObject>();
    if (c.equals(File.class)) {
      LOGGER.error("Not implemented!");
    } else if (c.equals(EventComplexType.class)) {
      EventComplexType event = PremisUtils.binaryToEvent(payload, validatePremis);
      List<LinkingObjectIdentifierComplexType> identifiers = event.getLinkingObjectIdentifierList();
      if (identifiers != null) {
        for (LinkingObjectIdentifierComplexType loict : identifiers) {
          IndexedPreservationObject object = new IndexedPreservationObject();

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

  public static StoragePath getToolMetadataPath(String aipID, String representationId, List<String> directoryPath,
    String fileName, String fileSuffix, String type) throws RequestNotValidException {

    List<String> path = new ArrayList<>();
    path.add(RodaConstants.STORAGE_CONTAINER_AIP);
    path.add(aipID);
    path.add(RodaConstants.STORAGE_DIRECTORY_METADATA);
    path.add(RodaConstants.STORAGE_DIRECTORY_OTHER);
    path.add(type);
    path.add(representationId);
    if (directoryPath != null) {
      path.addAll(directoryPath);
    }
    path.add(fileName + fileSuffix);

    return DefaultStoragePath.parse(path);

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
      RodaConstants.STORAGE_DIRECTORY_AGENTS, agentID + RodaConstants.PREMIS_AGENT_SUFFIX);
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
      representationID + RodaConstants.PREMIS_REPRESENTATION_SUFFIX);
  }

  public static StoragePath getPath(StoragePath parent, String children) throws RequestNotValidException {
    return DefaultStoragePath.parse(parent, children);
  }

  // TODO right now, all premis for files are saved under the representation
  // folder... if need, add parameter "fileID"...
  /**
   * @deprecated this is not using preservation metadata type
   */
  @Deprecated
  public static StoragePath getPreservationFilePathRaw(String aipId, String representationId, String preservationID)
    throws RequestNotValidException {
    return DefaultStoragePath.parse(getAIPRepresentationPreservationPath(aipId, representationId), preservationID);
  }

}
