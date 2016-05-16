/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.plugins.risks;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.roda.core.common.iterables.CloseableIterable;
import org.roda.core.data.common.RodaConstants.PreservationEventType;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.InvalidParameterException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.common.OptionalWithCause;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.File;
import org.roda.core.data.v2.ip.Representation;
import org.roda.core.data.v2.jobs.PluginType;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.data.v2.risks.Risk;
import org.roda.core.data.v2.risks.RiskIncidence;
import org.roda.core.index.IndexService;
import org.roda.core.model.ModelService;
import org.roda.core.plugins.AbstractPlugin;
import org.roda.core.plugins.Plugin;
import org.roda.core.plugins.PluginException;
import org.roda.core.plugins.plugins.PluginHelper;
import org.roda.core.storage.StorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RiskIncidenceRemoverPlugin extends AbstractPlugin<AIP> {

  private static final Logger LOGGER = LoggerFactory.getLogger(RiskIncidenceRemoverPlugin.class);
  private static String riskIds;
  private static boolean aipRemoved;
  private static String OTHER_METADATA_TYPE = "RiskIncidence";

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
    return "Removes old risk incidences";
  }

  @Override
  public String getDescription() {
    return "Removes all risk incidences with removed risks";
  }

  @Override
  public String getVersionImpl() {
    return "1.0";
  }

  @Override
  public void setParameterValues(Map<String, String> parameters) throws InvalidParameterException {
    super.setParameterValues(parameters);

    if (parameters.containsKey("riskIds")) {
      riskIds = parameters.get("riskIds");
    }

    if (parameters.containsKey("aipRemoved")) {
      aipRemoved = Boolean.parseBoolean(parameters.get("aipRemoved"));
    } else {
      aipRemoved = false;
    }
  }

  @Override
  public Report execute(IndexService index, ModelService model, StorageService storage, List<AIP> list)
    throws PluginException {

    LOGGER.debug("Removing old risk incidences");
    Report pluginReport = PluginHelper.initPluginReport(this);

    try {
      if (aipRemoved) {
        executeWhenAipsRemoved(index, model, list);
      } else {
        executeWhenRisksRemoved(index, model, list);
      }
    } catch (GenericException | NotFoundException | AuthorizationDeniedException | RequestNotValidException e) {
      LOGGER.error("Could not delete risk incidence");
    }

    LOGGER.debug("Done removing old risk incidences");
    return pluginReport;
  }

  private void executeWhenAipsRemoved(IndexService index, ModelService model, List<AIP> list)
    throws GenericException, NotFoundException, AuthorizationDeniedException, RequestNotValidException {
    for (AIP aip : list) {
      RiskIncidence incidence = model.retrieveRiskIncidence(aip.getId(), null, null, null, OTHER_METADATA_TYPE);
      for (String riskId : incidence.getRisks()) {
        model.deleteRiskIncidence(riskId, aip.getId(), null, null, null, OTHER_METADATA_TYPE);
      }

      for (Representation representation : aip.getRepresentations()) {
        incidence = model.retrieveRiskIncidence(aip.getId(), representation.getId(), null, null, OTHER_METADATA_TYPE);
        for (String riskId : incidence.getRisks()) {
          model.deleteRiskIncidence(riskId, aip.getId(), representation.getId(), null, null, OTHER_METADATA_TYPE);
        }

        boolean recursive = true;
        CloseableIterable<OptionalWithCause<File>> allFiles = model.listFilesUnder(aip.getId(), representation.getId(),
          recursive);

        for (OptionalWithCause<File> ofile : allFiles) {
          File file = ofile.get();
          incidence = model.retrieveRiskIncidence(aip.getId(), representation.getId(), file.getPath(), file.getId(),
            OTHER_METADATA_TYPE);
          for (String riskId : incidence.getRisks()) {
            model.deleteRiskIncidence(riskId, aip.getId(), representation.getId(), new ArrayList<>(), file.getId(),
              OTHER_METADATA_TYPE);
          }
        }
      }
    }
  }

  private void executeWhenRisksRemoved(IndexService index, ModelService model, List<AIP> list)
    throws GenericException, NotFoundException, AuthorizationDeniedException, RequestNotValidException {
    if (riskIds != null) {
      String[] risks = riskIds.split(",");
      for (String riskId : risks) {
        for (AIP aip : list) {
          model.deleteRiskIncidence(riskId, aip.getId(), null, null, null, OTHER_METADATA_TYPE);

          for (Representation representation : aip.getRepresentations()) {
            model.deleteRiskIncidence(riskId, aip.getId(), representation.getId(), null, null, OTHER_METADATA_TYPE);

            boolean recursive = true;
            CloseableIterable<OptionalWithCause<File>> allFiles = model.listFilesUnder(aip.getId(),
              representation.getId(), recursive);

            for (OptionalWithCause<File> ofile : allFiles) {
              File file = ofile.get();
              model.deleteRiskIncidence(riskId, aip.getId(), representation.getId(), new ArrayList<>(), file.getId(),
                OTHER_METADATA_TYPE);
            }
          }
        }
      }
    }

    if (aipRemoved) {
      index.commit(Risk.class);
    }
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
  public Plugin<AIP> cloneMe() {
    return new RiskIncidenceRemoverPlugin();
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
