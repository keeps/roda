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
import org.roda.core.data.v2.formats.Format;
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

public class ReindexFormatPlugin extends AbstractPlugin<Format> {

  private static final Logger LOGGER = LoggerFactory.getLogger(ReindexFormatPlugin.class);
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
    return "Reindex Formats";
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
    if (parameters != null && parameters.get(RodaConstants.PLUGIN_PARAMS_BOOLEAN_VALUE) != null) {
      try {
        clearIndexes = Boolean.parseBoolean(parameters.get(RodaConstants.PLUGIN_PARAMS_BOOLEAN_VALUE));
      } catch (NumberFormatException e) {
        // do nothing
      }
    }
  }

  @Override
  public Report execute(IndexService index, ModelService model, StorageService storage, List<Format> list)
    throws PluginException {

    CloseableIterable<Resource> listResourcesUnderDirectory = null;
    try {
      boolean recursive = false;
      listResourcesUnderDirectory = storage.listResourcesUnderContainer(ModelUtils.getFormatContainerPath(), recursive);
      LOGGER.info("Reindexing all formats under " + ModelUtils.getFormatContainerPath());

      for (Resource resource : listResourcesUnderDirectory) {
        if (!resource.isDirectory()) {
          Binary binary = (Binary) resource;
          InputStream inputStream = binary.getContent().createInputStream();
          String jsonString = IOUtils.toString(inputStream);
          Format format = JsonUtils.getObjectFromJson(jsonString, Format.class);
          IOUtils.closeQuietly(inputStream);
          index.reindexFormat(format);
        }
      }

    } catch (NotFoundException | GenericException | AuthorizationDeniedException | RequestNotValidException
      | IOException e) {
      LOGGER.error("Error re-indexing formats", e);
    } finally {
      IOUtils.closeQuietly(listResourcesUnderDirectory);
    }

    return null;

  }

  @Override
  public Report beforeExecute(IndexService index, ModelService model, StorageService storage) throws PluginException {
    if (clearIndexes) {
      LOGGER.debug("Clearing indexes");
      try {
        index.clearIndex(RodaConstants.INDEX_FORMAT);
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
      index.optimizeIndex(RodaConstants.INDEX_FORMAT);
    } catch (GenericException e) {
      throw new PluginException("Error optimizing index", e);
    }

    return null;
  }

  @Override
  public Plugin<Format> cloneMe() {
    return new ReindexFormatPlugin();
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
    return "Reindex all formats";
  }

  @Override
  public String getPreservationEventSuccessMessage() {
    return "All formats reindexing run successfully";
  }

  @Override
  public String getPreservationEventFailureMessage() {
    return "All formats reindexing failed";
  }
}
