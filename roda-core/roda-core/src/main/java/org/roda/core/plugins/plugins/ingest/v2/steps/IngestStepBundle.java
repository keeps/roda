/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.plugins.ingest.v2.steps;

import java.util.List;
import java.util.Map;

import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.SIPInformation;
import org.roda.core.data.v2.ip.TransferredResource;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.PluginParameter;
import org.roda.core.index.IndexService;
import org.roda.core.model.ModelService;
import org.roda.core.plugins.Plugin;
import org.roda.core.plugins.orchestrate.IngestJobPluginInfo;
import org.roda.core.storage.StorageService;

public class IngestStepBundle {
  private Plugin<?> ingestPlugin;
  private IndexService index;
  private ModelService model;
  private StorageService storage;
  private IngestJobPluginInfo jobPluginInfo;
  private PluginParameter pluginParameter;
  private Map<String, String> parameterValues;
  private List<TransferredResource> resources;
  private List<AIP> aips;
  private Job cachedJob;
  private SIPInformation sipInformation;

  public IngestStepBundle(Plugin<?> ingestPlugin, IndexService index, ModelService model, StorageService storage,
                          IngestJobPluginInfo jobPluginInfo, PluginParameter pluginParameter, Map<String, String> parameterValues,
                          List<TransferredResource> resources, List<AIP> aips, Job cachedJob, SIPInformation sipInformation) {
    this.ingestPlugin = ingestPlugin;
    this.index = index;
    this.model = model;
    this.storage = storage;
    this.jobPluginInfo = jobPluginInfo;
    this.pluginParameter = pluginParameter;
    this.parameterValues = parameterValues;
    this.resources = resources;
    this.aips = aips;
    this.cachedJob = cachedJob;
    this.sipInformation = sipInformation;
  }

  public Plugin<?> getIngestPlugin() {
    return ingestPlugin;
  }

  public void setIngestPlugin(Plugin<?> ingestPlugin) {
    this.ingestPlugin = ingestPlugin;
  }

  public IndexService getIndex() {
    return index;
  }

  public void setIndex(IndexService index) {
    this.index = index;
  }

  public ModelService getModel() {
    return model;
  }

  public void setModel(ModelService model) {
    this.model = model;
  }

  public StorageService getStorage() {
    return storage;
  }

  public void setStorage(StorageService storage) {
    this.storage = storage;
  }

  public IngestJobPluginInfo getJobPluginInfo() {
    return jobPluginInfo;
  }

  public void setJobPluginInfo(IngestJobPluginInfo jobPluginInfo) {
    this.jobPluginInfo = jobPluginInfo;
  }

  public PluginParameter getPluginParameter() {
    return pluginParameter;
  }

  public void setPluginParameter(PluginParameter pluginParameter) {
    this.pluginParameter = pluginParameter;
  }

  public Map<String, String> getParameterValues() {
    return parameterValues;
  }

  public void setParameterValues(Map<String, String> parameterValues) {
    this.parameterValues = parameterValues;
  }

  public List<TransferredResource> getResources() {
    return resources;
  }

  public void setResources(List<TransferredResource> resources) {
    this.resources = resources;
  }

  public List<AIP> getAips() {
    return aips;
  }

  public void setAips(List<AIP> aips) {
    this.aips = aips;
  }

  public Job getCachedJob() {
    return cachedJob;
  }

  public void setCachedJob(Job cachedJob) {
    this.cachedJob = cachedJob;
  }

  public SIPInformation getSipInformation() {
    return sipInformation;
  }

  public void setSipInformation(SIPInformation sipInformation) {
    this.sipInformation = sipInformation;
  }
}
