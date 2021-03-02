/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.model.utils;

import java.io.IOException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.roda.core.RodaCoreFactory;
import org.roda.core.common.iterables.CloseableIterable;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.AlreadyExistsException;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.utils.JsonUtils;
import org.roda.core.data.v2.IsRODAObject;
import org.roda.core.data.v2.index.select.SelectedItems;
import org.roda.core.data.v2.index.select.SelectedItemsFilter;
import org.roda.core.data.v2.index.select.SelectedItemsList;
import org.roda.core.data.v2.index.sublist.Sublist;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.DIP;
import org.roda.core.data.v2.ip.DIPFile;
import org.roda.core.data.v2.ip.File;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.IndexedDIP;
import org.roda.core.data.v2.ip.IndexedFile;
import org.roda.core.data.v2.ip.IndexedRepresentation;
import org.roda.core.data.v2.ip.Representation;
import org.roda.core.data.v2.ip.ShallowFile;
import org.roda.core.data.v2.ip.StoragePath;
import org.roda.core.data.v2.ip.disposal.DisposalConfirmation;
import org.roda.core.data.v2.ip.disposal.DisposalHold;
import org.roda.core.data.v2.ip.disposal.DisposalSchedule;
import org.roda.core.data.v2.ip.metadata.DescriptiveMetadata;
import org.roda.core.data.v2.ip.metadata.PreservationMetadata;
import org.roda.core.data.v2.ip.metadata.PreservationMetadata.PreservationMetadataType;
import org.roda.core.data.v2.jobs.IndexedReport;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.data.v2.log.LogEntry;
import org.roda.core.data.v2.notifications.Notification;
import org.roda.core.data.v2.ri.RepresentationInformation;
import org.roda.core.data.v2.risks.IndexedRisk;
import org.roda.core.data.v2.risks.Risk;
import org.roda.core.data.v2.risks.RiskIncidence;
import org.roda.core.index.IndexService;
import org.roda.core.protocols.Protocol;
import org.roda.core.storage.ContentPayload;
import org.roda.core.storage.DefaultBinary;
import org.roda.core.storage.DefaultStoragePath;
import org.roda.core.storage.JsonContentPayload;
import org.roda.core.storage.Resource;
import org.roda.core.storage.StorageService;
import org.roda.core.storage.fs.FileStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Model related utility class
 * 
 * @author Hélder Silva <hsilva@keep.pt>
 * @author Luis Faria <lfaria@keep.pt>
 */
public final class ModelUtils {

  private static final Logger LOGGER = LoggerFactory.getLogger(ModelUtils.class);

  /**
   * Private empty constructor
   */
  private ModelUtils() {
    // do nothing
  }

  private static List<String> build(List<String> basePath, String... path) {
    List<String> ret = new ArrayList<>(basePath);
    for (String pathItem : path) {
      ret.add(pathItem);
    }
    return ret;
  }

