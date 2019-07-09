/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.roda.core.data.common.RodaConstants.PreservationAgentType;
import org.roda.core.data.exceptions.InvalidParameterException;
import org.roda.core.data.v2.IsRODAObject;
import org.roda.core.data.v2.jobs.PluginParameter;
import org.roda.core.plugins.orchestrate.JobPluginInfo;
import org.roda.core.plugins.plugins.notifications.JobNotification;

public abstract class AbstractPlugin<T extends IsRODAObject> implements Plugin<T> {

  private List<PluginParameter> parameters = new ArrayList<>();
  private Map<String, String> parameterValues = new HashMap<>();
  private String version = null;
  private JobPluginInfo jobPluginInfo;

  @Override
  public void injectJobPluginInfo(JobPluginInfo jobPluginInfo) {
    this.jobPluginInfo = jobPluginInfo;
  }

  @Override
  public <T1 extends JobPluginInfo> T1 getJobPluginInfo(Class<T1> jobPluginInfoClass) {
    return jobPluginInfoClass.cast(jobPluginInfo);
  }

  @Override
  public PreservationAgentType getAgentType() {
    return PreservationAgentType.SOFTWARE;
  }

  @Override
  public List<PluginParameter> getParameters() {
    return parameters;
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

  @Override
  public String getVersion() {
    if (version == null) {
      version = getVersionImpl();
    }
    return version;
  }

  public abstract String getVersionImpl();

  @Override
  public List<JobNotification> getNotifications() {
    return new ArrayList<>();
  }

}
