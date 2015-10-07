package org.roda.action.orchestrate.actions;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.roda.action.orchestrate.Plugin;
import org.roda.action.orchestrate.PluginException;
import org.roda.action.orchestrate.actions.antivirus.AVGAntiVirus;
import org.roda.action.orchestrate.actions.antivirus.AntiVirus;
import org.roda.action.orchestrate.actions.antivirus.VirusCheckResult;
import org.roda.core.common.InvalidParameterException;
import org.roda.core.data.PluginParameter;
import org.roda.core.data.Report;
import org.roda.index.IndexService;
import org.roda.model.AIP;
import org.roda.model.ModelService;
import org.roda.model.utils.ModelUtils;
import org.roda.storage.StoragePath;
import org.roda.storage.StorageService;
import org.roda.storage.StorageServiceException;
import org.roda.storage.fs.FileStorageService;

public class AntivirusAction implements Plugin<AIP> {
  private final Logger logger = Logger.getLogger(getClass());
  String antiVirusClassName;
  private AntiVirus antiVirus = null;

  @Override
  public void init() throws PluginException {
    for (PluginParameter parameter : getParameters()) {
      if (parameter.getName().equalsIgnoreCase("antivirusClassName")) {
        try {
          antiVirusClassName = parameter.getValue();
          logger.info("Loading antivirus class " + antiVirusClassName); //$NON-NLS-1$
          setAntiVirus((AntiVirus) Class.forName(antiVirusClassName).newInstance());
          logger.info("Using antivirus " + getAntiVirus().getClass().getName());
        } catch (ClassNotFoundException e) {
          logger.warn("Antivirus class " + antiVirusClassName //$NON-NLS-1$
            + " not found - " + e.getMessage()); //$NON-NLS-1$
        } catch (InstantiationException e) {
          // not possible to create a new instance of the class
          logger.warn("Antivirus class " + antiVirusClassName //$NON-NLS-1$
            + " instantiation exception - " + e.getMessage()); //$NON-NLS-1$
        } catch (IllegalAccessException e) {
          // not possible to create a new instance of the class
          logger.warn("Antivirus class " + antiVirusClassName //$NON-NLS-1$
            + " illegal access exception - " + e.getMessage()); //$NON-NLS-1$
        }
      }
    }

    if (getAntiVirus() == null) {
      setAntiVirus(new AVGAntiVirus());
      logger.info("Using default antivirus " //$NON-NLS-1$
        + getAntiVirus().getClass().getName());
    }

    logger.info("init OK"); //$NON-NLS-1$
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
      try {
        Path p = Files.createTempDirectory("temp");
        StorageService tempStorage = new FileStorageService(p);
        StoragePath aipPath = ModelUtils.getAIPpath(aip.getId());
        tempStorage.copy(storage, aipPath, aipPath);
        VirusCheckResult virusCheckResult = null;
        try {
          virusCheckResult = getAntiVirus().checkForVirus(p);
          logger.debug("AIP "+aip.getId()+" is clean: "+virusCheckResult.isClean());
          logger.debug("AIP "+aip.getId()+" virus check report: "+virusCheckResult.getReport());
        } catch (RuntimeException e) {
          logger.debug("Exception running virus check on AIP " + aip.getId() //$NON-NLS-1$
            + " - " + e.getMessage(), e); //$NON-NLS-1$
          throw new PluginException("Exception running virus check on AIP " + aip.getId() //$NON-NLS-1$
            + " - " + e.getMessage(), e); //$NON-NLS-1$
        }
      } catch (StorageServiceException e) {
        logger.error("Error processing AIP "+aip.getId(),e);
      } catch (IOException e){
        logger.error("Error creating temp folder for AIP "+aip.getId(),e);
      } finally {
        try {
          storage.deleteResource(ModelUtils.getAIPpath(aip.getId()));
        } catch (StorageServiceException e) {
          logger.error("Error removing temp storage",e);
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

}
