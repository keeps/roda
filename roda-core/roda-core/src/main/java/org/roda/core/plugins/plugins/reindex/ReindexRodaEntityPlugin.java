/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.plugins.reindex;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.common.RodaConstants.PreservationEventType;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.InvalidParameterException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.NotSupportedException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.exceptions.ReturnWithExceptions;
import org.roda.core.data.v2.IsRODAObject;
import org.roda.core.data.v2.LiteOptionalWithCause;
import org.roda.core.data.v2.index.IsIndexed;
import org.roda.core.data.v2.index.select.SelectedItems;
import org.roda.core.data.v2.index.select.SelectedItemsAll;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.PluginParameter;
import org.roda.core.data.v2.jobs.PluginParameter.PluginParameterType;
import org.roda.core.data.v2.jobs.PluginType;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.data.v2.jobs.Report.PluginState;
import org.roda.core.index.IndexService;
import org.roda.core.index.schema.SolrCollectionRegistry;
import org.roda.core.model.ModelObserver;
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

public abstract class ReindexRodaEntityPlugin<T extends IsRODAObject> extends AbstractPlugin<T> {
  private static final Logger LOGGER = LoggerFactory.getLogger(ReindexRodaEntityPlugin.class);
  private boolean clearIndexes = false;
  private boolean optimizeIndexes = false;

