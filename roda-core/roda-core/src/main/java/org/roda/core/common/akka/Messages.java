/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.common.akka;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.roda.core.data.v2.IsRODAObject;
import org.roda.core.data.v2.LiteOptionalWithCause;
import org.roda.core.data.v2.SerializableOptional;
import org.roda.core.data.v2.jobs.Job.JOB_STATE;
import org.roda.core.data.v2.user.Group;
import org.roda.core.data.v2.user.User;
import org.roda.core.plugins.Plugin;
import org.roda.core.plugins.orchestrate.JobPluginInfo;
import org.roda.core.util.IdUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import akka.actor.ActorRef;

public class Messages {
  private static final Logger LOGGER = LoggerFactory.getLogger(Messages.class);

  private static final Messages INSTANCE = new Messages();

  private Messages() {
    // do nothing
  }

  public abstract class AbstractMessage implements Serializable {
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
  public static JobsManagerTick newJobsManagerTick() {
    return INSTANCE.new JobsManagerTick();
  }

  public final class JobsManagerTick extends AbstractMessage {
    private static final long serialVersionUID = -2514581679498648676L;

    public JobsManagerTick() {
      super();
    }

    @Override
    public String toString() {
      return "JobManagerTick []";
    }
  }

  public static JobsManagerJobEnded newJobsManagerJobEnded(String jobId, String plugin) {
    return INSTANCE.new JobsManagerJobEnded(jobId, plugin);
  }

  public final class JobsManagerJobEnded extends AbstractMessage {
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

  public static JobsManagerAcquireLock newJobsManagerAcquireLock(List<String> lites, boolean waitForLockIfLocked,
    int secondsToExpire, String requestUuid) {
    return INSTANCE.new JobsManagerAcquireLock(lites, waitForLockIfLocked, secondsToExpire, requestUuid);
  }

  public final class JobsManagerAcquireLock extends AbstractMessage {
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

  public static JobsManagerReleaseLock newJobsManagerReleaseLock(List<String> lites, String requestUuid) {
    return INSTANCE.new JobsManagerReleaseLock(lites, requestUuid);
  }

  public final class JobsManagerReleaseLock extends AbstractMessage {
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

  public static JobsManagerReleaseAllLocks newJobsManagerReleaseAllLocks() {
    return INSTANCE.new JobsManagerReleaseAllLocks();
  }

  public final class JobsManagerReleaseAllLocks extends AbstractMessage {
    private static final long serialVersionUID = -2842420964416530808L;

    public JobsManagerReleaseAllLocks() {
      super();
    }

    @Override
    public String toString() {
      return "JobsManagerReleaseAllLocks []";
    }
  }

  public static JobsManagerReplyToAcquireLock newJobsManagerReplyToAcquireLock(List<String> lites) {
    return INSTANCE.new JobsManagerReplyToAcquireLock(lites);
  }

  public final class JobsManagerReplyToAcquireLock extends AbstractMessage {
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

  public static JobsManagerReplyToReleaseLock newJobsManagerReplyToReleaseLock(List<String> lites) {
    return INSTANCE.new JobsManagerReplyToReleaseLock(lites);
  }

  public final class JobsManagerReplyToReleaseLock extends AbstractMessage {
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

  public static JobsManagerNotLockableAtTheTime newJobsManagerNotLockableAtTheTime() {
    return INSTANCE.new JobsManagerNotLockableAtTheTime();
  }

  public static JobsManagerNotLockableAtTheTime newJobsManagerNotLockableAtTheTime(String msg) {
    return INSTANCE.new JobsManagerNotLockableAtTheTime(msg);
  }

  public final class JobsManagerNotLockableAtTheTime extends AbstractMessage {
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
  public static JobInfoUpdated newJobInfoUpdated(Plugin<?> plugin, JobPluginInfo jobPluginInfo) {
    return INSTANCE.new JobInfoUpdated(plugin, jobPluginInfo);
  }

  public final class JobInfoUpdated extends AbstractMessage {
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

  public abstract class JobPartialUpdate extends AbstractMessage {
    private static final long serialVersionUID = 4722216970884172260L;

    @Override
    public String toString() {
      return "JobPartialUpdate []";
    }
  }

  public static JobSourceObjectsUpdated newJobSourceObjectsUpdated(Map<String, String> oldToNewIds) {
    return INSTANCE.new JobSourceObjectsUpdated(oldToNewIds);
  }

  public class JobSourceObjectsUpdated extends JobPartialUpdate {
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

  public static JobStateDetailsUpdated newJobStateDetailsUpdated(Plugin<?> plugin, Optional<String> stateDatails) {
    return INSTANCE.new JobStateDetailsUpdated(plugin, stateDatails);
  }

