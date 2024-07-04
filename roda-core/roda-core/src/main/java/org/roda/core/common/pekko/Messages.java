/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.common.pekko;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.roda.core.common.pekko.messages.events.EventGroupCreated;
import org.roda.core.common.pekko.messages.events.EventGroupDeleted;
import org.roda.core.common.pekko.messages.events.EventGroupUpdated;
import org.roda.core.common.pekko.messages.events.EventUserCreated;
import org.roda.core.common.pekko.messages.events.EventUserDeleted;
import org.roda.core.common.pekko.messages.events.EventUserUpdated;
import org.roda.core.common.pekko.messages.jobs.JobCleanup;
import org.roda.core.common.pekko.messages.jobs.JobInfoUpdated;
import org.roda.core.common.pekko.messages.jobs.JobInitEnded;
import org.roda.core.common.pekko.messages.jobs.JobSourceObjectsUpdated;
import org.roda.core.common.pekko.messages.jobs.JobStateDetailsUpdated;
import org.roda.core.common.pekko.messages.jobs.JobStateUpdated;
import org.roda.core.common.pekko.messages.jobs.JobStop;
import org.roda.core.common.pekko.messages.jobs.JobsManagerAcquireLock;
import org.roda.core.common.pekko.messages.jobs.JobsManagerJobEnded;
import org.roda.core.common.pekko.messages.jobs.JobsManagerNotLockableAtTheTime;
import org.roda.core.common.pekko.messages.jobs.JobsManagerReleaseAllLocks;
import org.roda.core.common.pekko.messages.jobs.JobsManagerReleaseLock;
import org.roda.core.common.pekko.messages.jobs.JobsManagerReplyToAcquireLock;
import org.roda.core.common.pekko.messages.jobs.JobsManagerReplyToReleaseLock;
import org.roda.core.common.pekko.messages.jobs.JobsManagerTick;
import org.roda.core.common.pekko.messages.plugins.PluginAfterAllExecuteIsDone;
import org.roda.core.common.pekko.messages.plugins.PluginAfterAllExecuteIsReady;
import org.roda.core.common.pekko.messages.plugins.PluginBeforeAllExecuteIsDone;
import org.roda.core.common.pekko.messages.plugins.PluginBeforeAllExecuteIsReady;
import org.roda.core.common.pekko.messages.plugins.PluginExecuteIsDone;
import org.roda.core.common.pekko.messages.plugins.PluginExecuteIsReady;
import org.roda.core.data.v2.IsRODAObject;
import org.roda.core.data.v2.LiteOptionalWithCause;
import org.roda.core.data.v2.jobs.Job.JOB_STATE;
import org.roda.core.data.v2.jobs.JobParallelism;
import org.roda.core.data.v2.jobs.JobStats;
import org.roda.core.data.v2.jobs.PluginType;
import org.roda.core.data.v2.user.Group;
import org.roda.core.data.v2.user.User;
import org.roda.core.plugins.Plugin;
import org.roda.core.plugins.orchestrate.JobPluginInfo;

public class Messages {
  private Messages() {
    // do nothing
  }

  public static <T extends IsRODAObject> PluginBeforeAllExecuteIsReady<T> newPluginBeforeAllExecuteIsReady(
    Plugin<T> plugin) {
    return new PluginBeforeAllExecuteIsReady<>(plugin);
  }

  /*-------------------- JOB MANAGER RELATED STATIC CLASSES --------------------*/
  public static JobsManagerTick newJobsManagerTick() {
    return new JobsManagerTick();
  }

  public static <T extends IsRODAObject> PluginExecuteIsReady<T> newPluginExecuteIsReady(Plugin<T> plugin,
    List<LiteOptionalWithCause> list) {
    return new PluginExecuteIsReady<>(plugin, list);
  }

  public static JobsManagerJobEnded newJobsManagerJobEnded(String jobId, String plugin, PluginType type, long duration,
    JobStats jobStats, JobParallelism jobParallelism) {
    return new JobsManagerJobEnded(jobId, plugin, type, duration, jobStats, jobParallelism);
  }

  public static JobsManagerAcquireLock newJobsManagerAcquireLock(List<String> lites, boolean waitForLockIfLocked,
    int secondsToExpire, String requestUuid) {
    return new JobsManagerAcquireLock(lites, waitForLockIfLocked, secondsToExpire, requestUuid);
  }

