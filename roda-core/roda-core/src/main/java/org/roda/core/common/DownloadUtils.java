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
import org.roda.core.data.v2.ip.StoragePath;
import org.roda.core.storage.Binary;
import org.roda.core.storage.BinaryConsumesOutputStream;
import org.roda.core.storage.Resource;
import org.roda.core.storage.StorageService;

public class DownloadUtils {

  private static final String ZIP_MEDIA_TYPE = "application/zip";
  private static final String ZIP_FILE_NAME_EXTENSION = ".zip";
  private static final String ZIP_PATH_DELIMITER = "/";

  private DownloadUtils() {
    // do nothing
  }

  public static ConsumesOutputStream download(final StorageService storage, final Resource resource)
    throws GenericException, RequestNotValidException, NotFoundException, AuthorizationDeniedException {
    return download(storage, resource, null);
  }

  public static ConsumesOutputStream download(final StorageService storage, final Resource resource, String name)
    throws GenericException, RequestNotValidException, NotFoundException, AuthorizationDeniedException {
    ConsumesOutputStream stream;
    final StoragePath storagePath = resource.getStoragePath();

    if (resource.isDirectory()) {
      // send zip with directory contents
      final String fileName = name == null ? storagePath.getName() : name;

      stream = new ConsumesOutputStream() {

        @Override
        public void consumeOutputStream(OutputStream out) throws IOException {

          try (BufferedOutputStream bos = new BufferedOutputStream(out);
            ZipOutputStream zos = new ZipOutputStream(bos);
            CloseableIterable<Resource> resources = storage.listResourcesUnderDirectory(storagePath, true);) {
            int basePathSize = storagePath.asList().size();

            for (Resource r : resources) {
              List<String> pathAsList = r.getStoragePath().asList();
              List<String> relativePathAsList = pathAsList.subList(basePathSize, pathAsList.size());
              String entryPath = relativePathAsList.stream().collect(Collectors.joining(ZIP_PATH_DELIMITER));

              if (r.isDirectory()) {
                // adding a directory
                entryPath += ZIP_PATH_DELIMITER;
                zos.putNextEntry(new ZipEntry(entryPath));
                zos.closeEntry();
              } else {
                // adding a file
                ZipEntry entry = new ZipEntry(entryPath);
                zos.putNextEntry(entry);
                Binary binary = storage.getBinary(r.getStoragePath());
                try (InputStream inputStream = binary.getContent().createInputStream()) {
                  IOUtils.copy(inputStream, zos);
                }
                zos.closeEntry();
              }
            }
          } catch (GenericException | RequestNotValidException | NotFoundException | AuthorizationDeniedException e) {
            throw new IOException(e);
          }
        }

        @Override
        public String getFileName() {
          return fileName + ZIP_FILE_NAME_EXTENSION;
        }

        @Override
        public String getMediaType() {
          return ZIP_MEDIA_TYPE;
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

    } else {
      // send the one file
      stream = new BinaryConsumesOutputStream(storage.getBinary(storagePath));
    }

    return stream;
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
