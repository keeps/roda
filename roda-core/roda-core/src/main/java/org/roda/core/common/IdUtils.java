/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.common;

import java.util.List;
import java.util.UUID;

import org.roda.core.data.v2.ip.File;
import org.roda.core.data.v2.ip.Representation;
import org.roda.core.data.v2.ip.metadata.PreservationMetadata.PreservationMetadataType;

public final class IdUtils {
  private static final String ID_SEPARATOR = "-";

  /** Private empty constructor */
  private IdUtils() {

  }

  public static String getRepresentationId(Representation representation) {
    return getRepresentationId(representation.getAipId(), representation.getId());
  }

  public static String getRepresentationId(String aipId, String representationId) {
    StringBuilder idBuilder = new StringBuilder();
    idBuilder.append(aipId);
    idBuilder.append(ID_SEPARATOR);
    idBuilder.append(representationId);
    return UUID.nameUUIDFromBytes(idBuilder.toString().getBytes()).toString();
  }

  public static String getFileId(String aipId, String representationId, List<String> fileDirectoryPath, String fileId) {
    StringBuilder idBuilder = new StringBuilder();
    idBuilder.append(aipId);
    idBuilder.append(ID_SEPARATOR);
    idBuilder.append(representationId);
    idBuilder.append(ID_SEPARATOR);
    for (String dir : fileDirectoryPath) {
      idBuilder.append(dir);
      idBuilder.append(ID_SEPARATOR);
    }
    idBuilder.append(fileId);

    return UUID.nameUUIDFromBytes(idBuilder.toString().getBytes()).toString();
  }

  public static String getFileId(File file) {
    return getFileId(file.getAipId(), file.getRepresentationId(), file.getPath(), file.getId());
  }

  public static String getPreservationMetadataId(PreservationMetadataType type, String aipId, String representationId) {
    return type + ":" + getFileId(aipId, representationId, null, null, type.toString(), ID_SEPARATOR);
  }

  public static String getPreservationMetadataId(PreservationMetadataType type, String aipId, String representationId,
    List<String> fileDirectoryPath, String fileId) {
    return type + ":" + getFileId(aipId, representationId, fileDirectoryPath, fileId, type.toString(), ID_SEPARATOR);
  }

  public static String getOtherMetadataId(String type, String aipId, String representationId,
    List<String> fileDirectoryPath, String fileId) {
    return getFileId(aipId, representationId, fileDirectoryPath, fileId, type, ID_SEPARATOR);
  }

  public static String getJobReportId(String jobId, String aipId) {
    return jobId + ID_SEPARATOR + aipId;
  }

  private static String getFileId(String aipId, String representationId, List<String> fileDirectoryPath, String fileId,
    String type, String separator) {
    StringBuilder idBuilder = new StringBuilder();

    if (type != null) {
      idBuilder.append(type);
    }
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
    return pluginClassName + "@" + version;
  }
  
  public static String getUserAgentId(String username) {
    return username;
  }

}
