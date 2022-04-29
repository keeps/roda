package org.roda.core.plugins.base.synchronization.instanceIdentifier;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.InvalidParameterException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.IsRODAObject;
import org.roda.core.data.v2.LiteOptionalWithCause;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.DIP;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.PluginParameter;
import org.roda.core.data.v2.jobs.PluginState;
import org.roda.core.data.v2.jobs.PluginType;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.data.v2.notifications.Notification;
import org.roda.core.data.v2.ri.RepresentationInformation;
import org.roda.core.data.v2.risks.IndexedRisk;
import org.roda.core.data.v2.risks.Risk;
import org.roda.core.data.v2.risks.RiskIncidence;
import org.roda.core.index.IndexService;
import org.roda.core.model.ModelService;
import org.roda.core.plugins.AbstractPlugin;
import org.roda.core.plugins.Plugin;
import org.roda.core.plugins.PluginException;
import org.roda.core.plugins.RODAObjectsProcessingLogic;
import org.roda.core.plugins.orchestrate.JobPluginInfo;
import org.roda.core.plugins.PluginHelper;
import org.roda.core.storage.StorageService;
import org.roda.core.storage.utils.RODAInstanceUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Tiago Fraga <tfraga@keep.pt>
 */

public abstract class InstanceIdentifierRodaEntityPlugin<T extends IsRODAObject> extends AbstractPlugin<T> {
  private static final Logger LOGGER = LoggerFactory.getLogger(InstanceIdentifierRodaEntityPlugin.class);
  private static Map<String, PluginParameter> pluginParameters = new HashMap<>();

