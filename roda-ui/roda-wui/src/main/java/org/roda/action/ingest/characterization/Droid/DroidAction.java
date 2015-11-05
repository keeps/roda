/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.action.ingest.characterization.Droid;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.roda.action.ingest.characterization.Droid.utils.DroidUtils;
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

import edu.harvard.hul.ois.fits.exceptions.FitsException;

public class DroidAction implements Plugin<AIP> {
  private static final Logger LOGGER = Logger.getLogger(DroidAction.class);

  @Override
  public void init() throws PluginException {
  }

  @Override
  public void shutdown() {
    // do nothing
  }

  @Override
  public String getName() {
    return "Droid characterization action";
  }

  @Override
  public String getDescription() {
    return "Update the premis files with the object characterization";
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
    try {
      for (AIP aip : list) {
        LOGGER.debug("Processing AIP " + aip.getId());
        try {
          for (String representationID : aip.getRepresentationIds()) {
            LOGGER.debug("Processing representation " + representationID + " of AIP " + aip.getId());

            /*
             * Representation representation =
             * model.retrieveRepresentation(aip.getId(), representationID); for
             * (String fileID : representation.getFileIds()) { LOGGER.debug(
             * "Processing file " + fileID + " of representation " +
             * representationID + " from AIP " + aip.getId()); File file =
             * model.retrieveFile(aip.getId(), representationID, fileID); Binary
             * binary = storage.getBinary(file.getStoragePath());
             * 
             * Path fitsResult = FITSUtils.runFits(file, binary,
             * getParameterValues()); Binary resource = (Binary)
             * FSUtils.convertPathToResource(fitsResult.getParent(),
             * fitsResult); model.createOtherMetadata(aip.getId(),
             * representationID, file.getStoragePath().getName() + ".xml",
             * "FITS", resource); FSUtils.deletePath(fitsResult);
             * 
             * }
             */
            Path data = Files.createTempDirectory("data");
            StorageService tempStorage = new FileStorageService(data);
            StoragePath representationPath = ModelUtils.getRepresentationPath(aip.getId(), representationID);
            tempStorage.copy(storage, representationPath, representationPath);
            String droidOutput = DroidUtils.runDROIDOnPath(data.resolve(representationPath.asString()));
            LOGGER.debug("DROID OUTPUT: " + droidOutput);

            for (String outputLine : droidOutput.split("\n")) {
              int splitterPosition = outputLine.lastIndexOf(",");
              String filename = outputLine.substring(0, splitterPosition);
              filename = filename.substring(filename.lastIndexOf(File.separatorChar) + 1);
              String format = outputLine.substring(splitterPosition + 1);
              LOGGER.error("FILE: " + filename);
              LOGGER.error("FORMAT: " + format);
              String xmlOutput = "<droid>" + format + "</droid>";
              Path p = Files.createTempFile("temp", ".temp");
              Files.write(p, xmlOutput.getBytes());
              Binary resource = (Binary) FSUtils.convertPathToResource(p.getParent(), p);
              LOGGER.debug("Creating other metadata (AIP: " + aip.getId() + ", REPRESENTATION: " + representationID
                + ", FILE: " + filename + ")");
              model.createOtherMetadata(aip.getId(), representationID, filename + ".xml", "DROID", resource);
            }
            FSUtils.deletePath(data);
          }
        } catch (StorageServiceException sse) {
          LOGGER.error("Error processing AIP " + aip.getId() + ": " + sse.getMessage());
        } catch (FitsException fe) {
          LOGGER.error("Error processing AIP " + aip.getId() + ": " + fe.getMessage());
        } catch (ModelServiceException mse) {
          LOGGER.error("Error processing AIP " + aip.getId() + ": " + mse.getMessage());
        }
      }
    } catch (IOException ioe) {
      LOGGER.error("Error executing DroidAction: " + ioe.getMessage(), ioe);
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
