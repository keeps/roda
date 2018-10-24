/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.orchestrate.akka;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.TimeUnit;

import org.roda.core.RodaCoreFactory;
import org.roda.core.common.akka.AkkaBaseActor;
import org.roda.core.common.akka.Messages;
import org.roda.core.common.akka.Messages.JobsManagerAcquireLock;
import org.roda.core.common.akka.Messages.JobsManagerReleaseAllLocks;
import org.roda.core.common.akka.Messages.JobsManagerReleaseLock;
import org.roda.core.data.v2.jobs.Job;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.MetricRegistry;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.routing.RoundRobinPool;
import scala.concurrent.duration.Duration;

public class AkkaJobsManager extends AkkaBaseActor {
  private static final Logger LOGGER = LoggerFactory.getLogger(AkkaJobsManager.class);

  public static final String LOCK_REQUESTS_WAITING_TO_ACQUIRE_LOCK = "lockRequestsWaitingToAcquireLock";

  private static final String LOCK_TIMEOUT = "core.orchestrator.lock_timeout";
  private static final int DEFAULT_LOCK_TIMEOUT = 600;

  // state
  private int maxNumberOfJobsInParallel;
  private Queue<JobWaiting> jobsWaiting;
  private Map<String, ActorRef> jobsWaitingCreators;
  private ActorRef jobsRouter;
  // <Lite, LockInfo>
  private Map<String, LockInfo> objectsLocked;
  // <RequestUUID, List<Lite>>
  private Map<String, List<String>> requestUuidLites;
  private List<JobsManagerAcquireLock> waitingToAcquireLockRequests;

  // metrics
  private Counter ticksWaitingToBeProcessed;
  private Counter jobsBeingExecuted;
  private Counter jobsWaitingToBeExecuted;
  private Histogram jobsBeingExecutedHisto;
  private Histogram jobsWaitingToBeExecutedHisto;
  private Histogram jobsTimeInTheQueueInMilis;
  private Counter lockRequestsWaitingToAcquireLock;
  private Histogram lockRequestsWaitingToAcquireLockHisto;
  private Counter objectsWaitingToAcquireLock;
  private Histogram objectsWaitingToAcquireLockHisto;
  private Histogram messagesProcessingTimeInMilis;

  // parallelization
  private List<String> nonParallelizablePlugins;
  private boolean nonParallelizableJobIsRunning = false;
  private int nonParallelizableJobsQueued = 0;

  public AkkaJobsManager(int maxNumberOfJobsInParallel) {
    super();
    this.maxNumberOfJobsInParallel = maxNumberOfJobsInParallel;
    this.jobsWaiting = new LinkedList<>();
    this.jobsWaitingCreators = new HashMap<>();
    this.objectsLocked = new HashMap<>();
    this.requestUuidLites = new HashMap<>();
    this.waitingToAcquireLockRequests = new ArrayList<>();

    Props jobsProps = new RoundRobinPool(maxNumberOfJobsInParallel).props(Props.create(AkkaJobActor.class, getSelf()));
    jobsRouter = getContext().actorOf(jobsProps, "JobsRouter");

    initMetrics(maxNumberOfJobsInParallel);

    loadParallelizationInformation();

    getContext().system().scheduler().schedule(Duration.create(0, TimeUnit.MILLISECONDS),
      Duration.create(2, TimeUnit.SECONDS), () -> {
        if (jobsWaitingToBeExecuted.getCount() > 0 || !waitingToAcquireLockRequests.isEmpty()
          || !objectsLocked.isEmpty()) {
          sendTick();
        }
      }, getContext().system().dispatcher());
  }

