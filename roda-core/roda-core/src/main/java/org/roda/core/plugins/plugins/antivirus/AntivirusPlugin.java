/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.plugins.antivirus;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.roda.core.RodaCoreFactory;
import org.roda.core.data.PluginParameter;
import org.roda.core.data.Report;
import org.roda.core.data.common.InvalidParameterException;
import org.roda.core.index.IndexService;
import org.roda.core.model.AIP;
import org.roda.core.model.ModelService;
import org.roda.core.model.utils.ModelUtils;
import org.roda.core.plugins.Plugin;
import org.roda.core.plugins.PluginException;
import org.roda.core.storage.StoragePath;
import org.roda.core.storage.StorageService;
import org.roda.core.storage.StorageServiceException;
import org.roda.core.storage.fs.FileStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AntivirusPlugin implements Plugin<AIP> {
  private static final Logger LOGGER = LoggerFactory.getLogger(AntivirusPlugin.class);

  private Map<String, String> parameters;

  private String antiVirusClassName;
  private AntiVirus antiVirus = null;

  @Override
  public void init() throws PluginException {
    antiVirusClassName = RodaCoreFactory.getRodaConfiguration().getString(
      "core.plugins.internal.virus_check.antiVirusClassname", "org.roda.core.plugins.plugins.antivirus.ClamAntiVirus");

    try {
      LOGGER.info("Loading antivirus class " + antiVirusClassName); //$NON-NLS-1$
      setAntiVirus((AntiVirus) Class.forName(antiVirusClassName).newInstance());
      LOGGER.info("Using antivirus " + getAntiVirus().getClass().getName());
    } catch (ClassNotFoundException e) {
      LOGGER.warn("Antivirus class " + antiVirusClassName //$NON-NLS-1$
        + " not found - " + e.getMessage()); //$NON-NLS-1$
    } catch (InstantiationException e) {
      // not possible to create a new instance of the class
      LOGGER.warn("Antivirus class " + antiVirusClassName //$NON-NLS-1$
        + " instantiation exception - " + e.getMessage()); //$NON-NLS-1$
    } catch (IllegalAccessException e) {
      // not possible to create a new instance of the class
      LOGGER.warn("Antivirus class " + antiVirusClassName //$NON-NLS-1$
        + " illegal access exception - " + e.getMessage()); //$NON-NLS-1$
    }

    if (getAntiVirus() == null) {
      setAntiVirus(new AVGAntiVirus());
      LOGGER.info("Using default antivirus " //$NON-NLS-1$
        + getAntiVirus().getClass().getName());
    }

    LOGGER.info("init OK"); //$NON-NLS-1$
  }

  @Override
  public void shutdown() {
    // do nothing
  }

  @Override
  public String getName() {
    return "Virus check";
  }

  @Override
  public String getDescription() {
    return "Verifies if a SIP is free of virus.";
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
    return parameters;
  }

  @Override
  public void setParameterValues(Map<String, String> parameters) throws InvalidParameterException {
    this.parameters = parameters;
  }

  @Override
  public Report execute(IndexService index, ModelService model, StorageService storage, List<AIP> list)
    throws PluginException {
    for (AIP aip : list) {
      try {
        Path tempDirectory = Files.createTempDirectory("temp");
        StorageService tempStorage = new FileStorageService(tempDirectory);
        StoragePath aipPath = ModelUtils.getAIPpath(aip.getId());
        tempStorage.copy(storage, aipPath, aipPath);
        VirusCheckResult virusCheckResult = null;
        try {
          virusCheckResult = getAntiVirus().checkForVirus(tempDirectory);
          LOGGER.debug("AIP " + aip.getId() + " is clean: " + virusCheckResult.isClean());
          LOGGER.debug("AIP " + aip.getId() + " virus check report: " + virusCheckResult.getReport());
        } catch (RuntimeException e) {
          LOGGER.debug("Exception running virus check on AIP " + aip.getId() //$NON-NLS-1$
            + " - " + e.getMessage(), e); //$NON-NLS-1$
          throw new PluginException("Exception running virus check on AIP " + aip.getId() //$NON-NLS-1$
            + " - " + e.getMessage(), e); //$NON-NLS-1$
        }
      } catch (StorageServiceException e) {
        LOGGER.error("Error processing AIP " + aip.getId(), e);
      } catch (IOException e) {
        LOGGER.error("Error creating temp folder for AIP " + aip.getId(), e);
      } finally {
        try {
          storage.deleteResource(ModelUtils.getAIPpath(aip.getId()));
        } catch (StorageServiceException e) {
          LOGGER.error("Error removing temp storage", e);
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

  public String getAntiVirusClassName() {
    return antiVirusClassName;
  }

  public void setAntiVirusClassName(String antiVirusClassName) {
    this.antiVirusClassName = antiVirusClassName;
  }

  public AntiVirus getAntiVirus() {
    return antiVirus;
  }

  public void setAntiVirus(AntiVirus antiVirus) {
    this.antiVirus = antiVirus;
  }

  @Override
  public Plugin<AIP> cloneMe() {
    AntivirusPlugin antivirusPlugin = new AntivirusPlugin();
    antivirusPlugin.setAntiVirus(getAntiVirus());
    return antivirusPlugin;
  }

}
