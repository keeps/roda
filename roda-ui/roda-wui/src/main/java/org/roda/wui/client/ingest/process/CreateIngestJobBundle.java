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

import org.roda.core.data.v2.jobs.PluginInfo;

public class CreateIngestJobBundle implements Serializable {

  private static final long serialVersionUID = 1473697025923496778L;

  private List<PluginInfo> ingestPlugins;
  private List<PluginInfo> sipToAipPlugins;

  public CreateIngestJobBundle() {
    super();
  }

  public CreateIngestJobBundle(List<PluginInfo> ingestPlugins, List<PluginInfo> sipToAipPlugins) {
    super();
    this.ingestPlugins = ingestPlugins;
    this.sipToAipPlugins = sipToAipPlugins;
  }

  public List<PluginInfo> getIngestPlugins() {
    return ingestPlugins;
  }

  public void setIngestPlugins(List<PluginInfo> ingestPlugins) {
    this.ingestPlugins = ingestPlugins;
  }

  public List<PluginInfo> getSipToAipPlugins() {
    return sipToAipPlugins;
  }

  public void setSipToAipPlugins(List<PluginInfo> sipToAipPlugins) {
    this.sipToAipPlugins = sipToAipPlugins;
  }

}
