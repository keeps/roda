/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.ingest.process;

import java.io.Serializable;
import java.util.List;

import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.PluginInfo;

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