  public static StoragePath getAIPContainerPath() throws RequestNotValidException {
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

  private static List<String> getAIPOtherMetadataPath(String aipId, String type) {
    return build(getAIPMetadataPath(aipId), RodaConstants.STORAGE_DIRECTORY_OTHER, type);
  }

  public static StoragePath getAIPOtherMetadataStoragePath(String aipId, String type) throws RequestNotValidException {
    return DefaultStoragePath.parse(getAIPOtherMetadataPath(aipId, type));
  }

  private static List<String> getSubmissionPath(String aipId) {
    return build(getAIPPath(aipId), RodaConstants.STORAGE_DIRECTORY_SUBMISSION);
  }

  public static StoragePath getSubmissionStoragePath(String aipId) throws RequestNotValidException {
    return DefaultStoragePath.parse(getSubmissionPath(aipId));
  }

  private static List<String> getRepresentationPath(String aipId, String representationId) {
    if (representationId == null) {
      return getAIPPath(aipId);
    } else {
      return build(getAIPPath(aipId), RodaConstants.STORAGE_DIRECTORY_REPRESENTATIONS, representationId);
    }
  }

  public static StoragePath getRepresentationsContainerPath(String aipId) throws RequestNotValidException {
    return DefaultStoragePath.parse(RodaConstants.STORAGE_CONTAINER_AIP, aipId,
      RodaConstants.STORAGE_DIRECTORY_REPRESENTATIONS);
  }

  public static StoragePath getRepresentationStoragePath(String aipId, String representationId)
    throws RequestNotValidException {
    return DefaultStoragePath.parse(getRepresentationPath(aipId, representationId));
  }

  private static List<String> getRepresentationMetadataPath(String aipId, String representationId) {
    return build(getRepresentationPath(aipId, representationId), RodaConstants.STORAGE_DIRECTORY_METADATA);
  }

  public static StoragePath getRepresentationMetadataStoragePath(String aipId, String representationId)
    throws RequestNotValidException {
    return DefaultStoragePath.parse(getRepresentationMetadataPath(aipId, representationId));
  }

  private static List<String> getRepresentationPreservationMetadataPath(String aipId, String representationId) {
    return build(getRepresentationMetadataPath(aipId, representationId), RodaConstants.STORAGE_DIRECTORY_PRESERVATION);
  }

  public static StoragePath getRepresentationPreservationMetadataStoragePath(String aipId, String representationId)
    throws RequestNotValidException {
    return DefaultStoragePath.parse(getRepresentationPreservationMetadataPath(aipId, representationId));
  }

  private static List<String> getRepresentationOtherMetadataFolderPath(String aipId, String representationId) {
    return build(getRepresentationMetadataPath(aipId, representationId), RodaConstants.STORAGE_DIRECTORY_OTHER);
  }

  private static List<String> getRepresentationOtherMetadataPath(String aipId, String representationId, String type) {
    return build(getRepresentationMetadataPath(aipId, representationId), RodaConstants.STORAGE_DIRECTORY_OTHER, type);
  }

  public static StoragePath getRepresentationOtherMetadataFolderStoragePath(String aipId, String representationId)
    throws RequestNotValidException {
    return DefaultStoragePath.parse(getRepresentationOtherMetadataFolderPath(aipId, representationId));
  }

  public static List<String> getOtherMetadataStoragePath(String aipId, String representationId, List<String> filePath,
    String fileId, String type) {
    List<String> path;
    if (type == null) {
      path = getRepresentationOtherMetadataFolderPath(aipId, representationId);
    } else {
      path = getRepresentationOtherMetadataPath(aipId, representationId, type);
    }

    if (filePath != null) {
      path.addAll(filePath);
    }

    if (fileId != null) {
      path.add(fileId);
    }

    return path;
  }

  private static List<String> getRepresentationDataPath(String aipId, String representationId) {
    return build(getRepresentationPath(aipId, representationId), RodaConstants.STORAGE_DIRECTORY_DATA);
  }

  public static StoragePath getRepresentationDataStoragePath(String aipId, String representationId)
    throws RequestNotValidException {
    return DefaultStoragePath.parse(getRepresentationDataPath(aipId, representationId));
  }

  private static List<String> getDIPDataPath(String dipId) {
    return build(Arrays.asList(RodaConstants.STORAGE_CONTAINER_DIP, dipId), RodaConstants.STORAGE_DIRECTORY_DATA);
  }

  public static StoragePath getDIPDataStoragePath(String dipId) throws RequestNotValidException {
    return DefaultStoragePath.parse(getDIPDataPath(dipId));
  }

  public static StoragePath getDescriptiveMetadataStoragePath(String aipId, String descriptiveMetadataBinaryId)
    throws RequestNotValidException {
    return getDescriptiveMetadataStoragePath(aipId, null, descriptiveMetadataBinaryId);
  }

  public static StoragePath getDescriptiveMetadataStoragePath(String aipId, String representationId,
    String descriptiveMetadataBinaryId) throws RequestNotValidException {
    List<String> path = build(getRepresentationPath(aipId, representationId), RodaConstants.STORAGE_DIRECTORY_METADATA,
      RodaConstants.STORAGE_DIRECTORY_DESCRIPTIVE, descriptiveMetadataBinaryId);
    return DefaultStoragePath.parse(path);
  }

  public static StoragePath getDescriptiveMetadataDirectoryStoragePath(String aipId, String representationId)
    throws RequestNotValidException {
    List<String> path = build(getRepresentationPath(aipId, representationId), RodaConstants.STORAGE_DIRECTORY_METADATA,
      RodaConstants.STORAGE_DIRECTORY_DESCRIPTIVE);
    return DefaultStoragePath.parse(path);
  }

  public static StoragePath getDescriptiveMetadataStoragePath(DescriptiveMetadata descriptiveMetadata)
    throws RequestNotValidException {
    return getDescriptiveMetadataStoragePath(descriptiveMetadata.getAipId(), descriptiveMetadata.getRepresentationId(),
      descriptiveMetadata.getId());
  }

  private static List<String> getDocumentationPath(String aipId) {
    return build(getAIPPath(aipId), RodaConstants.STORAGE_DIRECTORY_DOCUMENTATION);
  }

  private static List<String> getDocumentationPath(String aipId, String representationId) {
    return build(getRepresentationPath(aipId, representationId), RodaConstants.STORAGE_DIRECTORY_DOCUMENTATION);
  }

  public static StoragePath getDocumentationStoragePath(String aipId) throws RequestNotValidException {
    return DefaultStoragePath.parse(getDocumentationPath(aipId));
  }

  public static StoragePath getDocumentationStoragePath(String aipId, String representationId)
    throws RequestNotValidException {
    return DefaultStoragePath.parse(getDocumentationPath(aipId, representationId));
  }

  public static StoragePath getDocumentationStoragePath(String aipId, String representationId,
    List<String> directoryPath, String fileId) throws RequestNotValidException {
    List<String> path = getDocumentationPath(aipId, representationId);
    if (directoryPath != null) {
      path.addAll(directoryPath);
    }
    path.add(fileId);
    return DefaultStoragePath.parse(path);
  }

  private static List<String> getSchemasPath(String aipId) {
    return build(getAIPPath(aipId), RodaConstants.STORAGE_DIRECTORY_SCHEMAS);
  }

  private static List<String> getSchemasPath(String aipId, String representationId) {
    return build(getRepresentationPath(aipId, representationId), RodaConstants.STORAGE_DIRECTORY_SCHEMAS);
  }

  public static StoragePath getSchemasStoragePath(String aipId) throws RequestNotValidException {
    return DefaultStoragePath.parse(getSchemasPath(aipId));
  }

  public static StoragePath getSchemasStoragePath(String aipId, String representationId)
    throws RequestNotValidException {
    return DefaultStoragePath.parse(getSchemasPath(aipId, representationId));
  }

  public static StoragePath getSchemaStoragePath(String aipId, String representationId, List<String> directoryPath,
    String fileId) throws RequestNotValidException {
    List<String> path = getSchemasPath(aipId, representationId);
    if (directoryPath != null) {
      path.addAll(directoryPath);
    }
    path.add(fileId);
    return DefaultStoragePath.parse(path);
  }

  public static StoragePath getFileStoragePath(String aipId, String representationId, List<String> directoryPath,
    String fileId) throws RequestNotValidException {
    List<String> path = getRepresentationDataPath(aipId, representationId);
    if (directoryPath != null) {
      path.addAll(directoryPath);
    }
    if (StringUtils.isNotBlank(fileId)) {
      path.add(fileId);
    }
    return DefaultStoragePath.parse(path);
  }

  public static StoragePath getFileStoragePath(File f) throws RequestNotValidException {
    return getFileStoragePath(f.getAipId(), f.getRepresentationId(), f.getPath(), f.getId());
  }

  public static StoragePath getDIPFileStoragePath(String dipId, List<String> directoryPath, String fileId)
    throws RequestNotValidException {
    List<String> path = getDIPDataPath(dipId);
    if (directoryPath != null) {
      path.addAll(directoryPath);
    }
    if (fileId != null) {
      path.add(fileId);
    }
    return DefaultStoragePath.parse(path);
  }

  public static StoragePath getDIPFileStoragePath(DIPFile f) throws RequestNotValidException {
    return getDIPFileStoragePath(f.getDipId(), f.getPath(), f.getId());
  }

  public static Optional<String> extractAipId(StoragePath path) {
    // AIP/[aipId]/...

    String container = path.getContainerName();
    List<String> directoryPath = path.getDirectoryPath();

    if (container.equals(RodaConstants.STORAGE_CONTAINER_AIP) && !directoryPath.isEmpty()) {
      return Optional.of(directoryPath.get(0));
    }

    return Optional.empty();
  }

  public static Optional<String> extractRepresentationId(StoragePath path) {
    // AIP/[aipId]/representations/[representationId]/...

    String container = path.getContainerName();
    List<String> directoryPath = path.getDirectoryPath();

    if (container.equals(RodaConstants.STORAGE_CONTAINER_AIP) && directoryPath.size() > 1
      && directoryPath.get(1).equals(RodaConstants.STORAGE_DIRECTORY_REPRESENTATIONS)) {
      if (directoryPath.size() > 2) {
        return Optional.of(directoryPath.get(2));
      } else {
        return Optional.of(path.getName());
      }
    }

    return Optional.empty();
  }

  public static List<String> extractFilePathFromRepresentationData(StoragePath path) {
    // AIP/[aipId]/representations/[representationId]/data/.../file.bin

    String container = path.getContainerName();
    List<String> directoryPath = path.getDirectoryPath();

    if (container.equals(RodaConstants.STORAGE_CONTAINER_AIP) && directoryPath.size() > 3
      && directoryPath.get(1).equals(RodaConstants.STORAGE_DIRECTORY_REPRESENTATIONS)
      && directoryPath.get(3).equals(RodaConstants.STORAGE_DIRECTORY_DATA)) {
      return directoryPath.subList(4, directoryPath.size());
    }

    return new ArrayList<>();
  }

  public static Optional<String> extractDipId(StoragePath path) {
    // DIP/[dipId]/...

    String container = path.getContainerName();
    List<String> directoryPath = path.getDirectoryPath();

    if (container.equals(RodaConstants.STORAGE_CONTAINER_DIP) && !directoryPath.isEmpty()) {
      return Optional.of(directoryPath.get(0));
    }

    return Optional.empty();
  }

  public static List<String> extractFilePathFromDIPData(StoragePath path) {
    // DIP/[dipId]/data/.../file.bin

    String container = path.getContainerName();
    List<String> directoryPath = path.getDirectoryPath();

    if (container.equals(RodaConstants.STORAGE_CONTAINER_DIP) && directoryPath.size() > 1
      && directoryPath.get(1).equals(RodaConstants.STORAGE_DIRECTORY_DATA)) {
      return directoryPath.subList(2, directoryPath.size());
    }

    return new ArrayList<>();
  }

  public static List<String> extractFilePathFromAipPreservationMetadata(StoragePath path) {
    // AIP/[aipId]/metadata/preservation/.../file.bin

    String container = path.getContainerName();
    List<String> directoryPath = path.getDirectoryPath();

    if (container.equals(RodaConstants.STORAGE_CONTAINER_AIP) && directoryPath.size() > 2
      && directoryPath.get(1).equals(RodaConstants.STORAGE_DIRECTORY_METADATA)
      && directoryPath.get(2).equals(RodaConstants.STORAGE_DIRECTORY_PRESERVATION)) {
      return directoryPath.subList(3, directoryPath.size());
    }

    return new ArrayList<>();
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
    }

    return new ArrayList<>();
  }