  public class JobStateDetailsUpdated extends JobPartialUpdate {
    private static final long serialVersionUID = 1946036502369851214L;

    private Plugin<?> plugin;
    private SerializableOptional<String> stateDatails;

    public JobStateDetailsUpdated(Plugin<?> plugin, Optional<String> stateDatails) {
      super();
      this.plugin = plugin;
      this.stateDatails = SerializableOptional.setOptional(stateDatails);
    }

    public JobStateDetailsUpdated(Plugin<?> plugin, Throwable throwable) {
      this(plugin, Optional.of(throwable.getClass().getName() + ": " + throwable.getMessage()));
    }

    public Plugin<?> getPlugin() {
      return plugin;
    }

    public Optional<String> getStateDatails() {
      return stateDatails.getOptional();
    }

    @Override
    public String toString() {
      return "JobStateDetailsUpdated [plugin=" + plugin + ", stateDatails=" + stateDatails + "]";
    }
  }

  public static JobStateUpdated newJobStateUpdated(Plugin<?> plugin, JOB_STATE state) {
    return INSTANCE.new JobStateUpdated(plugin, state);
  }

  public static JobStateUpdated newJobStateUpdated(Plugin<?> plugin, JOB_STATE state, Optional<String> stateDatails) {
    return INSTANCE.new JobStateUpdated(plugin, state, stateDatails);
  }

  public static JobStateUpdated newJobStateUpdated(Plugin<?> plugin, JOB_STATE state, Throwable throwable) {
    return INSTANCE.new JobStateUpdated(plugin, state, throwable);
  }

  public class JobStateUpdated extends JobStateDetailsUpdated {
    private static final long serialVersionUID = 1946036502369851214L;

    private JOB_STATE state;

    public JobStateUpdated(Plugin<?> plugin, JOB_STATE state) {
      this(plugin, state, Optional.empty());
    }

    public JobStateUpdated(Plugin<?> plugin, JOB_STATE state, Optional<String> stateDatails) {
      super(plugin, stateDatails);
      this.state = state;
    }

    public JobStateUpdated(Plugin<?> plugin, JOB_STATE state, Throwable throwable) {
      this(plugin, state, Optional.of(throwable.getClass().getName() + ": " + throwable.getMessage()));
    }

    public JOB_STATE getState() {
      return state;
    }

    @Override
    public String toString() {
      return "JobStateUpdated [plugin=" + getPlugin() + ", state=" + state + ", stateDatails=" + getStateDatails()
        + "]";
    }
  }

  public static JobInitEnded newJobInitEnded(JobPluginInfo jobPluginInfo, boolean noObjectsOrchestrated) {
    return INSTANCE.new JobInitEnded(jobPluginInfo, noObjectsOrchestrated);
  }

  public final class JobInitEnded extends AbstractMessage {
    private static final long serialVersionUID = 5040958276243865900L;

    private boolean noObjectsOrchestrated;
    private JobPluginInfo jobPluginInfo;

    public JobInitEnded(JobPluginInfo jobPluginInfo, boolean noObjectsOrchestrated) {
      super();
      this.jobPluginInfo = jobPluginInfo;
      this.noObjectsOrchestrated = noObjectsOrchestrated;
    }

    public JobPluginInfo getJobPluginInfo() {
      return jobPluginInfo;
    }

    public boolean isNoObjectsOrchestrated() {
      return noObjectsOrchestrated;
    }

    @Override
    public String toString() {
      return "JobInitEnded [noObjectsOrchestrated=" + noObjectsOrchestrated + ", jobPluginInfo=" + jobPluginInfo + "]";
    }
  }

  public static JobCleanup newJobCleanup() {
    return INSTANCE.new JobCleanup();
  }

  public class JobCleanup extends AbstractMessage {
    private static final long serialVersionUID = -5175825019027462407L;

    public JobCleanup() {
      super();
    }

    @Override
    public String toString() {
      return "JobCleanup []";
    }
  }

  public static JobStop newJobStop() {
    return INSTANCE.new JobStop();
  }

  public class JobStop extends AbstractMessage {
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

  private class PluginMethodIsReady<T extends IsRODAObject> extends AbstractMessage {
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

  private class PluginMethodIsDone extends AbstractMessage {
    private static final long serialVersionUID = -8701179264086005994L;

    private Plugin<?> plugin;
    private boolean withError;
    private String errorMessage = "";

    public PluginMethodIsDone(Plugin<?> plugin, boolean withError) {
      super();
      this.plugin = plugin;
      this.withError = withError;
    }

