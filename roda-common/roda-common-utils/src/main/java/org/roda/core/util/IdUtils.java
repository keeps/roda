/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.util;

import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

import org.roda.core.data.utils.URNUtils;
import org.roda.core.data.v2.ip.DIPFile;
import org.roda.core.data.v2.ip.File;
import org.roda.core.data.v2.ip.FileLink;
import org.roda.core.data.v2.ip.Representation;
import org.roda.core.data.v2.ip.RepresentationLink;
import org.roda.core.data.v2.ip.metadata.PreservationMetadata.PreservationMetadataType;
import org.roda.core.data.v2.user.RodaPrincipal;

public final class IdUtils {
  private static final String ID_SEPARATOR = "-";

  /** Private empty constructor */
  private IdUtils() {
    // do nothing
  }

  public static String createUUID() {
    return UUID.randomUUID().toString();
  }

  /**
   * This function will not verify if given string is null
   **/
  public static String createUUID(String fromString) {
    return UUID.nameUUIDFromBytes(fromString.getBytes()).toString();
  }

  public static String getRepresentationId(Representation representation) {
    return getRepresentationId(representation.getAipId(), representation.getId());
  }

  public static String getRepresentationId(RepresentationLink link) {
    return getRepresentationId(link.getAipId(), link.getRepresentationId());
  }

  public static String getRepresentationId(String aipId, String representationId) {
    StringBuilder idBuilder = new StringBuilder();
    idBuilder.append(aipId);
    idBuilder.append(ID_SEPARATOR);
    idBuilder.append(representationId);
    return IdUtils.createUUID(idBuilder.toString());
  }

  public static String getFileId(String aipId, String representationId, List<String> fileDirectoryPath, String fileId) {
    StringBuilder idBuilder = new StringBuilder();
    idBuilder.append(aipId);
    idBuilder.append(ID_SEPARATOR);
    if (representationId != null) {
      idBuilder.append(representationId);
      idBuilder.append(ID_SEPARATOR);
    }
    if (fileDirectoryPath != null) {
      for (String dir : fileDirectoryPath) {
        idBuilder.append(dir);
        idBuilder.append(ID_SEPARATOR);
      }
    }
    idBuilder.append(fileId);
    return IdUtils.createUUID(idBuilder.toString());
  }

  public static String getFileId(FileLink link) {
    return getFileId(link.getAipId(), link.getRepresentationId(), link.getPath(), link.getFileId());
  }

  public static String getFileId(File file) {
    return getFileId(file.getAipId(), file.getRepresentationId(), file.getPath(), file.getId());
  }

  public static String getDIPFileId(String dipId, List<String> fileDirectoryPath, String fileId) {
    StringBuilder idBuilder = new StringBuilder();
    idBuilder.append(dipId);
    idBuilder.append(ID_SEPARATOR);
    for (String dir : fileDirectoryPath) {
      idBuilder.append(dir);
      idBuilder.append(ID_SEPARATOR);
    }
    idBuilder.append(fileId);

    return IdUtils.createUUID(idBuilder.toString());
  }

  public static String getDIPFileId(DIPFile file) {
    return getDIPFileId(file.getDipId(), file.getPath(), file.getId());
  }

  public static String getOtherMetadataId(String aipId, String representationId, List<String> fileDirectoryPath,
    String fileId) {
    return getFileId(aipId, representationId, fileDirectoryPath, fileId, ID_SEPARATOR);
  }

  public static String getJobReportId(String jobId, String sourceObjectId, String outcomeObjectId) {
    return jobId + ID_SEPARATOR + sourceObjectId + ID_SEPARATOR + outcomeObjectId;
  }

  // FIXME 20160809 hsilva: type is not being used. but should it???
  private static String getFileId(String aipId, String representationId, List<String> fileDirectoryPath, String fileId,
    String separator) {
    StringBuilder idBuilder = new StringBuilder();
    addNonNullStringToBuilder(idBuilder, aipId, separator);
    addNonNullStringToBuilder(idBuilder, representationId, separator);
    addNonNullStringToBuilder(idBuilder, fileDirectoryPath, separator);
    addNonNullStringToBuilder(idBuilder, fileId, separator);
    return idBuilder.toString();
  }

  private static StringBuilder addNonNullStringToBuilder(StringBuilder idBuilder, String string, String separator) {
    if (string != null) {
      if (idBuilder.length() > 0) {
        idBuilder.append(separator);
      }
      idBuilder.append(string);
    }
    return idBuilder;
  }

  private static StringBuilder addNonNullStringToBuilder(StringBuilder idBuilder, List<String> strings,
    String separator) {
    if (strings != null) {
      for (String string : strings) {
        idBuilder.append(separator).append(string);
      }
    }
    return idBuilder;
  }

  public static String getRiskId(String prefix, String suffix) {
    return prefix + ID_SEPARATOR + suffix;
  }

  public static String getPluginAgentId(String pluginClassName, String version) {
    return URNUtils.createRodaPreservationURN(PreservationMetadataType.AGENT, pluginClassName + "@" + version);
  }

  public static String getUserAgentId(String username) {
    return URNUtils.createRodaPreservationURN(PreservationMetadataType.AGENT, username);
  }

  public static String createPreservationMetadataId(PreservationMetadataType type) {
    return URNUtils.createRodaPreservationURN(type, IdUtils.createUUID());
  }

  public static String getRepresentationPreservationId(String aipId, String representationId) {
    return getPreservationId(PreservationMetadataType.REPRESENTATION, aipId, representationId, null, null);
  }

  public static String getPreservationId(PreservationMetadataType type, String aipId, String representationId,
    List<String> fileDirectoryPath, String fileId) {
    return URNUtils.createRodaPreservationURN(type,
      IdUtils.createUUID(getFileId(aipId, representationId, fileDirectoryPath, fileId, ID_SEPARATOR)));
  }

  public static String getPreservationFileId(File file) {
    return URNUtils.createRodaPreservationURN(file);
  }

  public static String getPreservationFileId(final List<String> path, String fileid) {
    return URNUtils.createRodaPreservationURN(PreservationMetadataType.FILE, path, fileid);
  }

  public static PreservationMetadataType getPreservationTypeFromId(String id) {
    return URNUtils.getPreservationMetadataTypeFromId(id);
  }

  public static String getTransferredResourceUUID(Path relativeToBase) {
    return getTransferredResourceUUID(relativeToBase.toString());
  }

  public static String getTransferredResourceUUID(String relativeToBase) {
    return IdUtils.createUUID(relativeToBase);
  }

  public static String getUserId(String username) {
    return RodaPrincipal.getUserUUID(username);
  }

  public static String getGroupId(String groupname) {
    return RodaPrincipal.getGroupUUID(groupname);
  }

}
