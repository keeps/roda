/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.plugins.base;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.InvalidParameterException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.JobReport;
import org.roda.core.data.v2.jobs.PluginParameter;
import org.roda.core.data.v2.jobs.PluginType;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.index.IndexService;
import org.roda.core.model.ModelService;
import org.roda.core.model.utils.ModelUtils;
import org.roda.core.plugins.Plugin;
import org.roda.core.plugins.PluginException;
import org.roda.core.storage.Binary;
import org.roda.core.storage.ClosableIterable;
import org.roda.core.storage.Resource;
import org.roda.core.storage.StorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReindexJobPlugin implements Plugin<Job> {

  private static final Logger LOGGER = LoggerFactory.getLogger(ReindexJobPlugin.class);
  private boolean clearIndexes = true;

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
  public String getVersion() {
    return "1.0";
  }

  @Override
  public List<PluginParameter> getParameters() {
    return new ArrayList<>();
  }

  @Override
  public Map<String, String> getParameterValues() {
    return new HashMap<>();
  }

  @Override
  public void setParameterValues(Map<String, String> parameters) throws InvalidParameterException {
    if (parameters != null && parameters.get(RodaConstants.PLUGIN_PARAMS_BOOLEAN_VALUE) != null) {
      try {
        clearIndexes = Boolean.parseBoolean(parameters.get(RodaConstants.PLUGIN_PARAMS_BOOLEAN_VALUE));
      } catch (NumberFormatException e) {
        // do nothing
      }
    }
  }

  @Override
  public Report execute(IndexService index, ModelService model, StorageService storage, List<Job> list)
    throws PluginException {

    ClosableIterable<Resource> listResourcesUnderDirectory = null;
    try {
      listResourcesUnderDirectory = storage.listResourcesUnderDirectory(ModelUtils.getJobContainerPath());

      for (Resource resource : listResourcesUnderDirectory) {
        Binary binary = storage.getBinary(resource.getStoragePath());
        Job objectFromJson = ModelUtils.getObjectFromJson(binary.getContent().createInputStream(), Job.class);
        index.reindexJob(objectFromJson);
      }

    } catch (NotFoundException | GenericException | AuthorizationDeniedException | RequestNotValidException
      | IOException e) {
      LOGGER.error("", e);
    }

    try {
      if (listResourcesUnderDirectory != null) {
        listResourcesUnderDirectory.close();
      }
    } catch (IOException e) {
      LOGGER.warn("");
    }

    try {
      listResourcesUnderDirectory = storage.listResourcesUnderDirectory(ModelUtils.getJobReportContainerPath());

      for (Resource resource : listResourcesUnderDirectory) {
        Binary binary = storage.getBinary(resource.getStoragePath());
        JobReport objectFromJson = ModelUtils.getObjectFromJson(binary.getContent().createInputStream(),
          JobReport.class);
        index.reindexJobReport(objectFromJson);
      }

    } catch (NotFoundException | GenericException | AuthorizationDeniedException | RequestNotValidException
      | IOException e) {
      LOGGER.error("", e);
    }

    try {
      if (listResourcesUnderDirectory != null) {
        listResourcesUnderDirectory.close();
      }
    } catch (IOException e) {
      LOGGER.warn("");
    }

    return null;

  }

  @Override
  public Report beforeExecute(IndexService index, ModelService model, StorageService storage) throws PluginException {
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
  public Report afterExecute(IndexService index, ModelService model, StorageService storage) throws PluginException {
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
}
