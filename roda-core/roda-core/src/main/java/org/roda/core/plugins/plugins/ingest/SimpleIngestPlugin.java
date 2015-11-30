package org.roda.core.plugins.plugins.ingest;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.roda.core.RodaCoreFactory;
import org.roda.core.data.PluginParameter;
import org.roda.core.data.Report;
import org.roda.core.data.adapter.filter.Filter;
import org.roda.core.data.adapter.filter.OneOfManyFilterParameter;
import org.roda.core.data.adapter.sublist.Sublist;
import org.roda.core.data.common.InvalidParameterException;
import org.roda.core.data.common.NotFoundException;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.IndexResult;
import org.roda.core.data.v2.Job;
import org.roda.core.data.v2.TransferredResource;
import org.roda.core.index.IndexService;
import org.roda.core.index.IndexServiceException;
import org.roda.core.model.AIP;
import org.roda.core.model.ModelService;
import org.roda.core.plugins.Plugin;
import org.roda.core.plugins.PluginException;
import org.roda.core.plugins.plugins.antivirus.AntivirusPlugin;
import org.roda.core.plugins.plugins.base.AIPValidationPlugin;
import org.roda.core.storage.StorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimpleIngestPlugin implements Plugin<TransferredResource> {
  private static final Logger LOGGER = LoggerFactory.getLogger(SimpleIngestPlugin.class);

  private Map<String, String> parameters;

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
    return "Simple Ingest Plugin";
  }

  @Override
  public String getVersion() {
    return "1.0.0";
  }

  @Override
  public String getDescription() {
    return "Performs all the tasks needed to ingest an SIP into an AIP";
  }

  @Override
  public List<PluginParameter> getParameters() {
    return new ArrayList<PluginParameter>();
  }

  @Override
  public Map<String, String> getParameterValues() {
    return parameters;
  }

  @Override
  public void setParameterValues(Map<String, String> parameters) throws InvalidParameterException {
    this.parameters = parameters;
  }

  @Override
  public Report execute(IndexService index, ModelService model, StorageService storage, List<TransferredResource> list)
    throws PluginException {
    // 1) transform TransferredResource into an AIP
    List<AIP> aips = transformTransferredResourceIntoAnAIP(index, model, storage, list);
    // 2) do virus check
    doVirusCheck(index, model, storage, aips);
    // 3) verify if AIP is well formed
    verifyIfAipIsWellFormed(index, model, storage, aips);

    return null;
  }

  private List<AIP> transformTransferredResourceIntoAnAIP(IndexService index, ModelService model,
    StorageService storage, List<TransferredResource> transferredResources) {
    List<AIP> aips = new ArrayList<AIP>();
    // TODO the type of transferred resource should be obtained via plugin
    // parameters
    String pluginClassName = BagitToAIPPlugin.class.getName();
    Plugin<TransferredResource> plugin = (Plugin<TransferredResource>) RodaCoreFactory.getPluginManager()
      .getPlugin(pluginClassName);
    try {
      plugin.setParameterValues(getParameterValues());
      plugin.execute(index, model, storage, transferredResources);
    } catch (PluginException | InvalidParameterException e) {
      // FIXME handle failure
      LOGGER.error("Error executing plug-in", e);
    }

    try {
      Job job = index.retrieve(Job.class, getJobId());
      // FIXME this should be a constant and the maximum number of objects sent
      // to a plugin
      int maxAips = 200;
      IndexResult<AIP> aipsFromIndex = index.find(AIP.class,
        new Filter(
          new OneOfManyFilterParameter(RodaConstants.AIP_ID, new ArrayList<>(job.getObjectIdsToAipIds().values()))),
        null, new Sublist(0, maxAips));
      aips = aipsFromIndex.getResults();

    } catch (IndexServiceException | NotFoundException e) {
      LOGGER.error("Error getting AIPs from index", e);
    }

    return aips;
  }

  private void doVirusCheck(IndexService index, ModelService model, StorageService storage, List<AIP> aips) {
    String pluginClassName = AntivirusPlugin.class.getName();
    Plugin<AIP> plugin = (Plugin<AIP>) RodaCoreFactory.getPluginManager().getPlugin(pluginClassName);
    try {
      plugin.setParameterValues(getParameterValues());
      plugin.execute(index, model, storage, aips);
    } catch (PluginException | InvalidParameterException e) {
      // FIXME handle failure
      LOGGER.error("Error executing plug-in", e);
    }
  }

  private void verifyIfAipIsWellFormed(IndexService index, ModelService model, StorageService storage, List<AIP> aips) {
    String pluginClassName = AIPValidationPlugin.class.getName();
    Plugin<AIP> plugin = (Plugin<AIP>) RodaCoreFactory.getPluginManager().getPlugin(pluginClassName);
    try {
      plugin.setParameterValues(getParameterValues());
      plugin.execute(index, model, storage, aips);
    } catch (PluginException | InvalidParameterException e) {
      // FIXME handle failure
      LOGGER.error("Error executing plug-in", e);
    }
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
  public Plugin<TransferredResource> cloneMe() {
    return new SimpleIngestPlugin();
  }

  private String getJobId() {
    return parameters.get("job.id");
  }

}