  private static Map<String, PluginParameter> pluginParameters = new HashMap<>();
  static {
    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_CLEAR_INDEXES,
      new PluginParameter(RodaConstants.PLUGIN_PARAMS_CLEAR_INDEXES, "Clear indexes", PluginParameterType.BOOLEAN,
        "false", false, false, "Clear all indexes before reindexing them."));

    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_OPTIMIZE_INDEXES,
      new PluginParameter(RodaConstants.PLUGIN_PARAMS_OPTIMIZE_INDEXES, "Optimize indexes", PluginParameterType.BOOLEAN,
        "true", false, false, "Optimize indexes after reindexing them."));
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
  public abstract String getName();

  @Override
  public String getDescription() {
    return "Clears the index and recreates it from actual physical data that exists on the storage. This task aims to fix inconsistencies between what is shown in "
      + "the graphical user interface of the repository and what is actually kept at the storage layer. Such inconsistencies may occur for various reasons, e.g. "
      + "index corruption, ungraceful shutdown of the repository, etc.";
  }

  @Override
  public abstract String getVersionImpl();

  @Override
  public List<PluginParameter> getParameters() {
    ArrayList<PluginParameter> parameters = new ArrayList<>();
    parameters.add(pluginParameters.get(RodaConstants.PLUGIN_PARAMS_CLEAR_INDEXES));
    parameters.add(pluginParameters.get(RodaConstants.PLUGIN_PARAMS_OPTIMIZE_INDEXES));
    return parameters;
  }

  @Override
  public void setParameterValues(Map<String, String> parameters) throws InvalidParameterException {
    super.setParameterValues(parameters);
    if (parameters != null && parameters.containsKey(RodaConstants.PLUGIN_PARAMS_CLEAR_INDEXES)) {
      clearIndexes = Boolean.parseBoolean(parameters.get(RodaConstants.PLUGIN_PARAMS_CLEAR_INDEXES));
    }

    if (parameters != null && parameters.containsKey(RodaConstants.PLUGIN_PARAMS_OPTIMIZE_INDEXES)) {
      optimizeIndexes = Boolean.parseBoolean(parameters.get(RodaConstants.PLUGIN_PARAMS_OPTIMIZE_INDEXES));
    }
  }

  @Override
  public Report execute(IndexService index, ModelService model, StorageService storage,
    List<LiteOptionalWithCause> liteList) throws PluginException {
    return PluginHelper.processObjects(this, new RODAObjectsProcessingLogic<T>() {
      @Override
      public void process(IndexService index, ModelService model, StorageService storage, Report report, Job cachedJob,
        JobPluginInfo jobPluginInfo, Plugin<T> plugin, List<T> objects) {
        reindex(index, model, report, jobPluginInfo, cachedJob, objects);
      }
    }, index, model, storage, liteList);
  }

  private void reindex(IndexService index, ModelService model, Report pluginReport, JobPluginInfo jobPluginInfo,
    Job job, List<T> list) {
    pluginReport.setPluginState(PluginState.SUCCESS);

    // clearing specific indexes from a id list
    try {
      SelectedItems<?> selectedItems = job.getSourceObjects();
      if (!(selectedItems instanceof SelectedItemsAll) && clearIndexes) {
        List<String> ids = list.stream().map(obj -> obj.getId()).collect(Collectors.toList());
        clearSpecificIndexes(index, ids);
      }
    } catch (GenericException | RequestNotValidException | AuthorizationDeniedException e) {
      LOGGER.error("Error clearing specific indexes of a RODA entity", e);
    }

    // executing reindex
    for (T object : list) {
      if (LOGGER.isTraceEnabled()) {
        LOGGER.trace("Reindexing {} {}", object.getClass().getSimpleName(), object.getId());
      }

      ReturnWithExceptions<Void, ModelObserver> exceptions = index.reindex(object);
      List<Exception> exceptionList = exceptions.getExceptions();
      if (exceptionList.isEmpty()) {
        jobPluginInfo.incrementObjectsProcessedWithSuccess();
      } else {
        jobPluginInfo.incrementObjectsProcessedWithFailure();
        Report reportItem = PluginHelper.initPluginReportItem(this, object.getId(), object.getClass());
        reportItem.setPluginState(PluginState.FAILURE);

        for (Exception e : exceptionList) {
          reportItem.addPluginDetails(e.getMessage() + "\n");
        }

        pluginReport.addReport(reportItem);
        PluginHelper.updatePartialJobReport(this, model, reportItem, true, job);
      }
    }
  }

  @Override
  public Report beforeAllExecute(IndexService index, ModelService model, StorageService storage)
    throws PluginException {
    if (clearIndexes) {
      LOGGER.debug("Clearing indexes");
      try {
        Job job = PluginHelper.getJob(this, index);
        if (job.getSourceObjects() instanceof SelectedItemsAll) {
          @SuppressWarnings("unchecked")
          Class<? extends IsIndexed> selectedClass = (Class<? extends IsIndexed>) Class
            .forName(job.getSourceObjects().getSelectedClass());
          index.clearIndexes(SolrCollectionRegistry.getCommitIndexNames(selectedClass));
          if (selectedClass.equals(AIP.class) || selectedClass.equals(IndexedAIP.class)) {
            index.clearAIPEventIndex();
          }
        }
      } catch (GenericException | NotFoundException | ClassNotFoundException | RequestNotValidException
        | NotSupportedException | AuthorizationDeniedException e) {
        throw new PluginException("Error clearing index", e);
      }

    } else {
      LOGGER.debug("Skipping clear indexes");
    }

    return new Report();
  }

  @Override
  public Report afterAllExecute(IndexService index, ModelService model, StorageService storage) throws PluginException {
    LOGGER.debug("Optimizing indexes");
    if (optimizeIndexes) {
      try {
        Job job = PluginHelper.getJob(this, index);
        @SuppressWarnings("unchecked")
        Class<? extends IsIndexed> selectedClass = (Class<? extends IsIndexed>) Class
          .forName(job.getSourceObjects().getSelectedClass());
        index.optimizeIndexes(SolrCollectionRegistry.getCommitIndexNames(selectedClass));
      } catch (GenericException | NotFoundException | ClassNotFoundException | RequestNotValidException
        | NotSupportedException | AuthorizationDeniedException e) {
        throw new PluginException("Error optimizing index", e);
      }
    }

    return new Report();
  }

  public abstract void clearSpecificIndexes(IndexService index, List<String> ids)
    throws GenericException, RequestNotValidException, AuthorizationDeniedException;

  @Override
  public abstract Plugin<T> cloneMe();

  @Override
  public PluginType getType() {
    return PluginType.MISC;
  }

  @Override
  public boolean areParameterValuesValid() {
    return true;
  }

  @Override
  public PreservationEventType getPreservationEventType() {
    return PreservationEventType.NONE;
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
    return Arrays.asList(RodaConstants.PLUGIN_CATEGORY_REINDEX);
  }

  @Override
  public abstract List<Class<T>> getObjectClasses();

}