  public static Optional<String> extractTypeFromAipOtherMetadata(StoragePath path) {
    // AIP/[aipId]/metadata/other/[type]/...

    String container = path.getContainerName();
    List<String> directoryPath = path.getDirectoryPath();

    if (container.equals(RodaConstants.STORAGE_CONTAINER_AIP) && directoryPath.size() > 2
      && directoryPath.get(1).equals(RodaConstants.STORAGE_DIRECTORY_METADATA)
      && directoryPath.get(2).equals(RodaConstants.STORAGE_DIRECTORY_OTHER)) {
      if (directoryPath.size() > 3) {
        return Optional.of(directoryPath.get(3));
      } else {
        return Optional.of(path.getName());
      }
    }

    return Optional.empty();
  }

  public static List<String> extractFilePathFromAipOtherMetadata(StoragePath path) {
    // AIP/[aipId]/metadata/other/[type]/.../file.bin

    String container = path.getContainerName();
    List<String> directoryPath = path.getDirectoryPath();

    if (container.equals(RodaConstants.STORAGE_CONTAINER_AIP) && directoryPath.size() > 3
      && directoryPath.get(1).equals(RodaConstants.STORAGE_DIRECTORY_METADATA)
      && directoryPath.get(2).equals(RodaConstants.STORAGE_DIRECTORY_OTHER)) {
      return directoryPath.subList(4, directoryPath.size());
    }

    return new ArrayList<>();
  }

