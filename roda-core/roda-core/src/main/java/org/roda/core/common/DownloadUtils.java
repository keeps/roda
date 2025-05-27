/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.common;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.IOUtils;
import org.roda.core.common.iterables.CloseableIterable;
import org.roda.core.common.tools.ZipEntryInfo;
import org.roda.core.common.tools.ZipTools;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.ConsumesOutputStream;
import org.roda.core.data.v2.StreamResponse;
import org.roda.core.data.v2.ip.StoragePath;
import org.roda.core.storage.Binary;
import org.roda.core.storage.BinaryConsumesOutputStream;
import org.roda.core.storage.Resource;
import org.roda.core.storage.StorageService;

public class DownloadUtils {

  public static final String ZIP_MEDIA_TYPE = "application/zip";
  public static final String ZIP_FILE_NAME_EXTENSION = ".zip";
  public static final String ZIP_PATH_DELIMITER = "/";

  private DownloadUtils() {
    // do nothing
  }

  public static StreamResponse createZipStreamResponse(List<ZipEntryInfo> zipEntries, String zipName) {
    final ConsumesOutputStream stream = new ConsumesOutputStream() {

      @Override
      public String getMediaType() {
        return ZIP_MEDIA_TYPE;
      }

      @Override
      public String getFileName() {
        return zipName + ZIP_FILE_NAME_EXTENSION;
      }

      @Override
      public void consumeOutputStream(OutputStream out) throws IOException {
        ZipTools.zip(zipEntries, out);
      }

      @Override
      public Date getLastModified() {
        return null;
      }

      @Override
      public long getSize() {
        return -1;
      }
    };

    return new StreamResponse(stream);
  }
}