    public PluginMethodIsDone(Plugin<?> plugin, boolean withError, String errorMessage) {
      super();
      this.plugin = plugin;
      this.withError = withError;
      this.errorMessage = errorMessage;
    }

    public Plugin<?> getPlugin() {
      return plugin;
    }

    public boolean isWithError() {
      return withError;
    }

    public String getErrorMessage() {
      return errorMessage;
    }

    @Override
    public String toString() {
      return "PluginMethodIsDone [plugin=" + plugin + ", withError=" + withError + ", errorMessage=" + errorMessage
        + "]";
    }
  }

  public static <T extends IsRODAObject> PluginBeforeAllExecuteIsReady<T> newPluginBeforeAllExecuteIsReady(
    Plugin<T> plugin) {
    return INSTANCE.new PluginBeforeAllExecuteIsReady<T>(plugin);
  }

  public class PluginBeforeAllExecuteIsReady<T extends IsRODAObject> extends PluginMethodIsReady<T> {
    private static final long serialVersionUID = -7730727049162062388L;

    public PluginBeforeAllExecuteIsReady(Plugin<T> plugin) {
      super(plugin);
    }

    @Override
    public String toString() {
      return "PluginBeforeAllExecuteIsReady [getPlugin()=" + getPlugin() + "]";
    }
  }

  public static PluginBeforeAllExecuteIsDone newPluginBeforeAllExecuteIsDone(Plugin<?> plugin, boolean withError) {
    return INSTANCE.new PluginBeforeAllExecuteIsDone(plugin, withError);
  }

  public class PluginBeforeAllExecuteIsDone extends PluginMethodIsDone {
    private static final long serialVersionUID = 7449486178368177015L;

    public PluginBeforeAllExecuteIsDone(Plugin<?> plugin, boolean withError) {
      super(plugin, withError);
    }

    @Override
    public String toString() {
      return "PluginBeforeAllExecuteIsDone [getPlugin()=" + getPlugin() + ", isWithError()=" + isWithError() + "]";
    }
  }

  public static <T extends IsRODAObject> PluginExecuteIsReady<T> newPluginExecuteIsReady(Plugin<T> plugin,
    List<LiteOptionalWithCause> list) {
    return INSTANCE.new PluginExecuteIsReady<T>(plugin, list);
  }

  public class PluginExecuteIsReady<T extends IsRODAObject> extends PluginMethodIsReady<T> {
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

  public static PluginExecuteIsDone newPluginExecuteIsDone(Plugin<?> plugin, boolean withError) {
    return INSTANCE.new PluginExecuteIsDone(plugin, withError);
  }

  public static PluginExecuteIsDone newPluginExecuteIsDone(Plugin<?> plugin, boolean withError, String errorMessage) {
    return INSTANCE.new PluginExecuteIsDone(plugin, withError, errorMessage);
  }

  public class PluginExecuteIsDone extends PluginMethodIsDone {
    private static final long serialVersionUID = -5136014936634139026L;

    public PluginExecuteIsDone(Plugin<?> plugin, boolean withError) {
      super(plugin, withError);
    }

    public PluginExecuteIsDone(Plugin<?> plugin, boolean withError, String errorMessage) {
      super(plugin, withError, errorMessage);
    }

    @Override
    public String toString() {
      return "PluginExecuteIsDone [getPlugin()=" + getPlugin() + ", isWithError()=" + isWithError()
        + ", getErrorMessage()=" + getErrorMessage() + "]";
    }
  }

  public static <T extends IsRODAObject> PluginAfterAllExecuteIsReady<T> newPluginAfterAllExecuteIsReady(
    Plugin<T> plugin) {
    return INSTANCE.new PluginAfterAllExecuteIsReady<T>(plugin);
  }

  public class PluginAfterAllExecuteIsReady<T extends IsRODAObject> extends PluginMethodIsReady<T> {
    private static final long serialVersionUID = 8852688692792086166L;

    public PluginAfterAllExecuteIsReady(Plugin<T> plugin) {
      super(plugin);
    }

    @Override
    public String toString() {
      return "PluginAfterAllExecuteIsReady [plugin=" + getPlugin() + "]";
    }
  }

  public static PluginAfterAllExecuteIsDone newPluginAfterAllExecuteIsDone(Plugin<?> plugin, boolean withError) {
    return INSTANCE.new PluginAfterAllExecuteIsDone(plugin, withError);
  }

  public class PluginAfterAllExecuteIsDone extends PluginMethodIsDone {
    private static final long serialVersionUID = -5136014936634139026L;

    public PluginAfterAllExecuteIsDone(Plugin<?> plugin, boolean withError) {
      super(plugin, withError);
    }

