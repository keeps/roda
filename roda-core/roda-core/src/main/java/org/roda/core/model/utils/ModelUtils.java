/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.model.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.ip.File;
import org.roda.core.data.v2.ip.StoragePath;
import org.roda.core.data.v2.ip.metadata.DescriptiveMetadata;
import org.roda.core.data.v2.ip.metadata.PreservationMetadata;
import org.roda.core.data.v2.ip.metadata.PreservationMetadata.PreservationMetadataType;
import org.roda.core.storage.DefaultStoragePath;

/**
 * Model related utility class
 * 
 * @author Hélder Silva <hsilva@keep.pt>
 * @author Luis Faria <lfaria@keep.pt>
 */
public final class ModelUtils {

  /**
   * Private empty constructor
   */
  private ModelUtils() {

  }

  private static List<String> build(List<String> basePath, String... path) {
    List<String> ret = new ArrayList<>(basePath);
    for (String pathItem : path) {
      ret.add(pathItem);
    }
    return ret;
  }

  public static StoragePath getAIPcontainerPath() throws RequestNotValidException {
    return DefaultStoragePath.parse(RodaConstants.STORAGE_CONTAINER_AIP);
  }

  private static List<String> getAIPPath(String aipId) {
    return Arrays.asList(RodaConstants.STORAGE_CONTAINER_AIP, aipId);
  }

  public static StoragePath getAIPStoragePath(String aipId) throws RequestNotValidException {
    return DefaultStoragePath.parse(getAIPPath(aipId));
  }

  private static List<String> getAIPMetadataPath(String aipId) {
    return build(getAIPPath(aipId), RodaConstants.STORAGE_DIRECTORY_METADATA);
  }

  private static List<String> getAIPPreservationMetadataPath(String aipId) {
    return build(getAIPMetadataPath(aipId), RodaConstants.STORAGE_DIRECTORY_PRESERVATION);
  }

  public static StoragePath getAIPPreservationMetadataStoragePath(String aipId) throws RequestNotValidException {
    return DefaultStoragePath.parse(getAIPPreservationMetadataPath(aipId));
  }

  private static List<String> getAIPOtherMetadataPath(String aipId) {
    return build(getAIPMetadataPath(aipId), RodaConstants.STORAGE_DIRECTORY_OTHER);
  }

  public static StoragePath getAIPOtherMetadataStoragePath(String aipId, String type) throws RequestNotValidException {
    return DefaultStoragePath.parse(getAIPOtherMetadataPath(aipId));
  }

  private static List<String> getRepresentationPath(String aipId, String representationId) {
    return build(getAIPPath(aipId), RodaConstants.STORAGE_DIRECTORY_REPRESENTATIONS, representationId);
  }

  public static StoragePath getRepresentationStoragePath(String aipId, String representationId)
    throws RequestNotValidException {
    return DefaultStoragePath.parse(getRepresentationPath(aipId, representationId));
  }

  private static List<String> getRepresentationMetadataPath(String aipId, String representationId) {
    return build(getRepresentationPath(aipId, representationId), RodaConstants.STORAGE_DIRECTORY_METADATA);
  }

  private static List<String> getRepresentationPreservationMetadataPath(String aipId, String representationId) {
    return build(getRepresentationMetadataPath(aipId, representationId), RodaConstants.STORAGE_DIRECTORY_PRESERVATION);
  }

  public static StoragePath getRepresentationPreservationMetadataStoragePath(String aipId, String representationId)
    throws RequestNotValidException {
    return DefaultStoragePath.parse(getRepresentationPreservationMetadataPath(aipId, representationId));
  }

  private static List<String> getRepresentationOtherMetadataPath(String aipId, String representationId, String type) {
    return build(getRepresentationMetadataPath(aipId, representationId), RodaConstants.STORAGE_DIRECTORY_OTHER, type);
  }

  public static StoragePath getRepresentationOtherMetadataStoragePath(String aipId, String representationId,
    String type) throws RequestNotValidException {
    return DefaultStoragePath.parse(getRepresentationOtherMetadataPath(aipId, representationId, type));
  }

  private static List<String> getRepresentationDataPath(String aipId, String representationId) {
    // TODO check in AIP metadata if representation overrides data folder
    return build(getRepresentationPath(aipId, representationId), RodaConstants.STORAGE_DIRECTORY_DATA);
  }

