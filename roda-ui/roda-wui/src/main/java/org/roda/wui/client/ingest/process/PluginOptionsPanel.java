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

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.jobs.PluginInfo;
import org.roda.core.data.v2.jobs.PluginParameter;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;

public class PluginOptionsPanel extends Composite {

  private final List<PluginParameterPanel> panels;
  private final FlowPanel layout;
  private PluginInfo pluginInfo = null;

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
        PluginParameterPanel panel = new PluginParameterPanel(parameter, pluginInfo.getId());
        panels.add(panel);
        layout.add(panel);
      }
    }
  }

  public void setPluginInfo(PluginInfo pluginInfo) {
    this.pluginInfo = pluginInfo;
    update();
  }

  public Map<String, String> getValue() {
    Map<String, String> ret = new HashMap<>();

    for (PluginParameterPanel panel : panels) {
      String key = panel.getParameter().getId();
      if (panel.isConversionPanel()) {
        if (panel.getValue().equals(RodaConstants.PLUGIN_PARAMS_CONVERSION_DISSEMINATION)) {
          ret.put(key, panel.getDisseminationParameter().printAsParameter(panel.getValue()));
        } else {
          ret.put(key,
            panel.getRepresentationParameter().printAsParameter(RodaConstants.PLUGIN_PARAMS_CONVERSION_REPRESENTATION));
        }
        ret.put(RodaConstants.PLUGIN_PARAMS_CONVERSION_PROFILE, panel.getProfile());
      } else {
        String value = panel.getValue();
        if (value != null) {
          ret.put(key, value);
        }
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
}
