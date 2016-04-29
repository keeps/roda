/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.planning;

import java.io.Serializable;
import java.util.List;

import org.roda.core.data.v2.jobs.PluginInfo;

public class RiskJobBundle implements Serializable {

  private static final long serialVersionUID = 8853301017531332147L;

  private List<PluginInfo> plugins;

  public RiskJobBundle() {
    super();
  }

  public RiskJobBundle(List<PluginInfo> plugins) {
    super();
    this.plugins = plugins;
  }

  public List<PluginInfo> getPlugins() {
    return plugins;
  }

  public void setPlugins(List<PluginInfo> plugins) {
    this.plugins = plugins;
  }

}
