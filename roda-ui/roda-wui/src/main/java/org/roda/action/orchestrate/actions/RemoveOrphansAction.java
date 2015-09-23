package org.roda.action.orchestrate.actions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.roda.action.orchestrate.Plugin;
import org.roda.action.orchestrate.PluginException;
import org.roda.index.IndexService;
import org.roda.index.IndexServiceException;
import org.roda.model.AIP;
import org.roda.model.ModelService;
import org.roda.model.ModelServiceException;
import org.roda.model.utils.ModelUtils;
import org.roda.storage.StoragePath;
import org.roda.storage.StorageService;
import org.roda.storage.StorageServiceException;

import pt.gov.dgarq.roda.core.common.InvalidParameterException;
import pt.gov.dgarq.roda.core.common.RodaConstants;
import pt.gov.dgarq.roda.core.data.PluginParameter;
import pt.gov.dgarq.roda.core.data.Report;
import pt.gov.dgarq.roda.core.data.v2.SimpleDescriptionObject;

public class RemoveOrphansAction implements Plugin<AIP> {
  private final Logger logger = Logger.getLogger(getClass());
  private AIP newParent;

  @Override
  public void init() throws PluginException {
    // do nothing
  }

  @Override
  public void shutdown() {
    // do nothing
  }

  @Override
  public String getName() {
    return "MoveOrphansToNewParent";
  }

  @Override
  public String getDescription() {
    return "Moves the orphans AIP (not Fonds) to a new parent";
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

  public AIP getNewParent() {
    return newParent;
  }

  public void setNewParent(AIP newParent) {
    this.newParent = newParent;
  }

  @Override
  public Report execute(IndexService index, ModelService model, StorageService storage, List<AIP> list)
    throws PluginException {

    boolean reindexParent = false;
    for (AIP aip : list) {
      try {
        logger.debug("Processing AIP " + aip.getId());
        StoragePath aipPath = ModelUtils.getAIPpath(aip.getId());
        SimpleDescriptionObject sdo = index.retrieve(SimpleDescriptionObject.class, aip.getId());
        if (sdo.getLevel() == null || !sdo.getLevel().trim().equalsIgnoreCase("fonds")) {
          logger.debug("Moving orphan " + aip.getId() + " to under " + newParent.getId());
          Map<String, Set<String>> aipMetadata = storage.getMetadata(aipPath);
          aipMetadata.put(RodaConstants.STORAGE_META_PARENT_ID, new HashSet<String>(Arrays.asList(newParent.getId())));
          storage.updateMetadata(aipPath, aipMetadata, true);
          index.reindexAIP(aip);
          reindexParent=true;
        } else {
          logger.debug("AIP doesn't need to be moved... Level: " + sdo.getLevel());
        }
      } catch (StorageServiceException | IndexServiceException e) {
        logger.error("Error processing AIP " + aip.getId() + " (RemoveOrphansAction)");
        logger.error(e.getMessage(), e);
      }
    }
    
    if(reindexParent){
      logger.debug("Reindexing parent");
      try{
        newParent = model.retrieveAIP(newParent.getId());
        index.reindexAIP(newParent);
      } catch (ModelServiceException e) {
        logger.error("Error reindexing parent " + newParent.getId() + " (RemoveOrphansAction)");
        logger.error(e.getMessage(), e);
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
    logger.debug("End");
    return null;
  }

}
