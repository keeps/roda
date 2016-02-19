package org.roda.core.plugins;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.InvalidParameterException;
import org.roda.core.data.v2.jobs.PluginParameter;

public abstract class AbstractPlugin<T extends Serializable> implements Plugin<T> {

  private List<PluginParameter> pluginParameters = new ArrayList<PluginParameter>();
  private Map<String, String> parameterValues = new HashMap<String, String>();

  @Override
  public String getAgentType() {
    return RodaConstants.PRESERVATION_AGENT_TYPE_SOFTWARE;
  }

  @Override
  public List<PluginParameter> getParameters() {
    return pluginParameters;
  }

  @Override
  public Map<String, String> getParameterValues() {
    return parameterValues;
  }

  @Override
  public void setParameterValues(Map<String, String> parameters) throws InvalidParameterException {
    if (parameters != null) {
      this.parameterValues = parameters;
    }
  }

}
