/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.base.disposal.hold;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.AlreadyExistsException;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.IllegalOperationException;
import org.roda.core.data.exceptions.InvalidParameterException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.LiteOptionalWithCause;
import org.roda.core.data.v2.Void;
import org.roda.core.data.v2.common.Pair;
import org.roda.core.data.v2.disposal.hold.DisposalHold;
import org.roda.core.data.v2.disposal.hold.DisposalHoldState;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.filter.SimpleFilterParameter;
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
import org.roda.core.plugins.RODAProcessingLogic;
import org.roda.core.plugins.orchestrate.JobPluginInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class LiftDisposalHoldPlugin extends AbstractPlugin<Void> {
  private static final Logger LOGGER = LoggerFactory.getLogger(LiftDisposalHoldPlugin.class);

  private String disposalHoldId;
  private String details;

  private static final Map<String, PluginParameter> pluginParameters = new HashMap<>();

  static {
    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_DISPOSAL_HOLD_ID,
      PluginParameter
        .getBuilder(RodaConstants.PLUGIN_PARAMS_DISPOSAL_HOLD_ID, "Disposal hold id",
          PluginParameter.PluginParameterType.STRING)
        .isMandatory(true).isReadOnly(false).withDescription("Disposal hold identifier").build());

    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_DETAILS,
      PluginParameter
        .getBuilder(RodaConstants.PLUGIN_PARAMS_DETAILS, "Details", PluginParameter.PluginParameterType.STRING)
        .isMandatory(false).withDescription("Details that will be used when creating event").build());
  }

  @Override
  public List<PluginParameter> getParameters() {
    ArrayList<PluginParameter> parameters = new ArrayList<>();
    parameters.add(pluginParameters.get(RodaConstants.PLUGIN_PARAMS_DISPOSAL_HOLD_ID));
    parameters.add(pluginParameters.get(RodaConstants.PLUGIN_PARAMS_DETAILS));
    return parameters;
  }

  @Override
  public void setParameterValues(Map<String, String> parameters) throws InvalidParameterException {
    super.setParameterValues(parameters);

    if (parameters.containsKey(RodaConstants.PLUGIN_PARAMS_DISPOSAL_HOLD_ID)) {
      disposalHoldId = parameters.get(RodaConstants.PLUGIN_PARAMS_DISPOSAL_HOLD_ID);
    }

    if (parameters.containsKey(RodaConstants.PLUGIN_PARAMS_DETAILS)) {
      details = parameters.get(RodaConstants.PLUGIN_PARAMS_DETAILS);
    }
  }

  @Override
  public String getVersionImpl() {
    return "1.0";
  }

  public static String getStaticName() {
    return "Lift disposal hold";
  }

  @Override
  public String getName() {
    return getStaticName();
  }

  public static String getStaticDescription() {
    return "";
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
    return "Lift disposal hold";
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
    return new Report();
  }

  @Override
  public Report execute(IndexService index, ModelService model, List<LiteOptionalWithCause> liteList)
    throws PluginException {
    return PluginHelper.processVoids(this, new RODAProcessingLogic<Void>() {
      @Override
      public void process(IndexService index, ModelService model, Report report, Job cachedJob,
        JobPluginInfo jobPluginInfo, Plugin<Void> plugin) {
        liftDisposalHold(index, model, report, cachedJob, jobPluginInfo);
      }
    }, index, model);
  }

  private void liftDisposalHold(IndexService index, ModelService model, Report report, Job cachedJob,
    JobPluginInfo jobPluginInfo) {
    report.addPluginDetails(details);
    int count = 0;

    try (IterableIndexResult<IndexedAIP> aipsToDelete = findAipsWithDisposalHold(index, disposalHoldId)) {
      for (IndexedAIP indexedAIP : aipsToDelete) {
        String outcomeText;
        PluginState state = PluginState.SUCCESS;
        Report reportItem = PluginHelper.initPluginReportItem(this, indexedAIP.getId(), AIP.class);
        PluginHelper.updatePartialJobReport(this, model, reportItem, false, cachedJob);
        LOGGER.debug("Processing AIP {}", indexedAIP.getId());

        try {
          AIP aip = model.retrieveAIP(indexedAIP.getId());
          Pair<Boolean, String> outcome = DisposalHoldPluginUtils.disassociateDisposalHoldFromAIP(disposalHoldId, aip,
            reportItem);
          boolean lifted = outcome.getFirst();
          outcomeText = outcome.getSecond();
          processTransitiveAIP(model, index, cachedJob, aip.getId(), disposalHoldId, jobPluginInfo, report);
          model.updateAIP(aip, cachedJob.getUsername());
          if (lifted) {
            jobPluginInfo.incrementObjectsProcessedWithSuccess();
          } else {
            jobPluginInfo.incrementObjectsProcessedWithSkipped();
          }
          reportItem.setPluginState(state);
        } catch (GenericException | NotFoundException | RequestNotValidException | AuthorizationDeniedException e) {
          outcomeText = "Error lifting disposal hold" + disposalHoldId + " from AIP " + indexedAIP.getId();
          LOGGER.error("Error lifting disposal hold '{}' from '{}': {}", disposalHoldId, indexedAIP.getId(),
            e.getMessage(), e);
          state = PluginState.FAILURE;
          jobPluginInfo.incrementObjectsProcessedWithFailure();
          reportItem.setPluginState(state).setPluginDetails("Error lifting disposal hold '" + disposalHoldId
            + "' from AIP '" + indexedAIP.getId() + "': " + e.getMessage());
        }

        report.addReport(reportItem);
        PluginHelper.updatePartialJobReport(this, model, reportItem, true, cachedJob);

        try {
          PluginHelper.createPluginEvent(this, indexedAIP.getId(), model, index, null, null,
            reportItem.getPluginState(), outcomeText, true, cachedJob);
        } catch (ValidationException | RequestNotValidException | NotFoundException | GenericException
          | AuthorizationDeniedException | AlreadyExistsException e) {
          LOGGER.error("Error creating event: {}", e.getMessage(), e);
        }

        count++;
      }
      jobPluginInfo.setSourceObjectsCount(count);

      DisposalHold disposalHold = model.retrieveDisposalHold(disposalHoldId);
      disposalHold.setState(DisposalHoldState.LIFTED);
      disposalHold.setLiftedBy(cachedJob.getUsername());
      disposalHold.setLiftedOn(new Date());
      model.updateDisposalHold(disposalHold, cachedJob.getUsername(), details);

    } catch (IOException | GenericException | RequestNotValidException | NotFoundException
      | AuthorizationDeniedException | IllegalOperationException e) {
      LOGGER.error("Error getting AIPs to delete", e);
    }
  }

  private IterableIndexResult<IndexedAIP> findAipsWithDisposalHold(IndexService index, String disposalHoldId)
    throws GenericException, RequestNotValidException {
    Filter aipsFilter = new Filter();
    aipsFilter.add(new SimpleFilterParameter(RodaConstants.AIP_DISPOSAL_HOLDS_ID, disposalHoldId));
    return index.findAll(IndexedAIP.class, aipsFilter, false, List.of(RodaConstants.INDEX_UUID));
  }

  private void processTransitiveAIP(ModelService model, IndexService index, Job cachedJob, String aipId, String holdId,
    JobPluginInfo jobPluginInfo, Report report) throws GenericException, NotFoundException, RequestNotValidException {
    IterableIndexResult<IndexedAIP> results = DisposalHoldPluginUtils.getTransitivesHoldsAIPs(index, aipId);

    for (IndexedAIP indexedAIP : results) {
      String outcomeText;
      PluginState state = PluginState.SUCCESS;
      Report reportItem = PluginHelper.initPluginReportItem(this, indexedAIP.getId(), AIP.class);
      PluginHelper.updatePartialJobReport(this, model, reportItem, false, cachedJob);
      jobPluginInfo.incrementObjectsCount();

      try {
        AIP aipChildren = model.retrieveAIP(indexedAIP.getId());
        LOGGER.debug("Processing transitive AIP {}", aipId);
        outcomeText = DisposalHoldPluginUtils.disassociateTransitiveDisposalHoldFromAIP(disposalHoldId, aipChildren, reportItem);
        model.updateAIP(aipChildren, cachedJob.getUsername());
        reportItem.setPluginState(state)
          .addPluginDetails("transitive Disposal hold '" + holdId + " was successfully lifting to AIP '" + aipId + "'");
        jobPluginInfo.incrementObjectsProcessedWithSuccess();

      } catch (AuthorizationDeniedException e) {
        state = PluginState.FAILURE;
        outcomeText = "Can't retrieve AIP " + aipId + " for lifting transitive hold " + holdId + ".";
        LOGGER.debug(outcomeText, e);
        jobPluginInfo.incrementObjectsProcessedWithFailure();
        reportItem.setPluginState(state).setPluginDetails(
          "Error lifting transitive disposal hold " + holdId + " on aip " + indexedAIP.getId() + ": " + e.getMessage());
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

  @Override
  public List<Class<Void>> getObjectClasses() {
    return Collections.singletonList(Void.class);
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
  public Plugin<Void> cloneMe() {
    return new LiftDisposalHoldPlugin();
  }

  @Override
  public boolean areParameterValuesValid() {
    return true;
  }
}
