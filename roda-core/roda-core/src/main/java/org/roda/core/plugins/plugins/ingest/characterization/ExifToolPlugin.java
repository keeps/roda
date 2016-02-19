/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.plugins.ingest.characterization;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.Representation;
import org.roda.core.data.v2.ip.StoragePath;
import org.roda.core.data.v2.jobs.PluginType;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.index.IndexService;
import org.roda.core.model.ModelService;
import org.roda.core.model.utils.ModelUtils;
import org.roda.core.plugins.AbstractPlugin;
import org.roda.core.plugins.Plugin;
import org.roda.core.plugins.PluginException;
import org.roda.core.storage.ContentPayload;
import org.roda.core.storage.DirectResourceAccess;
import org.roda.core.storage.StorageService;
import org.roda.core.storage.fs.FSPathContentPayload;
import org.roda.core.storage.fs.FSUtils;
import org.roda.core.util.CommandException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExifToolPlugin extends AbstractPlugin<AIP> {
  private static final Logger LOGGER = LoggerFactory.getLogger(ExifToolPlugin.class);

  @Override
  public void init() throws PluginException {
  }

  @Override
  public void shutdown() {
    // do nothing
  }

  @Override
  public String getName() {
    return "ExifTool characterization action";
  }

  @Override
  public String getDescription() {
    return "Generates the ExifTool output for each file in the AIP";
  }

  @Override
  public String getVersion() {
    return "1.0";
  }

  @Override
  public Report execute(IndexService index, ModelService model, StorageService storage, List<AIP> list)
    throws PluginException {
    for (AIP aip : list) {
      LOGGER.debug("Processing AIP " + aip.getId());
      boolean inotify = false;
      for (Representation representation : aip.getRepresentations()) {

        LOGGER.debug("Processing representation " + representation.getId() + " from AIP " + aip.getId());

        DirectResourceAccess directAccess = null;
        try {
          StoragePath representationDataPath = ModelUtils.getRepresentationDataStoragePath(aip.getId(),
            representation.getId());
          directAccess = storage.getDirectAccess(representationDataPath);

          Path metadata = Files.createTempDirectory("metadata");
          String exifOutput = ExifToolPluginUtils.runExifToolOnPath(directAccess.getPath(), metadata);
          LOGGER.debug("ExifOutput: " + exifOutput);

          try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(metadata)) {
            for (Path path : directoryStream) {
              ContentPayload payload = new FSPathContentPayload(path);
              List<String> fileDirectoryPath = new ArrayList<>();

              Path relativePath = metadata.relativize(path);
              for (int i = 0; i < relativePath.getNameCount() - 2; i++) {
                fileDirectoryPath.add(relativePath.getName(i).toString());
              }

              String fileId = path.getFileName().toString();
              model.createOtherMetadata(aip.getId(), representation.getId(), fileDirectoryPath, fileId, ".xml",
                "ExifTool", payload, inotify);
            }
          }
          FSUtils.deletePath(metadata);
        } catch (RODAException | IOException | CommandException e) {
          LOGGER.error("Error processing AIP " + aip.getId() + ": " + e.getMessage());
        } finally {
          IOUtils.closeQuietly(directAccess);
        }
      }

      try {
        model.notifyAIPUpdated(aip.getId());
      } catch (RequestNotValidException | GenericException | NotFoundException | AuthorizationDeniedException e) {
        LOGGER.error("Error notifying of AIP update", e);
      }
    }
    return null;
  }

  @Override
  public Report beforeExecute(IndexService index, ModelService model, StorageService storage) throws PluginException {

    return null;
  }

  @Override
  public Report afterExecute(IndexService index, ModelService model, StorageService storage) throws PluginException {

    return null;
  }

  @Override
  public Plugin<AIP> cloneMe() {
    return new ExifToolPlugin();
  }

  @Override
  public PluginType getType() {
    return PluginType.AIP_TO_AIP;
  }

  @Override
  public boolean areParameterValuesValid() {
    return true;
  }

}
