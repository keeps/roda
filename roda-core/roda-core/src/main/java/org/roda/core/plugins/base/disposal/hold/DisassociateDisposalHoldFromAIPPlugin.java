/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.base.disposal.hold;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.AlreadyExistsException;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.IllegalOperationException;
import org.roda.core.data.exceptions.InvalidParameterException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.LiteOptionalWithCause;
import org.roda.core.data.v2.common.Pair;
import org.roda.core.data.v2.disposal.hold.DisposalHold;
import org.roda.core.data.v2.disposal.hold.DisposalHoldState;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.PluginParameter;
import org.roda.core.data.v2.jobs.PluginState;
import org.roda.core.data.v2.jobs.PluginType;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.data.v2.validation.ValidationException;
import org.roda.core.index.IndexService;
import org.roda.core.index.utils.IterableIndexResult;
import org.roda.core.model.ModelService;
import org.roda.core.plugins.AbstractPlugin;
import org.roda.core.plugins.Plugin;
import org.roda.core.plugins.PluginException;
import org.roda.core.plugins.PluginHelper;
import org.roda.core.plugins.RODAObjectsProcessingLogic;
import org.roda.core.plugins.orchestrate.JobPluginInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class DisassociateDisposalHoldFromAIPPlugin extends AbstractPlugin<AIP> {
  private static final Logger LOGGER = LoggerFactory.getLogger(DisassociateDisposalHoldFromAIPPlugin.class);
  private static final Map<String, PluginParameter> pluginParameters = new HashMap<>();

  static {
    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_DISPOSAL_HOLD_ID,
      PluginParameter
        .getBuilder(RodaConstants.PLUGIN_PARAMS_DISPOSAL_HOLD_ID, "Disposal hold id",
          PluginParameter.PluginParameterType.STRING)
        .isMandatory(true).isReadOnly(false).withDescription("Disposal hold identifier").build());

    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_DISPOSAL_HOLD_DISASSOCIATE_ALL,
      PluginParameter
        .getBuilder(RodaConstants.PLUGIN_PARAMS_DISPOSAL_HOLD_DISASSOCIATE_ALL, "Disassociate all holds",
          PluginParameter.PluginParameterType.BOOLEAN)
        .withDefaultValue("false").isMandatory(true).isReadOnly(false)
        .withDescription("Disassociate all disposal holds associated to AIP").build());

    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_DETAILS,
      PluginParameter
        .getBuilder(RodaConstants.PLUGIN_PARAMS_DETAILS, "Details", PluginParameter.PluginParameterType.STRING)
        .isMandatory(false).withDescription("Details that will be used when creating event").build());
  }

  private String disposalHoldId;
  private boolean clearAll;
  private String details;

  public static String getStaticName() {
    return "Disassociate disposal hold from AIP";
  }

  public static String getStaticDescription() {
    return "";
  }

  @Override
  public List<PluginParameter> getParameters() {
    ArrayList<PluginParameter> parameters = new ArrayList<>();
    parameters.add(pluginParameters.get(RodaConstants.PLUGIN_PARAMS_DISPOSAL_HOLD_ID));
    parameters.add(pluginParameters.get(RodaConstants.PLUGIN_PARAMS_DISPOSAL_HOLD_DISASSOCIATE_ALL));
    parameters.add(pluginParameters.get(RodaConstants.PLUGIN_PARAMS_DETAILS));
    return parameters;
  }

  @Override
  public void setParameterValues(Map<String, String> parameters) throws InvalidParameterException {
    super.setParameterValues(parameters);

    if (parameters.containsKey(RodaConstants.PLUGIN_PARAMS_DISPOSAL_HOLD_ID)) {
      disposalHoldId = parameters.get(RodaConstants.PLUGIN_PARAMS_DISPOSAL_HOLD_ID);
    }

    if (parameters.containsKey(RodaConstants.PLUGIN_PARAMS_DISPOSAL_HOLD_DISASSOCIATE_ALL)) {
      clearAll = Boolean.parseBoolean(parameters.get(RodaConstants.PLUGIN_PARAMS_DISPOSAL_HOLD_DISASSOCIATE_ALL));
    }

    if (parameters.containsKey(RodaConstants.PLUGIN_PARAMS_DETAILS)) {
      details = parameters.get(RodaConstants.PLUGIN_PARAMS_DETAILS);
    }
  }

  @Override
  public String getVersionImpl() {
    return "1.0";
  }

  @Override
  public String getName() {
    return getStaticName();
  }

  @Override
  public String getDescription() {
    return getStaticDescription();
  }

  @Override
  public RodaConstants.PreservationEventType getPreservationEventType() {
    return RodaConstants.PreservationEventType.POLICY_ASSIGNMENT;
  }

  @Override
  public String getPreservationEventDescription() {
    return "Disassociate disposal hold from AIP";
  }

  @Override
  public String getPreservationEventSuccessMessage() {
    return "";
  }

  @Override
  public String getPreservationEventFailureMessage() {
    return "";
  }

  @Override
  public PluginType getType() {
    return PluginType.AIP_TO_AIP;
  }

  @Override
  public List<String> getCategories() {
    return Collections.singletonList(RodaConstants.PLUGIN_CATEGORY_NOT_LISTABLE);
  }

  @Override
  public Plugin<AIP> cloneMe() {
    return new DisassociateDisposalHoldFromAIPPlugin();
  }

  @Override
  public boolean areParameterValuesValid() {
    return true;
  }

  @Override
  public void init() throws PluginException {
    // do nothing
  }

  @Override
  public List<Class<AIP>> getObjectClasses() {
    return Collections.singletonList(AIP.class);
  }

  @Override
  public Report beforeAllExecute(IndexService index, ModelService model) throws PluginException {
    return new Report();
  }

  @Override
  public Report execute(IndexService index, ModelService model, List<LiteOptionalWithCause> liteList)
    throws PluginException {
    return PluginHelper.processObjects(this, new RODAObjectsProcessingLogic<AIP>() {
      @Override
      public void process(IndexService index, ModelService model, Report report, Job cachedJob,
        JobPluginInfo jobPluginInfo, Plugin<AIP> plugin, List<AIP> objects) {
        processAIP(index, model, report, cachedJob, jobPluginInfo, objects);
      }
    }, index, model, liteList);
  }

  private void processAIP(IndexService index, ModelService model, Report report, Job cachedJob,
    JobPluginInfo jobPluginInfo, List<AIP> aips) {
    report.addPluginDetails(details);

    for (AIP aip : aips) {
      String outcomeText;
      PluginState state = PluginState.SUCCESS;
      Report reportItem = PluginHelper.initPluginReportItem(this, aip.getId(), AIP.class);
      PluginHelper.updatePartialJobReport(this, model, reportItem, false, cachedJob);
      LOGGER.debug("Processing AIP {}", aip.getId());

      if (clearAll) {
        try {
          // lift disposal holds
          if (aip.getHolds() != null && !aip.getHolds().isEmpty()) {
            outcomeText = "Cannot found any active direct disposal hold for disassociate from AIP : " + aip.getId();
            boolean hasAtLeastOneDirectHold = false;
            for (DisposalHold hold : model.retrieveDirectActiveDisposalHolds(aip.getId())) {
              hasAtLeastOneDirectHold = true;
              outcomeText = DisposalHoldPluginUtils.disassociateDisposalHoldFromAIP(hold.getId(), aip, reportItem)
                .getSecond();
              processTransitiveAIP(model, index, cachedJob, aip, hold.getId(), jobPluginInfo, report);
            }

            if (hasAtLeastOneDirectHold) {
              model.updateAIP(aip, cachedJob.getUsername());
              jobPluginInfo.incrementObjectsProcessedWithSuccess();
              reportItem.setPluginState(state);
            } else {
              state = PluginState.SKIPPED;
              LOGGER.info("{} aip : {}", outcomeText, aip.getId());
              jobPluginInfo.incrementObjectsProcessed(state);
              reportItem.setPluginState(state).setPluginDetails(outcomeText);
            }

          } else {
            state = PluginState.SKIPPED;
            outcomeText = "There are no direct Disposal hold on this AIP";
            LOGGER.info("{} aip : {}", outcomeText, aip.getId());
            jobPluginInfo.incrementObjectsProcessed(state);
            reportItem.setPluginState(state).setPluginDetails(outcomeText + " aip : " + aip.getId());
          }

        } catch (GenericException | NotFoundException | RequestNotValidException | AuthorizationDeniedException e) {
          outcomeText = "Error lifting all disposal holds from AIP " + aip.getId();
          LOGGER.error("Error lifting all disposal holds from '{}': {}", aip.getId(), e.getMessage(), e);
          state = PluginState.FAILURE;
          jobPluginInfo.incrementObjectsProcessedWithFailure();
          reportItem.setPluginState(state)
            .setPluginDetails("Error lifting all disposal holds from AIP '" + aip.getId() + "': " + e.getMessage());
        }
      } else {
        try {
          Pair<Boolean, String> outcome = DisposalHoldPluginUtils.disassociateDisposalHoldFromAIP(disposalHoldId, aip,
            reportItem);
          boolean lifted = outcome.getFirst();
          outcomeText = outcome.getSecond();
          processTransitiveAIP(model, index, cachedJob, aip, disposalHoldId, jobPluginInfo, report);
          model.updateAIP(aip, cachedJob.getUsername());
          if (lifted) {
            jobPluginInfo.incrementObjectsProcessedWithSuccess();
          } else {
            jobPluginInfo.incrementObjectsProcessedWithSkipped();
          }
          reportItem.setPluginState(state);
        } catch (GenericException | NotFoundException | RequestNotValidException | AuthorizationDeniedException e) {
          outcomeText = "Error lifting disposal hold" + disposalHoldId + " from AIP " + aip.getId();
          LOGGER.error("Error lifting disposal hold '{}' from '{}': {}", disposalHoldId, aip.getId(), e.getMessage(),
            e);
          state = PluginState.FAILURE;
          jobPluginInfo.incrementObjectsProcessedWithFailure();
          reportItem.setPluginState(state).setPluginDetails(
            "Error lifting disposal hold '" + disposalHoldId + "' from AIP '" + aip.getId() + "': " + e.getMessage());
        }
      }

      report.addReport(reportItem);
      PluginHelper.updatePartialJobReport(this, model, reportItem, true, cachedJob);

      try {
        PluginHelper.createPluginEvent(this, aip.getId(), model, index, null, null, reportItem.getPluginState(),
          outcomeText, true, cachedJob);
      } catch (ValidationException | RequestNotValidException | NotFoundException | GenericException
        | AuthorizationDeniedException | AlreadyExistsException e) {
        LOGGER.error("Error creating event: {}", e.getMessage(), e);
      }
    }
  }

  private void processTransitiveAIP(ModelService model, IndexService index, Job cachedJob, AIP aip, String holdId,
    JobPluginInfo jobPluginInfo, Report report) throws GenericException, NotFoundException, RequestNotValidException {
    IterableIndexResult<IndexedAIP> results = DisposalHoldPluginUtils.getTransitivesHoldsAIPs(index, aip.getId());
    for (IndexedAIP indexedAIP : results) {
      String outcomeText;
      PluginState state = PluginState.SUCCESS;
      Report reportItem = PluginHelper.initPluginReportItem(this, indexedAIP.getId(), AIP.class);
      PluginHelper.updatePartialJobReport(this, model, reportItem, false, cachedJob);
      jobPluginInfo.incrementObjectsCount();

      try {
        AIP aipChildren = model.retrieveAIP(indexedAIP.getId());
        LOGGER.debug("Processing transitive AIP {}", aip.getId());
        outcomeText = DisposalHoldPluginUtils.disassociateTransitiveDisposalHoldFromAIP(holdId, aipChildren,
          reportItem);
        model.updateAIP(aipChildren, cachedJob.getUsername());
        reportItem.setPluginState(state).addPluginDetails(outcomeText);
        jobPluginInfo.incrementObjectsProcessedWithSuccess();

      } catch (AuthorizationDeniedException e) {
        state = PluginState.FAILURE;
        outcomeText = "Can't retrieve AIP " + aip.getId() + " for disassociate transitive hold " + holdId + ".";
        LOGGER.debug(outcomeText, e);
        jobPluginInfo.incrementObjectsProcessedWithFailure();
        reportItem.setPluginState(state).setPluginDetails("Error disassociate transitive disposal hold " + holdId
          + " on aip " + indexedAIP.getId() + ": " + e.getMessage());
      } finally {
        report.addReport(reportItem);
        PluginHelper.updatePartialJobReport(this, model, reportItem, true, cachedJob);
      }

      try {
        PluginHelper.createPluginEvent(this, indexedAIP.getId(), model, index, null, null, reportItem.getPluginState(),
          outcomeText, true, cachedJob);
      } catch (ValidationException | RequestNotValidException | NotFoundException | GenericException
        | AuthorizationDeniedException | AlreadyExistsException e) {
        LOGGER.error("Error creating event: {}", e.getMessage(), e);
      }
    }

  }

  @Override
  public Report afterAllExecute(IndexService index, ModelService model) throws PluginException {
    // do nothing
    return null;
  }

  @Override
  public void shutdown() {
    // do nothing
  }
}