  public static StoragePath getRepresentationDataStoragePath(String aipId, String representationId)
    throws RequestNotValidException {
    return DefaultStoragePath.parse(getRepresentationDataPath(aipId, representationId));
  }

  public static StoragePath getDescriptiveMetadataPath(String aipId, String descriptiveMetadataBinaryId)
    throws RequestNotValidException {
    List<String> path = build(getAIPMetadataPath(aipId), RodaConstants.STORAGE_DIRECTORY_DESCRIPTIVE,
      descriptiveMetadataBinaryId);
    return DefaultStoragePath.parse(path);
  }

  public static StoragePath getDescriptiveMetadataPath(String aipId, String representationId,
    String descriptiveMetadataBinaryId) throws RequestNotValidException {
    List<String> path = build(getRepresentationPath(aipId, representationId), RodaConstants.STORAGE_DIRECTORY_METADATA,
      RodaConstants.STORAGE_DIRECTORY_DESCRIPTIVE, descriptiveMetadataBinaryId);
    return DefaultStoragePath.parse(path);
  }

  public static StoragePath getDescriptiveMetadataStoragePath(DescriptiveMetadata descriptiveMetadata)
    throws RequestNotValidException {
    // TODO check if descriptive metadata is from a representation
    return getDescriptiveMetadataPath(descriptiveMetadata.getAipId(), descriptiveMetadata.getId());
  }

  public static StoragePath getFileStoragePath(String aipId, String representationId, List<String> directoryPath,
    String fileId) throws RequestNotValidException {
    List<String> path = getRepresentationDataPath(aipId, representationId);
    if (directoryPath != null) {
      path.addAll(directoryPath);
    }
    path.add(fileId);
    return DefaultStoragePath.parse(path);
  }

  public static StoragePath getFileStoragePath(File f) throws RequestNotValidException {
    return getFileStoragePath(f.getAipId(), f.getRepresentationId(), f.getPath(), f.getId());
  }

  public static String extractAipId(StoragePath path) {
    // AIP/[aipId]/...

    String container = path.getContainerName();
    List<String> directoryPath = path.getDirectoryPath();

    if (container.equals(RodaConstants.STORAGE_CONTAINER_AIP) && directoryPath.size() > 0) {
      return directoryPath.get(0);
    } else {
      return null;
    }
  }

  public static String extractRepresentationId(StoragePath path) {
    // AIP/[aipId]/representations/[representationId]/...

    String container = path.getContainerName();
    List<String> directoryPath = path.getDirectoryPath();

    if (container.equals(RodaConstants.STORAGE_CONTAINER_AIP) && directoryPath.size() > 1
      && directoryPath.get(1).equals(RodaConstants.STORAGE_DIRECTORY_REPRESENTATIONS)) {
      String representationId;
      if (directoryPath.size() > 2) {
        representationId = directoryPath.get(2);
      } else {
        representationId = path.getName();
      }

      return representationId;
    } else {
      return null;
    }
  }

  public static List<String> extractFilePathFromRepresentationData(StoragePath path) {
    // AIP/[aipId]/representations/[representationId]/data/.../file.bin

    String container = path.getContainerName();
    List<String> directoryPath = path.getDirectoryPath();
    if (container.equals(RodaConstants.STORAGE_CONTAINER_AIP) && directoryPath.size() > 3
      && directoryPath.get(1).equals(RodaConstants.STORAGE_DIRECTORY_REPRESENTATIONS)
      && directoryPath.get(3).equals(RodaConstants.STORAGE_DIRECTORY_DATA)) {
      return directoryPath.subList(4, directoryPath.size());
    } else {
      return new ArrayList<>();
    }
  }

  public static List<String> extractFilePathFromAipPreservationMetadata(StoragePath path) {
    // AIP/[aipId]/metadata/preservation/.../file.bin

    String container = path.getContainerName();
    List<String> directoryPath = path.getDirectoryPath();
    if (container.equals(RodaConstants.STORAGE_CONTAINER_AIP) && directoryPath.size() > 2
      && directoryPath.get(1).equals(RodaConstants.STORAGE_DIRECTORY_METADATA)
      && directoryPath.get(2).equals(RodaConstants.STORAGE_DIRECTORY_PRESERVATION)) {
      return directoryPath.subList(3, directoryPath.size());
    } else {
      return null;
    }
  }

