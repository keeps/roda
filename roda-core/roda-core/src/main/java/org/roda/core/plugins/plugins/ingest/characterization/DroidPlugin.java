/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.plugins.ingest.characterization;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.roda.core.data.exceptions.InvalidParameterException;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.Representation;
import org.roda.core.data.v2.ip.StoragePath;
import org.roda.core.data.v2.jobs.PluginParameter;
import org.roda.core.data.v2.jobs.PluginType;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.index.IndexService;
import org.roda.core.model.ModelService;
import org.roda.core.model.utils.ModelUtils;
import org.roda.core.plugins.Plugin;
import org.roda.core.plugins.PluginException;
import org.roda.core.storage.Binary;
import org.roda.core.storage.StorageService;
import org.roda.core.storage.fs.FSUtils;
import org.roda.core.storage.fs.FileStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DroidPlugin implements Plugin<AIP> {
  private static final Logger LOGGER = LoggerFactory.getLogger(DroidPlugin.class);

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

    for (AIP aip : list) {
      LOGGER.debug("Processing AIP " + aip.getId());
      for (Representation representation : aip.getRepresentations()) {
        LOGGER.debug("Processing representation " + representation.getId() + " of AIP " + aip.getId());
        try {
          /*
           * Representation representation =
           * model.retrieveRepresentation(aip.getId(), representation.getId()); for
           * (String fileID : representation.getFileIds()) { LOGGER.debug(
           * "Processing file " + fileID + " of representation " +
           * representation.getId() + " from AIP " + aip.getId()); File file =
           * model.retrieveFile(aip.getId(), representation.getId(), fileID); Binary
           * binary = storage.getBinary(file.getStoragePath());
           * 
           * Path fitsResult = FITSUtils.runFits(file, binary,
           * getParameterValues()); Binary resource = (Binary)
           * FSUtils.convertPathToResource(fitsResult.getParent(), fitsResult);
           * model.createOtherMetadata(aip.getId(), representation.getId(),
           * file.getStoragePath().getName() + ".xml", "FITS", resource);
           * FSUtils.deletePath(fitsResult);
           * 
           * }
           */
          Path data = Files.createTempDirectory("data");
          StorageService tempStorage = new FileStorageService(data);
          StoragePath representationPath = ModelUtils.getRepresentationPath(aip.getId(), representation.getId());
          tempStorage.copy(storage, representationPath, representationPath);
          String droidOutput = DroidPluginUtils.runDROIDOnPath(data.resolve(representationPath.asString()));
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
            LOGGER.debug("Creating other metadata (AIP: " + aip.getId() + ", REPRESENTATION: " + representation.getId()
              + ", FILE: " + filename + ")");
            model.createOtherMetadata(aip.getId(), representation.getId(), filename + ".xml", "DROID", resource);
          }
          FSUtils.deletePath(data);
        } catch (RODAException | IOException e) {
          LOGGER.error("Error processing AIP " + aip.getId() + ": " + e.getMessage());
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

  @Override
  public Plugin<AIP> cloneMe() {
    return new DroidPlugin();
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