  public static Optional<String> extractTypeFromRepresentationOtherMetadata(StoragePath path) {
    // AIP/[aipId]/representations/[representationId]/metadata/other/[type]/...

    String container = path.getContainerName();
    List<String> directoryPath = path.getDirectoryPath();

    if (container.equals(RodaConstants.STORAGE_CONTAINER_AIP) && directoryPath.size() > 4
      && directoryPath.get(1).equals(RodaConstants.STORAGE_DIRECTORY_REPRESENTATIONS)
      && directoryPath.get(3).equals(RodaConstants.STORAGE_DIRECTORY_METADATA)
      && directoryPath.get(4).equals(RodaConstants.STORAGE_DIRECTORY_OTHER)) {
      if (directoryPath.size() > 5) {
        return Optional.of(directoryPath.get(5));
      } else {
        return Optional.of(path.getName());
      }
    }

    return Optional.empty();
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
    }

    return new ArrayList<>();
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

  public static StoragePath getPreservationAgentStoragePath() throws RequestNotValidException {
    List<String> path = Arrays.asList(RodaConstants.STORAGE_CONTAINER_PRESERVATION,
      RodaConstants.STORAGE_DIRECTORY_AGENTS);
    return DefaultStoragePath.parse(path);
  }

  public static StoragePath getPreservationRepositoryEventStoragePath() throws RequestNotValidException {
    List<String> path = Arrays.asList(RodaConstants.STORAGE_CONTAINER_PRESERVATION,
      RodaConstants.STORAGE_DIRECTORY_EVENTS);
    return DefaultStoragePath.parse(path);
  }

  public static StoragePath getPreservationAIPStoragePath(String aipId) throws RequestNotValidException {
    List<String> path = Arrays.asList(RodaConstants.STORAGE_CONTAINER_AIP, aipId,
      RodaConstants.STORAGE_DIRECTORY_METADATA, RodaConstants.STORAGE_DIRECTORY_PRESERVATION);
    return DefaultStoragePath.parse(path);
  }