  @Override
  public void onReceive(Object msg) throws Throwable {
    Date messageProcessingStart = new Date();
    boolean doPostProcessingTasks = true;
    try {
      if (msg instanceof Job) {
        handleJob((Job) msg);
      } else if (msg instanceof Messages.JobsManagerTick) {
        doPostProcessingTasks = false;
        handleTick(true);
      } else if (msg instanceof Messages.JobsManagerJobEnded) {
        handleJobEnded((Messages.JobsManagerJobEnded) msg);
      } else if (msg instanceof Messages.JobsManagerAcquireLock) {
        handleAcquireLock(((Messages.JobsManagerAcquireLock) msg).setSender(getSender()));
      } else if (msg instanceof Messages.JobsManagerReleaseLock) {
        handleReleaseLock((Messages.JobsManagerReleaseLock) msg);
      } else if (msg instanceof Messages.JobsManagerReleaseAllLocks) {
        handleReleaseAllLocks((Messages.JobsManagerReleaseAllLocks) msg);
      } else {
        LOGGER.error("Received a message that don't know how to process ({})...", msg.getClass().getName());
        unhandled(msg);
      }
    } finally {
      if (doPostProcessingTasks) {
        handleTick(false);
      }
      registerMessageProcessingTime(messageProcessingStart);
    }
  }

  private void handleJob(Job job) {
    if (jobIsNotParallelizable(job)) {
      if (!nonParallelizableJobIsRunning && jobsWaitingToBeExecuted.getCount() == 0
        && jobsBeingExecuted.getCount() < maxNumberOfJobsInParallel) {
        nonParallelizableJobIsRunning = true;
        sendJobForExecution(job);
      } else {
        queueJob(job, true);
      }
    } else {
      if (jobsWaitingToBeExecuted.getCount() == 0 && jobsBeingExecuted.getCount() < maxNumberOfJobsInParallel) {
        sendJobForExecution(job);
      } else {
        queueJob(job, false);
      }
    }
  }

  private boolean jobIsNotParallelizable(Job job) {
    return jobIsNotParallelizable(job.getPlugin());
  }

  private boolean jobIsNotParallelizable(String plugin) {
    return nonParallelizablePlugins.contains(plugin);
  }

  private void sendJobForExecution(Job job) {
    updateJobsBeingExecuted(true);
    jobsTimeInTheQueueInMilis.update(0);
    jobsRouter.tell(job, getSender());
    log("Will execute job", job.getId());
  }

  private void updateJobsBeingExecuted(boolean increment) {
    if (increment) {
      jobsBeingExecuted.inc();
    } else {
      jobsBeingExecuted.dec();
    }
    jobsBeingExecutedHisto.update(jobsBeingExecuted.getCount());
  }

  private void queueJob(Job job, boolean jobIsJobParallelizable) {
    if (jobIsJobParallelizable) {
      nonParallelizableJobsQueued++;
    }
    jobsWaiting.offer(new JobWaiting(job));
    jobsWaitingCreators.put(job.getId(), getSender());
    updateJobsWaitingToBeExecuted(true);
    log("Queued job", job.getId());
  }

  private void updateJobsWaitingToBeExecuted(boolean increment) {
    if (increment) {
      jobsWaitingToBeExecuted.inc();
    } else {
      jobsWaitingToBeExecuted.dec();
    }
    jobsWaitingToBeExecutedHisto.update(jobsWaitingToBeExecuted.getCount());
  }

  private void handleTick(boolean decrementTicksWaitingCounter) {
    // jobs related
    if (jobsWaitingToBeExecuted.getCount() > 0 && jobsBeingExecuted.getCount() < maxNumberOfJobsInParallel) {
      dequeueJobs(
        Math.min(jobsWaitingToBeExecuted.getCount(), maxNumberOfJobsInParallel - jobsBeingExecuted.getCount()));
    }

    // lock requests waiting related
    processWaitingToAcquireLockRequests();

    // locks acquired timeout related
    processAcquiredLocksTimeout();

    if (decrementTicksWaitingCounter) {
      ticksWaitingToBeProcessed.dec();
    }
  }

