package org.roda.core.plugins.plugins.internal.disposal.hold;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.IllegalOperationException;
import org.roda.core.data.exceptions.InvalidParameterException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.LiteOptionalWithCause;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.filter.FilterParameter;
import org.roda.core.data.v2.index.filter.OneOfManyFilterParameter;
import org.roda.core.data.v2.index.filter.OrFiltersParameters;
import org.roda.core.data.v2.index.filter.SimpleFilterParameter;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.disposal.DisposalHold;
import org.roda.core.data.v2.ip.disposal.DisposalHoldAssociation;
import org.roda.core.data.v2.ip.disposal.DisposalHoldState;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.PluginParameter;
import org.roda.core.data.v2.jobs.PluginState;
import org.roda.core.data.v2.jobs.PluginType;
import org.roda.core.data.v2.jobs.Report;
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
public class LiftDisposalHoldFromAIPPlugin extends AbstractPlugin<AIP> {
  private static final Logger LOGGER = LoggerFactory.getLogger(LiftDisposalHoldFromAIPPlugin.class);

  private String disposalHoldId;
  private boolean clearAll;
  private boolean recursive;

  private static final Map<String, PluginParameter> pluginParameters = new HashMap<>();

  static {
    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_DISPOSAL_HOLD_ID,
      new PluginParameter(RodaConstants.PLUGIN_PARAMS_DISPOSAL_HOLD_ID, "Disposal hold id",
        PluginParameter.PluginParameterType.STRING, "", true, false, "Disposal hold identifier"));

    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_RECURSIVE,
      new PluginParameter(RodaConstants.PLUGIN_PARAMS_RECURSIVE, "Recursive mode",
        PluginParameter.PluginParameterType.BOOLEAN, "true", true, false, "Execute in recursive mode."));

    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_DISPOSAL_HOLD_LIFT_ALL,
      new PluginParameter(RodaConstants.PLUGIN_PARAMS_DISPOSAL_HOLD_LIFT_ALL, "Lift all holds",
        PluginParameter.PluginParameterType.BOOLEAN, "false", true, false,
        "Lift all disposal holds associated to AIP"));
  }

  @Override
  public List<PluginParameter> getParameters() {
    ArrayList<PluginParameter> parameters = new ArrayList<>();
    parameters.add(pluginParameters.get(RodaConstants.PLUGIN_PARAMS_DISPOSAL_HOLD_ID));
    parameters.add(pluginParameters.get(RodaConstants.PLUGIN_PARAMS_RECURSIVE));
    parameters.add(pluginParameters.get(RodaConstants.PLUGIN_PARAMS_DISPOSAL_HOLD_LIFT_ALL));
    return parameters;
  }

  @Override
  public void setParameterValues(Map<String, String> parameters) throws InvalidParameterException {
    super.setParameterValues(parameters);

    if (parameters.containsKey(RodaConstants.PLUGIN_PARAMS_DISPOSAL_HOLD_ID)) {
      disposalHoldId = parameters.get(RodaConstants.PLUGIN_PARAMS_DISPOSAL_HOLD_ID);
    }

    if (parameters.containsKey(RodaConstants.PLUGIN_PARAMS_RECURSIVE)) {
      recursive = Boolean.parseBoolean(parameters.get(RodaConstants.PLUGIN_PARAMS_RECURSIVE));
    }

    if (parameters.containsKey(RodaConstants.PLUGIN_PARAMS_DISPOSAL_HOLD_LIFT_ALL)) {
      clearAll = Boolean.parseBoolean(parameters.get(RodaConstants.PLUGIN_PARAMS_DISPOSAL_HOLD_LIFT_ALL));
    }
  }

  @Override
  public String getVersionImpl() {
    return "1.0";
  }

  public static String getStaticName() {
    return "Lift disposal hold from AIP";
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
    return "Lift disposal hold from AIP";
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
    if (recursive) {
      calculateResourcesCounter(index, jobPluginInfo, aips);
    }

    for (AIP aip : aips) {
      Report reportItem = PluginHelper.initPluginReportItem(this, aip.getId(), AIP.class);
      PluginHelper.updatePartialJobReport(this, model, reportItem, false, cachedJob);

      PluginState state = PluginState.SUCCESS;
      LOGGER.debug("Processing AIP {}", aip.getId());

      if (clearAll) {
        try {
          // lift disposal holds
          DisposalHoldPluginUtils.liftAllDisposalHoldsFromAIP(model, state, aip, cachedJob, reportItem);
          liftDisposalTransitiveHold(model, index, cachedJob, aip, jobPluginInfo, report,
            aip.getTransitiveDisposalHoldAssociation());
          model.updateAIP(aip, cachedJob.getUsername());

          jobPluginInfo.incrementObjectsProcessedWithSuccess();
          reportItem.setPluginState(state);
        } catch (GenericException | NotFoundException | RequestNotValidException | AuthorizationDeniedException e) {
          LOGGER.error("Error lifting all disposal holds from '{}': {}", aip.getId(), e.getMessage(), e);
          state = PluginState.FAILURE;
          jobPluginInfo.incrementObjectsProcessedWithFailure();
          reportItem.setPluginState(state)
            .setPluginDetails("Error lifting all disposal holds from AIP '" + aip.getId() + "': " + e.getMessage());
        }
      } else {
        try {
          DisposalHoldPluginUtils.liftDisposalHoldFromAIP(model, state, aip, disposalHoldId, cachedJob, reportItem);
          model.updateAIP(aip, cachedJob.getUsername());
          DisposalHoldAssociation transitiveDisposalHoldAssociationByID = aip
            .getDisposalHoldAssociationByID(disposalHoldId);
          liftDisposalTransitiveHold(model, index, cachedJob, aip, jobPluginInfo, report,
            Arrays.asList(transitiveDisposalHoldAssociationByID));
          jobPluginInfo.incrementObjectsProcessedWithSuccess();
          reportItem.setPluginState(state);
        } catch (GenericException | NotFoundException | RequestNotValidException | AuthorizationDeniedException e) {
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

      // if (recursive) {
      // processChildren(model, index, cachedJob, aip, jobPluginInfo, report);
      // }
    }

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
  }

  private void liftDisposalTransitiveHold(ModelService model, IndexService index, Job cachedJob, AIP aip,
    JobPluginInfo jobPluginInfo, Report report, List<DisposalHoldAssociation> transitiveDisposalHoldAssociationList) {
    try {
      final IndexedAIP indexedAip = index.retrieve(IndexedAIP.class, aip.getId(), new ArrayList<>());
      List<FilterParameter> ancestorsList = new ArrayList<>();
      for (String ancestor : indexedAip.getAncestors()) {
        ancestorsList.add(new SimpleFilterParameter(RodaConstants.INDEX_UUID, ancestor));
      }
      ancestorsList.add(new SimpleFilterParameter(RodaConstants.AIP_ANCESTORS, aip.getId()));
      Filter ancestorsFilter = new Filter(new OrFiltersParameters(ancestorsList));

      try {
        IterableIndexResult<IndexedAIP> result = index.findAll(IndexedAIP.class, ancestorsFilter, false,
          Arrays.asList(RodaConstants.INDEX_UUID, RodaConstants.AIP_ID));
        for (IndexedAIP indexedAIP : result) {
          String outcomeText;
          Report reportItem = PluginHelper.initPluginReportItem(this, indexedAIP.getId(), AIP.class);
          PluginHelper.updatePartialJobReport(this, model, reportItem, false, cachedJob);

          PluginState state = PluginState.SUCCESS;

          try {
            AIP aipChildren = model.retrieveAIP(indexedAIP.getId());
            LOGGER.debug("Processing transitive AIP {}", aip.getId());
            try {
              DisposalHoldPluginUtils.liftTransitiveDisposalHoldFromAIP(model, state, aipChildren, disposalHoldId,
                cachedJob, reportItem, transitiveDisposalHoldAssociationList);
              model.updateAIP(aipChildren, cachedJob.getUsername());

              jobPluginInfo.incrementObjectsProcessedWithSuccess();
              reportItem.setPluginState(state);
            } catch (GenericException | NotFoundException | RequestNotValidException | AuthorizationDeniedException e) {
              LOGGER.error("Error lifting disposal hold '{}' from '{}': {}", disposalHoldId, aipChildren.getId(),
                e.getMessage(), e);
              state = PluginState.FAILURE;
              jobPluginInfo.incrementObjectsProcessedWithFailure();
              reportItem.setPluginState(state).setPluginDetails("Error lifting disposal hold '" + disposalHoldId
                + "' from AIP '" + aipChildren.getId() + "': " + e.getMessage());
            }

            report.addReport(reportItem);
            PluginHelper.updatePartialJobReport(this, model, reportItem, true, cachedJob);
          } catch (NotFoundException | AuthorizationDeniedException e) {
            LOGGER.debug("Can't disassociate disposal hold It wasn't found.", e);
            jobPluginInfo.incrementObjectsProcessedWithFailure();
            reportItem.setPluginState(PluginState.FAILURE).setPluginDetails(
              "Error disassociate disposal hold on aip " + indexedAIP.getId() + ": " + e.getMessage());
          }
        }
      } catch (RequestNotValidException e) {
        e.printStackTrace();
      }
    } catch (NotFoundException | GenericException e) {
      e.printStackTrace();
    }
  }

  private void calculateResourcesCounter(IndexService index, JobPluginInfo jobPluginInfo, List<AIP> aips) {
    try {
      ArrayList<String> ancestorList = aips.stream().map(AIP::getId).collect(Collectors.toCollection(ArrayList::new));
      Filter ancestorFilter = new Filter(new OneOfManyFilterParameter(RodaConstants.AIP_ANCESTORS, ancestorList));
      int resourceCounter = index.count(IndexedAIP.class, ancestorFilter).intValue();
      jobPluginInfo.setSourceObjectsCount(resourceCounter + aips.size());
    } catch (GenericException | RequestNotValidException e) {
      // do nothing
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
    return new LiftDisposalHoldFromAIPPlugin();
  }

  @Override
  public boolean areParameterValuesValid() {
    return true;
  }
}
