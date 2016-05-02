/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.plugins.risks;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.roda.core.common.iterables.CloseableIterable;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.common.RodaConstants.PreservationEventType;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.InvalidParameterException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.common.OptionalWithCause;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.Representation;
import org.roda.core.data.v2.ip.metadata.OtherMetadata;
import org.roda.core.data.v2.jobs.PluginType;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.data.v2.risks.Risk;
import org.roda.core.data.v2.risks.RiskIncidence;
import org.roda.core.index.IndexService;
import org.roda.core.model.ModelService;
import org.roda.core.model.utils.JsonUtils;
import org.roda.core.plugins.AbstractPlugin;
import org.roda.core.plugins.Plugin;
import org.roda.core.plugins.PluginException;
import org.roda.core.plugins.plugins.PluginHelper;
import org.roda.core.storage.Binary;
import org.roda.core.storage.StorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RiskCounterPlugin<T extends Serializable> extends AbstractPlugin<T> {

  private static final Logger LOGGER = LoggerFactory.getLogger(RiskCounterPlugin.class);
  private static String riskIds = null;

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
    return "Create risk incidences";
  }

  @Override
  public String getDescription() {
    return "Associate risk with AIPs, representations and files";
  }

  @Override
  public String getVersionImpl() {
    return "1.0";
  }

  @Override
  public void setParameterValues(Map<String, String> parameters) throws InvalidParameterException {
    super.setParameterValues(parameters);

    if (parameters.containsKey(RodaConstants.PLUGIN_PARAMS_RISK_ID)) {
      riskIds = parameters.get(RodaConstants.PLUGIN_PARAMS_RISK_ID);
    }
  }

  @Override
  public Report execute(IndexService index, ModelService model, StorageService storage, List<T> list)
    throws PluginException {

    LOGGER.debug("Counting risk incidences");
    Report pluginReport = PluginHelper.createPluginReport(this);
    Map<String, Integer> riskCounter = new HashMap<String, Integer>();

    try {
      for (T element : list) {
        CloseableIterable<OptionalWithCause<OtherMetadata>> objectsMetadata = null;

        if (element instanceof AIP) {
          AIP aip = (AIP) element;
          objectsMetadata = model.listOtherMetadata(aip.getId(), null);
        } else if (element instanceof Representation) {
          // files are included on representations other metadata folder
          Representation representation = (Representation) element;
          objectsMetadata = model.listOtherMetadata(representation.getAipId(), representation.getId());
        }

        if (objectsMetadata != null) {
          for (OptionalWithCause<OtherMetadata> m : objectsMetadata) {
            Binary binary = model.retrieveOtherMetadataBinary(m.get());
            InputStream inputStream = binary.getContent().createInputStream();
            RiskIncidence incidence = JsonUtils.getObjectFromJson(inputStream, RiskIncidence.class);

            for (String riskId : incidence.getRisks()) {
              if (riskCounter.containsKey(riskId)) {
                riskCounter.put(riskId, riskCounter.get(riskId) + 1);
              } else {
                riskCounter.put(riskId, 1);
              }
            }
          }
        }
      }

      for (String riskId : riskCounter.keySet()) {
        Risk risk = model.retrieveRisk(riskId);
        risk.setObjectsSize(riskCounter.get(riskId));
        model.updateRisk(risk, null);
      }

    } catch (NotFoundException | GenericException | AuthorizationDeniedException | RequestNotValidException
      | IOException e) {
      e.printStackTrace();
    }

    LOGGER.debug("Done counting risk incidences");
    return pluginReport;
  }

  @Override
  public Report beforeAllExecute(IndexService index, ModelService model, StorageService storage)
    throws PluginException {
    // do nothing
    return null;
  }

  @Override
  public Report beforeBlockExecute(IndexService index, ModelService model, StorageService storage)
    throws PluginException {
    // do nothing
    return null;
  }

  @Override
  public Report afterBlockExecute(IndexService index, ModelService model, StorageService storage)
    throws PluginException {
    // do nothing
    return null;
  }

  @Override
  public Report afterAllExecute(IndexService index, ModelService model, StorageService storage) throws PluginException {
    // do nothing
    return null;
  }

  @Override
  public Plugin<T> cloneMe() {
    return new RiskCounterPlugin<T>();
  }

  @Override
  public PluginType getType() {
    return PluginType.MISC;
  }

  @Override
  public boolean areParameterValuesValid() {
    return true;
  }

  // TODO FIX
  @Override
  public PreservationEventType getPreservationEventType() {
    return null;
  }

  @Override
  public String getPreservationEventDescription() {
    return "XXXXXXXXXX";
  }

  @Override
  public String getPreservationEventSuccessMessage() {
    return "XXXXXXXXXXXXXXXXXXXXXXXX";
  }

  @Override
  public String getPreservationEventFailureMessage() {
    return "XXXXXXXXXXXXXXXXXXXXXXXXXX";
  }
}