  public static List<String> extractFilePathFromRepresentationPreservationMetadata(StoragePath path) {
    // AIP/[aipId]/representations/[representationId]/metadata/preservation/.../file.bin

    String container = path.getContainerName();
    List<String> directoryPath = path.getDirectoryPath();
    if (container.equals(RodaConstants.STORAGE_CONTAINER_AIP) && directoryPath.size() > 4
      && directoryPath.get(1).equals(RodaConstants.STORAGE_DIRECTORY_REPRESENTATIONS)
      && directoryPath.get(3).equals(RodaConstants.STORAGE_DIRECTORY_METADATA)
      && directoryPath.get(4).equals(RodaConstants.STORAGE_DIRECTORY_PRESERVATION)) {
      return directoryPath.subList(5, directoryPath.size());
    } else {
      return null;
    }
  }

  public static String extractTypeFromAipOtherMetadata(StoragePath path) {
    // AIP/[aipId]/metadata/other/[type]/...

    String container = path.getContainerName();
    List<String> directoryPath = path.getDirectoryPath();
    if (container.equals(RodaConstants.STORAGE_CONTAINER_AIP) && directoryPath.size() > 2
      && directoryPath.get(1).equals(RodaConstants.STORAGE_DIRECTORY_METADATA)
      && directoryPath.get(2).equals(RodaConstants.STORAGE_DIRECTORY_OTHER)) {
      String type;
      if (directoryPath.size() > 3) {
        type = directoryPath.get(3);
      } else {
        type = path.getName();
      }

      return type;
    } else {
      return null;
    }
  }

  public static List<String> extractFilePathFromAipOtherMetadata(StoragePath path) {
    // AIP/[aipId]/metadata/other/[type]/.../file.bin

    String container = path.getContainerName();
    List<String> directoryPath = path.getDirectoryPath();
    if (container.equals(RodaConstants.STORAGE_CONTAINER_AIP) && directoryPath.size() > 3
      && directoryPath.get(1).equals(RodaConstants.STORAGE_DIRECTORY_METADATA)
      && directoryPath.get(2).equals(RodaConstants.STORAGE_DIRECTORY_OTHER)) {
      return directoryPath.subList(4, directoryPath.size());
    } else {
      return null;
    }
  }

  public static String extractTypeFromRepresentationOtherMetadata(StoragePath path) {
    // AIP/[aipId]/representations/[representationId]/metadata/other/[type]/...

    String container = path.getContainerName();
    List<String> directoryPath = path.getDirectoryPath();
    if (container.equals(RodaConstants.STORAGE_CONTAINER_AIP) && directoryPath.size() > 4
      && directoryPath.get(1).equals(RodaConstants.STORAGE_DIRECTORY_REPRESENTATIONS)
      && directoryPath.get(3).equals(RodaConstants.STORAGE_DIRECTORY_METADATA)
      && directoryPath.get(4).equals(RodaConstants.STORAGE_DIRECTORY_OTHER)) {
      String type;
      if (directoryPath.size() > 5) {
        type = directoryPath.get(5);
      } else {
        type = path.getName();
      }

      return type;
    } else {
      return null;
    }
  }

