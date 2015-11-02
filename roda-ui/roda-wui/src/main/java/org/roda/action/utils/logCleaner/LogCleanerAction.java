package org.roda.action.utils.logCleaner;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.SolrServerException;
import org.roda.action.orchestrate.Plugin;
import org.roda.action.orchestrate.PluginException;
import org.roda.core.common.InvalidParameterException;
import org.roda.core.data.PluginParameter;
import org.roda.core.data.Report;
import org.roda.index.IndexService;
import org.roda.model.AIP;
import org.roda.model.ModelService;
import org.roda.storage.StorageService;

public class LogCleanerAction implements Plugin<AIP> {
  private static final Logger LOGGER = Logger.getLogger(LogCleanerAction.class);

  @Override
  public void init() throws PluginException {
    // TODO Auto-generated method stub

  }

  @Override
  public void shutdown() {
    // TODO Auto-generated method stub

  }

  @Override
  public String getName() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String getVersion() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String getDescription() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public List<PluginParameter> getParameters() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Map<String, String> getParameterValues() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void setParameterValues(Map<String, String> parameters) throws InvalidParameterException {
    // TODO Auto-generated method stub

  }

  // TODO time interval must be a plugin configuration...
  @Override
  public Report execute(IndexService index, ModelService model, StorageService storage, List<AIP> list)
    throws PluginException {
    Calendar cal = Calendar.getInstance();
    // FIXME this value (6) should be a parameter
    cal.add(Calendar.MONTH, -6);
    Date until = cal.getTime();
    try {
      index.deleteActionLog(until);
    } catch (SolrServerException | IOException e) {
      LOGGER.error("Error deleting actionlog until " + until);
    }
    return null;
  }

  @Override
  public Report beforeExecute(IndexService index, ModelService model, StorageService storage) throws PluginException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Report afterExecute(IndexService index, ModelService model, StorageService storage) throws PluginException {
    // TODO Auto-generated method stub
    return null;
  }

}
