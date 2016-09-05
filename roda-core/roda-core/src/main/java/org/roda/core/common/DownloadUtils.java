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
import java.util.List;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.IOUtils;
import org.roda.core.common.iterables.CloseableIterable;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.ip.StoragePath;
import org.roda.core.storage.Binary;
import org.roda.core.storage.Resource;
import org.roda.core.storage.StorageService;

public class DownloadUtils {

  private static final String BIN_MEDIA_TYPE = "application/octet-stream";
  private static final String ZIP_MEDIA_TYPE = "application/zip";
  private static final String ZIP_FILE_NAME_EXTENSION = ".zip";
  private static final String ZIP_PATH_DELIMITER = "/";

  public static ConsumesOutputStream download(final StorageService storage, final Resource resource) {

    ConsumesOutputStream stream;

    final StoragePath storagePath = resource.getStoragePath();

    if (resource.isDirectory()) {
      // send zip with directory contents

      stream = new ConsumesOutputStream() {

        @Override
        public void consumeOutputStream(OutputStream out) throws IOException {
          CloseableIterable<Resource> resources;
          BufferedOutputStream bos = new BufferedOutputStream(out);
          ZipOutputStream zos = new ZipOutputStream(bos);
          try {
            resources = storage.listResourcesUnderDirectory(storagePath, true);
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
                InputStream inputStream = binary.getContent().createInputStream();
                IOUtils.copy(inputStream, zos);
                IOUtils.closeQuietly(inputStream);
                zos.closeEntry();
              }
            }

            IOUtils.closeQuietly(resources);

          } catch (GenericException | RequestNotValidException | NotFoundException | AuthorizationDeniedException e) {
            throw new IOException(e);
          } finally {
            IOUtils.closeQuietly(zos);
            IOUtils.closeQuietly(bos);
            IOUtils.closeQuietly(out);
          }

        }

        @Override
        public String getFileName() {
          return storagePath.getName() + ZIP_FILE_NAME_EXTENSION;
        }

        @Override
        public String getMediaType() {
          return ZIP_MEDIA_TYPE;
        }
      };

    } else {
      // send the one file
      stream = new ConsumesOutputStream() {

        @Override
        public void consumeOutputStream(OutputStream out) throws IOException {
          BufferedOutputStream bos = new BufferedOutputStream(out);
          Binary binary;
          try {
            binary = storage.getBinary(storagePath);
            InputStream inputStream = binary.getContent().createInputStream();
            IOUtils.copy(inputStream, bos);
            IOUtils.closeQuietly(inputStream);
          } catch (GenericException | RequestNotValidException | NotFoundException | AuthorizationDeniedException e) {
            throw new IOException(e);
          } finally {
            IOUtils.closeQuietly(bos);
            IOUtils.closeQuietly(out);
          }

        }

        @Override
        public String getFileName() {
          return storagePath.getName();
        }

        @Override
        public String getMediaType() {
          return BIN_MEDIA_TYPE;
        }

      };

    }

    return stream;
  }

}
