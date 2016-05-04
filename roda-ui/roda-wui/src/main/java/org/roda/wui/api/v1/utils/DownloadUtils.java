package org.roda.wui.api.v1.utils;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.StreamingOutput;

import org.apache.commons.compress.utils.IOUtils;
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

  private static final String ZIP_MEDIA_TYPE = "application/zip";
  private static final String ZIP_FILE_NAME_EXTENSION = ".zip";
  private static final String ZIP_PATH_DELIMITER = "/";

  public static StreamResponse download(final StorageService storage, final Resource resource) {

    String filename;
    String mediaType;
    StreamingOutput stream;

    final StoragePath storagePath = resource.getStoragePath();

    if (resource.isDirectory()) {
      // send zip with directory contents
      filename = storagePath.getName() + ZIP_FILE_NAME_EXTENSION;
      mediaType = ZIP_MEDIA_TYPE;
      stream = new StreamingOutput() {

        @Override
        public void write(OutputStream out) throws IOException, WebApplicationException {
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

          } catch (NotFoundException | GenericException | AuthorizationDeniedException | RequestNotValidException e) {
            // TODO re-throw correct web application exception
            throw new InternalServerErrorException(e);
          } finally {
            IOUtils.closeQuietly(zos);
            IOUtils.closeQuietly(bos);
            IOUtils.closeQuietly(out);
          }

        }
      };

    } else {
      // send the one file
      filename = storagePath.getName();
      mediaType = MediaType.APPLICATION_OCTET_STREAM;
      stream = new StreamingOutput() {

        @Override
        public void write(OutputStream out) throws IOException, WebApplicationException {
          BufferedOutputStream bos = new BufferedOutputStream(out);
          Binary binary;
          try {
            binary = storage.getBinary(storagePath);
            InputStream inputStream = binary.getContent().createInputStream();
            IOUtils.copy(inputStream, bos);
            IOUtils.closeQuietly(inputStream);
          } catch (GenericException | RequestNotValidException | NotFoundException | AuthorizationDeniedException e) {
            // TODO re-throw correct web application exception
            throw new InternalServerErrorException(e);
          } finally {
            IOUtils.closeQuietly(out);
          }

        }

      };

    }

    return new StreamResponse(filename, mediaType, stream);
  }

}
