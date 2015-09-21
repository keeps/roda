package org.roda.action.orchestrate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
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
import pt.gov.dgarq.roda.core.data.SimpleDescriptionObject;
import pt.gov.dgarq.roda.core.data.eadc.DescriptionLevel;

public class RemoveOrphansAction implements Plugin<AIP> {
  private final Logger logger = Logger.getLogger(getClass());
  private String parentID;

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

  public String getParentID() {
    return parentID;
  }

  public void setParentID(String parentID) {
    this.parentID = parentID;
  }

  @Override
  public Report execute(IndexService index, ModelService model, StorageService storage, List<AIP> list)
    throws PluginException {

    try {
      model.retrieveAIP(parentID);
      for (AIP aip : list) {
        try {
          StoragePath aipPath = ModelUtils.getAIPpath(aip.getId());
          SimpleDescriptionObject sdo = index.retrieve(SimpleDescriptionObject.class, aip.getId());
          if (sdo.getParentPID() == null || sdo.getParentPID().trim().equals("")) {
            if (sdo.getLevel() == null || sdo.getLevel() != DescriptionLevel.FONDS) {
              Map<String, Set<String>> aipMetadata = storage.getMetadata(aipPath);
              aipMetadata.put(RodaConstants.STORAGE_META_PARENT_ID, new HashSet<String>(Arrays.asList(parentID)));
              storage.updateMetadata(aipPath, aipMetadata, true);
              index.reindexAIP(aip);
            }
          }
        } catch (StorageServiceException | IndexServiceException e) {
          logger.error("Error processing AIP " + aip.getId() + " (RemoveOrphansAction)");
        }
      }
    } catch (ModelServiceException e1) {
      logger.error("New parent (" + parentID + ") doesn't exist.");
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
