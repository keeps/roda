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
import java.util.ArrayList;
import java.util.List;

import org.apache.xmlbeans.XmlException;
import org.roda.core.common.PremisV3Utils;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.File;
import org.roda.core.data.v2.ip.metadata.Fixity;
import org.roda.core.model.ModelService;
import org.roda.core.storage.fs.FSUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileExportPluginUtils {

  private static final Logger LOGGER = LoggerFactory.getLogger(FileExportPluginUtils.class);

  public static void mergeFiles(List<Path> files, Path mergedFile) throws IOException {
    try (FileChannel out = FileChannel.open(mergedFile, StandardOpenOption.APPEND, StandardOpenOption.WRITE)) {
      for (Path path : files) {
        try (FileChannel in = FileChannel.open(path, StandardOpenOption.READ)) {
          for (long p = 0, l = in.size(); p < l;)
            p += in.transferTo(p, l - p, out);
        }
      }
    } catch (IOException e) {
      throw e;
    }
  }

  public static List<String> retrieveFileInfo(List<String> fields, File file, ModelService model) {

    List<String> fileInfo = new ArrayList<String>();
    List<Fixity> fixities = null;
    for (String fieldName : fields) {
      if (fieldName.equalsIgnoreCase(FileExportPlugin.CSV_FIELD_SIP_ID)) {
        try {
          AIP aip = model.retrieveAIP(file.getAipId());
          fileInfo.add(aip.getIngestSIPId());
        } catch (RequestNotValidException | NotFoundException | GenericException | AuthorizationDeniedException e) {
          fileInfo.add("");
        }
      } else if (fieldName.equalsIgnoreCase(FileExportPlugin.CSV_FIELD_AIP_ID)) {
        fileInfo.add(file.getAipId());
      } else if (fieldName.equalsIgnoreCase(FileExportPlugin.CSV_FIELD_REPRESENTATION_ID)) {
        fileInfo.add(file.getRepresentationId());
      } else if (fieldName.equalsIgnoreCase(FileExportPlugin.CSV_FIELD_FILE_PATH)) {
        fileInfo.add(FSUtils.asString(file.getPath()));
      } else if (fieldName.equalsIgnoreCase(FileExportPlugin.CSV_FIELD_FILE_ID)) {
        fileInfo.add(file.getId());
      } else if (fieldName.equalsIgnoreCase(FileExportPlugin.CSV_FIELD_ISDIRECTORY)) {
        fileInfo.add(String.valueOf(file.isDirectory()));
      } else if (FileExportPlugin.CHECKSUM_ALGORITHMS.contains(fieldName.toUpperCase())) {
        if (!file.isDirectory()) {
          if (fixities == null) {
            try {
              fixities = PremisV3Utils.extractFixities(model.retrievePreservationFile(file));
            } catch (GenericException | RequestNotValidException | NotFoundException | AuthorizationDeniedException
              | XmlException | IOException e) {
            }
          }
          if (fixities != null) {
            fileInfo.add(getFixity(fieldName, fixities));
          } else {
            fileInfo.add("");
          }
        }
      } else {
        fileInfo.add("");
      }
    }
    return fileInfo;
  }

  private static String getFixity(String fixityAlgorithm, List<Fixity> fixities) {
    String fixity = "";
    if (fixities != null && fixities.size() > 0) {
      for (Fixity f : fixities) {
        if (f.getMessageDigestAlgorithm() != null && f.getMessageDigestAlgorithm().equalsIgnoreCase(fixityAlgorithm)) {
          fixity = f.getMessageDigest();
          break;
        }
      }
    }
    return fixity;
  }
}
