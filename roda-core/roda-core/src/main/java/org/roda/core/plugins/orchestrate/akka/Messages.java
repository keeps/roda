/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.orchestrate.akka;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;

import org.roda.core.data.v2.jobs.Job.JOB_STATE;
import org.roda.core.plugins.Plugin;
import org.roda.core.plugins.orchestrate.JobPluginInfo;

public class Messages {
  public static final class JobInfoUpdated implements Serializable {
    private static final long serialVersionUID = -6918015956027259760L;

    public Plugin<?> plugin;
    public JobPluginInfo jobPluginInfo;

    public JobInfoUpdated(Plugin<?> plugin, JobPluginInfo jobPluginInfo) {
      this.plugin = plugin;
      this.jobPluginInfo = jobPluginInfo;
    }

    @Override
    public String toString() {
      return "JobInfoMessage [plugin=" + plugin + ", jobPluginInfo=" + jobPluginInfo + "]";
    }
  }

  public static final class JobStateUpdated implements Serializable {
    private static final long serialVersionUID = 1946036502369851214L;

    private Plugin<?> plugin;
    private JOB_STATE state;
    private Optional<String> stateDatails;

    public JobStateUpdated(Plugin<?> plugin, JOB_STATE state) {
      this.plugin = plugin;
      this.state = state;
      this.stateDatails = Optional.empty();
    }

    public JobStateUpdated(Plugin<?> plugin, JOB_STATE state, Optional<String> stateDatails) {
      this.plugin = plugin;
      this.state = state;
      this.stateDatails = stateDatails;
    }

    public JobStateUpdated(Plugin<?> plugin, JOB_STATE state, Throwable throwable) {
      this.plugin = plugin;
      this.state = state;
      this.stateDatails = Optional.ofNullable(throwable.getClass().getName() + ": " + throwable.getMessage());
    }

    public Plugin<?> getPlugin() {
      return plugin;
    }

    public JOB_STATE getState() {
      return state;
    }

    public Optional<String> getStateDatails() {
      return stateDatails;
    }

    @Override
    public String toString() {
      return "JobIsDone [plugin=" + plugin + ", state=" + state + "]";
    }

  }

  public static class PluginToBeExecuted<T extends Serializable> implements Serializable {
    private static final long serialVersionUID = -5214600055070295410L;

    private List<T> list;
    private Plugin<T> plugin;

    public PluginToBeExecuted(List<T> list, Plugin<T> plugin) {
      this.list = list;
      this.plugin = plugin;
    }

    public List<T> getList() {
      return list;
    }

    public void setList(List<T> list) {
      this.list = list;
    }

    public Plugin<T> getPlugin() {
      return plugin;
    }

    public void setPlugin(Plugin<T> plugin) {
      this.plugin = plugin;
    }

    @Override
    public String toString() {
      return "PluginToBeExecuted [list=" + list + ", plugin=" + plugin + "]";
    }

  }

  public static class PluginExecuteIsDone implements Serializable {
    private static final long serialVersionUID = -8701179264086005994L;

    private Plugin<?> plugin;
    private boolean withError;

    public PluginExecuteIsDone(Plugin<?> plugin, boolean withError) {
      this.plugin = plugin;
      this.withError = withError;
    }

    public Plugin<?> getPlugin() {
      return plugin;
    }

    public void setPlugin(Plugin<?> plugin) {
      this.plugin = plugin;
    }

    public boolean isWithError() {
      return withError;
    }

    public void setWithError(boolean withError) {
      this.withError = withError;
    }

    @Override
    public String toString() {
      return "PluginExecuteIsDone [plugin=" + plugin + ", withError=" + withError + "]";
    }
  }
}
