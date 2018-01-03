/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.orchestrate.akka;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.TimeUnit;

import org.roda.core.RodaCoreFactory;
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

  // state
  private int maxNumberOfJobsInParallel;
  private Queue<JobWaiting> jobsWaiting;
  private Map<String, ActorRef> jobsWaitingCreators;
  private ActorRef jobsRouter;

  // metrics
  private Counter jobsBeingExecuted;
  private Counter jobsWaitingToBeExecuted;
  private Counter ticksWaitingToBeProcessed;
  private Histogram jobsBeingExecutedHisto;
  private Histogram jobsWaitingToBeExecutedHisto;
  private Histogram jobsTimeInTheQueueInMilis;

  // parallelization
  private List<String> nonParallelizablePlugins;
  private boolean nonParallelizableJobIsRunning = false;
  private int nonParallelizableJobsQueued = 0;

  public AkkaJobsManager(int maxNumberOfJobsInParallel) {
    super();
    this.maxNumberOfJobsInParallel = maxNumberOfJobsInParallel;
    this.jobsWaiting = new LinkedList<>();
    this.jobsWaitingCreators = new HashMap<>();

    Props jobsProps = new RoundRobinPool(maxNumberOfJobsInParallel).props(Props.create(AkkaJobActor.class, getSelf()));
    jobsRouter = getContext().actorOf(jobsProps, "JobsRouter");

    initMetrics(maxNumberOfJobsInParallel);

    loadParallelizationInformation();

    getContext().system().scheduler().schedule(Duration.create(0, TimeUnit.MILLISECONDS),
      Duration.create(2, TimeUnit.SECONDS), () -> {
        if (jobsWaitingToBeExecuted.getCount() > 0) {
          sendTick();
        }
      }, getContext().system().dispatcher());
  }

  @Override
  public void onReceive(Object msg) throws Throwable {
    if (msg instanceof Job) {
      handleJob((Job) msg);
    } else if (msg instanceof Messages.JobsManagerTick) {
      handleTick();
    } else if (msg instanceof Messages.JobsManagerJobEnded) {
      handleJobEnded((Messages.JobsManagerJobEnded) msg);
    } else {
      LOGGER.error("Received a message that don't know how to process ({})...", msg.getClass().getName());
      unhandled(msg);
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

  private boolean jobIsNotParallelizable(String plugin) {
    return nonParallelizablePlugins.contains(plugin);
  }

  private boolean jobIsNotParallelizable(Job job) {
    return jobIsNotParallelizable(job.getPlugin());
  }

  private void handleTick() {
    if (jobsWaitingToBeExecuted.getCount() > 0 && jobsBeingExecuted.getCount() < maxNumberOfJobsInParallel) {
      dequeueJobs(
        Math.min(jobsWaitingToBeExecuted.getCount(), maxNumberOfJobsInParallel - jobsBeingExecuted.getCount()));
    }
    ticksWaitingToBeProcessed.dec();
  }

  private void handleJobEnded(Messages.JobsManagerJobEnded jobEnded) {
    if (jobIsNotParallelizable(jobEnded.getPlugin())) {
      nonParallelizableJobIsRunning = false;
    }
    updateJobsBeingExecuted(false);
    sendTick();
    log("The end for job", jobEnded.getJobId());
  }

  private void sendTick() {
    if (ticksWaitingToBeProcessed.getCount() == 0) {
      self().tell(new Messages.JobsManagerTick(), self());
      ticksWaitingToBeProcessed.inc();
    }
  }

  private void updateJobsBeingExecuted(boolean increment) {
    if (increment) {
      jobsBeingExecuted.inc();
    } else {
      jobsBeingExecuted.dec();
    }
    jobsBeingExecutedHisto.update(jobsBeingExecuted.getCount());
  }

  private void updateJobsWaitingToBeExecuted(boolean increment) {
    if (increment) {
      jobsWaitingToBeExecuted.inc();
    } else {
      jobsWaitingToBeExecuted.dec();
    }
    jobsWaitingToBeExecutedHisto.update(jobsWaitingToBeExecuted.getCount());
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

  private void sendJobForExecution(Job job) {
    updateJobsBeingExecuted(true);
    jobsTimeInTheQueueInMilis.update(0);
    jobsRouter.tell(job, getSender());
    log("Will execute job", job.getId());
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

  private void log(String msg, String jobId) {
    LOGGER.info("{} '{}' (max: {}| exec: {}| wait: {})", msg, jobId, maxNumberOfJobsInParallel,
      jobsBeingExecuted.getCount(), jobsWaitingToBeExecuted.getCount());
  }

  private void initMetrics(int maxNumberOfJobsInParallel) {
    MetricRegistry metrics = getMetricRegistry();
    String className = AkkaJobsManager.class.getSimpleName();
    Counter maxNumberOfJobsInParallelCounter = metrics
      .counter(MetricRegistry.name(className, "maxNumberOfJobsInParallel"));
    maxNumberOfJobsInParallelCounter.inc(maxNumberOfJobsInParallel);
    jobsBeingExecuted = metrics.counter(MetricRegistry.name(className, "jobsBeingExecuted"));
    jobsWaitingToBeExecuted = metrics.counter(MetricRegistry.name(className, "jobsWaitingToBeExecuted"));
    ticksWaitingToBeProcessed = metrics.counter(MetricRegistry.name(className, "ticksWaitingToBeProcessed"));
    jobsBeingExecutedHisto = metrics.histogram(MetricRegistry.name(className, "jobsBeingExecutedHistogram"));
    jobsWaitingToBeExecutedHisto = metrics
      .histogram(MetricRegistry.name(className, "jobsWaitingToBeExecutedHistogram"));
    jobsTimeInTheQueueInMilis = metrics.histogram(MetricRegistry.name(className, "jobsTimeInTheQueueInMilis"));
  }

  private void loadParallelizationInformation() {
    nonParallelizablePlugins = RodaCoreFactory
      .getRodaConfigurationAsList("core.orchestrator.non_parallelizable_plugins");
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

}