  public static StoragePath getPreservationMetadataStoragePath(String id, PreservationMetadataType type, String aipId,
    String representationId, List<String> fileDirectoryPath, String fileId) throws RequestNotValidException {
    List<String> path;
    if (type != null) {
      if (type.equals(PreservationMetadataType.AGENT)) {
        path = Arrays.asList(RodaConstants.STORAGE_CONTAINER_PRESERVATION, RodaConstants.STORAGE_DIRECTORY_AGENTS,
          id + RodaConstants.PREMIS_SUFFIX);
      } else if (type.equals(PreservationMetadataType.REPRESENTATION)) {
        if (aipId != null && representationId != null) {
          String pFileId = id + RodaConstants.PREMIS_SUFFIX;
          path = build(getRepresentationPreservationMetadataPath(aipId, representationId), pFileId);
        } else {
          throw new RequestNotValidException("Cannot request a representation object with null AIP or Representation. "
            + "AIP id = " + aipId + " and Representation id = " + representationId);
        }
      } else if (type.equals(PreservationMetadataType.EVENT)) {
        String pFileId = id + RodaConstants.PREMIS_SUFFIX;
        if (aipId != null) {
          if (representationId != null) {
            if (fileId != null) {
              path = getRepresentationPreservationMetadataPath(aipId, representationId);
              if (fileDirectoryPath != null) {
                path.addAll(fileDirectoryPath);
              }

              try {
                String separator = URLEncoder.encode(RodaConstants.URN_SEPARATOR, RodaConstants.DEFAULT_ENCODING);
                if (StringUtils.countMatches(id, separator) > 0) {
                  path.add(id + RodaConstants.PREMIS_SUFFIX);
                } else {
                  path.add(id + separator + fileId + RodaConstants.PREMIS_SUFFIX);
                }
              } catch (UnsupportedEncodingException e) {
                LOGGER.error("Error encoding urn separator when creating file event preservation metadata");
              }
            } else {
              path = build(getRepresentationPreservationMetadataPath(aipId, representationId), pFileId);
            }
          } else {
            path = build(getAIPPreservationMetadataPath(aipId), pFileId);
          }
        } else {
          path = Arrays.asList(RodaConstants.STORAGE_CONTAINER_PRESERVATION, RodaConstants.STORAGE_DIRECTORY_EVENTS,
            id + RodaConstants.PREMIS_SUFFIX);
        }
      } else if (type.equals(PreservationMetadataType.FILE)) {
        path = getRepresentationMetadataPath(aipId, representationId);
        path.add(RodaConstants.STORAGE_DIRECTORY_PRESERVATION);
        if (fileDirectoryPath != null) {
          path.addAll(fileDirectoryPath);
        }
        path.add(id + RodaConstants.PREMIS_SUFFIX);
      } else if (type.equals(PreservationMetadataType.OTHER)) {
        path = getRepresentationMetadataPath(aipId, representationId);
        path.add(RodaConstants.STORAGE_DIRECTORY_PRESERVATION);
        path.add(RodaConstants.STORAGE_DIRECTORY_OTHER_TECH_METADATA);
        if (fileDirectoryPath != null) {
          path.addAll(fileDirectoryPath);
        }
        path.add(fileId + RodaConstants.OTHER_TECH_METADATA_FILE_SUFFIX);
      } else {
        throw new RequestNotValidException("Unsupported preservation metadata type: " + type);
      }
    } else {
      throw new RequestNotValidException("Preservation metadata type is null");
    }

    return DefaultStoragePath.parse(path);
  }

  public static StoragePath getLogContainerPath() throws RequestNotValidException {
    return DefaultStoragePath.parse(RodaConstants.STORAGE_CONTAINER_ACTIONLOG);
  }

  public static StoragePath getLogStoragePath(String logFile) throws RequestNotValidException {
    return DefaultStoragePath.parse(RodaConstants.STORAGE_CONTAINER_ACTIONLOG, logFile);
  }

  public static StoragePath getJobContainerPath() throws RequestNotValidException {
    return DefaultStoragePath.parse(RodaConstants.STORAGE_CONTAINER_JOB);
  }

  public static StoragePath getJobStoragePath(String jobId) throws RequestNotValidException {
    return DefaultStoragePath.parse(RodaConstants.STORAGE_CONTAINER_JOB, jobId + RodaConstants.JOB_FILE_EXTENSION);
  }

  public static StoragePath getDisposalHoldStoragePath(String disposalHoldId) throws RequestNotValidException {
    return DefaultStoragePath.parse(RodaConstants.STORAGE_CONTAINER_DISPOSAL_HOLD,
      disposalHoldId + RodaConstants.DISPOSAL_HOLD_FILE_EXTENSION);
  }

  public static StoragePath getDisposalHoldContainerPath() throws RequestNotValidException {
    return DefaultStoragePath.parse(RodaConstants.STORAGE_CONTAINER_DISPOSAL_HOLD);
  }

  public static String getJobId(StoragePath jobPath) {
    return jobPath.getName().replace(RodaConstants.JOB_FILE_EXTENSION, "");
  }

  public static StoragePath getJobReportContainerPath() throws RequestNotValidException {
    return DefaultStoragePath.parse(RodaConstants.STORAGE_CONTAINER_JOB_REPORT);
  }

  public static StoragePath getJobReportsStoragePath(String jobId) throws RequestNotValidException {
    return DefaultStoragePath.parse(RodaConstants.STORAGE_CONTAINER_JOB_REPORT, jobId);
  }

  public static StoragePath getJobReportStoragePath(String jobId, String jobReportId) throws RequestNotValidException {
    return DefaultStoragePath.parse(RodaConstants.STORAGE_CONTAINER_JOB_REPORT, jobId,
      jobReportId + RodaConstants.JOB_REPORT_FILE_EXTENSION);
  }

  public static List<String> getJobAndReportIds(StoragePath jobReportPath) {
    String reportId = jobReportPath.getName().replace(RodaConstants.JOB_REPORT_FILE_EXTENSION, "");
    String jobId = jobReportPath.getDirectoryPath().get(0);
    return Arrays.asList(jobId, reportId);
  }

  public static StoragePath getRiskContainerPath() throws RequestNotValidException {
    return DefaultStoragePath.parse(RodaConstants.STORAGE_CONTAINER_RISK);
  }

