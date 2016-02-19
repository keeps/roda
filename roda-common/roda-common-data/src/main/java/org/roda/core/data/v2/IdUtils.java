package org.roda.core.data.v2;

import java.util.List;

import org.roda.core.data.v2.ip.File;
import org.roda.core.data.v2.ip.metadata.PreservationMetadata.PreservationMetadataType;

public class IdUtils {
  public static final String ID_SEPARATOR = "-";
  public static final String LINKING_ID_SEPARATOR = "/";

  public static String getRepresentationId(String aipId, String representationId) {
    StringBuilder idBuilder = new StringBuilder();
    idBuilder.append(aipId);
    idBuilder.append(ID_SEPARATOR);
    idBuilder.append(representationId);
    return idBuilder.toString();
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
    return idBuilder.toString();
  }

  public static String getFileId(File file) {
    return getFileId(file.getAipId(), file.getRepresentationId(), file.getPath(), file.getId());
  }

  public static String getPreservationMetadataId(PreservationMetadataType type, String aipId, String representationId,
    List<String> fileDirectoryPath, String fileId) {
    StringBuilder idBuilder = new StringBuilder();
    idBuilder.append(type.toString());
    if (aipId != null) {
      idBuilder.append(ID_SEPARATOR);    idBuilder.append(type.toString());

      idBuilder.append(aipId);
    }
    if (representationId != null) {
      idBuilder.append(ID_SEPARATOR);
      idBuilder.append(representationId);
    }
    if (fileDirectoryPath != null) {
      for (String dir : fileDirectoryPath) {
        idBuilder.append(ID_SEPARATOR);
        idBuilder.append(dir);
      }
    }
    if (fileId != null) {
      idBuilder.append(ID_SEPARATOR);
      idBuilder.append(fileId);
    }
    return idBuilder.toString();
  }

  public static String getOtherMetadataId(String type, String aipId, String representationId,
    List<String> fileDirectoryPath, String fileId) {
    StringBuilder idBuilder = new StringBuilder();
    idBuilder.append(type);
    if (aipId != null) {
      idBuilder.append(ID_SEPARATOR);
      idBuilder.append(aipId);
    }
    if (representationId != null) {
      idBuilder.append(ID_SEPARATOR);
      idBuilder.append(representationId);
    }
    if (fileDirectoryPath != null) {
      for (String dir : fileDirectoryPath) {
        idBuilder.append(ID_SEPARATOR);
        idBuilder.append(dir);
      }
    }
    if (fileId != null) {
      idBuilder.append(ID_SEPARATOR);
      idBuilder.append(fileId);
    }
    return idBuilder.toString();
  }

  public static String getJobReportId(String jobId, String aipId) {
    return jobId + ID_SEPARATOR + aipId;
  }

  public static String getLinkingIdentifier(String aipId, String representationId,
    List<String> fileDirectoryPath, String fileId) {
    StringBuilder idBuilder = new StringBuilder();
    if (aipId != null) {
      idBuilder.append(aipId);
    }
    if (representationId != null) {
      idBuilder.append(LINKING_ID_SEPARATOR);
      idBuilder.append(representationId);
    }
    if (fileDirectoryPath != null) {
      for (String dir : fileDirectoryPath) {
        idBuilder.append(LINKING_ID_SEPARATOR);
        idBuilder.append(dir);
      }
    }
    if (fileId != null) {
      idBuilder.append(LINKING_ID_SEPARATOR);
      idBuilder.append(fileId);
    }
    return idBuilder.toString();
  }

}
