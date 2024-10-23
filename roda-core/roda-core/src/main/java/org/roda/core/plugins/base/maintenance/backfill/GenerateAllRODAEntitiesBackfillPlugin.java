/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE.md file at the root of the source
 * tree and available online at
 * <p>
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.base.maintenance.backfill;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.common.RodaConstants.PreservationEventType;
import org.roda.core.data.exceptions.InvalidParameterException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.v2.IsRODAObject;
import org.roda.core.data.v2.LiteOptionalWithCause;
import org.roda.core.data.v2.Void;
import org.roda.core.data.v2.index.select.SelectedItemsAll;
import org.roda.core.data.v2.index.select.SelectedItemsNone;
import org.roda.core.data.v2.ip.TransferredResource;
import org.roda.core.data.v2.ip.metadata.IndexedPreservationAgent;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.PluginParameter;
import org.roda.core.data.v2.jobs.PluginState;
import org.roda.core.data.v2.jobs.PluginType;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.data.v2.log.LogEntry;
import org.roda.core.data.v2.user.RODAMember;
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
  private int blockSize = 100000;
  private HashMap<String, HashSet<String>> initialIdsManifest = new HashMap<>();
  private HashMap<String, HashSet<String>> processedIdsManifest = new HashMap<>();

  private static Map<String, PluginParameter> pluginParameters = new HashMap<>();

  static {
    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_BLOCK_SIZE,
      PluginParameter
        .getBuilder(RodaConstants.PLUGIN_PARAMS_BLOCK_SIZE, "Block size", PluginParameter.PluginParameterType.INTEGER)
        .withDefaultValue("100000").isMandatory(false)
        .withDescription("Number of documents in each index documents block.").build());
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
    parameters.add(pluginParameters.get(RodaConstants.PLUGIN_PARAMS_BLOCK_SIZE));
    return parameters;
  }

  @Override
  public void setParameterValues(Map<String, String> parameters) throws InvalidParameterException {
    super.setParameterValues(parameters);
    if (parameters != null && parameters.containsKey(RodaConstants.PLUGIN_PARAMS_BLOCK_SIZE)) {
      blockSize = Integer.parseInt(parameters.get(RodaConstants.PLUGIN_PARAMS_BLOCK_SIZE));
    }
  }

  @Override
  public Report execute(IndexService index, ModelService model, StorageService storage,
    List<LiteOptionalWithCause> liteList) throws PluginException {
    List<Class<? extends IsRODAObject>> classes = PluginHelper.getReindexObjectClasses();
    return PluginHelper.processVoids(this,
      (RODAProcessingLogic<Void>) (index1, model1, storage1, report, cachedJob, jobPluginInfo,
        plugin) -> generateBackfill(model1, index1, storage1, report, jobPluginInfo, cachedJob, classes),
      index, model, storage);
  }

  protected void generateBackfill(ModelService model, IndexService index, StorageService storage, Report report,
    JobPluginInfo jobPluginInfo, Job cachedJob, List<Class<? extends IsRODAObject>> classes) {
    for (Class<? extends IsRODAObject> clazz : classes) {
      // TODO handle exceptions
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

    if (model.hasObjects(clazz)) {
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

    Map<String, String> pluginParameters = new HashMap<>();
    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_BLOCK_SIZE, String.valueOf(blockSize));
    job.setPluginParameters(pluginParameters);
    job.setPluginType(PluginType.MISC);
    job.setUsername(username);

    job.setPlugin(GenerateBackfillPluginUtils.getGeneratedBackfillPluginName(clazz));
    job.setSourceObjects(SelectedItemsAll.create(clazz));

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
    // Get all currently indexed IDs
    /*
     * List<Class<? extends IsRODAObject>> classes =
     * PluginHelper.getReindexObjectClasses(); for (Class<? extends IsRODAObject>
     * clazz : classes) { // TODO handle exceptions try { Class<? extends IsIndexed>
     * indexClass = GenerateBackfillPluginUtils.getIndexClass(clazz);
     * CloseableIterable<? extends OptionalWithCause<? extends IsIndexed>>
     * indexedObjects = index.list(indexClass, List.of("id")); HashSet<String>
     * objectIds = new HashSet<>(); indexedObjects.forEach(o ->
     * objectIds.add(o.get().getId())); indexedObjects.close();
     * initialIdsManifest.put(clazz.getSimpleName(), objectIds); } catch
     * (NotFoundException e) { throw new RuntimeException(e); } catch
     * (RequestNotValidException e) { throw new RuntimeException(e); } catch
     * (GenericException e) { throw new RuntimeException(e); } catch (IOException e)
     * { throw new RuntimeException(e); } }
     */
    return null;
  }

  @Override
  public Report afterAllExecute(IndexService index, ModelService model, StorageService storage) {

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
