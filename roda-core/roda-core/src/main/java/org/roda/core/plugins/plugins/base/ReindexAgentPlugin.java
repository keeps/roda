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
import org.roda.core.data.v2.agents.Agent;
import org.roda.core.data.v2.jobs.PluginType;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.index.IndexService;
import org.roda.core.model.ModelService;
import org.roda.core.model.utils.JsonUtils;
import org.roda.core.model.utils.ModelUtils;
import org.roda.core.plugins.AbstractPlugin;
import org.roda.core.plugins.Plugin;
import org.roda.core.plugins.PluginException;
import org.roda.core.storage.Binary;
import org.roda.core.storage.Resource;
import org.roda.core.storage.StorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReindexAgentPlugin extends AbstractPlugin<Agent> {

  private static final Logger LOGGER = LoggerFactory.getLogger(ReindexAgentPlugin.class);
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
    return "Reindex Agents";
  }

  @Override
  public String getDescription() {
    return "Cleanup indexes and recreate them from data in storage";
  }

  @Override
  public String getVersionImpl() {
    return "1.0";
  }

  @Override
  public void setParameterValues(Map<String, String> parameters) throws InvalidParameterException {
    super.setParameterValues(parameters);
    if (parameters != null && parameters.get(RodaConstants.PLUGIN_PARAMS_BOOLEAN_VALUE) != null) {
      try {
        clearIndexes = Boolean.parseBoolean(parameters.get(RodaConstants.PLUGIN_PARAMS_BOOLEAN_VALUE));
      } catch (NumberFormatException e) {
        // do nothing
      }
    }
  }

  @Override
  public Report execute(IndexService index, ModelService model, StorageService storage, List<Agent> list)
    throws PluginException {

    CloseableIterable<Resource> listResourcesUnderDirectory = null;
    try {
      boolean recursive = false;
      listResourcesUnderDirectory = storage.listResourcesUnderContainer(ModelUtils.getAgentContainerPath(), recursive);
      LOGGER.debug("Reindexing all agents under {}", ModelUtils.getAgentContainerPath());

      for (Resource resource : listResourcesUnderDirectory) {
        if (!resource.isDirectory()) {
          Binary binary = (Binary) resource;
          InputStream inputStream = binary.getContent().createInputStream();
          String jsonString = IOUtils.toString(inputStream);
          Agent agent = JsonUtils.getObjectFromJson(jsonString, Agent.class);
          IOUtils.closeQuietly(inputStream);
          index.reindexAgent(agent);
        }
      }

    } catch (NotFoundException | GenericException | AuthorizationDeniedException | RequestNotValidException
      | IOException e) {
      LOGGER.error("Error reindexing agents", e);
    } finally {
      IOUtils.closeQuietly(listResourcesUnderDirectory);
    }

    return new Report();

  }

  @Override
  public Report beforeAllExecute(IndexService index, ModelService model, StorageService storage)
    throws PluginException {
    if (clearIndexes) {
      LOGGER.debug("Clearing indexes");
      try {
        index.clearIndex(RodaConstants.INDEX_AGENT);
      } catch (GenericException e) {
        throw new PluginException("Error clearing index", e);
      }
    } else {
      LOGGER.debug("Skipping clear indexes");
    }

    return new Report();
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
      index.optimizeIndex(RodaConstants.INDEX_AGENT);
    } catch (GenericException e) {
      throw new PluginException("Error optimizing index", e);
    }

    return new Report();
  }

  @Override
  public Plugin<Agent> cloneMe() {
    return new ReindexAgentPlugin();
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
    return "Reindex all agents";
  }

  @Override
  public String getPreservationEventSuccessMessage() {
    return "All agents reindexing run successfully";
  }

  @Override
  public String getPreservationEventFailureMessage() {
    return "All agents reindexing failed";
  }

}
