/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE.md file at the root of the source
 * tree and available online at
 * <p>
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.base.maintenance.backfill;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.common.RodaConstants.PreservationEventType;
import org.roda.core.data.exceptions.InvalidParameterException;
import org.roda.core.data.exceptions.LogEntryJsonParseException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.v2.IsRODAObject;
import org.roda.core.data.v2.LiteOptionalWithCause;
import org.roda.core.data.v2.Void;
import org.roda.core.data.v2.index.filter.DateIntervalFilterParameter;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.select.SelectedItems;
import org.roda.core.data.v2.index.select.SelectedItemsAll;
import org.roda.core.data.v2.index.select.SelectedItemsFilter;
import org.roda.core.data.v2.index.select.SelectedItemsNone;
import org.roda.core.data.v2.ip.DIPFile;
import org.roda.core.data.v2.ip.File;
import org.roda.core.data.v2.ip.Representation;
import org.roda.core.data.v2.ip.metadata.PreservationMetadata;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.PluginParameter;
import org.roda.core.data.v2.jobs.PluginState;
import org.roda.core.data.v2.jobs.PluginType;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.data.v2.log.LogEntry;
import org.roda.core.index.IndexService;
import org.roda.core.model.ModelService;
import org.roda.core.plugins.AbstractPlugin;
import org.roda.core.plugins.Plugin;
import org.roda.core.plugins.PluginException;
import org.roda.core.plugins.PluginHelper;
import org.roda.core.plugins.RODAProcessingLogic;
import org.roda.core.plugins.orchestrate.JobPluginInfo;
import org.roda.core.storage.StorageService;
import org.roda.core.util.IdUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GenerateAllRODAEntitiesBackfillPlugin extends AbstractPlugin<Void> {

  private static final Logger LOGGER = LoggerFactory.getLogger(GenerateAllRODAEntitiesBackfillPlugin.class);
  private String outputDirectory = ".";
  private boolean onlyGenerateInventory = false;
  private Date startDate = null;

  private static final Map<String, PluginParameter> pluginParameters = new HashMap<>();

  static {
    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_OUTPUT_DIRECTORY,
      PluginParameter
        .getBuilder(RodaConstants.PLUGIN_PARAMS_OUTPUT_DIRECTORY, "Output directory",
          PluginParameter.PluginParameterType.STRING)
        .withDefaultValue(".").isMandatory(true).withDescription("This job's output directory path").build());
    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_ONLY_GENERATE_INVENTORY,
      PluginParameter
        .getBuilder(RodaConstants.PLUGIN_PARAMS_ONLY_GENERATE_INVENTORY, "Only generate inventory",
          PluginParameter.PluginParameterType.BOOLEAN)
        .withDefaultValue("false").isMandatory(true)
        .withDescription(
          "Whether this job should only generate the inventory of RODA objects and not the index backfill files")
        .build());
    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_START_DATE,
      PluginParameter
        .getBuilder(RodaConstants.PLUGIN_PARAMS_START_DATE, "Object starting date",
          PluginParameter.PluginParameterType.STRING)
        .isMandatory(false).withDescription(
          "The last modified data for source objects to process. If not set, all objects will be processed.")
        .build());
  }

  @Override
  public void init() throws PluginException {
    // do nothing
  }

  @Override
  public List<Class<Void>> getObjectClasses() {
    return Arrays.asList(Void.class);
  }

  @Override
  public void shutdown() {
    // do nothing
  }

  @Override
  public String getName() {
    return "Generate complete index backfill";
  }

  @Override
  public String getDescription() {
    return "Description of example plugin";
  }

  @Override
  public String getVersionImpl() {
    // Get from pom.xml <version>
    return getClass().getPackage().getImplementationVersion();
  }

  @Override
  public List<PluginParameter> getParameters() {
    ArrayList<PluginParameter> parameters = new ArrayList<>();
    parameters.add(pluginParameters.get(RodaConstants.PLUGIN_PARAMS_OUTPUT_DIRECTORY));
    parameters.add(pluginParameters.get(RodaConstants.PLUGIN_PARAMS_ONLY_GENERATE_INVENTORY));
    parameters.add(pluginParameters.get(RodaConstants.PLUGIN_PARAMS_START_DATE));
    return parameters;
  }

  @Override
  public void setParameterValues(Map<String, String> parameters) throws InvalidParameterException {
    super.setParameterValues(parameters);
    if (parameters != null && parameters.containsKey(RodaConstants.PLUGIN_PARAMS_OUTPUT_DIRECTORY)) {
      outputDirectory = parameters.get(RodaConstants.PLUGIN_PARAMS_OUTPUT_DIRECTORY);
    }
    if (parameters != null && parameters.containsKey(RodaConstants.PLUGIN_PARAMS_ONLY_GENERATE_INVENTORY)) {
      onlyGenerateInventory = Boolean.parseBoolean(parameters.get(RodaConstants.PLUGIN_PARAMS_ONLY_GENERATE_INVENTORY));
    }
    if (parameters != null && parameters.containsKey(RodaConstants.PLUGIN_PARAMS_START_DATE)) {
      String dateString = parameters.get(RodaConstants.PLUGIN_PARAMS_START_DATE);
      if (!dateString.isEmpty()) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        try {
          startDate = formatter.parse(parameters.get(RodaConstants.PLUGIN_PARAMS_START_DATE));
        } catch (ParseException e) {
          throw new InvalidParameterException(e);
        }
      }
    }
  }

  @Override
  public Report execute(IndexService index, ModelService model, StorageService storage,
    List<LiteOptionalWithCause> liteList) throws PluginException {
    List<Class<? extends IsRODAObject>> classes = GenerateBackfillPluginUtils.getBackfillObjectClasses();
    return PluginHelper.processVoids(this,
      (RODAProcessingLogic<Void>) (index1, model1, storage1, report, cachedJob, jobPluginInfo,
        plugin) -> generateBackfill(model1, index1, storage1, report, jobPluginInfo, cachedJob, classes),
      index, model, storage);
  }

  protected void generateBackfill(ModelService model, IndexService index, StorageService storage, Report report,
    JobPluginInfo jobPluginInfo, Job cachedJob, List<Class<? extends IsRODAObject>> classes) {
    for (Class<? extends IsRODAObject> clazz : classes) {
      Report reportItem = generateRODAObjectBackfill(model, clazz, jobPluginInfo);
      if (reportItem != null) {
        report.addReport(reportItem);
        PluginHelper.updatePartialJobReport(this, model, reportItem, true, cachedJob);
      }
    }
    report.setPluginState(PluginState.SUCCESS);
  }

  protected Report generateRODAObjectBackfill(ModelService model, Class<? extends IsRODAObject> clazz,
    JobPluginInfo jobPluginInfo) {
    Report report = null;

    if (model.hasObjects(clazz) || (clazz.equals(DIPFile.class) || clazz.equals(Representation.class)
      || clazz.equals(File.class) || clazz.equals(PreservationMetadata.class))) {
      String jobId = IdUtils.createUUID();
      String jobName = "Generate index backfill for RODA entity (" + clazz.getSimpleName() + ")";
      report = PluginHelper.initPluginReportItem(this, jobId, Job.class);
      try {
        String username = PluginHelper.getJobUsername(this, model);
        Job job = initGenerateBackfillJob(clazz, jobId, jobName, username);
        PluginHelper.createAndExecuteJob(job);
        jobPluginInfo.incrementObjectsProcessedWithSuccess();
        report.setPluginState(PluginState.SUCCESS).setPluginDetails(jobName + " ran successfully");
      } catch (RODAException e) {
        LOGGER.error("Error creating job to generate index backfill for {}", clazz.getSimpleName(), e);
        jobPluginInfo.incrementObjectsProcessedWithFailure();
        report.setPluginState(PluginState.FAILURE).setPluginDetails(jobName + " did not run successfully");
      }
    } else {
      jobPluginInfo.incrementObjectsProcessedWithSuccess();
    }

    return report;
  }

  protected <T extends IsRODAObject> Job initGenerateBackfillJob(Class<T> clazz, String jobId, String jobName, String username) throws NotFoundException {
    Job job = new Job();
    job.setId(jobId);
    job.setName(jobName);

    Map<String, String> localPluginParameters = new HashMap<>();
    localPluginParameters.put(RodaConstants.PLUGIN_PARAMS_OUTPUT_DIRECTORY, outputDirectory);
    localPluginParameters.put(RodaConstants.PLUGIN_PARAMS_ONLY_GENERATE_INVENTORY,
      String.valueOf(onlyGenerateInventory));
    if (startDate != null) {
      SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
      localPluginParameters.put(RodaConstants.PLUGIN_PARAMS_START_DATE, dateFormat.format(startDate));
    }
    job.setPluginParameters(localPluginParameters);
    job.setPluginType(PluginType.MISC);
    job.setUsername(username);

    job.setPlugin(GenerateBackfillPluginUtils.getGeneratedBackfillPluginName(clazz));
    SelectedItems<?> selectedItems;
    if (clazz.equals(LogEntry.class)) {
      selectedItems = new SelectedItemsNone<>();
    }
    else if (startDate != null) {
      SelectedItemsFilter<?> selectedItemsFilter = new SelectedItemsFilter<>();
      Filter filter = new Filter();
      DateIntervalFilterParameter dateIntervalFilterParameter = new DateIntervalFilterParameter();
      dateIntervalFilterParameter.setFromValue(startDate);
      filter.add(dateIntervalFilterParameter);
      selectedItemsFilter.setFilter(filter);
      selectedItems = selectedItemsFilter;
    } else {
      selectedItems = SelectedItemsAll.create(clazz);
    }
    job.setSourceObjects(selectedItems);

    return job;
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
    return PreservationEventType.MIGRATION;
  }

  @Override
  public String getPreservationEventDescription() {
    return "Checked if ...";
  }

  @Override
  public String getPreservationEventSuccessMessage() {
    return "... with success.";
  }

  @Override
  public String getPreservationEventFailureMessage() {
    return "Failed to ...";
  }

  @Override
  public Report beforeAllExecute(IndexService index, ModelService model, StorageService storage) {
    // do nothing
    return null;
  }

  @Override
  public Report afterAllExecute(IndexService index, ModelService model, StorageService storage) {
    // do nothing
    return null;
  }

  @Override
  public List<String> getCategories() {
    return Collections.singletonList(RodaConstants.PLUGIN_CATEGORY_EXPERIMENTAL);
  }

  @Override
  public Plugin<Void> cloneMe() {
    return new GenerateAllRODAEntitiesBackfillPlugin();
  }
}
