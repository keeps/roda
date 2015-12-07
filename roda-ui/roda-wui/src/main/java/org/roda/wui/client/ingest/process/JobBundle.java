package org.roda.wui.client.ingest.process;

import java.io.Serializable;
import java.util.List;

import org.roda.core.data.PluginInfo;
import org.roda.core.data.v2.Job;

public class JobBundle implements Serializable {

  private static final long serialVersionUID = 1473697025923496778L;

  private Job job;
  private List<PluginInfo> pluginsInfo;

  public JobBundle() {
    super();
  }

  public JobBundle(Job job, List<PluginInfo> pluginsInfo) {
    super();
    this.job = job;
    this.pluginsInfo = pluginsInfo;
  }

  public Job getJob() {
    return job;
  }

  public void setJob(Job job) {
    this.job = job;
  }

  public List<PluginInfo> getPluginsInfo() {
    return pluginsInfo;
  }

  public void setPluginsInfo(List<PluginInfo> pluginsInfo) {
    this.pluginsInfo = pluginsInfo;
  }

}
