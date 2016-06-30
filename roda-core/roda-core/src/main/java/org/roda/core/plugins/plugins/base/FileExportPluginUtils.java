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

import org.apache.commons.lang3.StringUtils;
import org.apache.xmlbeans.XmlException;
import org.roda.core.common.PremisV3Utils;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.ip.File;
import org.roda.core.data.v2.ip.metadata.Fixity;
import org.roda.core.model.ModelService;
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
      if (fieldName.equalsIgnoreCase("file id")) {
        fileInfo.add(file.getId());
      } else if (fieldName.equalsIgnoreCase("path")) {
        fileInfo.add(StringUtils.join(file.getPath(), RodaConstants.FILE_STORAGEPATH));
      } else if (fieldName.equalsIgnoreCase("sip id")) {
        try {
          fileInfo.add(model.retrieveAIP(file.getAipId()).getIngestSIPId());
        } catch (RequestNotValidException | NotFoundException | GenericException | AuthorizationDeniedException e) {
          fileInfo.add("");
        }
      } else if (fieldName.equalsIgnoreCase("sha-1") || fieldName.equalsIgnoreCase("sha-256")) {
        if (!file.isDirectory()) {
          if (fixities == null) {
            try {
              fixities = PremisV3Utils.extractFixities(model.retrievePreservationFile(file));
              fileInfo.add(getFixity(fieldName, fixities));
            } catch (GenericException | RequestNotValidException | NotFoundException | AuthorizationDeniedException
              | XmlException | IOException e) {
              fileInfo.add("");
            }
          }

        }
      } else if (fieldName.equalsIgnoreCase("isDirectory")) {
        fileInfo.add(String.valueOf(file.isDirectory()));
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
