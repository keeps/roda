/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.base.maintenance.reindex;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.roda.core.RodaCoreFactory;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.common.RodaConstants.PreservationEventType;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.InvalidParameterException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.LiteOptionalWithCause;
import org.roda.core.data.v2.Void;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.ip.TransferredResource;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.PluginParameter;
import org.roda.core.data.v2.jobs.PluginParameter.PluginParameterType;
import org.roda.core.data.v2.jobs.PluginState;
import org.roda.core.data.v2.jobs.PluginType;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.index.IndexService;
import org.roda.core.model.ModelService;
import org.roda.core.plugins.AbstractPlugin;
import org.roda.core.plugins.Plugin;
import org.roda.core.plugins.PluginException;
import org.roda.core.plugins.PluginHelper;
import org.roda.core.plugins.RODAProcessingLogic;
import org.roda.core.plugins.orchestrate.JobPluginInfo;
import org.roda.core.storage.StorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReindexTransferredResourcePlugin extends AbstractPlugin<Void> {

  private static final Logger LOGGER = LoggerFactory.getLogger(ReindexTransferredResourcePlugin.class);

  private boolean clearIndexes = false;
  private boolean optimizeIndexes = true;
  private int resourceCounter = 0;

  private static Map<String, PluginParameter> pluginParameters = new HashMap<>();
  static {
    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_CLEAR_INDEXES,
      PluginParameter
        .getBuilder(RodaConstants.PLUGIN_PARAMS_CLEAR_INDEXES, "Clear indexes", PluginParameterType.BOOLEAN)
        .withDefaultValue("false").isMandatory(false).withDescription("Clear all indexes before reindexing them.")
        .build());

    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_OPTIMIZE_INDEXES,
      PluginParameter
        .getBuilder(RodaConstants.PLUGIN_PARAMS_OPTIMIZE_INDEXES, "Optimize indexes", PluginParameterType.BOOLEAN)
        .withDefaultValue("true").isMandatory(false).withDescription("Optimize indexes after reindexing them.")
        .build());
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
    return "Rebuild transferred resource index";
  }

  @Override
  public String getDescription() {
    return "Clears the index and recreates it from actual physical data that exists on the storage. This task aims to fix inconsistencies between what is "
      + "shown in the graphical user interface of the repository and what is actually kept at the storage layer. Such inconsistencies may occur for "
      + "various reasons, e.g. index corruption, ungraceful shutdown of the repository, etc.";
  }

  @Override
  public String getVersionImpl() {
    return "1.0";
  }

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
    List<LiteOptionalWithCause> list) throws PluginException {
    return PluginHelper.processVoids(this, new RODAProcessingLogic<Void>() {
      @Override
      public void process(IndexService index, ModelService model, StorageService storage, Report report, Job cachedJob,
        JobPluginInfo jobPluginInfo, Plugin<Void> plugin) {
        reindexTransferredResources(report, jobPluginInfo);
      }
    }, index, model, storage);
  }

  private void reindexTransferredResources(Report report, JobPluginInfo jobPluginInfo) {
    report.setPluginState(PluginState.SUCCESS);
    jobPluginInfo.setSourceObjectsCount(resourceCounter);
    try {
      RodaCoreFactory.getTransferredResourcesScanner().updateTransferredResources(Optional.empty(), true);
      // FIXME 20170116 hsilva: it makes no sense relying on a count made
      // before the indexing start to set counters
      jobPluginInfo.incrementObjectsProcessedWithSuccess(resourceCounter);
    } catch (Exception e) {
      LOGGER.error("Error updating transferred resources");
      // FIXME 20170116 hsilva: it makes no sense relying on a count made
      // before the indexing start to set counters
      jobPluginInfo.incrementObjectsProcessedWithFailure(resourceCounter);
      report.setPluginState(PluginState.FAILURE);
    }
  }

  @Override
  public Report beforeAllExecute(IndexService index, ModelService model, StorageService storage)
    throws PluginException {

    try {
      resourceCounter = index.count(TransferredResource.class, Filter.ALL).intValue();
    } catch (GenericException | RequestNotValidException e) {
      // do nothing
    }

    if (clearIndexes) {
      LOGGER.debug("Clearing indexes");
      try {
        index.clearIndex(RodaConstants.INDEX_TRANSFERRED_RESOURCE);
      } catch (GenericException | AuthorizationDeniedException e) {
        throw new PluginException("Error clearing index", e);
      }
    } else {
      LOGGER.debug("Skipping clear indexes");
    }

    return new Report();
  }

  @Override
  public Report afterAllExecute(IndexService index, ModelService model, StorageService storage) throws PluginException {
    if (optimizeIndexes) {
      LOGGER.debug("Optimizing indexes");
      try {
        index.optimizeIndex(RodaConstants.INDEX_TRANSFERRED_RESOURCE);
      } catch (GenericException | AuthorizationDeniedException e) {
        throw new PluginException("Error optimizing index", e);
      }
    }

    return new Report();
  }

  @Override
  public Plugin<Void> cloneMe() {
    return new ReindexTransferredResourcePlugin();
  }

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
  public List<Class<Void>> getObjectClasses() {
    return Arrays.asList(Void.class);
  }

}
