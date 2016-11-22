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
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.roda.core.data.v2.IsRODAObject;
import org.roda.core.data.v2.jobs.Job.JOB_STATE;
import org.roda.core.plugins.Plugin;
import org.roda.core.plugins.orchestrate.JobPluginInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Messages {
  private static final Logger LOGGER = LoggerFactory.getLogger(Messages.class);

  private static abstract class AbstractMessage implements Serializable {
    private static final long serialVersionUID = 1898368418865765060L;
    private String uuid;

    private AbstractMessage() {
      if (LOGGER.isTraceEnabled()) {
        uuid = UUID.randomUUID().toString();
        LOGGER.trace("{} Created message {}", uuid, getClass().getSimpleName());
      }
    }

    public void logProcessingStarted() {
      if (LOGGER.isTraceEnabled()) {
        LOGGER.trace("{} Started processing message {} [{}]", uuid, getClass().getSimpleName(), toString());
      }
    }

    public void logProcessingEnded() {
      if (LOGGER.isTraceEnabled()) {
        LOGGER.trace("{} Ended processing message {}", uuid, getClass().getSimpleName());
      }
    }
  }

  /*-------------------- JOB MANAGER RELATED STATIC CLASSES --------------------*/
  public static final class JobsManagerTick extends AbstractMessage {
    private static final long serialVersionUID = -2514581679498648676L;

    public JobsManagerTick() {
      super();
    }

    @Override
    public String toString() {
      return "JobManagerTick []";
    }
  }

  public static final class JobsManagerJobEnded extends AbstractMessage {
    private static final long serialVersionUID = -2514581679498648676L;

    private String jobId;

    public JobsManagerJobEnded(String jobId) {
      super();
      this.jobId = jobId;
    }

    public String getJobId() {
      return jobId;
    }

    @Override
    public String toString() {
      return "JobsManagerJobEnded [jobId=" + jobId + "]";
    }
  }

  /*-------------------- JOB STATE RELATED STATIC CLASSES --------------------*/
  public static final class JobInfoUpdated extends AbstractMessage {
    private static final long serialVersionUID = -6918015956027259760L;

    private Plugin<?> plugin;
    private JobPluginInfo jobPluginInfo;

    public JobInfoUpdated(Plugin<?> plugin, JobPluginInfo jobPluginInfo) {
      super();
      this.plugin = plugin;
      this.jobPluginInfo = jobPluginInfo;
    }

    public Plugin<?> getPlugin() {
      return plugin;
    }

    public JobPluginInfo getJobPluginInfo() {
      return jobPluginInfo;
    }

    @Override
    public String toString() {
      return "JobInfoUpdated [plugin=" + plugin + ", jobPluginInfo=" + jobPluginInfo + "]";
    }
  }

  public static abstract class JobPartialUpdate extends AbstractMessage {
    private static final long serialVersionUID = 4722216970884172260L;

    @Override
    public String toString() {
      return "JobPartialUpdate []";
    }
  }

  public static class JobSourceObjectsUpdated extends JobPartialUpdate {
    private static final long serialVersionUID = -8395563279621159731L;

    private Map<String, String> oldToNewIds;

    public JobSourceObjectsUpdated(Map<String, String> oldToNewIds) {
      super();
      this.oldToNewIds = oldToNewIds;
    }

    public Map<String, String> getOldToNewIds() {
      return oldToNewIds;
    }

    @Override
    public String toString() {
      return "JobSourceObjectsUpdated [oldToNewIds=" + oldToNewIds + "]";
    }
  }

  public static final class JobStateUpdated extends JobPartialUpdate {
    private static final long serialVersionUID = 1946036502369851214L;

    private Plugin<?> plugin;
    private JOB_STATE state;
    private Optional<String> stateDatails;

    public JobStateUpdated(Plugin<?> plugin, JOB_STATE state) {
      this(plugin, state, Optional.empty());
    }

    public JobStateUpdated(Plugin<?> plugin, JOB_STATE state, Optional<String> stateDatails) {
      super();
      this.plugin = plugin;
      this.state = state;
      this.stateDatails = stateDatails;
    }

    public JobStateUpdated(Plugin<?> plugin, JOB_STATE state, Throwable throwable) {
      this(plugin, state, Optional.of(throwable.getClass().getName() + ": " + throwable.getMessage()));
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
      return "JobStateUpdated [plugin=" + plugin + ", state=" + state + ", stateDatails=" + stateDatails + "]";
    }
  }

  public static final class JobInitEnded extends AbstractMessage {
    private static final long serialVersionUID = 5040958276243865900L;

    public JobInitEnded() {
      super();
    }

    @Override
    public String toString() {
      return "JobInitEnded []";
    }
  }

  public static class JobCleanup extends AbstractMessage {
    private static final long serialVersionUID = -5175825019027462407L;

    public JobCleanup() {
      super();
    }

    @Override
    public String toString() {
      return "JobCleanup []";
    }
  }

  public static class JobStop extends AbstractMessage {
    private static final long serialVersionUID = -8806029242967727412L;

    public JobStop() {
      super();
    }

    @Override
    public String toString() {
      return "JobStop []";
    }
  }

  /*-------------------- PLUGIN STATE TRANSITIONS RELATED STATIC CLASSES --------------------*/

  private static class PluginMethodIsReady<T extends IsRODAObject> extends AbstractMessage {
    private static final long serialVersionUID = -5214600055070295410L;

    private Plugin<T> plugin;

    public PluginMethodIsReady(Plugin<T> plugin) {
      super();
      this.plugin = plugin;
    }

    public Plugin<T> getPlugin() {
      return plugin;
    }

    @Override
    public String toString() {
      return "PluginMethodIsReady [plugin=" + plugin + "]";
    }
  }

  private static class PluginMethodIsDone extends AbstractMessage {
    private static final long serialVersionUID = -8701179264086005994L;

    private Plugin<?> plugin;
    private boolean withError;

    public PluginMethodIsDone(Plugin<?> plugin, boolean withError) {
      super();
      this.plugin = plugin;
      this.withError = withError;
    }

    public Plugin<?> getPlugin() {
      return plugin;
    }

    public boolean isWithError() {
      return withError;
    }

    @Override
    public String toString() {
      return "PluginMethodIsDone [plugin=" + plugin + ", withError=" + withError + "]";
    }
  }

  public static class PluginBeforeAllExecuteIsReady<T extends IsRODAObject> extends PluginMethodIsReady<T> {
    private static final long serialVersionUID = -7730727049162062388L;

    public PluginBeforeAllExecuteIsReady(Plugin<T> plugin) {
      super(plugin);
    }

    @Override
    public String toString() {
      return "PluginBeforeAllExecuteIsReady [getPlugin()=" + getPlugin() + "]";
    }
  }

  public static class PluginBeforeAllExecuteIsDone extends PluginMethodIsDone {
    private static final long serialVersionUID = 7449486178368177015L;

    public PluginBeforeAllExecuteIsDone(Plugin<?> plugin, boolean withError) {
      super(plugin, withError);
    }

    @Override
    public String toString() {
      return "PluginBeforeAllExecuteIsDone [getPlugin()=" + getPlugin() + ", isWithError()=" + isWithError() + "]";
    }
  }

  public static class PluginExecuteIsReady<T extends IsRODAObject> extends PluginMethodIsReady<T> {
    private static final long serialVersionUID = 1821489252490235130L;

    private List<T> list;
    private boolean hasBeenForwarded = false;

    public PluginExecuteIsReady(Plugin<T> plugin, List<T> list) {
      super(plugin);
      this.list = list;
    }

    public List<T> getList() {
      return list;
    }

    public void setHasBeenForwarded() {
      this.hasBeenForwarded = true;
    }

    @Override
    public String toString() {
      return "PluginExecuteIsReady [list=" + list + ", hasBeenForwarded=" + hasBeenForwarded + ", getPlugin()="
        + getPlugin() + "]";
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

  public static class PluginAfterAllExecuteIsReady<T extends IsRODAObject> extends PluginMethodIsReady<T> {
    private static final long serialVersionUID = 8852688692792086166L;

    public PluginAfterAllExecuteIsReady(Plugin<T> plugin) {
      super(plugin);
    }

    @Override
    public String toString() {
      return "PluginAfterAllExecuteIsReady [plugin=" + getPlugin() + "]";
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
