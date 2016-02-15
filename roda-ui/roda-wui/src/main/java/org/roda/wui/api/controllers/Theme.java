/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.api.controllers;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Date;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.StreamingOutput;

import org.apache.commons.io.IOUtils;
import org.roda.core.RodaCoreFactory;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.wui.api.v1.utils.StreamResponse;
import org.roda.wui.common.RodaCoreService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Theme extends RodaCoreService {

  private static Logger logger = LoggerFactory.getLogger(Theme.class);

  private static String RESOURCE_PATH = "/org/roda/wui/public/";

  private Theme() {
    super();
  }

  public static StreamResponse getResource(String resourceId) throws IOException, NotFoundException {
    StreamResponse streamResponse = null;

    final Path themePath = RodaCoreFactory.getThemePath();
    final Path filePath = themePath.resolve(resourceId);

    logger.debug(filePath.toString());

    if (Files.exists(filePath) && !Files.isDirectory(filePath)) {

      StreamingOutput streamingOutput = new StreamingOutput() {

        @Override
        public void write(OutputStream os) throws IOException, WebApplicationException {
          Files.copy(filePath, os);
        }
      };

      streamResponse = new StreamResponse(resourceId, MediaType.APPLICATION_OCTET_STREAM, streamingOutput);

    } else {
      InputStream inputStream = Theme.class.getResourceAsStream(RESOURCE_PATH + resourceId);

      if (inputStream != null) {
        StreamingOutput streamingOutput = new StreamingOutput() {

          @Override
          public void write(OutputStream os) throws IOException, WebApplicationException {
            IOUtils.copy(inputStream, os);
          }
        };

        streamResponse = new StreamResponse(resourceId, MediaType.APPLICATION_OCTET_STREAM, streamingOutput);
      } else {
        throw new NotFoundException("File " + resourceId + " doesn't exit!");
      }
    }

    return streamResponse;
  }

  public static Date getLastModifiedDate(String resourceId) throws IOException {
    Date modifiedDate = new Date();

    final Path themePath = RodaCoreFactory.getThemePath();
    final Path filePath = themePath.resolve(resourceId);

    if (Files.exists(filePath) && !Files.isDirectory(filePath)) {
      modifiedDate = new Date(Files.getLastModifiedTime(filePath).toMillis());
    }

    return modifiedDate;
  }
}