  public static StoragePath getRiskStoragePath(String riskId) throws RequestNotValidException {
    return DefaultStoragePath.parse(RodaConstants.STORAGE_CONTAINER_RISK, riskId + RodaConstants.RISK_FILE_EXTENSION);
  }

  public static String getRiskId(StoragePath riskPath) {
    return riskPath.getName().replace(RodaConstants.RISK_FILE_EXTENSION, "");
  }

  public static StoragePath getRiskIncidenceContainerPath() throws RequestNotValidException {
    return DefaultStoragePath.parse(RodaConstants.STORAGE_CONTAINER_RISK_INCIDENCE);
  }

  public static StoragePath getRiskIncidenceStoragePath(String riskIncidenceId) throws RequestNotValidException {
    return DefaultStoragePath.parse(RodaConstants.STORAGE_CONTAINER_RISK_INCIDENCE,
      riskIncidenceId + RodaConstants.RISK_INCIDENCE_FILE_EXTENSION);
  }

  public static String getRiskIncidenceId(StoragePath incidencePath) {
    return incidencePath.getName().replace(RodaConstants.RISK_INCIDENCE_FILE_EXTENSION, "");
  }

  public static StoragePath getRepresentationInformationContainerPath() throws RequestNotValidException {
    return DefaultStoragePath.parse(RodaConstants.STORAGE_CONTAINER_REPRESENTATION_INFORMATION);
  }

  public static StoragePath getRepresentationInformationStoragePath(String representationInformationId)
    throws RequestNotValidException {
    return DefaultStoragePath.parse(RodaConstants.STORAGE_CONTAINER_REPRESENTATION_INFORMATION,
      representationInformationId + RodaConstants.REPRESENTATION_INFORMATION_FILE_EXTENSION);
  }

  public static String getRepresentationInformationId(StoragePath representationInformationPath) {
    return representationInformationPath.getName().replace(RodaConstants.REPRESENTATION_INFORMATION_FILE_EXTENSION, "");
  }

  public static StoragePath getNotificationContainerPath() throws RequestNotValidException {
    return DefaultStoragePath.parse(RodaConstants.STORAGE_CONTAINER_NOTIFICATION);
  }

  public static StoragePath getNotificationStoragePath(String notificationId) throws RequestNotValidException {
    return DefaultStoragePath.parse(RodaConstants.STORAGE_CONTAINER_NOTIFICATION,
      notificationId + RodaConstants.NOTIFICATION_FILE_EXTENSION);
  }

  public static String getNotificationId(StoragePath notificationPath) {
    return notificationPath.getName().replace(RodaConstants.NOTIFICATION_FILE_EXTENSION, "");
  }

  public static StoragePath getDIPContainerPath() throws RequestNotValidException {
    return DefaultStoragePath.parse(RodaConstants.STORAGE_CONTAINER_DIP);
  }

  public static StoragePath getDisposalRuleContainerPath() throws RequestNotValidException {
    return DefaultStoragePath.parse(RodaConstants.STORAGE_CONTAINER_DISPOSAL_RULE);
  }

  public static StoragePath getDisposalScheduleContainerPath() throws RequestNotValidException {
    return DefaultStoragePath.parse(RodaConstants.STORAGE_CONTAINER_DISPOSAL_SCHEDULE);
  }

  public static StoragePath getDisposalConfirmationContainerPath() throws RequestNotValidException {
    return DefaultStoragePath.parse(RodaConstants.STORAGE_CONTAINER_DISPOSAL_CONFIRMATION);
  }

  public static StoragePath getDIPStoragePath(String dipId) throws RequestNotValidException {
    return DefaultStoragePath.parse(RodaConstants.STORAGE_CONTAINER_DIP, dipId);
  }

  public static StoragePath getDIPMetadataStoragePath(String dipId) throws RequestNotValidException {
    return DefaultStoragePath.parse(getDIPStoragePath(dipId), RodaConstants.STORAGE_DIP_METADATA_FILENAME);
  }

  public static StoragePath getOtherMetadataFolderStoragePath(String aipId, String representationId)
    throws RequestNotValidException {
    return DefaultStoragePath.parse(getRepresentationOtherMetadataFolderPath(aipId, representationId));
  }

  public static StoragePath getOtherMetadataStoragePath(String aipId, String representationId,
    List<String> directoryPath, String fileName, String fileSuffix, String type) throws RequestNotValidException {

    if (fileSuffix == null) {
      throw new RequestNotValidException("File suffix cannot be null");
    }

    if (StringUtils.isBlank(type)) {
      throw new RequestNotValidException("Type cannot be empty");
    }

    List<String> path;

    if (aipId != null && representationId != null && directoryPath != null && fileName != null) {
      // other metadata pertaining to a file
      path = getRepresentationOtherMetadataPath(aipId, representationId, type);
      path.addAll(directoryPath);
      path.add(fileName + fileSuffix);
    } else if (aipId != null && representationId != null) {
      // other metadata pertaining to a representation
      path = getRepresentationOtherMetadataPath(aipId, representationId, type);
      path.add(representationId + fileSuffix);
      // XXX What if representation id is equal to a file id? Maybe move
      // this to AIP metadata folder and have id
      // [aipId+"-"+representationId+fileSuffix]
    } else if (aipId != null) {
      path = getRepresentationOtherMetadataPath(aipId, representationId, type);
      path.add(aipId + fileSuffix);
    } else {
      throw new RequestNotValidException("AIP id cannot be null");
    }
    return DefaultStoragePath.parse(path);
  }

