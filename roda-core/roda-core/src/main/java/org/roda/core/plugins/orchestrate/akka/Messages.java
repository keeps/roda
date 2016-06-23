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

  private static class PluginMethodIsReady<T extends Serializable> implements Serializable {
    private static final long serialVersionUID = -5214600055070295410L;

    private Plugin<T> plugin;

    public PluginMethodIsReady(Plugin<T> plugin) {
      this.plugin = plugin;
    }

    public Plugin<T> getPlugin() {
      return plugin;
    }

    public void setPlugin(Plugin<T> plugin) {
      this.plugin = plugin;
    }

    @Override
    public String toString() {
      return "PluginMethodIsReady [plugin=" + plugin + "]";
    }

  }

  public static class PluginExecuteIsReady<T extends Serializable> extends PluginMethodIsReady<T> {
    private static final long serialVersionUID = 1821489252490235130L;

    private List<T> list;

    public PluginExecuteIsReady(List<T> list, Plugin<T> plugin) {
      super(plugin);
      this.list = list;
    }

    public List<T> getList() {
      return list;
    }

    public void setList(List<T> list) {
      this.list = list;
    }

    @Override
    public String toString() {
      return "PluginExecuteIsReady [list=" + list + ", plugin=" + getPlugin() + "]";
    }
  }

  public static class PluginAfterAllExecuteIsReady<T extends Serializable> extends PluginMethodIsReady<T> {
    private static final long serialVersionUID = 1821489252490235130L;

    public PluginAfterAllExecuteIsReady(Plugin<T> plugin) {
      super(plugin);
    }

    @Override
    public String toString() {
      return "PluginAfterAllExecuteIsReady [plugin=" + getPlugin() + "]";
    }
  }

  private static class PluginMethodIsDone implements Serializable {
    private static final long serialVersionUID = -8701179264086005994L;

    private Plugin<?> plugin;
    private boolean withError;

    public PluginMethodIsDone(Plugin<?> plugin, boolean withError) {
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
      return "PluginMethodIsDone [plugin=" + plugin + ", withError=" + withError + "]";
    }
  }

  public static class PluginExecuteIsDone extends PluginMethodIsDone {
    private static final long serialVersionUID = -5136014936634139026L;

    public PluginExecuteIsDone(Plugin<?> plugin, boolean withError) {
      super(plugin, withError);
    }

    @Override
    public String toString() {
      return "PluginExecuteIsDone [getPlugin()=" + getPlugin() + ", isWithError()=" + isWithError() + "]";
    }
  }

  public static class PluginAfterAllExecuteIsDone extends PluginMethodIsDone {
    private static final long serialVersionUID = -5136014936634139026L;

    public PluginAfterAllExecuteIsDone(Plugin<?> plugin, boolean withError) {
      super(plugin, withError);
    }

    @Override
    public String toString() {
      return "PluginAfterAllExecuteIsDone [getPlugin()=" + getPlugin() + ", isWithError()=" + isWithError() + "]";
    }
  }
}
