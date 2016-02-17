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
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.solr.client.solrj.SolrServerException;
import org.roda.core.RodaCoreFactory;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.InvalidParameterException;
import org.roda.core.data.v2.jobs.PluginParameter;
import org.roda.core.data.v2.jobs.PluginType;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.data.v2.log.LogEntry;
import org.roda.core.index.IndexService;
import org.roda.core.model.ModelService;
import org.roda.core.plugins.Plugin;
import org.roda.core.plugins.PluginException;
import org.roda.core.storage.StorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogCleanerPlugin implements Plugin<LogEntry> {
  private static final Logger LOGGER = LoggerFactory.getLogger(LogCleanerPlugin.class);
  private int deleteOlderThanXDays = RodaCoreFactory.getRodaConfigurationAsInt(0, "core", "actionlogs",
    "delete_older_than_x_days");

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
    return "Log entries cleaner";
  }
  
  @Override
  public String getAgentType(){
    return RodaConstants.PRESERVATION_AGENT_TYPE;
  }

  @Override
  public String getVersion() {
    return "1.0";
  }

  @Override
  public String getDescription() {
    return "Removes, from the index, all log entries older than " + deleteOlderThanXDays + " days.";
  }

  @Override
  public List<PluginParameter> getParameters() {
    // no parameters
    return new ArrayList<>();
  }

  @Override
  public Map<String, String> getParameterValues() {
    return new HashMap<>();
  }

  @Override
  public void setParameterValues(Map<String, String> parameters) throws InvalidParameterException {
    if (parameters != null && parameters.get(RodaConstants.PLUGIN_PARAMS_INT_VALUE) != null) {
      try {
        int deleteOlderThanXDays = Integer.parseInt(parameters.get(RodaConstants.PLUGIN_PARAMS_INT_VALUE));
        this.deleteOlderThanXDays = deleteOlderThanXDays;
      } catch (NumberFormatException e) {
        // do nothing
      }
    }
  }

  @Override
  public Report execute(IndexService index, ModelService model, StorageService storage, List<LogEntry> entries)
    throws PluginException {

    if (deleteOlderThanXDays > 0) {
      Calendar cal = Calendar.getInstance();

      cal.add(Calendar.DAY_OF_YEAR, -1 * deleteOlderThanXDays);
      Date until = cal.getTime();
      try {
        index.deleteActionLog(until);
      } catch (SolrServerException | IOException e) {
        LOGGER.error("Error deleting actionlog until " + until);
      }
    } else {
      // do nothing
    }

    return null;
  }

  @Override
  public Report beforeExecute(IndexService index, ModelService model, StorageService storage) throws PluginException {
    // do nothing
    return null;
  }

  @Override
  public Report afterExecute(IndexService index, ModelService model, StorageService storage) throws PluginException {
    // do nothing
    return null;
  }

  @Override
  public Plugin<LogEntry> cloneMe() {
    return new LogCleanerPlugin();
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
