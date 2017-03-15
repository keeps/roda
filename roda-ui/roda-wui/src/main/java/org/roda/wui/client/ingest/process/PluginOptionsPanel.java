/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.ingest.process;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.roda.core.data.v2.jobs.PluginInfo;
import org.roda.core.data.v2.jobs.PluginParameter;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;

public class PluginOptionsPanel extends Composite {

  private PluginInfo pluginInfo = null;
  private List<PluginInfo> plugins = new ArrayList<>();

  private List<PluginParameterPanel> panels;

  private FlowPanel layout;

  public PluginOptionsPanel() {
    super();
    layout = new FlowPanel();
    initWidget(layout);
    panels = new ArrayList<>();
    layout.addStyleName("plugin-options");
  }

  private void update() {
    panels.clear();
    layout.clear();

    if (pluginInfo != null) {
      for (PluginParameter parameter : pluginInfo.getParameters()) {
        PluginParameterPanel panel = new PluginParameterPanel(parameter);
        panels.add(panel);
        layout.add(panel);
      }
    }
  }

  public PluginInfo getPluginInfo() {
    return pluginInfo;
  }

  public void setPluginInfo(PluginInfo pluginInfo) {
    this.pluginInfo = pluginInfo;
    update();
  }

  public Map<String, String> getValue() {
    Map<String, String> ret = new HashMap<>();

    for (PluginParameterPanel panel : panels) {
      String key = panel.getParameter().getId();
      String value = panel.getValue();
      if (value != null) {
        ret.put(key, value);
      }
    }

    return ret;
  }

  public List<PluginParameter> getMissingMandatoryParameters() {
    List<PluginParameter> missing = new ArrayList<>();

    for (PluginParameterPanel panel : panels) {
      PluginParameter parameter = panel.getParameter();
      String value = panel.getValue();

      if (parameter.isMandatory() && value == null) {
        missing.add(parameter);
      }
    }

    return missing;
  }

  public List<PluginInfo> getPlugins() {
    return plugins;
  }

  public void setPlugins(List<PluginInfo> plugins) {
    this.plugins = plugins;
  }

}