  private void dequeueJobs(long numberOfJobsToDequeue) {
    List<JobWaiting> jobsToDequeued = new ArrayList<>();
    boolean dequeueOneNonParallelizableJob = !nonParallelizableJobIsRunning;

    // 20180104 hsilva: Optimization 1 - if one non-parallelizable job is
    // already running & all waiting are non-parallelizable, there is no point
    // in doing the for cycle
    if (nonParallelizableJobIsRunning && jobsWaiting.size() == nonParallelizableJobsQueued) {
      return;
    }

    for (JobWaiting jobWaiting : jobsWaiting) {
      boolean jobIsNotParallelizable = jobIsNotParallelizable(jobWaiting.job);
      if (jobIsNotParallelizable) {
        if (dequeueOneNonParallelizableJob) {
          nonParallelizableJobsQueued--;
          dequeueOneNonParallelizableJob = false;
          nonParallelizableJobIsRunning = true;
          jobsToDequeued.add(jobWaiting);
        }
      } else {
        jobsToDequeued.add(jobWaiting);
      }

      if (jobsToDequeued.size() == numberOfJobsToDequeue) {
        break;
      }
    }

    for (JobWaiting jobToDequeue : jobsToDequeued) {
      jobsWaiting.remove(jobToDequeue);
      jobsTimeInTheQueueInMilis.update(jobToDequeue.timeInQueueInMillis());
      Job job = jobToDequeue.job;
      ActorRef jobCreator = jobsWaitingCreators.remove(job.getId());
      updateJobsBeingExecuted(true);
      updateJobsWaitingToBeExecuted(false);
      jobsRouter.tell(job, jobCreator);
      log("Dequeued job", job.getId());
    }
  }

  private void processWaitingToAcquireLockRequests() {
    for (Iterator<JobsManagerAcquireLock> iterator = waitingToAcquireLockRequests.iterator(); iterator.hasNext();) {
      JobsManagerAcquireLock acquireLockRequest = iterator.next();

      if (expireDateAlreadyExpired(acquireLockRequest)) {
        LOGGER.warn("Deleting lock request for objects '{}' due to expire ({})", acquireLockRequest.getLites(),
          acquireLockRequest.getExpireDate());
        iterator.remove();
        // FIXME 20180605 hsilva: send message?
      } else {
        if (areObjectsLockable(acquireLockRequest)) {
          lock(acquireLockRequest, true);
          iterator.remove();
        }
      }
    }
  }

  private boolean expireDateAlreadyExpired(JobsManagerAcquireLock acquireLockRequest) {
    return new Date().after(acquireLockRequest.getExpireDate());
  }

  private boolean areObjectsLockable(JobsManagerAcquireLock msg) {
    boolean allLockable = true;

    for (String lite : msg.getLites()) {
      if (!objectsLocked.containsKey(lite)) {
        continue;
      }

      LockInfo lockInfo = objectsLocked.get(lite);
      if (!lockInfo.requestUuid.equals(msg.getRequestUuid())) {
        allLockable = false;
        break;
      }
    }
    return allLockable;
  }

  private void lock(JobsManagerAcquireLock msg, boolean decreaseWaiting) {
    for (String lite : msg.getLites()) {
      // reentrant test
      if (objectsLocked.get(lite) != null) {
        objectsLocked.get(lite).increaseReentrantAmount();
      } else {
        String requestUuid = msg.getRequestUuid();
        objectsLocked.put(lite, new LockInfo(requestUuid));
        requestUuidLites.computeIfAbsent(requestUuid, key -> new ArrayList<>()).add(lite);
      }
    }
    // 20180606 hsilva: not sending any list to the sender as it will most
    // certainly end up in deadletters
    msg.getSender().tell(Messages.newJobsManagerReplyToAcquireLock(Collections.emptyList()), getSelf());

    if (decreaseWaiting) {
      updateLockRequestsWaitingToAcquireLock(false);
      updateObjectsWaitingToAcquireLock(msg.getLites().size(), false);
    }
  }

  private void updateLockRequestsWaitingToAcquireLock(boolean increment) {
    if (increment) {
      lockRequestsWaitingToAcquireLock.inc();
    } else {
      lockRequestsWaitingToAcquireLock.dec();
    }
    lockRequestsWaitingToAcquireLockHisto.update(lockRequestsWaitingToAcquireLock.getCount());
  }

