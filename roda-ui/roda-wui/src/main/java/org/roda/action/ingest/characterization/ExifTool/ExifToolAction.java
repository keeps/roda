/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.action.ingest.characterization.ExifTool;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.roda.action.ingest.characterization.ExifTool.utils.ExifToolUtils;
import org.roda.action.orchestrate.Plugin;
import org.roda.action.orchestrate.PluginException;
import org.roda.core.common.InvalidParameterException;
import org.roda.core.data.PluginParameter;
import org.roda.core.data.Report;
import org.roda.index.IndexService;
import org.roda.model.AIP;
import org.roda.model.ModelService;
import org.roda.model.ModelServiceException;
import org.roda.model.utils.ModelUtils;
import org.roda.storage.Binary;
import org.roda.storage.StoragePath;
import org.roda.storage.StorageService;
import org.roda.storage.StorageServiceException;
import org.roda.storage.fs.FSUtils;
import org.roda.storage.fs.FileStorageService;
import org.roda.util.CommandException;

public class ExifToolAction implements Plugin<AIP> {
  private static final Logger LOGGER = Logger.getLogger(ExifToolAction.class);

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
  public List<PluginParameter> getParameters() {
    return new ArrayList<>();
  }

  @Override
  public Map<String, String> getParameterValues() {
    return new HashMap<>();
  }

  @Override
  public void setParameterValues(Map<String, String> parameters) throws InvalidParameterException {
    // no params
  }

  @Override
  public Report execute(IndexService index, ModelService model, StorageService storage, List<AIP> list)
    throws PluginException {
    for (AIP aip : list) {
      LOGGER.debug("Processing AIP " + aip.getId());

      for (String representationID : aip.getRepresentationIds()) {

        LOGGER.debug("Processing representation " + representationID + " from AIP " + aip.getId());
        try {
          /*
           * OLD VERSION... FILE BY FILE Representation representation =
           * model.retrieveRepresentation(aip.getId(), representationID); for
           * (String fileID : representation.getFileIds()) { LOGGER.debug(
           * "Processing file " + fileID + " from " + representationID +
           * " of AIP " + aip.getId()); File file =
           * model.retrieveFile(aip.getId(), representationID, fileID); Binary
           * binary = storage.getBinary(file.getStoragePath());
           * 
           * Path exifToolResults = ExifToolUtils.runExifTool(file, binary,
           * getParameterValues()); Binary resource = (Binary)
           * FSUtils.convertPathToResource(exifToolResults.getParent(),
           * exifToolResults); model.createOtherMetadata(aip.getId(),
           * representationID, file.getStoragePath().getName() + ".xml",
           * "ExifTool", resource); exifToolResults.toFile().delete(); }
           */
          // NEW VERSION
          // TODO: if storage is filesystem, no need to copy all files to a
          // "temp" FileStorageService
          Path data = Files.createTempDirectory("data");
          StorageService tempStorage = new FileStorageService(data);
          StoragePath representationPath = ModelUtils.getRepresentationPath(aip.getId(), representationID);
          tempStorage.copy(storage, representationPath, representationPath);
          Path metadata = Files.createTempDirectory("metadata");
          String exifOutput = ExifToolUtils.runExifToolOnPath(data.resolve(representationPath.asString()), metadata);
          LOGGER.debug("ExifOutput: " + exifOutput);

          try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(metadata)) {
            for (Path path : directoryStream) {
              Binary resource = (Binary) FSUtils.convertPathToResource(path.getParent(), path);
              LOGGER.debug("Creating other metadata (AIP: " + aip.getId() + ", REPRESENTATION: " + representationID
                + ", FILE: " + path.toFile().getName() + ")");
              model.createOtherMetadata(aip.getId(), representationID, path.toFile().getName(), "ExifTool", resource);
            }
          }
          FSUtils.deletePath(data);
          FSUtils.deletePath(metadata);
        } catch (StorageServiceException sse) {
          LOGGER.error("Error processing AIP " + aip.getId() + ": " + sse.getMessage());
        } catch (IOException ioe) {
          LOGGER.error("Error processing AIP " + aip.getId() + ": " + ioe.getMessage());
        } catch (CommandException ce) {
          LOGGER.error("Error processing AIP " + aip.getId() + ": " + ce.getMessage());
        } catch (ModelServiceException mse) {
          LOGGER.error("Error processing AIP " + aip.getId() + ": " + mse.getMessage());
        }
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

}
