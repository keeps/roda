/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.plugins.base;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.io.IOUtils;
import org.roda.core.common.iterables.CloseableIterable;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.common.RodaConstants.PreservationEventType;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.InvalidParameterException;
import org.roda.core.data.exceptions.JobException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.utils.JsonUtils;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.PluginParameter;
import org.roda.core.data.v2.jobs.PluginParameter.PluginParameterType;
import org.roda.core.data.v2.jobs.PluginType;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.data.v2.jobs.Report.PluginState;
import org.roda.core.data.v2.validation.ValidationIssue;
import org.roda.core.data.v2.validation.ValidationReport;
import org.roda.core.index.IndexService;
import org.roda.core.model.ModelService;
import org.roda.core.model.utils.ModelUtils;
import org.roda.core.plugins.AbstractPlugin;
import org.roda.core.plugins.Plugin;
import org.roda.core.plugins.PluginException;
import org.roda.core.plugins.orchestrate.SimpleJobPluginInfo;
import org.roda.core.plugins.plugins.PluginHelper;
import org.roda.core.storage.Binary;
import org.roda.core.storage.Resource;
import org.roda.core.storage.StorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReindexJobPlugin extends AbstractPlugin<Job> {

  private static final Logger LOGGER = LoggerFactory.getLogger(ReindexJobPlugin.class);
  private boolean clearIndexes = true;

  private static Map<String, PluginParameter> pluginParameters = new HashMap<>();
  static {
    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_CLEAR_INDEXES,
      new PluginParameter(RodaConstants.PLUGIN_PARAMS_CLEAR_INDEXES, "Clear indexes", PluginParameterType.BOOLEAN,
        "false", false, false, "Clear all the Job and Report indexes before reindexing them."));
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
    return "Reindex Jobs/JobReports";
  }

  @Override
  public String getDescription() {
    return "Clean-up indexes and re-create them from data in storage";
  }

  @Override
  public String getVersionImpl() {
    return "1.0";
  }

  @Override
  public List<PluginParameter> getParameters() {
    ArrayList<PluginParameter> parameters = new ArrayList<PluginParameter>();
    parameters.add(pluginParameters.get(RodaConstants.PLUGIN_PARAMS_CLEAR_INDEXES));
    return parameters;
  }

  @Override
  public void setParameterValues(Map<String, String> parameters) throws InvalidParameterException {
    super.setParameterValues(parameters);

    if (parameters != null && parameters.get(RodaConstants.PLUGIN_PARAMS_CLEAR_INDEXES) != null) {
      clearIndexes = Boolean.parseBoolean(parameters.get(RodaConstants.PLUGIN_PARAMS_CLEAR_INDEXES));
    }
  }

  @Override
  public Report execute(IndexService index, ModelService model, StorageService storage, List<Job> list)
    throws PluginException {

    String reportId = UUID.randomUUID().toString();
    Report report = PluginHelper.initPluginReport(this).setOutcomeObjectId(reportId);
    report.setPluginState(PluginState.SUCCESS);

    try {
      SimpleJobPluginInfo jobPluginInfo = PluginHelper.getInitialJobInformation(this, list.size());
      PluginHelper.updateJobInformation(this, jobPluginInfo);

      report = indexJobs(model, index, storage, report, jobPluginInfo, reportId);
      indexJobsReports(index, storage);

      jobPluginInfo.finalizeInfo();
      PluginHelper.updateJobInformation(this, jobPluginInfo);
    } catch (JobException e) {
      LOGGER.error("Could not update Job information");
    }

    PluginHelper.createJobReport(this, model, report);
    return report;
  }

  private Report indexJobs(ModelService model, IndexService index, StorageService storage, Report report,
    SimpleJobPluginInfo jobPluginInfo, String reportId) {
    CloseableIterable<Resource> listResourcesUnderDirectory = null;

    try {
      boolean recursive = false;
      listResourcesUnderDirectory = storage.listResourcesUnderDirectory(ModelUtils.getJobContainerPath(), recursive);
    } catch (NotFoundException | GenericException | AuthorizationDeniedException | RequestNotValidException e) {
      LOGGER.error("", e);
    }

    int jobCounter = 0;
    ValidationReport validationReport = new ValidationReport();

    if (listResourcesUnderDirectory != null) {
      for (Resource resource : listResourcesUnderDirectory) {
        jobCounter++;

        try {
          Binary binary = storage.getBinary(resource.getStoragePath());
          InputStream inputStream = binary.getContent().createInputStream();
          Job objectFromJson = JsonUtils.getObjectFromJson(inputStream, Job.class);
          IOUtils.closeQuietly(inputStream);
          index.reindexJob(objectFromJson);
          jobPluginInfo.incrementObjectsProcessedWithSuccess();
        } catch (NotFoundException | GenericException | AuthorizationDeniedException | RequestNotValidException
          | IOException e) {
          LOGGER.error("", e);
          jobPluginInfo.incrementObjectsProcessedWithFailure();
          report.setPluginState(PluginState.FAILURE);

          ValidationIssue issue = new ValidationIssue();
          issue.setMessage(e.getMessage() + " on: " + resource.getStoragePath());
          validationReport.addIssue(issue);
        }
      }
    }

    if (validationReport.getIssues().size() > 0) {
      report.setHtmlPluginDetails(true);
      report.setPluginDetails(validationReport.toHtml(false, false, false, "Error list"));
    }

    jobPluginInfo.setSourceObjectsCount(jobCounter);
    IOUtils.closeQuietly(listResourcesUnderDirectory);
    return report;
  }

  private void indexJobsReports(IndexService index, StorageService storage) {
    CloseableIterable<Resource> listResourcesUnderDirectory = null;
    try {
      boolean recursive = true;
      listResourcesUnderDirectory = storage.listResourcesUnderDirectory(ModelUtils.getJobReportContainerPath(),
        recursive);
    } catch (NotFoundException | GenericException | AuthorizationDeniedException | RequestNotValidException e) {
      LOGGER.error("", e);
    }

    if (listResourcesUnderDirectory != null) {
      for (Resource resource : listResourcesUnderDirectory) {
        if (!resource.isDirectory()) {
          try {
            Binary binary = storage.getBinary(resource.getStoragePath());
            InputStream inputStream = binary.getContent().createInputStream();
            Report objectFromJson = JsonUtils.getObjectFromJson(inputStream, Report.class);
            IOUtils.closeQuietly(inputStream);
            index.reindexJobReport(objectFromJson);
          } catch (NotFoundException | GenericException | AuthorizationDeniedException | RequestNotValidException
            | IOException e) {
            LOGGER.error("", e);
          }
        }
      }
    }

    IOUtils.closeQuietly(listResourcesUnderDirectory);
  }

  @Override
  public Report beforeAllExecute(IndexService index, ModelService model, StorageService storage)
    throws PluginException {
    if (clearIndexes) {
      LOGGER.debug("Clearing indexes");
      try {
        index.clearIndex(RodaConstants.INDEX_JOB);
        index.clearIndex(RodaConstants.INDEX_JOB_REPORT);
      } catch (GenericException e) {
        throw new PluginException("Error clearing index", e);
      }
    } else {
      LOGGER.debug("Skipping clear indexes");
    }

    return null;
  }

  @Override
  public Report beforeBlockExecute(IndexService index, ModelService model, StorageService storage)
    throws PluginException {
    // do nothing
    return null;
  }

  @Override
  public Report afterBlockExecute(IndexService index, ModelService model, StorageService storage)
    throws PluginException {
    // do nothing
    return null;
  }

  @Override
  public Report afterAllExecute(IndexService index, ModelService model, StorageService storage) throws PluginException {
    LOGGER.debug("Optimizing indexes");
    try {
      index.optimizeIndex(RodaConstants.INDEX_JOB);
      index.optimizeIndex(RodaConstants.INDEX_JOB_REPORT);
    } catch (GenericException e) {
      throw new PluginException("Error optimizing index", e);
    }

    return null;
  }

  @Override
  public Plugin<Job> cloneMe() {
    return new ReindexJobPlugin();
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
    return "XXXXXXXXXX";
  }

  @Override
  public String getPreservationEventSuccessMessage() {
    return "XXXXXXXXXXXXXXXXXXXXXXXX";
  }

  @Override
  public String getPreservationEventFailureMessage() {
    return "XXXXXXXXXXXXXXXXXXXXXXXXXX";
  }

  @Override
  public List<String> getCategories() {
    return Arrays.asList(RodaConstants.PLUGIN_CATEGORY_NOT_LISTABLE);
  }

}
