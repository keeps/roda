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
import org.roda.core.storage.ContentPayload;
import org.roda.core.storage.StorageService;
import org.roda.core.storage.StringContentPayload;
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

          Path data = Files.createTempDirectory("data");
          StorageService tempStorage = new FileStorageService(data);
          StoragePath representationPath = ModelUtils.getRepresentationPath(aip.getId(), representation.getId());
          tempStorage.copy(storage, representationPath, representationPath);
          String droidOutput = DroidPluginUtils.runDROIDOnPath(data.resolve(representationPath.asString()));
          LOGGER.debug("DROID OUTPUT: " + droidOutput);

          for (String outputLine : droidOutput.split("\n")) {
            int splitterPosition = outputLine.lastIndexOf(",");
            // TODO get file directory path
            List<String> fileDirectoryPath = new ArrayList<>();
            String fileId = outputLine.substring(0, splitterPosition);
            fileId = fileId.substring(fileId.lastIndexOf(File.separatorChar) + 1);
            String format = outputLine.substring(splitterPosition + 1);
            LOGGER.error("FILE: " + fileId);
            LOGGER.error("FORMAT: " + format);
            String xmlOutput = "<droid>" + format + "</droid>";
            ContentPayload payload = new StringContentPayload(xmlOutput);

            model.createOtherMetadata(aip.getId(), representation.getId(), fileDirectoryPath, fileId, ".xml", "DROID",
              payload);
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
