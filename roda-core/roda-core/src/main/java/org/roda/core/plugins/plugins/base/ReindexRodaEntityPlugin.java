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
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.common.RodaConstants.PreservationEventType;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.InvalidParameterException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.IsRODAObject;
import org.roda.core.data.v2.agents.Agent;
import org.roda.core.data.v2.formats.Format;
import org.roda.core.data.v2.jobs.PluginType;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.data.v2.notifications.Notification;
import org.roda.core.data.v2.risks.Risk;
import org.roda.core.data.v2.risks.RiskIncidence;
import org.roda.core.index.IndexService;
import org.roda.core.index.utils.SolrUtils;
import org.roda.core.model.ModelService;
import org.roda.core.plugins.AbstractPlugin;
import org.roda.core.plugins.Plugin;
import org.roda.core.plugins.PluginException;
import org.roda.core.storage.StorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReindexRodaEntityPlugin<T extends IsRODAObject> extends AbstractPlugin<T> {
  private static final Logger LOGGER = LoggerFactory.getLogger(ReindexRodaEntityPlugin.class);

  private boolean clearIndexes = true;
  private boolean recursiveListing = false;
  private Class<T> clazz = null;

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
    return "Reindex Roda entity";
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
    if (parameters != null) {
      if (parameters.get(RodaConstants.PLUGIN_PARAMS_CLEAR_INDEXES) != null) {
        clearIndexes = Boolean.parseBoolean(parameters.get(RodaConstants.PLUGIN_PARAMS_CLEAR_INDEXES));
      }
      if (parameters.get(RodaConstants.PLUGIN_PARAMS_CLASS_CANONICAL_NAME) != null) {
        try {
          String classCanonicalName = parameters.get(RodaConstants.PLUGIN_PARAMS_CLASS_CANONICAL_NAME);
          clazz = (Class<T>) Class.forName(classCanonicalName);
        } catch (ClassNotFoundException e) {
          // do nothing
        }
      }
      if (parameters.get(RodaConstants.PLUGIN_PARAMS_RECURSIVE_LISTING) != null) {
        recursiveListing = Boolean.parseBoolean(parameters.get(RodaConstants.PLUGIN_PARAMS_RECURSIVE_LISTING));
      }
    }
  }

  @Override
  public Report execute(IndexService index, ModelService model, StorageService storage, List<T> list)
    throws PluginException {

    LOGGER.debug("Reindexing Roda {}", clazz.getSimpleName());
    try {
      index.reindex(storage, clazz);
    } catch (NotFoundException | GenericException | AuthorizationDeniedException | RequestNotValidException
      | IOException e) {
      LOGGER.error("Error reindexing {}: ", clazz.getSimpleName(), e);
    }

    return new Report();
  }

  @Override
  public Report beforeAllExecute(IndexService index, ModelService model, StorageService storage)
    throws PluginException {
    if (clearIndexes) {
      LOGGER.debug("Clearing indexes");
      try {
        index.clearIndex(SolrUtils.getIndexName(clazz));
      } catch (GenericException e) {
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
    try {
      index.optimizeIndex(SolrUtils.getIndexName(clazz));
    } catch (GenericException e) {
      throw new PluginException("Error optimizing index", e);
    }

    return new Report();
  }

  @Override
  public Plugin<T> cloneMe() {
    return new ReindexRodaEntityPlugin<T>();
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
    return "Reindex Roda entity " + clazz.getSimpleName();
  }

  @Override
  public String getPreservationEventSuccessMessage() {
    return "All " + clazz.getSimpleName() + " were reindexed with success";
  }

  @Override
  public String getPreservationEventFailureMessage() {
    return "An error occured while reindexing all " + clazz.getSimpleName();
  }

  @Override
  public List<String> getCategories() {
    return Arrays.asList(RodaConstants.PLUGIN_CATEGORY_NOT_LISTABLE);
  }

  @Override
  public List<Class<T>> getObjectClasses() {
    List<Class<? extends IsRODAObject>> list = new ArrayList<>();
    list.add(Agent.class);
    list.add(Format.class);
    list.add(Notification.class);
    list.add(Risk.class);
    list.add(RiskIncidence.class);
    return (List) list;
  }

}
