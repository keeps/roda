/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.base.synchronization.instanceIdentifier;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.roda.core.common.PremisV3Utils;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.AlreadyExistsException;
import org.roda.core.data.exceptions.AlreadyHasInstanceIdentifier;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.InstanceIdNotUpdated;
import org.roda.core.data.exceptions.InvalidParameterException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.LiteOptionalWithCause;
import org.roda.core.data.v2.Void;
import org.roda.core.data.v2.index.filter.EmptyKeyFilterParameter;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.ip.metadata.IndexedPreservationEvent;
import org.roda.core.data.v2.ip.metadata.PreservationMetadata;
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
import org.roda.core.storage.StorageService;
import org.roda.core.storage.utils.RODAInstanceUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Tiago Fraga <tfraga@keep.pt>
 */

public class InstanceIdentifierRepositoryEventPlugin extends AbstractPlugin<Void> {

  private static final Logger LOGGER = LoggerFactory.getLogger(InstanceIdentifierRepositoryEventPlugin.class);
  private static Map<String, PluginParameter> pluginParameters = new HashMap<>();

  static {
    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_INSTANCE_IDENTIFIER,
      PluginParameter
        .getBuilder(RodaConstants.PLUGIN_PARAMS_INSTANCE_IDENTIFIER, "Instance Identifier",
          PluginParameter.PluginParameterType.STRING)
        .withDefaultValue(RODAInstanceUtils.retrieveLocalInstanceIdentifierToPlugin()).isReadOnly(true)
        .withDescription("Identifier from the RODA local instance").build());
  }

  private String instanceId;

  @Override
  public String getVersionImpl() {
    return "1.0";
  }

  public static String getStaticName() {
    return "Instance identifier repository preservation events";
  }

  @Override
  public String getName() {
    return getStaticName();
  }

  public static String getStaticDescription() {
    return "Add the instance identifier on the data that exists on the storage as also on the index. "
      + "If an object already has an instance identifier it will be updated by the new one. "
      + "This task aims to help the synchronization between a RODA central instance and the RODA local instance, "
      + "since when an local object is accessed in RODA Central it should have the instance identifier in order to "
      + "inform from which source is it from.";
  }

  @Override
  public String getDescription() {
    return getStaticDescription();
  }

  @Override
  public List<PluginParameter> getParameters() {
    ArrayList<PluginParameter> parameters = new ArrayList<>();
    parameters.add(pluginParameters.get(RodaConstants.PLUGIN_PARAMS_INSTANCE_IDENTIFIER));
    return parameters;
  }

  @Override
  public void setParameterValues(Map<String, String> parameters) throws InvalidParameterException {
    super.setParameterValues(parameters);
    if (parameters != null && parameters.containsKey(RodaConstants.PLUGIN_PARAMS_INSTANCE_IDENTIFIER)) {
      instanceId = parameters.get(RodaConstants.PLUGIN_PARAMS_INSTANCE_IDENTIFIER);
    }
  }

  @Override
  public RodaConstants.PreservationEventType getPreservationEventType() {
    return RodaConstants.PreservationEventType.UPDATE;
  }

  @Override
  public String getPreservationEventDescription() {
    return "Updated the repository preservation events instance identifier";
  }

  @Override
  public String getPreservationEventSuccessMessage() {
    return "The repository preservation event instance identifier was updated successfully";
  }

  @Override
  public String getPreservationEventFailureMessage() {
    return "Could not update the repository preservation event instance identifier";
  }

  @Override
  public PluginType getType() {
    return PluginType.INTERNAL;
  }

  @Override
  public List<String> getCategories() {
    return Arrays.asList(RodaConstants.PLUGIN_CATEGORY_NOT_LISTABLE);
  }

  @Override
  public Plugin<Void> cloneMe() {
    return new InstanceIdentifierRepositoryEventPlugin();
  }

  @Override
  public boolean areParameterValuesValid() {
    return false;
  }

  @Override
  public void init() throws PluginException {
    // do nothing
  }

  @Override
  public void shutdown() {
    // do nothing
  }

  @Override
  public List<Class<Void>> getObjectClasses() {
    return Arrays.asList(Void.class);
  }

  @Override
  public Report beforeAllExecute(IndexService index, ModelService model, StorageService storage)
    throws PluginException {
    return new Report();
  }

  @Override
  public Report execute(IndexService index, ModelService model, StorageService storage,
    List<LiteOptionalWithCause> list) throws PluginException {
    return PluginHelper.processVoids(this, new RODAProcessingLogic<Void>() {
      @Override
      public void process(IndexService index, ModelService model, StorageService storage, Report report, Job cachedJob,
        JobPluginInfo jobPluginInfo, Plugin<Void> plugin) throws PluginException {
        try {
          modifyInstanceId(model, index, cachedJob, report, jobPluginInfo);
        } catch (RequestNotValidException | GenericException | AuthorizationDeniedException | IOException e) {
          LOGGER.error("Could not modify Instance ID's in objects");
        }
      }
    }, index, model, storage);
  }

  private void modifyInstanceId(ModelService model, IndexService index, Job cachedJob, Report pluginReport,
    JobPluginInfo jobPluginInfo)
    throws RequestNotValidException, GenericException, AuthorizationDeniedException, IOException {
    PluginState pluginState = PluginState.SKIPPED;
    List<String> detailsList = new ArrayList<>();

    int countFail = 0;
    int countSuccess = 0;
    int countSkipped = 0;

    IterableIndexResult<IndexedPreservationEvent> indexedPreservationEvents = retrieveList(index);
    Report reportItem = PluginHelper.initPluginReportItem(this, cachedJob.getId(), Job.class);
    PluginHelper.updatePartialJobReport(this, model, reportItem, false, cachedJob);

    for (IndexedPreservationEvent indexedPreservationEvent : indexedPreservationEvents) {
      try {
        PremisV3Utils.updatePremisEventInstanceId(model.retrievePreservationMetadata(indexedPreservationEvent.getId(),
          PreservationMetadata.PreservationMetadataType.EVENT), model, index, instanceId, cachedJob.getUsername());

        countSuccess++;
      } catch (AuthorizationDeniedException | RequestNotValidException | GenericException | ValidationException
        | AlreadyExistsException | InstanceIdNotUpdated e) {
        detailsList.add(e.getMessage());
        countFail++;
      } catch (AlreadyHasInstanceIdentifier alreadyHasInstanceIdentifier) {
        countSkipped++;
        detailsList.add(alreadyHasInstanceIdentifier.getMessage());
      }
    }

    StringBuilder details = new StringBuilder();
    if (countFail > 0) {
      pluginState = PluginState.FAILURE;
      details.append("Updated the instance identifier on ").append(countSuccess).append(" Skipped ")
        .append(countSkipped).append(" Repository preservation events, ")
        .append("Repository Preservation event and failed to update ").append(countFail).append(".\n")
        .append(LocalInstanceRegisterUtils.getDetailsFromList(detailsList));
    } else if (countSuccess > 0) {
      pluginState = PluginState.SUCCESS;
      details.append("Updated the instance identifier on ").append(countSuccess).append(", Skipped ")
        .append(countSkipped).append(" Repository preservation events.\n ")
        .append(LocalInstanceRegisterUtils.getDetailsFromList(detailsList));
    } else {
      details.append("Skipped ").append(countSkipped).append(" Repository preservation events.\n ")
        .append(LocalInstanceRegisterUtils.getDetailsFromList(detailsList));
    }

    reportItem.setPluginDetails(details.toString());

    jobPluginInfo.incrementObjectsProcessed(pluginState);
    reportItem.setPluginState(pluginState);
    pluginReport.addReport(reportItem);
    PluginHelper.updatePartialJobReport(this, model, reportItem, true, cachedJob);
  }

  @Override
  public Report afterAllExecute(IndexService index, ModelService model, StorageService storage) throws PluginException {
    return new Report();
  }

  private IterableIndexResult<IndexedPreservationEvent> retrieveList(final IndexService index)
    throws RequestNotValidException, GenericException, AuthorizationDeniedException, IOException {
    final Filter filter = new Filter(new EmptyKeyFilterParameter(RodaConstants.PRESERVATION_EVENT_AIP_ID));
    RODAInstanceUtils.addLocalInstanceFilter(filter);
    return index.findAll(IndexedPreservationEvent.class, filter, Collections.singletonList(RodaConstants.INDEX_UUID));
  }
}
