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
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.solr.client.solrj.SolrServerException;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.common.RodaConstants.PreservationEventType;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.InvalidParameterException;
import org.roda.core.data.v2.LiteOptionalWithCause;
import org.roda.core.data.v2.Void;
import org.roda.core.data.v2.jobs.PluginParameter;
import org.roda.core.data.v2.jobs.PluginParameter.PluginParameterType;
import org.roda.core.data.v2.jobs.PluginType;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.index.IndexService;
import org.roda.core.model.ModelService;
import org.roda.core.plugins.AbstractPlugin;
import org.roda.core.plugins.Plugin;
import org.roda.core.plugins.PluginException;
import org.roda.core.storage.StorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ActionLogCleanerPlugin extends AbstractPlugin<Void> {
  private static final Logger LOGGER = LoggerFactory.getLogger(ActionLogCleanerPlugin.class);
  private int deleteOlderThanXDays = 90;

  private static Map<String, PluginParameter> pluginParameters = new HashMap<>();
  static {
    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_DELETE_OLDER_THAN_X_DAYS,
      new PluginParameter(RodaConstants.PLUGIN_PARAMS_DELETE_OLDER_THAN_X_DAYS, "Delete older than X days",
        PluginParameterType.INTEGER, "90", false, false,
        "The plugin will delete all logs older than the specified number of days."));
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
    return "Activity log truncation";
  }

  @Override
  public String getVersionImpl() {
    return "1.0";
  }

  @Override
  public String getDescription() {
    return "Removes all entries in the activity log that are older than the specified number of"
      + " days. The log is preserved as external physical files, however older entries will not be displayed on the graphical user interface. "
      + "To access older log entries one needs access to the storage layer of the repository server.\nIf log entries were never deleted, it "
      + "would eventually fill the index with rarely used information. Activity log truncation automatically frees index space and improves "
      + "performance of the repository as a whole.";
  }

  @Override
  public List<PluginParameter> getParameters() {
    ArrayList<PluginParameter> parameters = new ArrayList<>();
    parameters.add(pluginParameters.get(RodaConstants.PLUGIN_PARAMS_DELETE_OLDER_THAN_X_DAYS));
    return parameters;
  }

  @Override
  public void setParameterValues(Map<String, String> parameters) throws InvalidParameterException {
    super.setParameterValues(parameters);
    if (parameters != null && parameters.get(RodaConstants.PLUGIN_PARAMS_DELETE_OLDER_THAN_X_DAYS) != null) {
      try {
        int deleteDays = Integer.parseInt(parameters.get(RodaConstants.PLUGIN_PARAMS_DELETE_OLDER_THAN_X_DAYS));

        if (deleteDays >= 0) {
          this.deleteOlderThanXDays = deleteDays;
        } else {
          this.deleteOlderThanXDays = 0;
        }
      } catch (NumberFormatException e) {
        // do nothing
      }
    }
  }

  @Override
  public Report execute(IndexService index, ModelService model, StorageService storage,
    List<LiteOptionalWithCause> entries) throws PluginException {

    if (deleteOlderThanXDays > 0) {
      Calendar cal = Calendar.getInstance();

      cal.add(Calendar.DAY_OF_YEAR, -1 * deleteOlderThanXDays);
      Date until = cal.getTime();
      try {
        index.deleteActionLog(until);
      } catch (SolrServerException | IOException e) {
        LOGGER.error("Error deleting actionlog until {}", until);
      }
    } else {
      // do nothing
    }

    return null;
  }

  @Override
  public Report beforeAllExecute(IndexService index, ModelService model, StorageService storage)
    throws PluginException {
    // do nothing
    return null;
  }

  @Override
  public Report afterAllExecute(IndexService index, ModelService model, StorageService storage) throws PluginException {
    LOGGER.debug("Optimizing indexes");
    try {
      index.optimizeIndex(RodaConstants.INDEX_ACTION_LOG);
    } catch (GenericException e) {
      throw new PluginException("Error optimizing index", e);
    }
    return null;
  }

  @Override
  public Plugin<Void> cloneMe() {
    return new ActionLogCleanerPlugin();
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
    return PreservationEventType.DELETION;
  }

  @Override
  public String getPreservationEventDescription() {
    return "Log entries were cleaned";
  }

  @Override
  public String getPreservationEventSuccessMessage() {
    return "Log entries were cleaned successfully";
  }

  @Override
  public String getPreservationEventFailureMessage() {
    return "Log entries cleaning failed";
  }

  @Override
  public List<String> getCategories() {
    return Arrays.asList(RodaConstants.PLUGIN_CATEGORY_MANAGEMENT);
  }

  @Override
  public List<Class<Void>> getObjectClasses() {
    return Arrays.asList(Void.class);
  }

}
