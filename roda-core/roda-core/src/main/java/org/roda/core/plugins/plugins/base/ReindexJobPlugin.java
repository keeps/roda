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
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.roda.core.common.iterables.CloseableIterable;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.common.RodaConstants.PreservationEventType;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.InvalidParameterException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.utils.JsonUtils;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.PluginType;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.index.IndexService;
import org.roda.core.model.ModelService;
import org.roda.core.model.utils.ModelUtils;
import org.roda.core.plugins.AbstractPlugin;
import org.roda.core.plugins.Plugin;
import org.roda.core.plugins.PluginException;
import org.roda.core.storage.Binary;
import org.roda.core.storage.Resource;
import org.roda.core.storage.StorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReindexJobPlugin extends AbstractPlugin<Job> {

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
  public String getVersionImpl() {
    return "1.0";
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

    CloseableIterable<Resource> listResourcesUnderDirectory = null;
    try {
      boolean recursive = false;
      listResourcesUnderDirectory = storage.listResourcesUnderDirectory(ModelUtils.getJobContainerPath(), recursive);

      for (Resource resource : listResourcesUnderDirectory) {
        Binary binary = storage.getBinary(resource.getStoragePath());
        InputStream inputStream = binary.getContent().createInputStream();
        Job objectFromJson = JsonUtils.getObjectFromJson(inputStream, Job.class);
        IOUtils.closeQuietly(inputStream);
        index.reindexJob(objectFromJson);
      }

    } catch (NotFoundException | GenericException | AuthorizationDeniedException | RequestNotValidException
      | IOException e) {
      LOGGER.error("", e);
    } finally {
      IOUtils.closeQuietly(listResourcesUnderDirectory);
    }

    try {
      boolean recursive = true;
      listResourcesUnderDirectory = storage.listResourcesUnderDirectory(ModelUtils.getJobReportContainerPath(),
        recursive);

      for (Resource resource : listResourcesUnderDirectory) {
        if (!resource.isDirectory()) {
          Binary binary = storage.getBinary(resource.getStoragePath());
          InputStream inputStream = binary.getContent().createInputStream();
          Report objectFromJson = JsonUtils.getObjectFromJson(inputStream, Report.class);
          IOUtils.closeQuietly(inputStream);
          index.reindexJobReport(objectFromJson);
        }
      }

    } catch (NotFoundException | GenericException | AuthorizationDeniedException | RequestNotValidException
      | IOException e) {
      LOGGER.error("", e);
    } finally {
      IOUtils.closeQuietly(listResourcesUnderDirectory);
    }

    // FIXME 20160329 hsilva: this should return a better report
    return new Report();

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
    // TODO Auto-generated method stub
    return null;
  }

}
