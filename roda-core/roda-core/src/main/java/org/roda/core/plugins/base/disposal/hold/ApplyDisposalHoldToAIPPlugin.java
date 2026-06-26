/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.base.disposal.hold;

import static org.roda.core.data.common.RodaConstants.PreservationEventType.POLICY_ASSIGNMENT;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.roda.core.RodaCoreFactory;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.AlreadyExistsException;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.IllegalOperationException;
import org.roda.core.data.exceptions.InvalidParameterException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.LiteOptionalWithCause;
import org.roda.core.data.v2.disposal.hold.DisposalHold;
import org.roda.core.data.v2.disposal.metadata.DisposalHoldAIPMetadata;
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
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class ApplyDisposalHoldToAIPPlugin extends AbstractPlugin<AIP> {
  private static final Logger LOGGER = LoggerFactory.getLogger(ApplyDisposalHoldToAIPPlugin.class);
  private static final Map<String, PluginParameter> pluginParameters = new HashMap<>();

  static {

    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_DISPOSAL_HOLD_IDS,
      PluginParameter
        .getBuilder(RodaConstants.PLUGIN_PARAMS_DISPOSAL_HOLD_IDS,
          "plugin.applyDisposalHoldToAipPlugin.parameter.holdIds.name", PluginParameter.PluginParameterType.STRING)
        .isMandatory(false).isReadOnly(false)
        .withDescription("plugin.applyDisposalHoldToAipPlugin.parameter.holdIds.description").build());

    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_DISPOSAL_HOLD_OVERRIDE,
      PluginParameter
        .getBuilder(RodaConstants.PLUGIN_PARAMS_DISPOSAL_HOLD_OVERRIDE,
          "plugin.applyDisposalHoldToAipPlugin.parameter.override.name", PluginParameter.PluginParameterType.BOOLEAN)
        .withDefaultValue("false").isMandatory(true).isReadOnly(false)
        .withDescription("plugin.applyDisposalHoldToAipPlugin.parameter.override.description").build());

  }

  private String disposalHoldId;
  private List<String> disposalHoldIds;
  private boolean override;

  public static String getStaticName() {
    return "plugin.applyDisposalHoldToAipPlugin.name";
  }

  public static String getStaticDescription() {
    return "";
  }

  @Override
  public List<PluginParameter> getParameters() {
    ArrayList<PluginParameter> parameters = new ArrayList<>();
    parameters.add(pluginParameters.get(RodaConstants.PLUGIN_PARAMS_DISPOSAL_HOLD_IDS));
    parameters.add(pluginParameters.get(RodaConstants.PLUGIN_PARAMS_DISPOSAL_HOLD_OVERRIDE));
    return parameters;
  }

  @Override
  public void setParameterValues(Map<String, String> parameters) throws InvalidParameterException {
    super.setParameterValues(parameters);

    if (parameters.containsKey(RodaConstants.PLUGIN_PARAMS_DISPOSAL_HOLD_IDS)) {
      String ids = parameters.get(RodaConstants.PLUGIN_PARAMS_DISPOSAL_HOLD_IDS);
      disposalHoldIds = Arrays.stream(ids.split(",")).map(String::trim).filter(StringUtils::isNotBlank).toList();
    }

    if (parameters.containsKey(RodaConstants.PLUGIN_PARAMS_DISPOSAL_HOLD_OVERRIDE)) {
      override = Boolean.parseBoolean(parameters.get(RodaConstants.PLUGIN_PARAMS_DISPOSAL_HOLD_OVERRIDE));
    }

    if (disposalHoldIds.isEmpty()) {
      throw new InvalidParameterException("At least one disposal hold id is required");
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
    return POLICY_ASSIGNMENT;
  }

  @Override
  public String getPreservationEventDescription() {
    return "Apply disposal hold to AIP";
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
  public void init() throws PluginException {
    // do nothing
  }

  @Override
  public Report beforeAllExecute(IndexService index, ModelService model) throws PluginException {
    // do nothing
    return null;
  }

  @Override
  public Report execute(IndexService index, ModelService model, List<LiteOptionalWithCause> liteList)
    throws PluginException {
    return PluginHelper.processObjects(this, new RODAObjectsProcessingLogic<AIP>() {
      @Override
      public void process(IndexService index, ModelService model, Report report, Job cachedJob,
        JobPluginInfo jobPluginInfo, Plugin<AIP> plugin, List<AIP> objects) {
        processAIP(index, model, objects, report, jobPluginInfo, cachedJob);
      }
    }, index, model, liteList);
  }

  private void processAIP(IndexService index, ModelService model, List<AIP> aips, Report report,
    JobPluginInfo jobPluginInfo, Job cachedJob) {
    LOGGER.debug("Applying disposal holds {}", disposalHoldIds);

    Map<String, DisposalHold> disposalHolds = new LinkedHashMap<>();
    for (String holdId : disposalHoldIds) {
      DisposalHold disposalHold = RodaCoreFactory.getDisposalHold(holdId);
      if (disposalHold == null) {
        LOGGER.error("Failed to retrieve disposal hold {} from model", holdId);
      } else {
        disposalHolds.put(holdId, disposalHold);
      }
    }

    for (AIP aip : aips) {
      Report reportItem = PluginHelper.initPluginReportItem(this, aip.getId(), AIP.class);
      PluginHelper.updatePartialJobReport(this, model, reportItem, false, cachedJob);

      PluginState state = PluginState.SUCCESS;
      List<DisposalHoldAIPMetadata> holds = new ArrayList<>();
      List<DisposalHold> appliedHolds = new ArrayList<>();
      List<String> outcomeTexts = new ArrayList<>();
      boolean hasAppliedHold = false;
      boolean hasFailedHold = false;
      LOGGER.debug("Processing AIP {}", aip.getId());

      // Save a disposal list to use when disassociate transitives disposal holds
      if (aip.getHolds() != null) {
        holds = new ArrayList<>(aip.getHolds());
      }

      if (disposalHolds.size() != disposalHoldIds.size()) {
        state = PluginState.FAILURE;
        String details = "Failed to retrieve one or more disposal holds " + disposalHoldIds;
        reportItem.setPluginState(state).setPluginDetails(details);
        outcomeTexts.add(details);
      } else if (StringUtils.isNotBlank(aip.getDisposalConfirmationId())) {
        state = PluginState.FAILURE;
        LOGGER.error(
          "Error applying disposal holds {} to AIP '{}': This AIP is part of a disposal confirmation report and an hold cannot be applied",
          disposalHoldIds, aip.getId());
        reportItem.setPluginState(state).setPluginDetails("Error applying disposal hold to AIP '" + aip.getId()
          + "': This AIP is part of a disposal confirmation report and an hold cannot be applied");
        outcomeTexts.add("Applying disposal holds " + disposalHoldIds + " failed for AIP '" + aip.getId()
          + "'; This AIP is part of a disposal confirmation report and an hold cannot be applied");
      } else {
        if (override) {
          DisposalHoldPluginUtils.disassociateAllDisposalHoldsFromAIP(model, state, aip, cachedJob, reportItem);
        }

        for (Map.Entry<String, DisposalHold> entry : disposalHolds.entrySet()) {
          String holdId = entry.getKey();
          DisposalHold disposalHold = entry.getValue();

          try {
            if (!override && model.isAIPOnDirectHold(aip.getId(), holdId)) {
              LOGGER.info(
                "Applying disposal hold '{}' to AIP '{}' was skipped because it is already on the same disposal hold",
                holdId, aip.getId());
              String skippedText = "Applying disposal hold '" + holdId + "' to AIP '" + aip.getId()
                + "' was skipped because it is already on the same disposal hold";
              reportItem.addPluginDetails(skippedText);
              outcomeTexts.add(skippedText);
            } else {
              outcomeTexts.add(applyDisposalHold(aip, cachedJob, model, disposalHold, holdId, state, reportItem));
              appliedHolds.add(disposalHold);
              hasAppliedHold = true;
            }
          } catch (NotFoundException | GenericException | RequestNotValidException | AuthorizationDeniedException
            | IllegalOperationException e) {
            hasFailedHold = true;
            LOGGER.error("Error applying disposal hold {} to AIP '{}': {}", holdId, aip.getId(), e.getMessage(), e);
            String failedText = "Error applying disposal hold '" + holdId + "' to AIP '" + aip.getId() + "': "
              + e.getMessage();
            reportItem.addPluginDetails(failedText);
            outcomeTexts.add(failedText);
          }
        }

        if (hasFailedHold) {
          state = PluginState.FAILURE;
        } else if (hasAppliedHold) {
          state = PluginState.SUCCESS;
        } else {
          state = PluginState.SKIPPED;
        }

        if (!appliedHolds.isEmpty() && !PluginState.FAILURE.equals(state)) {
          try {
            model.updateAIP(aip, cachedJob.getUsername());
            for (DisposalHold appliedHold : appliedHolds) {
              applyDisposalTransitiveHolds(model, index, cachedJob, aip, appliedHold, appliedHold.getId(),
                jobPluginInfo, report, holds);
            }
          } catch (NotFoundException | RequestNotValidException | AuthorizationDeniedException | GenericException e) {
            state = PluginState.FAILURE;
            LOGGER.error("Error persisting disposal holds {} for AIP '{}': {}", disposalHoldIds, aip.getId(),
              e.getMessage(), e);
            String failedText = "Error persisting disposal holds " + disposalHoldIds + " for AIP '" + aip.getId()
              + "': " + e.getMessage();
            reportItem.addPluginDetails(failedText);
            outcomeTexts.add(failedText);
          }
        }
      }

      if (PluginState.FAILURE.equals(state)) {
        jobPluginInfo.incrementObjectsProcessedWithFailure();
      } else {
        jobPluginInfo.incrementObjectsProcessed(state);
      }

      reportItem.setPluginState(state);
      report.addReport(reportItem);
      PluginHelper.updatePartialJobReport(this, model, reportItem, true, cachedJob);

      try {
        PluginHelper.createPluginEvent(this, aip.getId(), model, index, null, null, state,
          String.join("; ", outcomeTexts), true, cachedJob);
      } catch (ValidationException | RequestNotValidException | NotFoundException | GenericException
        | AuthorizationDeniedException | AlreadyExistsException e) {
        LOGGER.error("Error creating event: {}", e.getMessage(), e);
      }
    }
  }

  private String applyDisposalHold(AIP aip, Job cachedJob, ModelService model, DisposalHold disposalHold, String holdId,
    PluginState state, Report reportItem) throws NotFoundException, RequestNotValidException,
    AuthorizationDeniedException, GenericException, IllegalOperationException {

    DisposalHoldPluginUtils.addDisposalHoldAIPMetadata(aip, holdId, cachedJob.getUsername());

    if (disposalHold.getFirstTimeUsed() == null) {
      disposalHold.setFirstTimeUsed(new Date());
      model.updateDisposalHoldFirstUseDate(disposalHold, cachedJob.getUsername());
    }

    reportItem.setPluginState(state).addPluginDetails("Disposal hold '" + disposalHold.getTitle() + "' (" + holdId
      + ") was successfully applied to AIP '" + aip.getId() + "'");

    return PluginHelper.createOutcomeTextForDisposalHold("was successfully applied to AIP", holdId,
      disposalHold.getTitle());
  }

  private void applyDisposalTransitiveHolds(ModelService model, IndexService index, Job cachedJob, AIP fromAIP,
    DisposalHold disposalHold, String holdId, JobPluginInfo jobPluginInfo, Report report,
    List<DisposalHoldAIPMetadata> holds) {
    try {
      IterableIndexResult<IndexedAIP> result = DisposalHoldPluginUtils.getTransitivesHoldsAIPs(index, fromAIP.getId());

      for (IndexedAIP indexedAIP : result) {
        String outcomeText;
        PluginState state = PluginState.SUCCESS;
        Report reportItem = PluginHelper.initPluginReportItem(this, indexedAIP.getId(), AIP.class);
        jobPluginInfo.incrementObjectsCount();
        PluginHelper.updatePartialJobReport(this, model, reportItem, false, cachedJob);
        LOGGER.debug("Processing children AIP {}", indexedAIP.getId());
        try {
          AIP transitiveAIP = model.retrieveAIP(indexedAIP.getId());
          List<String> holdsIdList = new ArrayList<>();

          DisposalHoldPluginUtils.addTransitiveDisposalHoldAIPMetadata(transitiveAIP, holdId, fromAIP.getId());

          if (override) {
            for (DisposalHoldAIPMetadata hold : holds) {
              if (transitiveAIP.removeTransitiveHold(hold.getId())) {
                String outcomeLiftText = "Transitive disposal holds " + holdsIdList.toString()
                  + " are disassociated from AIP " + transitiveAIP.getId();
                model.createEvent(transitiveAIP.getId(), null, null, null, POLICY_ASSIGNMENT,
                  LiftDisposalHoldPlugin.getStaticName(), null, null, state, outcomeLiftText, "",
                  cachedJob.getUsername(), true, null);
              }
            }
          }
          model.updateAIP(transitiveAIP, cachedJob.getUsername());
          outcomeText = PluginHelper.createOutcomeTextForDisposalHold("Transitive applied with success",
            disposalHold.getId(), disposalHold.getTitle());
          reportItem.setPluginState(state).addPluginDetails(outcomeText);
          jobPluginInfo.incrementObjectsProcessedWithSuccess();
        } catch (NotFoundException | AuthorizationDeniedException e) {
          LOGGER.debug("Can't associate disposal hold to child. It wasn't found.", e);
          jobPluginInfo.incrementObjectsProcessedWithFailure();
          reportItem.setPluginState(PluginState.FAILURE)
            .setPluginDetails("Error applying disposal schedule " + indexedAIP.getId() + ": " + e.getMessage());
          outcomeText = PluginHelper.createOutcomeTextForDisposalHold(" failed to be applied to AIP",
            disposalHold.getId(), disposalHold.getTitle());
        } finally {
          report.addReport(reportItem);
          PluginHelper.updatePartialJobReport(this, model, reportItem, true, cachedJob);
        }

        try {
          PluginHelper.createPluginEvent(this, indexedAIP.getId(), model, index, null, null,
            reportItem.getPluginState(), outcomeText, true, cachedJob);
        } catch (ValidationException | RequestNotValidException | NotFoundException | GenericException
          | AuthorizationDeniedException | AlreadyExistsException e) {
          LOGGER.error("Error creating event: {}", e.getMessage(), e);
        }
      }
    } catch (NotFoundException | GenericException | RequestNotValidException e) {
      LOGGER.error("Error getting transitives AIPs when associate to a transitive disposal hold", e);
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

  @Override
  public List<Class<AIP>> getObjectClasses() {
    return Collections.singletonList(AIP.class);
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
    return new ApplyDisposalHoldToAIPPlugin();
  }

  @Override
  public boolean areParameterValuesValid() {
    return true;
  }
}