  public static <T extends Serializable> StoragePath getContainerPath(Class<T> clazz) throws RequestNotValidException {
    if (clazz.equals(RepresentationInformation.class)) {
      return getRepresentationInformationContainerPath();
    } else if (clazz.equals(Notification.class)) {
      return getNotificationContainerPath();
    } else if (clazz.equals(Risk.class)) {
      return getRiskContainerPath();
    } else if (clazz.equals(LogEntry.class)) {
      return getLogContainerPath();
    } else if (clazz.equals(Job.class)) {
      return getJobContainerPath();
    } else if (clazz.equals(AIP.class)) {
      return getAIPContainerPath();
    } else if (clazz.equals(Report.class)) {
      return getJobReportContainerPath();
    } else if (clazz.equals(RiskIncidence.class)) {
      return getRiskIncidenceContainerPath();
    } else if (clazz.equals(DIP.class)) {
      return getDIPContainerPath();
    } else if (clazz.equals(DisposalSchedule.class)) {
      return getDisposalScheduleContainerPath();
    } else if (clazz.equals(DisposalHold.class)) {
      return getDisposalHoldContainerPath();
    } else if (clazz.equals(DisposalConfirmation.class)) {
      return getDisposalConfirmationContainerPath();
    } else {
      throw new RequestNotValidException("Unknown class for getting container path: " + clazz.getName());
    }
  }

  public static StoragePath getOtherMetadataStoragePath(String aipId, String representationId, String type,
    List<String> fileDirectoryPath, String fileId) throws RequestNotValidException {
    List<String> path = getRepresentationOtherMetadataPath(aipId, representationId, type);
    path.addAll(fileDirectoryPath);
    path.add(fileId);
    return DefaultStoragePath.parse(path);
  }

  public static List<IndexedAIP> getIndexedAIPsFromObjectIds(SelectedItems<IndexedAIP> selectedItems)
    throws GenericException, RequestNotValidException {
    List<IndexedAIP> res = new ArrayList<>();
    if (selectedItems instanceof SelectedItemsList) {
      SelectedItemsList<IndexedAIP> list = (SelectedItemsList<IndexedAIP>) selectedItems;
      for (String objectId : list.getIds()) {
        try {
          res.add(RodaCoreFactory.getIndexService().retrieve(IndexedAIP.class, objectId, new ArrayList<>()));
        } catch (GenericException | NotFoundException e) {
          LOGGER.error("Error retrieving AIP", e);
        }
      }
    } else if (selectedItems instanceof SelectedItemsFilter) {
      IndexService index = RodaCoreFactory.getIndexService();
      SelectedItemsFilter<IndexedAIP> selectedItemsFilter = (SelectedItemsFilter<IndexedAIP>) selectedItems;
      long count = index.count(IndexedAIP.class, selectedItemsFilter.getFilter());
      for (int i = 0; i < count; i += RodaConstants.DEFAULT_PAGINATION_VALUE) {
        List<IndexedAIP> aips = index.find(IndexedAIP.class, selectedItemsFilter.getFilter(), null,
          new Sublist(i, RodaConstants.DEFAULT_PAGINATION_VALUE), null).getResults();
        res.addAll(aips);
      }
    }
    return res;
  }

  public static List<IndexedDIP> getIndexedDIPsFromObjectIds(SelectedItems<IndexedDIP> selectedItems)
    throws GenericException, RequestNotValidException {
    List<IndexedDIP> res = new ArrayList<>();
    if (selectedItems instanceof SelectedItemsList) {
      SelectedItemsList<IndexedDIP> list = (SelectedItemsList<IndexedDIP>) selectedItems;
      for (String objectId : list.getIds()) {
        try {
          res.add(RodaCoreFactory.getIndexService().retrieve(IndexedDIP.class, objectId, new ArrayList<>()));
        } catch (GenericException | NotFoundException e) {
          LOGGER.error("Error retrieving DIP", e);
        }
      }
    } else if (selectedItems instanceof SelectedItemsFilter) {
      IndexService index = RodaCoreFactory.getIndexService();
      SelectedItemsFilter<IndexedDIP> selectedItemsFilter = (SelectedItemsFilter<IndexedDIP>) selectedItems;
      long count = index.count(IndexedDIP.class, selectedItemsFilter.getFilter());
      for (int i = 0; i < count; i += RodaConstants.DEFAULT_PAGINATION_VALUE) {
        List<IndexedDIP> dips = index.find(IndexedDIP.class, selectedItemsFilter.getFilter(), null,
          new Sublist(i, RodaConstants.DEFAULT_PAGINATION_VALUE), null).getResults();
        res.addAll(dips);
      }
    }
    return res;
  }

