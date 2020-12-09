package org.roda.core.plugins.plugins.internal.disposal.hold;

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
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.disposal.DisposalHold;
import org.roda.core.data.v2.ip.disposal.DisposalHoldState;
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
import org.roda.core.plugins.RODAObjectsProcessingLogic;
import org.roda.core.plugins.orchestrate.JobPluginInfo;
import org.roda.core.plugins.plugins.PluginHelper;
import org.roda.core.storage.StorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class LiftDisposalHoldPlugin extends AbstractPlugin<AIP> {
  private static final Logger LOGGER = LoggerFactory.getLogger(LiftDisposalHoldPlugin.class);

  private String disposalHoldId;

  private static final Map<String, PluginParameter> pluginParameters = new HashMap<>();

  static {
    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_DISPOSAL_HOLD_ID,
      new PluginParameter(RodaConstants.PLUGIN_PARAMS_DISPOSAL_HOLD_ID, "Disposal hold id",
        PluginParameter.PluginParameterType.STRING, "", true, false, "Disposal hold identifier"));
  }

  @Override
  public List<PluginParameter> getParameters() {
    ArrayList<PluginParameter> parameters = new ArrayList<>();
    parameters.add(pluginParameters.get(RodaConstants.PLUGIN_PARAMS_DISPOSAL_HOLD_ID));
    return parameters;
  }

  @Override
  public void setParameterValues(Map<String, String> parameters) throws InvalidParameterException {
    super.setParameterValues(parameters);

    if (parameters.containsKey(RodaConstants.PLUGIN_PARAMS_DISPOSAL_HOLD_ID)) {
      disposalHoldId = parameters.get(RodaConstants.PLUGIN_PARAMS_DISPOSAL_HOLD_ID);
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
  public Report beforeAllExecute(IndexService index, ModelService model, StorageService storage)
    throws PluginException {
    return new Report();
  }

  @Override
  public Report execute(IndexService index, ModelService model, StorageService storage,
    List<LiteOptionalWithCause> liteList) throws PluginException {
    return PluginHelper.processObjects(this, new RODAObjectsProcessingLogic<AIP>() {
      @Override
      public void process(IndexService index, ModelService model, StorageService storage, Report report, Job cachedJob,
        JobPluginInfo jobPluginInfo, Plugin<AIP> plugin, List<AIP> objects) {
        processAIP(index, model, report, cachedJob, jobPluginInfo, objects);
      }
    }, index, model, storage, liteList);
  }

  private void processAIP(IndexService index, ModelService model, Report report, Job cachedJob,
    JobPluginInfo jobPluginInfo, List<AIP> aips) {

    for (AIP aip : aips) {
      String outcomeText;
      PluginState state = PluginState.SUCCESS;
      Report reportItem = PluginHelper.initPluginReportItem(this, aip.getId(), AIP.class);
      PluginHelper.updatePartialJobReport(this, model, reportItem, false, cachedJob);
      LOGGER.debug("Processing AIP {}", aip.getId());

      try {
        if (StringUtils.isNotBlank(disposalHoldId)) {
          try {
            DisposalHold disposalHold = model.retrieveDisposalHold(disposalHoldId);
            disposalHold.setState(DisposalHoldState.LIFTED);
            model.updateDisposalHold(disposalHold, cachedJob.getUsername());
          } catch (RequestNotValidException | NotFoundException | GenericException | AuthorizationDeniedException
            | IllegalOperationException e) {
            LOGGER.error("Unable to update disposal hold {}: {}", disposalHoldId, e.getMessage(), e);
          }
        }
        outcomeText = DisposalHoldPluginUtils.liftDisposalHoldFromAIP(aip, disposalHoldId, reportItem);
        processTransitiveAIP(model, index, cachedJob, aip, disposalHoldId, jobPluginInfo, report);
        model.updateAIP(aip, cachedJob.getUsername());
        jobPluginInfo.incrementObjectsProcessedWithSuccess();
        reportItem.setPluginState(state);
      } catch (GenericException | NotFoundException | RequestNotValidException | AuthorizationDeniedException e) {
        outcomeText = "Error lifting disposal hold" + disposalHoldId + " from AIP " + aip.getId();
        LOGGER.error("Error lifting disposal hold '{}' from '{}': {}", disposalHoldId, aip.getId(), e.getMessage(), e);
        state = PluginState.FAILURE;
        jobPluginInfo.incrementObjectsProcessedWithFailure();
        reportItem.setPluginState(state).setPluginDetails(
          "Error lifting disposal hold '" + disposalHoldId + "' from AIP '" + aip.getId() + "': " + e.getMessage());
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
        outcomeText = DisposalHoldPluginUtils.liftTransitiveDisposalHoldFromAIP(aipChildren, holdId, reportItem);
        model.updateAIP(aipChildren, cachedJob.getUsername());
        reportItem.setPluginState(state).addPluginDetails(
          "transitive Disposal hold '" + holdId + " was successfully lifting to AIP '" + aip.getId() + "'");
        jobPluginInfo.incrementObjectsProcessedWithSuccess();

      } catch (AuthorizationDeniedException e) {
        state = PluginState.FAILURE;
        outcomeText = "Can't retrieve AIP " + aip.getId() + " for lifting transitive hold " + holdId + ".";
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
  public Report afterAllExecute(IndexService index, ModelService model, StorageService storage) throws PluginException {
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
    return new LiftDisposalHoldPlugin();
  }

  @Override
  public boolean areParameterValuesValid() {
    return true;
  }
}
