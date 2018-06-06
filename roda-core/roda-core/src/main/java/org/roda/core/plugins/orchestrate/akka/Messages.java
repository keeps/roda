/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.orchestrate.akka;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.roda.core.data.v2.IsRODAObject;
import org.roda.core.data.v2.LiteOptionalWithCause;
import org.roda.core.data.v2.SerializableOptional;
import org.roda.core.data.v2.jobs.Job.JOB_STATE;
import org.roda.core.plugins.Plugin;
import org.roda.core.plugins.orchestrate.JobPluginInfo;
import org.roda.core.util.IdUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import akka.actor.ActorRef;

public class Messages {
  private static final Logger LOGGER = LoggerFactory.getLogger(Messages.class);

  private Messages() {
    // do nothing
  }

  public abstract static class AbstractMessage implements Serializable {
    private static final long serialVersionUID = 1898368418865765060L;
    private String uuid;
    private long creationTime;

    private AbstractMessage() {
      if (LOGGER.isTraceEnabled()) {
        uuid = IdUtils.createUUID();
        LOGGER.trace("{} Created message {}", uuid, getClass().getSimpleName());
      }

      creationTime = System.currentTimeMillis();
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

    public long getTimeSinceCreation() {
      return System.currentTimeMillis() - creationTime;
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
    private String plugin;

    public JobsManagerJobEnded(String jobId, String plugin) {
      super();
      this.jobId = jobId;
      this.plugin = plugin;
    }

    public String getJobId() {
      return jobId;
    }

    public String getPlugin() {
      return plugin;
    }

    @Override
    public String toString() {
      return "JobsManagerJobEnded [jobId=" + jobId + ", plugin=" + plugin + "]";
    }
  }

  public static final class JobsManagerAcquireLock extends AbstractMessage {
    private static final long serialVersionUID = 4924002662559968741L;

    private List<String> lites;
    private boolean waitForLockIfLocked;
    private Date expireDate;
    private ActorRef sender;
    private String requestUuid;

    public JobsManagerAcquireLock(List<String> lites, boolean waitForLockIfLocked, int secondsToExpire,
      String requestUuid) {
      super();
      this.lites = lites;
      this.waitForLockIfLocked = waitForLockIfLocked;
      this.expireDate = new Date(new Date().getTime() + (secondsToExpire * 1000L));
      this.requestUuid = requestUuid;
    }

    public List<String> getLites() {
      return lites;
    }

    public ActorRef getSender() {
      return sender;
    }

    public JobsManagerAcquireLock setSender(ActorRef sender) {
      this.sender = sender;
      return this;
    }

    public boolean isWaitForLockIfLocked() {
      return waitForLockIfLocked;
    }

    public Date getExpireDate() {
      return expireDate;
    }

    public String getRequestUuid() {
      return requestUuid;
    }

    @Override
    public String toString() {
      return "JobsManagerAcquireLock [lites=" + lites + ", waitForLockIfLocked=" + waitForLockIfLocked + ", expireDate="
        + expireDate + ", requestUuid=" + requestUuid + "]";
    }

  }

  public static final class JobsManagerReleaseLock extends AbstractMessage {
    private static final long serialVersionUID = 4924002662559968741L;

    private List<String> lites;
    private String requestUuid;

    public JobsManagerReleaseLock(List<String> lites, String requestUuid) {
      super();
      this.lites = lites;
      this.requestUuid = requestUuid;
    }

    public List<String> getLites() {
      return lites;
    }

    public String getRequestUuid() {
      return requestUuid;
    }

    @Override
    public String toString() {
      return "JobsManagerReleaseLock [lites=" + lites + ", requestUuid=" + requestUuid + "]";
    }

  }

  public static final class JobsManagerReplyToAcquireLock extends AbstractMessage {
    private static final long serialVersionUID = 4924002662559968741L;

    private List<String> lites;

    public JobsManagerReplyToAcquireLock(List<String> lites) {
      super();
      this.lites = lites;
    }

    public List<String> getLites() {
      return lites;
    }

    @Override
    public String toString() {
      return "JobsManagerReplyToAcquireLock [lites=" + lites + "]";
    }
  }

  public static final class JobsManagerReplyToReleaseLock extends AbstractMessage {
    private static final long serialVersionUID = 4924002662559968741L;

    private List<String> lites;

    public JobsManagerReplyToReleaseLock(List<String> lites) {
      super();
      this.lites = lites;
    }

    public List<String> getLites() {
      return lites;
    }

    @Override
    public String toString() {
      return "JobsManagerReplyToReleaseLock [lites=" + lites + "]";
    }
  }

  public static final class JobsManagerNotLockableAtTheTime extends AbstractMessage {
    private static final long serialVersionUID = -2313831907910175641L;

    private String msg;

    public JobsManagerNotLockableAtTheTime() {
      super();
      this.msg = "";
    }

    public JobsManagerNotLockableAtTheTime(String msg) {
      super();
      this.msg = msg;
    }

    @Override
    public String toString() {
      return "JobsManagerUnlockableAtTheTime [msg=" + msg + "]";
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
    private SerializableOptional<String> stateDatails;

    public JobStateUpdated(Plugin<?> plugin, JOB_STATE state) {
      this(plugin, state, Optional.empty());
    }

    public JobStateUpdated(Plugin<?> plugin, JOB_STATE state, Optional<String> stateDatails) {
      super();
      this.plugin = plugin;
      this.state = state;
      this.stateDatails = SerializableOptional.setOptional(stateDatails);
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
      return stateDatails.getOptional();
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

    private List<LiteOptionalWithCause> list;
    private boolean hasBeenForwarded = false;

    public PluginExecuteIsReady(Plugin<T> plugin, List<LiteOptionalWithCause> list) {
      super(plugin);
      this.list = list;
    }

    public List<LiteOptionalWithCause> getList() {
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
