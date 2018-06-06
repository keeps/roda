/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.plugins.base;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.xmlbeans.XmlException;
import org.roda.core.common.PremisV3Utils;
import org.roda.core.common.iterables.CloseableIterable;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.common.OptionalWithCause;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.File;
import org.roda.core.data.v2.ip.Representation;
import org.roda.core.data.v2.ip.StoragePath;
import org.roda.core.data.v2.ip.metadata.DescriptiveMetadata;
import org.roda.core.data.v2.ip.metadata.Fixity;
import org.roda.core.data.v2.ip.metadata.OtherMetadata;
import org.roda.core.model.ModelService;
import org.roda.core.model.utils.ModelUtils;
import org.roda.core.storage.Binary;
import org.roda.core.storage.StorageService;
import org.roda.core.storage.fs.FSUtils;
import org.roda.core.util.FileUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InventoryReportPluginUtils {
  private static final Logger LOGGER = LoggerFactory.getLogger(InventoryReportPluginUtils.class);

  private InventoryReportPluginUtils() {
    // do nothing
  }

  public static void mergeFiles(List<Path> files, Path mergedFile) throws IOException {
    try (FileChannel out = FileChannel.open(mergedFile, StandardOpenOption.APPEND, StandardOpenOption.WRITE)) {
      for (Path path : files) {
        try (FileChannel in = FileChannel.open(path, StandardOpenOption.READ)) {
          for (long p = 0, l = in.size(); p < l;) {
            p += in.transferTo(p, l - p, out);
          }
        }
      }
    } catch (IOException e) {
      throw e;
    }
  }

  public static List<List<String>> getDataInformation(List<String> fields, AIP aip, ModelService model,
    StorageService storage) {
    List<List<String>> dataInformation = new ArrayList<>();
    for (Representation representation : aip.getRepresentations()) {
      boolean recursive = true;
      try (CloseableIterable<OptionalWithCause<File>> representationFiles = model.listFilesUnder(aip.getId(),
        representation.getId(), recursive)) {
        for (OptionalWithCause<File> subfile : representationFiles) {
          if (subfile.isPresent()) {
            dataInformation.add(retrieveFileInfo(fields, subfile.get(), aip, model, storage));
          } else {
            LOGGER.error("Cannot retrieve file information", subfile.getCause());
          }
        }
      } catch (NotFoundException | GenericException | RequestNotValidException | AuthorizationDeniedException
        | IOException e) {
        LOGGER.error("Error retrieving files of representation '{}' of AIP '{}': " + e.getMessage(),
          representation.getId(), aip.getId());
      }
    }
    return dataInformation;
  }

  public static List<List<String>> getDescriptiveMetadataInformation(List<String> fields, AIP aip, ModelService model,
    StorageService storage) {
    List<List<String>> descriptiveMetadataInformation = new ArrayList<>();
    for (DescriptiveMetadata dm : aip.getDescriptiveMetadata()) {
      descriptiveMetadataInformation.add(retrieveDescriptiveMetadataInfo(fields, aip, dm, storage));
    }

    if (aip.getRepresentations() != null) {
      for (Representation r : aip.getRepresentations()) {
        for (DescriptiveMetadata dm : r.getDescriptiveMetadata()) {
          descriptiveMetadataInformation.add(retrieveDescriptiveMetadataInfo(fields, aip, dm, storage));
        }
      }
    }

    return descriptiveMetadataInformation;
  }

  private static List<String> retrieveDescriptiveMetadataInfo(List<String> fields, AIP aip, DescriptiveMetadata dm,
    StorageService storage) {
    List<String> fileInfo = new ArrayList<>();
    Map<String, String> fixities = null;
    for (String fieldName : fields) {
      if (fieldName.equalsIgnoreCase(InventoryReportPlugin.CSV_FIELD_SIP_ID)) {
        fileInfo.add(FSUtils.asString(aip.getIngestSIPIds()));
      } else if (fieldName.equalsIgnoreCase(InventoryReportPlugin.CSV_FIELD_AIP_ID)) {
        fileInfo.add(dm.getAipId());
      } else if (fieldName.equalsIgnoreCase(InventoryReportPlugin.CSV_FIELD_REPRESENTATION_ID)) {
        fileInfo.add(dm.getRepresentationId());
      } else if (fieldName.equalsIgnoreCase(InventoryReportPlugin.CSV_FIELD_FILE_PATH)) {
        fileInfo.add("");
      } else if (fieldName.equalsIgnoreCase(InventoryReportPlugin.CSV_FIELD_FILE_ID)) {
        fileInfo.add(dm.getId());
      } else if (fieldName.equalsIgnoreCase(InventoryReportPlugin.CSV_FIELD_ISDIRECTORY)) {
        fileInfo.add("false");
      } else if (InventoryReportPlugin.CHECKSUM_ALGORITHMS.contains(fieldName.toUpperCase())) {
        if (fixities == null) {
          try {
            StoragePath descriptiveMetadataStoragePath = ModelUtils.getDescriptiveMetadataStoragePath(dm);
            Binary descriptiveMetadataBinary = storage.getBinary(descriptiveMetadataStoragePath);
            fixities = FileUtility.checksums(descriptiveMetadataBinary.getContent().createInputStream(),
              InventoryReportPlugin.CHECKSUM_ALGORITHMS);
          } catch (IOException | GenericException | RequestNotValidException | NotFoundException
            | AuthorizationDeniedException | NoSuchAlgorithmException e) {
            LOGGER.error("Error while calculating fixities for descriptive metadata '" + dm.getId() + "' of AIP '"
              + dm.getAipId() + "': " + e.getMessage(), e);
          }
        }
        if (fixities != null) {
          fileInfo.add(fixities.containsKey(fieldName) ? fixities.get(fieldName) : "");
        } else {
          fileInfo.add("");
        }
      } else if (fieldName.equalsIgnoreCase(InventoryReportPlugin.CSV_FILE_TYPE)) {
        fileInfo.add(InventoryReportPlugin.CSV_LINE_TYPE.METADATA_DESCRIPTIVE.toString());
      } else {
        fileInfo.add("");
      }
    }

    return fileInfo;
  }

  public static List<String> retrieveFileInfo(List<String> fields, File file, AIP aip, ModelService model,
    StorageService storage) {

    List<String> fileInfo = new ArrayList<>();
    List<Fixity> fixities = null;

    for (String fieldName : fields) {
      if (fieldName.equalsIgnoreCase(InventoryReportPlugin.CSV_FIELD_SIP_ID)) {
        fileInfo.add(FSUtils.asString(aip.getIngestSIPIds()));
      } else if (fieldName.equalsIgnoreCase(InventoryReportPlugin.CSV_FIELD_AIP_ID)) {
        fileInfo.add(file.getAipId());
      } else if (fieldName.equalsIgnoreCase(InventoryReportPlugin.CSV_FIELD_REPRESENTATION_ID)) {
        fileInfo.add(file.getRepresentationId());
      } else if (fieldName.equalsIgnoreCase(InventoryReportPlugin.CSV_FIELD_FILE_PATH)) {
        fileInfo.add(FSUtils.asString(file.getPath()));
      } else if (fieldName.equalsIgnoreCase(InventoryReportPlugin.CSV_FIELD_FILE_ID)) {
        fileInfo.add(file.getId());
      } else if (fieldName.equalsIgnoreCase(InventoryReportPlugin.CSV_FIELD_ISDIRECTORY)) {
        fileInfo.add(String.valueOf(file.isDirectory()));
      } else if (InventoryReportPlugin.CHECKSUM_ALGORITHMS.contains(fieldName.toUpperCase())) {
        if (!file.isDirectory()) {
          if (fixities == null) {
            try {
              fixities = PremisV3Utils.extractFixities(model.retrievePreservationFile(file));
            } catch (GenericException | RequestNotValidException | NotFoundException | AuthorizationDeniedException
              | XmlException | IOException e) {
              LOGGER.error("Error extracting fixities from premis file.", e);
            }
          }

          if (fixities != null) {
            fileInfo.add(getFixity(fieldName, fixities, file, storage));
          } else {
            fileInfo.add("");
          }
        }
      } else if (fieldName.equalsIgnoreCase(InventoryReportPlugin.CSV_FILE_TYPE)) {
        fileInfo.add(InventoryReportPlugin.CSV_LINE_TYPE.DATA.toString());
      } else {
        fileInfo.add("");
      }
    }

    return fileInfo;
  }

  private static String getFixity(String fixityAlgorithm, List<Fixity> fixities, File file, StorageService storage) {
    String fixity = "";
    if (fixities != null && !fixities.isEmpty()) {
      for (Fixity f : fixities) {
        if (f.getMessageDigestAlgorithm() != null && f.getMessageDigestAlgorithm().equalsIgnoreCase(fixityAlgorithm)) {
          fixity = f.getMessageDigest();
          break;
        }
      }
    }
    if (StringUtils.isBlank(fixity)) {
      try {
        Binary binary = storage.getBinary(ModelUtils.getFileStoragePath(file));
        fixity = FileUtility.checksum(binary.getContent().createInputStream(), fixityAlgorithm);
      } catch (NoSuchAlgorithmException | IOException | GenericException | RequestNotValidException | NotFoundException
        | AuthorizationDeniedException e) {
        fixity = "";
      }
    }
    return fixity;
  }

  public static List<List<String>> getOtherMetadataInformation(List<String> fields, String otherMetadataType, AIP aip,
    ModelService model, StorageService storage) {
    List<List<String>> otherMetadataInformation = new ArrayList<>();

    try (CloseableIterable<OptionalWithCause<OtherMetadata>> otherMetadatas = model.listOtherMetadata(aip.getId(),
      otherMetadataType, true)) {
      for (OptionalWithCause<OtherMetadata> otherMetadata : otherMetadatas) {
        if (otherMetadata.isPresent()) {
          otherMetadataInformation.add(retrieveOtherMetadataInfo(fields, otherMetadata.get(), aip, storage));
        } else {
          LOGGER.error("Cannot retrieve other metadata information", otherMetadata.getCause());
        }
      }
    } catch (RequestNotValidException | NotFoundException | GenericException | AuthorizationDeniedException
      | IOException e) {
      LOGGER.error("Error retrieving other metadata.", e);
    }

    return otherMetadataInformation;
  }

  private static List<String> retrieveOtherMetadataInfo(List<String> fields, OtherMetadata otherMetadata, AIP aip,
    StorageService storage) {
    List<String> fileInfo = new ArrayList<>();
    Map<String, String> fixities = null;
    for (String fieldName : fields) {
      if (fieldName.equalsIgnoreCase(InventoryReportPlugin.CSV_FIELD_SIP_ID)) {
        fileInfo.add(FSUtils.asString(aip.getIngestSIPIds()));
      } else if (fieldName.equalsIgnoreCase(InventoryReportPlugin.CSV_FIELD_AIP_ID)) {
        fileInfo.add(otherMetadata.getAipId());
      } else if (fieldName.equalsIgnoreCase(InventoryReportPlugin.CSV_FIELD_REPRESENTATION_ID)) {
        fileInfo.add(otherMetadata.getRepresentationId());
      } else if (fieldName.equalsIgnoreCase(InventoryReportPlugin.CSV_FIELD_FILE_PATH)) {
        fileInfo.add(FSUtils.asString(otherMetadata.getFileDirectoryPath()));
      } else if (fieldName.equalsIgnoreCase(InventoryReportPlugin.CSV_FIELD_FILE_ID)) {
        fileInfo.add(
          otherMetadata.getFileId() + (otherMetadata.getFileSuffix() != null ? otherMetadata.getFileSuffix() : ""));
      } else if (fieldName.equalsIgnoreCase(InventoryReportPlugin.CSV_FIELD_ISDIRECTORY)) {
        fileInfo.add("false");
      } else if (InventoryReportPlugin.CHECKSUM_ALGORITHMS.contains(fieldName.toUpperCase())) {
        if (fixities == null) {
          try {
            Binary otherMetadataBinary = storage.getBinary(ModelUtils.getOtherMetadataStoragePath(
              otherMetadata.getAipId(), otherMetadata.getRepresentationId(), otherMetadata.getFileDirectoryPath(),
              otherMetadata.getFileId(), otherMetadata.getFileSuffix(), otherMetadata.getType()));
            fixities = FileUtility.checksums(otherMetadataBinary.getContent().createInputStream(),
              InventoryReportPlugin.CHECKSUM_ALGORITHMS);
          } catch (IOException | GenericException | RequestNotValidException | NotFoundException
            | AuthorizationDeniedException | NoSuchAlgorithmException e) {
            LOGGER.error(
              "Error while calculating fixities for other metadata '" + otherMetadata.getId() + ": " + e.getMessage(),
              e);
          }
        }
        if (fixities != null) {
          fileInfo.add(fixities.containsKey(fieldName) ? fixities.get(fieldName) : "");
        } else {
          fileInfo.add("");
        }
      } else if (fieldName.equalsIgnoreCase(InventoryReportPlugin.CSV_FILE_TYPE)) {
        fileInfo.add(
          InventoryReportPlugin.CSV_LINE_TYPE.METADATA_OTHER.toString() + "_" + otherMetadata.getType().toUpperCase());
      } else {
        fileInfo.add("");
      }
    }

    return fileInfo;
  }
}