    @Override
    public String toString() {
      return "PluginAfterAllExecuteIsDone [getPlugin()=" + getPlugin() + ", isWithError()=" + isWithError() + "]";
    }
  }

  /*-------------------- EVENTS RELATED STATIC CLASSES --------------------*/
  public abstract class AbstractEventMessage extends AbstractMessage {
    private static final long serialVersionUID = -2517455273875624115L;

    private String senderId;

    public AbstractEventMessage(String senderId) {
      super();
      this.senderId = senderId;
    }

    public String getSenderId() {
      return senderId;
    }

    @Override
    public String toString() {
      return "AbstractEventMessage [senderId=" + senderId + "]";
    }
  }

  public static EventUserCreated newEventUserCreated(User user, String password, String senderId) {
    return INSTANCE.new EventUserCreated(user, password, senderId);
  }

  public final class EventUserCreated extends AbstractEventMessage {
    private static final long serialVersionUID = -2517455273875624115L;

    private User user;
    private String password;

    public EventUserCreated(User user, String password, String senderId) {
      super(senderId);
      this.user = user;
      this.password = password;
    }

    public User getUser() {
      return user;
    }

    public String getPassword() {
      return password;
    }

    @Override
    public String toString() {
      return "EventUserCreated [user=" + user + ", getSenderId()=" + getSenderId() + "]";
    }
  }

  public final static EventUserUpdated newEventUserUpdated(User user, String password, boolean myUser,
    String senderId) {
    return INSTANCE.new EventUserUpdated(user, password, myUser, senderId);
  }

  public final class EventUserUpdated extends AbstractEventMessage {
    private static final long serialVersionUID = -2517455273875624115L;

    private User user;
    private String password;
    private boolean myUser;

    public EventUserUpdated(User user, String password, boolean myUser, String senderId) {
      super(senderId);
      this.user = user;
      this.password = password;
      this.myUser = myUser;
    }

    public User getUser() {
      return user;
    }

    public String getPassword() {
      return password;
    }

    public boolean isMyUser() {
      return myUser;
    }

    @Override
    public String toString() {
      return "EventUserUpdated [user=" + user + ", password=" + password + ", myUser=" + myUser + ", getSenderId()="
        + getSenderId() + "]";
    }
  }

  public static EventUserDeleted newEventUserDeleted(String id, String senderId) {
    return INSTANCE.new EventUserDeleted(id, senderId);
  }

  public final class EventUserDeleted extends AbstractEventMessage {
    private static final long serialVersionUID = -7862917122791858311L;

    private String id;

    public EventUserDeleted(String id, String senderId) {
      super(senderId);
      this.id = id;
    }

    public String getId() {
      return id;
    }

    @Override
    public String toString() {
      return "EventUserDeleted [id=" + id + ", getSenderId()=" + getSenderId() + "]";
    }
  }

  public static EventGroupCreated newEventGroupCreated(Group group, String senderId) {
    return INSTANCE.new EventGroupCreated(group, senderId);
  }

  public final class EventGroupCreated extends AbstractEventMessage {
    private static final long serialVersionUID = -51380983717488740L;

    private Group group;

    public EventGroupCreated(Group group, String senderId) {
      super(senderId);
      this.group = group;
    }

    public Group getGroup() {
      return group;
    }

    @Override
    public String toString() {
      return "EventGroupCreated [group=" + group + ", getSenderId()=" + getSenderId() + "]";
    }
  }

  public static EventGroupUpdated newEventGroupUpdated(Group group, String senderId) {
    return INSTANCE.new EventGroupUpdated(group, senderId);
  }

  public final class EventGroupUpdated extends AbstractEventMessage {
    private static final long serialVersionUID = -51380983717488740L;

    private Group group;

    public EventGroupUpdated(Group group, String senderId) {
      super(senderId);
      this.group = group;
    }

    public Group getGroup() {
      return group;
    }

    @Override
    public String toString() {
      return "EventGroupUpdated [group=" + group + ", getSenderId()=" + getSenderId() + "]";
    }
  }

  public static EventGroupDeleted newEventGroupDeleted(String id, String senderId) {
    return INSTANCE.new EventGroupDeleted(id, senderId);
  }

  public final class EventGroupDeleted extends AbstractEventMessage {
    private static final long serialVersionUID = -7862917122791858311L;

    private String id;

    public EventGroupDeleted(String id, String senderId) {
      super(senderId);
      this.id = id;
    }

    public String getId() {
      return id;
    }

    @Override
    public String toString() {
      return "EventGroupDeleted [id=" + id + ", getSenderId()=" + getSenderId() + "]";
    }
  }

}