  public static <T extends IsRODAObject> Class<T> giveRespectiveModelClass(Class<T> inputClass) {
    // function that give the model representation object of a RODA object
    if (IndexedAIP.class.equals(inputClass)) {
      return (Class<T>) AIP.class;
    } else if (IndexedRepresentation.class.equals(inputClass)) {
      return (Class<T>) Representation.class;
    } else if (IndexedFile.class.equals(inputClass)) {
      return (Class<T>) File.class;
    } else if (IndexedRisk.class.equals(inputClass)) {
      return (Class<T>) Risk.class;
    } else if (IndexedDIP.class.equals(inputClass)) {
      return (Class<T>) DIP.class;
    } else if (IndexedReport.class.equals(inputClass)) {
      return (Class<T>) Report.class;
    } else {
      return inputClass;
    }
  }

  public static StoragePath getDisposalRuleStoragePath(String disposalRuleId) throws RequestNotValidException {
    return DefaultStoragePath.parse(RodaConstants.STORAGE_CONTAINER_DISPOSAL_RULE,
      disposalRuleId + RodaConstants.JOB_FILE_EXTENSION);
  }

  public static StoragePath getDisposalScheduleStoragePath(String disposalScheduleId) throws RequestNotValidException {
    return DefaultStoragePath.parse(RodaConstants.STORAGE_CONTAINER_DISPOSAL_SCHEDULE,
      disposalScheduleId + RodaConstants.JOB_FILE_EXTENSION);
  }

  public static StoragePath getDisposalConfirmationStoragePath(String disposalConfirmationId)
    throws RequestNotValidException {
    return DefaultStoragePath.parse(getDisposalConfirmationPath(disposalConfirmationId));
  }

  public static StoragePath getDisposalConfirmationAIPsPath(String disposalConfirmationId)
    throws RequestNotValidException {
    return DefaultStoragePath.parse(ModelUtils.getDisposalConfirmationStoragePath(disposalConfirmationId),
      RodaConstants.STORAGE_DIRECTORY_DISPOSAL_CONFIRMATION_AIPS_FILENAME);
  }

  private static List<String> getDisposalConfirmationPath(String confirmationId) {
    return Arrays.asList(RodaConstants.STORAGE_CONTAINER_DISPOSAL_CONFIRMATION, confirmationId);
  }

  public static StorageService resolveTemporaryResourceShallow(String jobId, StorageService storage,
    StoragePath storagePath) throws GenericException {
    FileStorageService temporaryStorage;
    Path tempPath = RodaCoreFactory.getFileShallowTmpDirectoryPath().resolve(jobId)
      .resolve(String.valueOf(storagePath.hashCode()));
    try {
      if (Files.exists(tempPath)) {
        temporaryStorage = new FileStorageService(tempPath, false, null, false);
      } else {
        Files.createDirectories(tempPath);
        temporaryStorage = new FileStorageService(tempPath, false, null, false);

        temporaryStorage.copy(storage, storagePath, storagePath);
        List<StoragePath> externalFiles = temporaryStorage.getShallowFiles(storagePath);
        for (StoragePath externalFile : externalFiles) {
          final CloseableIterable<Resource> resources = temporaryStorage.listResourcesUnderFile(externalFile, true);
          for (Resource resource : resources) {
            if (resource instanceof DefaultBinary) {
              ContentPayload content = ((DefaultBinary) resource).getContent();
              if (content instanceof JsonContentPayload) {
                ShallowFile shallowFile = JsonUtils.getObjectFromJson(content.createInputStream(), ShallowFile.class);
                Protocol pm = RodaCoreFactory.getProtocol(shallowFile.getLocation());
                // ProtocolManager pm =
                // ProtocolManagerFactory.createProtocolManager(shallowFile.getLocation());
                pm.downloadResource(temporaryStorage.getDirectAccess(externalFile).getPath().getParent());
              }

            }
          }
          temporaryStorage.deleteResource(externalFile);
        }
      }
    } catch (IOException | AlreadyExistsException | AuthorizationDeniedException e) {
      throw new GenericException("Cannot create temporary directory " + tempPath);
    } catch (RequestNotValidException | NotFoundException e) {
      throw new GenericException("Cannot found resources at " + storagePath.toString());
    }
    return temporaryStorage;
  }

  public static void removeTemporaryResourceShallow(String jobId, StoragePath storagePath) throws IOException {
    Path tempPath = RodaCoreFactory.getFileShallowTmpDirectoryPath().resolve(jobId)
      .resolve(String.valueOf(storagePath.hashCode()));
    if (Files.exists(tempPath)) {
      FileUtils.deleteDirectory(tempPath.toFile());
    }
  }

  public static void removeTemporaryResourceShallow(String jobId) throws IOException {
    Path tempPath = RodaCoreFactory.getFileShallowTmpDirectoryPath().resolve(jobId);
    if (Files.exists(tempPath)) {
      FileUtils.deleteDirectory(tempPath.toFile());
    }
  }
}