  static {
    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_INSTANCE_IDENTIFIER,
      new PluginParameter(RodaConstants.PLUGIN_PARAMS_INSTANCE_IDENTIFIER, "Instance Identifier",
        PluginParameter.PluginParameterType.STRING, RODAInstanceUtils.retrieveLocalInstanceIdentifierToPlugin(), true,
        true, "Identifier from the RODA local instance"));
  }

  private String instanceId;

  @Override
  public void init() throws PluginException {
    // do nothing
  }

  @Override
  public void shutdown() {
    // do nothing
  }

  @Override
  public abstract String getName();

  @Override
  public String getDescription() {
    return "Add the instance identifier on the data that exists on the storage as also on the index. "
      + "If an object already has an instance identifier it will be updated by the new one. "
      + "This task aims to help the synchronization between a RODA central instance and the RODA local instance, since when an local object is accessed in RODA Central it should have the instance identifier in order to inform from which source is it from.";
  }

  @Override
  public abstract String getVersionImpl();

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
    return RodaConstants.PreservationEventType.NONE;
  }

  @Override
  public String getPreservationEventDescription() {
    return null;
  }

  @Override
  public String getPreservationEventSuccessMessage() {
    return null;
  }

  @Override
  public String getPreservationEventFailureMessage() {
    return null;
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
  public abstract Plugin<T> cloneMe();

  @Override
  public boolean areParameterValuesValid() {
    return true;
  }

  @Override
  public List<Class<T>> getObjectClasses() {
    return null;
  }

  @Override
  public Report beforeAllExecute(IndexService index, ModelService model, StorageService storage)
    throws PluginException {
    return new Report();
  }

  @Override
  public Report execute(IndexService index, ModelService model, StorageService storage,
    List<LiteOptionalWithCause> liteList) throws PluginException {
    return PluginHelper.processObjects(this, new RODAObjectsProcessingLogic<T>() {
      @Override
      public void process(IndexService index, ModelService model, StorageService storage, Report report, Job cachedJob,
        JobPluginInfo jobPluginInfo, Plugin<T> plugin, List<T> objects) {
        modifyInstanceId(index, model, report, jobPluginInfo, cachedJob, objects);
      }
    }, index, model, storage, liteList);
  }

  private void modifyInstanceId(IndexService index, ModelService model, Report pluginReport,
    JobPluginInfo jobPluginInfo, Job cachedJob, List<T> list) {
    pluginReport.setPluginState(PluginState.SUCCESS);

    for (T object : list) {
      if (LOGGER.isTraceEnabled()) {
        LOGGER.trace("Modify instance identifier to {} {}", object.getClass().getSimpleName(), object.getId());
      }
      Report reportItem = PluginHelper.initPluginReportItem(this, object.getId(), object.getClass());

      Class<T> objectClass = (Class<T>) object.getClass();
      if (AIP.class.equals(objectClass) || IndexedAIP.class.equals(objectClass)) {
        try {
          model.updateAIPInstanceId(AIP.class.cast(object));
          jobPluginInfo.incrementObjectsProcessedWithSuccess();
        } catch (GenericException | NotFoundException | RequestNotValidException | AuthorizationDeniedException e) {
          jobPluginInfo.incrementObjectsProcessedWithFailure();
          reportItem.addPluginDetails(e.getMessage() + "\n");

          pluginReport.addReport(reportItem.setPluginState(PluginState.FAILURE));
          PluginHelper.updatePartialJobReport(this, model, reportItem, true, cachedJob);
        }
      } else if (DIP.class.equals(objectClass)) {
        try {
          model.updateDIPInstanceId(DIP.class.cast(object));
          jobPluginInfo.incrementObjectsProcessedWithSuccess();
        } catch (GenericException | NotFoundException | RequestNotValidException | AuthorizationDeniedException e) {
          jobPluginInfo.incrementObjectsProcessedWithFailure();
          reportItem.addPluginDetails(e.getMessage() + "\n");

          pluginReport.addReport(reportItem.setPluginState(PluginState.FAILURE));
          PluginHelper.updatePartialJobReport(this, model, reportItem, true, cachedJob);
        }
      } else if (RepresentationInformation.class.equals(objectClass)) {
        try {
          model.updateRepresentationInformationInstanceId(RepresentationInformation.class.cast(object),
            cachedJob.getUsername(), true);
          jobPluginInfo.incrementObjectsProcessedWithSuccess();
        } catch (GenericException | NotFoundException | RequestNotValidException | AuthorizationDeniedException e) {
          jobPluginInfo.incrementObjectsProcessedWithFailure();
          reportItem.addPluginDetails(e.getMessage() + "\n");

          pluginReport.addReport(reportItem.setPluginState(PluginState.FAILURE));
          PluginHelper.updatePartialJobReport(this, model, reportItem, true, cachedJob);
        }
      } else if (Notification.class.equals(objectClass)) {
        try {
          model.updateNotificationInstanceId(Notification.class.cast(object));
          jobPluginInfo.incrementObjectsProcessedWithSuccess();
        } catch (GenericException | NotFoundException | AuthorizationDeniedException e) {
          jobPluginInfo.incrementObjectsProcessedWithFailure();
          reportItem.addPluginDetails(e.getMessage() + "\n");

          pluginReport.addReport(reportItem.setPluginState(PluginState.FAILURE));
          PluginHelper.updatePartialJobReport(this, model, reportItem, true, cachedJob);
        }
      } else if (Risk.class.equals(objectClass) || IndexedRisk.class.equals(objectClass)) {
        try {
          model.updateRiskInstanceId(Risk.class.cast(object), true);
          jobPluginInfo.incrementObjectsProcessedWithSuccess();
        } catch (GenericException | AuthorizationDeniedException e) {
          jobPluginInfo.incrementObjectsProcessedWithFailure();
          reportItem.addPluginDetails(e.getMessage() + "\n");

          pluginReport.addReport(reportItem.setPluginState(PluginState.FAILURE));
          PluginHelper.updatePartialJobReport(this, model, reportItem, true, cachedJob);
        }
      } else if (RiskIncidence.class.equals(objectClass)) {
        try {
          model.updateRiskIncidenceInstanceId(RiskIncidence.class.cast(object), true);
          jobPluginInfo.incrementObjectsProcessedWithSuccess();
        } catch (GenericException | AuthorizationDeniedException e) {
          jobPluginInfo.incrementObjectsProcessedWithFailure();
          reportItem.addPluginDetails(e.getMessage() + "\n");

          pluginReport.addReport(reportItem.setPluginState(PluginState.FAILURE));
          PluginHelper.updatePartialJobReport(this, model, reportItem, true, cachedJob);
        }
      } else if (Job.class.equals(objectClass)) {
        try {
          model.updateJobInstanceId(Job.class.cast(object));
          jobPluginInfo.incrementObjectsProcessedWithSuccess();
        } catch (GenericException | NotFoundException | RequestNotValidException | AuthorizationDeniedException e) {
          jobPluginInfo.incrementObjectsProcessedWithFailure();
          reportItem.addPluginDetails(e.getMessage() + "\n");

          pluginReport.addReport(reportItem.setPluginState(PluginState.FAILURE));
          PluginHelper.updatePartialJobReport(this, model, reportItem, true, cachedJob);
        }

      } else {
        LOGGER.error("Error trying to modifying instance identifier to an unconfigured object class: {}",
          objectClass.getName());

        jobPluginInfo.incrementObjectsProcessedWithFailure();
        reportItem.addPluginDetails("Error trying to modifying instance identifier to an unconfigured object class: "
          + objectClass.getName() + "\n");

        pluginReport.addReport(reportItem.setPluginState(PluginState.FAILURE));
        PluginHelper.updatePartialJobReport(this, model, reportItem, true, cachedJob);
      }
    }
  }

  @Override
  public Report afterAllExecute(IndexService index, ModelService model, StorageService storage) throws PluginException {
    return new Report();
  }

}
