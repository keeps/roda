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

public class RemoveOrphansAction implements Plugin<SimpleDescriptionObject> {
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
  public Report execute(IndexService index, ModelService model, StorageService storage,
    List<SimpleDescriptionObject> list) throws PluginException {

    for (SimpleDescriptionObject sdo : list) {
      try {
        logger.debug("Processing AIP " + sdo.getId());
        if (sdo.getLevel() == null || !sdo.getLevel().trim().equalsIgnoreCase("fonds")) {
          AIP aip = model.retrieveAIP(sdo.getId());
          StoragePath aiPpath = ModelUtils.getAIPpath(aip.getId());

          logger.debug("  Moving orphan " + sdo.getId() + " to under " + newParent.getId());
          Map<String, Set<String>> SimpleDescriptionObjectMetadata = storage.getMetadata(aiPpath);
          SimpleDescriptionObjectMetadata.put(RodaConstants.STORAGE_META_PARENT_ID,
            new HashSet<String>(Arrays.asList(newParent.getId())));
          storage.updateMetadata(aiPpath, SimpleDescriptionObjectMetadata, true);
          aip.setParentId(newParent.getId());
          index.reindexAIP(aip);
        } else {
          logger.debug("  AIP doesn't need to be moved... Level: " + sdo.getLevel());
        }
      } catch (StorageServiceException | ModelServiceException e) {
        logger.error("Error processing SimpleDescriptionObject " + sdo.getId() + " (RemoveOrphansAction)");
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
