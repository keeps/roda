/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.base.maintenance;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.common.RodaConstants.PreservationEventType;
import org.roda.core.data.exceptions.AlreadyExistsException;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.InvalidParameterException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.LiteOptionalWithCause;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.PluginParameter;
import org.roda.core.data.v2.jobs.PluginParameter.PluginParameterType;
import org.roda.core.data.v2.jobs.PluginState;
import org.roda.core.data.v2.jobs.PluginType;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.data.v2.user.Group;
import org.roda.core.data.v2.user.RODAMember;
import org.roda.core.data.v2.user.User;
import org.roda.core.index.IndexService;
import org.roda.core.model.ModelService;
import org.roda.core.plugins.AbstractPlugin;
import org.roda.core.plugins.Plugin;
import org.roda.core.plugins.PluginException;
import org.roda.core.plugins.PluginHelper;
import org.roda.core.plugins.RODAObjectsProcessingLogic;
import org.roda.core.plugins.orchestrate.JobPluginInfo;
import org.roda.core.storage.StorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ChangeRodaMemberStatusPlugin<T extends RODAMember> extends AbstractPlugin<T> {
  private static final Logger LOGGER = LoggerFactory.getLogger(ChangeRodaMemberStatusPlugin.class);
  private static Map<String, PluginParameter> pluginParameters = new HashMap<>();

  static {
    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_RODA_MEMBER_ACTIVATE,
      PluginParameter
        .getBuilder(RodaConstants.PLUGIN_PARAMS_RODA_MEMBER_ACTIVATE, "Activate", PluginParameterType.BOOLEAN)
        .isMandatory(true).withDescription("Activate RODA members").build());
  }

  private boolean activate;

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
    return "Changes RODA user status";
  }

  @Override
  public String getDescription() {
    return "Activates or deactivates RODA users";
  }

  @Override
  public String getVersionImpl() {
    return "1.0";
  }

  @Override
  public List<PluginParameter> getParameters() {
    ArrayList<PluginParameter> parameters = new ArrayList<>();
    parameters.add(pluginParameters.get(RodaConstants.PLUGIN_PARAMS_RODA_MEMBER_ACTIVATE));
    return parameters;
  }

  @Override
  public void setParameterValues(Map<String, String> parameters) throws InvalidParameterException {
    super.setParameterValues(parameters);
    if (parameters.containsKey(RodaConstants.PLUGIN_PARAMS_RODA_MEMBER_ACTIVATE)) {
      String activateParamValue = parameters.get(RodaConstants.PLUGIN_PARAMS_RODA_MEMBER_ACTIVATE);

      activate = Boolean.parseBoolean(activateParamValue);
    }
  }

  @Override
  public Report execute(IndexService index, ModelService model, StorageService storage,
    List<LiteOptionalWithCause> liteList) throws PluginException {

    return PluginHelper.processObjects(this,
      (RODAObjectsProcessingLogic<T>) (index1, model1, storage1, report, cachedJob, jobPluginInfo, plugin, objects) -> {
        processRodaMember(model1, report, jobPluginInfo, cachedJob, objects);
      }, index, model, storage, liteList);
  }

  private void processRodaMember(ModelService model, Report report, JobPluginInfo jobPluginInfo, Job job,
    List<T> members) {
    for (T member : members) {
      LOGGER.debug("Processing Member {}", member.getId());
      Report reportItem = PluginHelper.initPluginReportItem(this, member.getId(), RODAMember.class);
      PluginHelper.updatePartialJobReport(this, model, reportItem, false, job);
      PluginState state = PluginState.SUCCESS;
      try {
        if (member.isUser()) {
          model.deActivateUser(member.getId(), activate, true);

          if (!activate) {
            model.deactivateUserAccessKeys(member.getId(), job.getUsername());
          }
        } else {
          state = PluginState.SKIPPED;
        }
      } catch (RequestNotValidException | AlreadyExistsException | NotFoundException | GenericException
        | AuthorizationDeniedException e) {
        state = PluginState.FAILURE;
        LOGGER.error("Error processing RODA member {}: {}", member.getId(), e.getMessage(), e);
      } finally {
        jobPluginInfo.incrementObjectsProcessed(state);
        reportItem.setPluginState(state);
        report.addReport(reportItem);
        PluginHelper.updatePartialJobReport(this, model, reportItem, true, job);
      }
    }
  }

  @Override
  public Report beforeAllExecute(IndexService index, ModelService model, StorageService storage)
    throws PluginException {
    return new Report();
  }

  @Override
  public Report afterAllExecute(IndexService index, ModelService model, StorageService storage) throws PluginException {
    return new Report();
  }

  @Override
  public Plugin<T> cloneMe() {
    return new ChangeRodaMemberStatusPlugin<>();
  }

  @Override
  public PluginType getType() {
    return PluginType.INTERNAL;
  }

  @Override
  public boolean areParameterValuesValid() {
    return true;
  }

  @Override
  public PreservationEventType getPreservationEventType() {
    return PreservationEventType.UPDATE;
  }

  @Override
  public String getPreservationEventDescription() {
    return "Changes RODA user status";
  }

  @Override
  public String getPreservationEventSuccessMessage() {
    return "The RODA user status were successfully updated";
  }

  @Override
  public String getPreservationEventFailureMessage() {
    return "The RODA user status were not successfully updated";
  }

  @Override
  public List<String> getCategories() {
    return Collections.singletonList(RodaConstants.PLUGIN_CATEGORY_NOT_LISTABLE);
  }

  @Override
  public List<Class<T>> getObjectClasses() {
    List<Class<? extends RODAMember>> list = new ArrayList<>();
    list.add(User.class);
    list.add(Group.class);
    return (List) list;
  }
}