  private void updateObjectsWaitingToAcquireLock(long value, boolean increment) {
    if (increment) {
      objectsWaitingToAcquireLock.inc(value);
    } else {
      objectsWaitingToAcquireLock.dec(value);
    }
    objectsWaitingToAcquireLockHisto.update(objectsWaitingToAcquireLock.getCount());
  }

  private void unlock(JobsManagerReleaseLock msg) {
    String requestUuid = msg.getRequestUuid();
    if (msg.getLites().isEmpty()) {
      for (String lite : requestUuidLites.getOrDefault(requestUuid, Collections.emptyList())) {
        objectsLocked.remove(lite);
      }
      requestUuidLites.remove(requestUuid);
    } else {
      for (String lite : msg.getLites()) {
        LockInfo lockInfo = objectsLocked.get(lite);
        if (lockInfo == null) {
          LOGGER.warn("Trying to remove lock from object '{}' whose lock does not exist!", lite);
        } else if (!lockInfo.requestUuid.equals(requestUuid)) {
          LOGGER.warn("Trying to remove lock from object '{}' whose lock wasn't created by this requester (uuid={})",
            lite, requestUuid);
        } else {
          // lock exists & request uuid matches
          if (lockInfo.reentrantAmount > 0) {
            lockInfo.decreaseReentrantAmount();
          } else {
            objectsLocked.remove(lite);
            requestUuidLites.get(requestUuid).remove(lite);
          }
        }
      }
    }
    // 20180606 hsilva: not sending any list to the sender as it will most
    // certainly end up in deadletters
    getSender().tell(Messages.newJobsManagerReplyToReleaseLock(Collections.emptyList()), getSelf());
  }

  private void processAcquiredLocksTimeout() {
    int lockTimeout = RodaCoreFactory.getRodaConfiguration().getInt(LOCK_TIMEOUT, DEFAULT_LOCK_TIMEOUT);
    for (Iterator<Map.Entry<String, LockInfo>> it = objectsLocked.entrySet().iterator(); it.hasNext();) {
      Map.Entry<String, LockInfo> lock = it.next();
      if (lock.getValue().releaseLockDueToExpire(lockTimeout)) {
        LOGGER.warn("Releasing lock for object '{}' due to lock timeout ({} seconds; no lock release was issued)",
          lock.getKey(), lockTimeout);
        it.remove();
      }
    }
  }

  private void handleJobEnded(Messages.JobsManagerJobEnded jobEnded) {
    if (jobIsNotParallelizable(jobEnded.getPlugin())) {
      nonParallelizableJobIsRunning = false;
    }
    updateJobsBeingExecuted(false);
    log("The end for job", jobEnded.getJobId());
  }

  private void sendTick() {
    if (ticksWaitingToBeProcessed.getCount() == 0) {
      self().tell(Messages.newJobsManagerTick(), self());
      ticksWaitingToBeProcessed.inc();
    }
  }

  private void handleAcquireLock(JobsManagerAcquireLock msg) {
    msg.logProcessingStarted();

    boolean areLockable = areObjectsLockable(msg);
    if (areLockable) {
      lock(msg.setSender(getSender()), false);
    } else if (msg.isWaitForLockIfLocked()) {
      waitingToAcquireLockRequests.add(msg);
      updateLockRequestsWaitingToAcquireLock(true);
      updateObjectsWaitingToAcquireLock(msg.getLites().size(), true);
    } else {
      // 20180530 hsilva: message stating that lock was not possible
      // (in order to avoid spending timeout to realize that)
      getSender().tell(Messages.newJobsManagerNotLockableAtTheTime(
        "Unable to acquire lock & configured to not wait for lock if already locked"), getSelf());
    }

    msg.logProcessingEnded();
  }

  private void handleReleaseLock(JobsManagerReleaseLock msg) {
    msg.logProcessingStarted();
    unlock(msg);
    msg.logProcessingEnded();
  }

