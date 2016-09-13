/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.plugins.base;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.roda.core.RodaCoreFactory;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.common.RodaConstants.PreservationEventType;
import org.roda.core.data.exceptions.InvalidParameterException;
import org.roda.core.data.exceptions.JobException;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.v2.IsRODAObject;
import org.roda.core.data.v2.Void;
import org.roda.core.data.v2.index.SelectedItemsAll;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.TransferredResource;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.PluginParameter;
import org.roda.core.data.v2.jobs.PluginParameter.PluginParameterType;
import org.roda.core.data.v2.jobs.PluginType;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.data.v2.jobs.Report.PluginState;
import org.roda.core.data.v2.log.LogEntry;
import org.roda.core.index.IndexService;
import org.roda.core.model.ModelService;
import org.roda.core.plugins.AbstractPlugin;
import org.roda.core.plugins.Plugin;
import org.roda.core.plugins.PluginException;
import org.roda.core.plugins.orchestrate.SimpleJobPluginInfo;
import org.roda.core.plugins.plugins.PluginHelper;
import org.roda.core.storage.StorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReindexAllRodaEntitiesPlugin extends AbstractPlugin<Void> {
  private static final Logger LOGGER = LoggerFactory.getLogger(ReindexAllRodaEntitiesPlugin.class);
  private Class<? extends IsRODAObject> clazz = null;

  private static Map<String, PluginParameter> pluginParameters = new HashMap<>();
  static {
    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_CLASS_CANONICAL_NAME,
      new PluginParameter(RodaConstants.PLUGIN_PARAMS_CLASS_CANONICAL_NAME, "RODA Object",
        PluginParameterType.RODA_OBJECT, AIP.class.getName(), false, false, "RODA object to reindex."));
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
  public String getName() {
    return "Reindex all Roda entities";
  }

  @Override
  public String getDescription() {
    return "Reset all RODA entities indexes and recreate them from data existing in the storage.";
  }

  @Override
  public String getVersionImpl() {
    return "1.0";
  }

  @Override
  public List<PluginParameter> getParameters() {
    ArrayList<PluginParameter> parameters = new ArrayList<PluginParameter>();
    parameters.add(pluginParameters.get(RodaConstants.PLUGIN_PARAMS_CLASS_CANONICAL_NAME));
    return parameters;
  }

  @Override
  public void setParameterValues(Map<String, String> parameters) throws InvalidParameterException {
    super.setParameterValues(parameters);
    if (parameters != null) {
      if (parameters.get(RodaConstants.PLUGIN_PARAMS_CLASS_CANONICAL_NAME) != null) {
        try {
          String classCanonicalName = parameters.get(RodaConstants.PLUGIN_PARAMS_CLASS_CANONICAL_NAME);
          if (!RodaConstants.PLUGIN_SELECT_ALL_RODA_OBJECTS.equals(classCanonicalName)) {
            clazz = (Class<? extends IsRODAObject>) Class.forName(classCanonicalName);
          }
        } catch (ClassNotFoundException e) {
          // do nothing
        }
      }
    }
  }

  @Override
  public Report execute(IndexService index, ModelService model, StorageService storage, List<Void> list)
    throws PluginException {
    Report pluginReport = PluginHelper.initPluginReport(this);

    try {
      SimpleJobPluginInfo jobPluginInfo = PluginHelper.getInitialJobInformation(this, list.size());
      PluginHelper.updateJobInformation(this, jobPluginInfo);

      if (clazz == null) {
        List<Class<? extends IsRODAObject>> classes = PluginHelper.getReindexObjectClasses();
        classes.remove(Job.class);
        jobPluginInfo.setSourceObjectsCount(classes.size());
        for (Class<? extends IsRODAObject> reindexClass : classes) {
          reindexRODAObject(model, reindexClass, jobPluginInfo);
        }
      } else {
        jobPluginInfo.setSourceObjectsCount(1);
        reindexRODAObject(model, clazz, jobPluginInfo);
      }

      pluginReport.setPluginState(PluginState.SUCCESS);
      jobPluginInfo.finalizeInfo();
      PluginHelper.updateJobInformation(this, jobPluginInfo);
    } catch (JobException e) {
      LOGGER.error("Error reindexing RODA entity", e);
    }

    return pluginReport;
  }

  private void reindexRODAObject(ModelService model, Class<? extends IsRODAObject> reindexClass,
    SimpleJobPluginInfo jobPluginInfo) {
    LOGGER.debug("Creating job to reindexing all {}", reindexClass.getSimpleName());

    try {
      if (TransferredResource.class.equals(reindexClass)) {
        // TransferredResource does not need a job
        RodaCoreFactory.getTransferredResourcesScanner().updateAllTransferredResources(null, false);
      } else {
        if (model.hasObjects(reindexClass)) {
          Job job = new Job();
          job.setId(UUID.randomUUID().toString());
          job.setName(ReindexRodaEntityPlugin.class.getSimpleName() + " (" + reindexClass.getSimpleName() + ")");
          Map<String, String> pluginParameters = new HashMap<>();
          pluginParameters.put(RodaConstants.PLUGIN_PARAMS_CLEAR_INDEXES, "true");
          job.setPluginParameters(pluginParameters);

          if (LogEntry.class.equals(reindexClass)) {
            job.setPlugin(ReindexActionLogPlugin.class.getName());
          } else {
            job.setPlugin(ReindexRodaEntityPlugin.class.getName());
          }

          job.setSourceObjects(SelectedItemsAll.create(reindexClass));
          job.setPluginType(PluginType.MISC);
          job.setUsername(PluginHelper.getJobUsername(this, model));
          PluginHelper.createAndExecuteJob(job, true);
        }
      }

      jobPluginInfo.incrementObjectsProcessedWithSuccess();
    } catch (RODAException e) {
      LOGGER.error("Error creating job to reindex all {}", reindexClass.getSimpleName(), e);
      jobPluginInfo.incrementObjectsProcessedWithFailure();
    }
  }

  @Override
  public Report beforeAllExecute(IndexService index, ModelService model, StorageService storage)
    throws PluginException {
    // Do not need to clear indexes, single jobs already does it by default
    return new Report();
  }

  @Override
  public Report afterAllExecute(IndexService index, ModelService model, StorageService storage) throws PluginException {
    // Do not need to optimize indexes, single jobs already does it by default
    return new Report();
  }

  @Override
  public Plugin<Void> cloneMe() {
    return new ReindexAllRodaEntitiesPlugin();
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
    return "Reindex Roda entity";
  }

  @Override
  public String getPreservationEventSuccessMessage() {
    return "All entities were reindexed with success";
  }

  @Override
  public String getPreservationEventFailureMessage() {
    return "An error occured while reindexing all entities";
  }

  @Override
  public List<String> getCategories() {
    return Arrays.asList(RodaConstants.PLUGIN_CATEGORY_MANAGEMENT);
  }

  @Override
  public List<Class<Void>> getObjectClasses() {
    return (List) Arrays.asList(Void.class);
  }

}
