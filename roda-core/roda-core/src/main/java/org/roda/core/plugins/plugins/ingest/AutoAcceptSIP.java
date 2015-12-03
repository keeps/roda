package org.roda.core.plugins.plugins.ingest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.roda.core.data.PluginParameter;
import org.roda.core.data.Report;
import org.roda.core.data.common.InvalidParameterException;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.PluginType;
import org.roda.core.index.IndexService;
import org.roda.core.model.AIP;
import org.roda.core.model.ModelService;
import org.roda.core.model.ModelServiceException;
import org.roda.core.model.utils.ModelUtils;
import org.roda.core.plugins.Plugin;
import org.roda.core.plugins.PluginException;
import org.roda.core.storage.StoragePath;
import org.roda.core.storage.StorageService;
import org.roda.core.storage.StorageServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AutoAcceptSIP implements Plugin<AIP> {
  private static final Logger LOGGER = LoggerFactory.getLogger(AutoAcceptSIP.class);

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
    return "Auto accept SIP";
  }

  @Override
  public String getVersion() {
    return "1.0.0";
  }

  @Override
  public String getDescription() {
    return "Automatically accepts SIPs ingested without manual validation";
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
    // do nothing for now
  }

  @Override
  public Report execute(IndexService index, ModelService model, StorageService storage, List<AIP> list)
    throws PluginException {

    for (AIP aip : list) {
      try {
        StoragePath aipPath = ModelUtils.getAIPpath(aip.getId());
        Map<String, Set<String>> aipMetadata = storage.getMetadata(aipPath);
        ModelUtils.setAs(aipMetadata, RodaConstants.STORAGE_META_ACTIVE, true);
        storage.updateMetadata(aipPath, aipMetadata, true);
        model.updateAIP(aip.getId());
      } catch (ModelServiceException | StorageServiceException e) {
        LOGGER.error("Error updating AIP metadata attribute active", e);
      }
    }

    return null;
  }

  @Override
  public Report beforeExecute(IndexService index, ModelService model, StorageService storage) throws PluginException {
    // do nothing
    return null;
  }

  @Override
  public Report afterExecute(IndexService index, ModelService model, StorageService storage) throws PluginException {
    // do nothing
    return null;
  }

  @Override
  public Plugin<AIP> cloneMe() {
    return new AutoAcceptSIP();
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