  private void handleReleaseAllLocks(JobsManagerReleaseAllLocks msg) {
    objectsLocked = new HashMap<>();
    requestUuidLites = new HashMap<>();
    waitingToAcquireLockRequests = new ArrayList<>();
  }

  private void log(String msg, String jobId) {
    LOGGER.info("{} '{}' (max: {}| exec: {}| wait: {})", msg, jobId, maxNumberOfJobsInParallel,
      jobsBeingExecuted.getCount(), jobsWaitingToBeExecuted.getCount());
  }

  private void initMetrics(int maxNumberOfJobsInParallel) {
    MetricRegistry metrics = getMetricRegistry();
    String className = AkkaJobsManager.class.getSimpleName();
    // general metrics
    ticksWaitingToBeProcessed = metrics.counter(MetricRegistry.name(className, "ticksWaitingToBeProcessed"));
    // jobs related metrics
    Counter maxNumberOfJobsInParallelCounter = metrics
      .counter(MetricRegistry.name(className, "maxNumberOfJobsInParallel"));
    maxNumberOfJobsInParallelCounter.inc(maxNumberOfJobsInParallel);
    jobsBeingExecuted = metrics.counter(MetricRegistry.name(className, "jobsBeingExecuted"));
    jobsWaitingToBeExecuted = metrics.counter(MetricRegistry.name(className, "jobsWaitingToBeExecuted"));
    jobsBeingExecutedHisto = metrics.histogram(MetricRegistry.name(className, "jobsBeingExecutedHistogram"));
    jobsWaitingToBeExecutedHisto = metrics
      .histogram(MetricRegistry.name(className, "jobsWaitingToBeExecutedHistogram"));
    jobsTimeInTheQueueInMilis = metrics.histogram(MetricRegistry.name(className, "jobsTimeInTheQueueInMilis"));
    // locks related metrics
    lockRequestsWaitingToAcquireLock = metrics
      .counter(MetricRegistry.name(className, LOCK_REQUESTS_WAITING_TO_ACQUIRE_LOCK));
    lockRequestsWaitingToAcquireLockHisto = metrics
      .histogram(MetricRegistry.name(className, "lockRequestsWaitingToAcquireLockHisto"));
    objectsWaitingToAcquireLock = metrics.counter(MetricRegistry.name(className, "objectsWaitingToAcquireLock"));
    objectsWaitingToAcquireLockHisto = metrics
      .histogram(MetricRegistry.name(className, "objectsWaitingToAcquireLockHisto"));

    messagesProcessingTimeInMilis = metrics.histogram(MetricRegistry.name(className, "messagesProcessingTimeInMilis"));
  }

  private void loadParallelizationInformation() {
    nonParallelizablePlugins = RodaCoreFactory
      .getRodaConfigurationAsList("core.orchestrator.non_parallelizable_plugins");
  }

  private void registerMessageProcessingTime(Date messageProcessingStart) {
    messagesProcessingTimeInMilis.update(new Date().getTime() - messageProcessingStart.getTime());
  }

  private class JobWaiting {
    public Job job;
    private long queuedIn;

    public JobWaiting(Job job) {
      this.job = job;
      this.queuedIn = new Date().getTime();
    }

    /** Time in milliseconds */
    public long timeInQueueInMillis() {
      return new Date().getTime() - queuedIn;
    }
  }

  private class LockInfo {
    public Date lockDate;
    public String requestUuid;
    public int reentrantAmount;

    public LockInfo(String requestUuid) {
      refreshLockDate();
      this.requestUuid = requestUuid;
      reentrantAmount = 0;
    }

    public void refreshLockDate() {
      lockDate = new Date();
    }

    public void increaseReentrantAmount() {
      reentrantAmount++;
      refreshLockDate();
    }

    public void decreaseReentrantAmount() {
      reentrantAmount--;
      refreshLockDate();
    }

    public boolean releaseLockDueToExpire(long lockTimeout) {
      return new Date().after(new Date(lockDate.getTime() + (lockTimeout * 1000)));
    }
  }

}
