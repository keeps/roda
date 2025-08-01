/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.base.maintenance;

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
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.InvalidParameterException;
import org.roda.core.data.v2.LiteOptionalWithCause;
import org.roda.core.data.v2.Void;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ActionLogCleanerPlugin extends AbstractPlugin<Void> {
  private static final Logger LOGGER = LoggerFactory.getLogger(ActionLogCleanerPlugin.class);
  private static final Map<String, PluginParameter> pluginParameters = new HashMap<>();

  static {
    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_DELETE_OLDER_THAN_X_DAYS,
      PluginParameter.getBuilder(RodaConstants.PLUGIN_PARAMS_DELETE_OLDER_THAN_X_DAYS,
        "Delete older than X days", PluginParameterType.INTEGER).withDefaultValue("90").isMandatory(false)
        .isReadOnly(false).withDescription("The plugin will delete all logs older than the specified number of days.")
        .build());
  }

  private int deleteOlderThanXDays = 90;

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
    return "Audit Log Truncator";
  }

  @Override
  public String getVersionImpl() {
    return "1.0";
  }

  @Override
  public String getDescription() {
    return "The Audit Log Truncator removes all entries in the audit log that are older than the specified number of"
      + " days. The log is preserved as external physical files, however older entries will not be displayed in the graphical user interface. "
      + "To access older log entries, one needs access to the storage layer of the repository server.\nAudit log truncation "
      + "automatically frees index space and improves performance of the repository as a whole.";
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

        this.deleteOlderThanXDays = Math.max(deleteDays, 0);
      } catch (NumberFormatException e) {
        // do nothing
      }
    }
  }

  @Override
  public Report execute(IndexService index, ModelService model,
    List<LiteOptionalWithCause> entries) throws PluginException {

    Report report = PluginHelper.initPluginReportItem(this, Report.NO_OUTCOME_OBJECT_ID, Report.NO_SOURCE_OBJECT_ID);
    report.setPluginState(PluginState.SUCCESS);

    if (deleteOlderThanXDays > 0) {
      Calendar cal = Calendar.getInstance();

      cal.add(Calendar.DAY_OF_YEAR, -1 * deleteOlderThanXDays);
      Date until = cal.getTime();
      try {
        index.deleteActionLog(until);
      } catch (SolrServerException | IOException | AuthorizationDeniedException e) {
        String errorMessage = "Error deleting audit logs until " + until;
        LOGGER.error(errorMessage);
        report.setPluginDetails(errorMessage);
        report.setPluginState(PluginState.FAILURE);
      }
    }

    return report;
  }

  @Override
  public Report beforeAllExecute(IndexService index, ModelService model)
    throws PluginException {
    // do nothing
    return null;
  }

  @Override
  public Report afterAllExecute(IndexService index, ModelService model) throws PluginException {
    LOGGER.debug("Optimizing indexes");
    try {
      index.optimizeIndex(RodaConstants.INDEX_ACTION_LOG);
    } catch (GenericException | AuthorizationDeniedException e) {
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
    return Arrays.asList(RodaConstants.PLUGIN_CATEGORY_MANAGEMENT, RodaConstants.PLUGIN_CATEGORY_MAINTENANCE);
  }

  @Override
  public List<Class<Void>> getObjectClasses() {
    return Arrays.asList(Void.class);
  }

}