  public static JobsManagerReleaseLock newJobsManagerReleaseLock(List<String> lites, String requestUuid) {
    return new JobsManagerReleaseLock(lites, requestUuid);
  }

  public static JobsManagerReleaseAllLocks newJobsManagerReleaseAllLocks() {
    return new JobsManagerReleaseAllLocks();
  }

  public static JobsManagerReplyToAcquireLock newJobsManagerReplyToAcquireLock(List<String> lites) {
    return new JobsManagerReplyToAcquireLock(lites);
  }

  public static JobsManagerReplyToReleaseLock newJobsManagerReplyToReleaseLock(List<String> lites) {
    return new JobsManagerReplyToReleaseLock(lites);
  }

  public static JobsManagerNotLockableAtTheTime newJobsManagerNotLockableAtTheTime() {
    return new JobsManagerNotLockableAtTheTime();
  }

  public static JobsManagerNotLockableAtTheTime newJobsManagerNotLockableAtTheTime(String msg) {
    return new JobsManagerNotLockableAtTheTime(msg);
  }

  public static JobInfoUpdated newJobInfoUpdated(Plugin<?> plugin, JobPluginInfo jobPluginInfo) {
    return new JobInfoUpdated(plugin, jobPluginInfo);
  }

  public static JobSourceObjectsUpdated newJobSourceObjectsUpdated(Map<String, String> oldToNewIds) {
    return new JobSourceObjectsUpdated(oldToNewIds);
  }

  public static JobStateDetailsUpdated newJobStateDetailsUpdated(Plugin<?> plugin, Optional<String> stateDatails) {
    return new JobStateDetailsUpdated(plugin, stateDatails);
  }

  public static JobStateUpdated newJobStateUpdated(Plugin<?> plugin, JOB_STATE state) {
    return new JobStateUpdated(plugin, state);
  }

  public static JobStateUpdated newJobStateUpdated(Plugin<?> plugin, JOB_STATE state, Optional<String> stateDetails) {
    return new JobStateUpdated(plugin, state, stateDetails);
  }

  public static JobStateUpdated newJobStateUpdated(Plugin<?> plugin, JOB_STATE state, Throwable throwable) {
    return new JobStateUpdated(plugin, state, throwable);
  }

  public static JobInitEnded newJobInitEnded(JobPluginInfo jobPluginInfo, boolean noObjectsOrchestrated) {
    return new JobInitEnded(jobPluginInfo, noObjectsOrchestrated);
  }

  public static JobCleanup newJobCleanup() {
    return new JobCleanup();
  }

  public static JobStop newJobStop() {
    return new JobStop();
  }

  public static PluginBeforeAllExecuteIsDone newPluginBeforeAllExecuteIsDone(Plugin<?> plugin, boolean withError) {
    return new PluginBeforeAllExecuteIsDone(plugin, withError);
  }

  public static <T extends IsRODAObject> PluginAfterAllExecuteIsReady<T> newPluginAfterAllExecuteIsReady(
    Plugin<T> plugin) {
    return new PluginAfterAllExecuteIsReady<>(plugin);
  }

  public static EventUserUpdated newEventUserUpdated(User user, boolean myUser, String senderId) {
    return new EventUserUpdated(user, myUser, senderId);
  }

  public static PluginExecuteIsDone newPluginExecuteIsDone(Plugin<?> plugin, boolean withError) {
    return new PluginExecuteIsDone(plugin, withError);
  }

  public static PluginExecuteIsDone newPluginExecuteIsDone(Plugin<?> plugin, boolean withError, String errorMessage) {
    return new PluginExecuteIsDone(plugin, withError, errorMessage);
  }

  public static PluginAfterAllExecuteIsDone newPluginAfterAllExecuteIsDone(Plugin<?> plugin, boolean withError) {
    return new PluginAfterAllExecuteIsDone(plugin, withError);
  }

  public static EventUserCreated newEventUserCreated(User user, String senderId) {
    return new EventUserCreated(user, senderId);
  }

  public static EventUserDeleted newEventUserDeleted(String id, String senderId) {
    return new EventUserDeleted(id, senderId);
  }

  public static EventGroupCreated newEventGroupCreated(Group group, String senderId) {
    return new EventGroupCreated(group, senderId);
  }

  public static EventGroupUpdated newEventGroupUpdated(Group group, String senderId) {
    return new EventGroupUpdated(group, senderId);
  }

  public static EventGroupDeleted newEventGroupDeleted(String id, String senderId) {
    return new EventGroupDeleted(id, senderId);
  }
}
