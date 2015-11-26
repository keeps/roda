/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.plugins.base;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.roda.core.RodaCoreFactory;
import org.roda.core.common.ValidationUtils;
import org.roda.core.data.PluginParameter;
import org.roda.core.data.Report;
import org.roda.core.data.common.InvalidParameterException;
import org.roda.core.index.IndexService;
import org.roda.core.model.AIP;
import org.roda.core.model.ModelService;
import org.roda.core.model.ModelServiceException;
import org.roda.core.plugins.Plugin;
import org.roda.core.plugins.PluginException;
import org.roda.core.storage.StorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AIPValidationPlugin implements Plugin<AIP> {
  private final Logger logger = LoggerFactory.getLogger(getClass());

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
    return "AIP Validation action";
  }

  @Override
  public String getDescription() {
    return "Validates the XML files in the AIP";
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
    List<String> validAIP = new ArrayList<String>();
    List<String> invalidAIP = new ArrayList<String>();
    for (AIP aip : list) {
      logger.debug("Processing AIP " + aip.getId());
      try {
        boolean descriptiveValid = ValidationUtils.isAIPDescriptiveMetadataValid(model, aip.getId(), true,
          RodaCoreFactory.getConfigPath());
        boolean preservationValid = ValidationUtils.isAIPPreservationMetadataValid(model, aip.getId(), true,
          RodaCoreFactory.getConfigPath());
        if (descriptiveValid && preservationValid) {
          validAIP.add(aip.getId());
        } else {
          invalidAIP.add(aip.getId());
        }
      } catch (ModelServiceException mse) {
        logger.error("Error processing AIP " + aip.getId() + ": " + mse.getMessage(), mse);
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
    return new AIPValidationPlugin();
  }

}
