/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.plugins.base;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.roda.core.common.ValidationUtils;
import org.roda.core.data.PluginParameter;
import org.roda.core.data.Report;
import org.roda.core.data.common.InvalidParameterException;
import org.roda.core.data.v2.EventPreservationObject;
import org.roda.core.data.v2.PluginType;
import org.roda.core.index.IndexService;
import org.roda.core.metadata.v2.premis.PremisMetadataException;
import org.roda.core.model.AIP;
import org.roda.core.model.ModelService;
import org.roda.core.model.ModelServiceException;
import org.roda.core.plugins.Plugin;
import org.roda.core.plugins.PluginException;
import org.roda.core.plugins.plugins.PluginUtils;
import org.roda.core.storage.StorageService;
import org.roda.core.storage.StorageServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AIPValidationPlugin implements Plugin<AIP> {
  private static final Logger LOGGER = LoggerFactory.getLogger(AIPValidationPlugin.class);

  private Map<String, String> parameters;

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
    return new ArrayList<PluginParameter>();
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
    List<String> validAIP = new ArrayList<String>();
    List<String> invalidAIP = new ArrayList<String>();
    for (AIP aip : list) {
      try {
        LOGGER.debug("Validating AIP " + aip.getId());
        boolean descriptiveValid = ValidationUtils.isAIPDescriptiveMetadataValid(model, aip.getId(), true);
        boolean preservationValid = ValidationUtils.isAIPPreservationMetadataValid(model, aip.getId(), true);
        if (descriptiveValid && preservationValid) {
          validAIP.add(aip.getId());
          LOGGER.debug("Done with validating AIP " + aip.getId() + ": valid!");
        } else {
          invalidAIP.add(aip.getId());
          LOGGER.debug("Done with validating AIP " + aip.getId() + ": invalid!");
        }
        createEvent(aip,model,descriptiveValid,preservationValid);
      } catch (ModelServiceException mse) {
        LOGGER.error("Error processing AIP " + aip.getId() + ": " + mse.getMessage(), mse);
      }
    }
    return null;
  }
  
  //TODO EVENT MUST BE "AIP EVENT" INSTEAD OF "REPRESENTATION EVENT"
  //TODO AGENT ID...
  private void createEvent(AIP aip, ModelService model, boolean descriptiveValid, boolean preservationValid) throws PluginException {
    try {
      boolean success = descriptiveValid && preservationValid;

      for (String representationID : aip.getRepresentationIds()) {
        PluginUtils.createPluginEvent(aip.getId(), representationID, model,
          EventPreservationObject.PRESERVATION_EVENT_TYPE_FORMAT_VALIDATION,
          "The AIP format was validated.",
          EventPreservationObject.PRESERVATION_EVENT_AGENT_ROLE_INGEST_TASK, "AGENT ID",
          Arrays.asList(representationID), success ? "success" : "error", "Report",
          "");
      }
    } catch (PremisMetadataException | IOException | StorageServiceException | ModelServiceException e) {
      throw new PluginException(e.getMessage(), e);
    }
    
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

  @Override
  public PluginType getType() {
    return PluginType.AIP_TO_AIP;
  }

  @Override
  public boolean areParameterValuesValid() {
    return true;
  }
}