  public static List<String> extractFilePathFromRepresentationOtherMetadata(StoragePath path) {
    // AIP/[aipId]/representations/[representationId]/metadata/other/[type]/.../file.bin

    String container = path.getContainerName();
    List<String> directoryPath = path.getDirectoryPath();
    if (container.equals(RodaConstants.STORAGE_CONTAINER_AIP) && directoryPath.size() > 5
      && directoryPath.get(1).equals(RodaConstants.STORAGE_DIRECTORY_REPRESENTATIONS)
      && directoryPath.get(3).equals(RodaConstants.STORAGE_DIRECTORY_METADATA)
      && directoryPath.get(4).equals(RodaConstants.STORAGE_DIRECTORY_OTHER)) {
      return directoryPath.subList(6, directoryPath.size());
    } else {
      return null;
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
    List<String> path = null;
    if (type != null) {
      if (type.equals(PreservationMetadataType.AGENT)) {
        path = Arrays.asList(RodaConstants.STORAGE_CONTAINER_PRESERVATION, RodaConstants.STORAGE_DIRECTORY_AGENTS,
          id + RodaConstants.PREMIS_AGENT_SUFFIX);
      } else if (type.equals(PreservationMetadataType.OBJECT_REPRESENTATION)) {
        if (aipId != null && representationId != null) {
          String pFileId = id + RodaConstants.PREMIS_REPRESENTATION_SUFFIX;
          path = build(getRepresentationPreservationMetadataPath(aipId, representationId), pFileId);
        } else {
          throw new RequestNotValidException("Cannot request a representation object with null AIP or Representation. "
            + "AIP id = " + aipId + " and Representation id = " + representationId);
        }
      } else if (type.equals(PreservationMetadataType.EVENT)) {
        if (aipId != null) {
          if (representationId != null) {
            String pFileId = id + RodaConstants.PREMIS_EVENT_SUFFIX;
            path = build(getRepresentationPreservationMetadataPath(aipId, representationId), pFileId);
          } else {
            String pFileId = id + RodaConstants.PREMIS_EVENT_SUFFIX;
            path = build(getAIPPreservationMetadataPath(aipId), pFileId);
          }

        } else {
          throw new RequestNotValidException("Requested an event preservation object with null AIP id");
        }
      } else if (type.equals(PreservationMetadataType.OBJECT_FILE)) {
        path = getRepresentationMetadataPath(aipId, representationId);
        path.add(RodaConstants.STORAGE_DIRECTORY_PRESERVATION);
        if (fileDirectoryPath != null) {
          path.addAll(fileDirectoryPath);
        }
        path.add(fileId + RodaConstants.PREMIS_FILE_SUFFIX);
      } else {
        throw new RequestNotValidException("Unsupported preservation metadata type: " + type);
      }
    } else {
      throw new RequestNotValidException("Preservation metadata type is null");
    }
    return DefaultStoragePath.parse(path);
  }

  public static StoragePath getLogStoragePath(String logFile) throws RequestNotValidException {
    return DefaultStoragePath.parse(RodaConstants.STORAGE_CONTAINER_ACTIONLOG, logFile);
  }

  public static StoragePath getJobContainerPath() throws RequestNotValidException {
    return DefaultStoragePath.parse(RodaConstants.STORAGE_CONTAINER_JOB);
  }

  public static StoragePath getJobStoragePath(String jobId) throws RequestNotValidException {
    return DefaultStoragePath.parse(RodaConstants.STORAGE_CONTAINER_JOB, jobId);
  }

  public static StoragePath getJobReportContainerPath() throws RequestNotValidException {
    return DefaultStoragePath.parse(RodaConstants.STORAGE_CONTAINER_JOB_REPORT);
  }

  public static StoragePath getJobReportStoragePath(String jobReportId) throws RequestNotValidException {
    return DefaultStoragePath.parse(RodaConstants.STORAGE_CONTAINER_JOB_REPORT, jobReportId);
  }

  public static StoragePath getOtherMetadataStoragePath(String aipId, String representationId,
    List<String> directoryPath, String fileName, String fileSuffix, String type) throws RequestNotValidException {

    if (StringUtils.isBlank(fileSuffix)) {
      throw new RequestNotValidException("File suffix cannot be empty");
    }

    if (fileSuffix.lastIndexOf('.') > 0) {
      throw new RequestNotValidException("File suffix have dots after index 0: " + fileSuffix);
    }

    if (StringUtils.isBlank(type)) {
      throw new RequestNotValidException("Type cannot be empty");
    }

    List<String> path;

    if (aipId != null && representationId != null && directoryPath != null && fileName != null) {
      // other metadata pertaining to a file
      path = getRepresentationOtherMetadataPath(aipId, representationId, type);
      if (directoryPath != null) {
        path.addAll(directoryPath);
      }
      path.add(fileName + fileSuffix);
    } else if (aipId != null && representationId != null) {
      // other metadata pertaining to a representation
      path = getRepresentationOtherMetadataPath(aipId, representationId, type);
      path.add(representationId + fileSuffix);
      // XXX What if representation id is equal to a file id? Maybe move this to
      // AIP metadata folder and have id [aipId+"-"+representationId+fileSuffix]
    } else if (aipId != null) {
      path = getRepresentationOtherMetadataPath(aipId, representationId, type);
      path.add(aipId + fileSuffix);
    } else {
      throw new RequestNotValidException("AIP id cannot be null");
    }
    return DefaultStoragePath.parse(path);
  }

}
